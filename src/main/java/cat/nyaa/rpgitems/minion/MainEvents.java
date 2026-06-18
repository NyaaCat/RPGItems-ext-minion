package cat.nyaa.rpgitems.minion;

import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.events.*;
import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.minion.MinionStatus;
import cat.nyaa.rpgitems.minion.minion.TargetPriority;
import cat.nyaa.rpgitems.minion.minion.impl.BaseMinion;
import cat.nyaa.rpgitems.minion.power.marker.ConditionedMarker;
import cat.nyaa.rpgitems.minion.power.marker.MinionMax;
import cat.nyaa.rpgitems.minion.power.trigger.BaseTrigger;
import cat.nyaa.rpgitems.minion.utils.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.entity.EntityMountEvent;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Condition;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.PropertyHolder;
import think.rpgitems.utils.LightContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static think.rpgitems.Events.*;

public class MainEvents implements Listener {

    // Context key for tracking minion attack context
    public static final String MINION_ATTACK_CONTEXT = "MinionAttackContext";

    // Track recent minion attacks with a time window (for async damage like projectiles)
    // Map: PlayerUUID -> MinionAttackInfo (minion, timestamp)
    private static final Map<UUID, MinionAttackInfo> recentMinionAttacks = new ConcurrentHashMap<>();
    private static final Map<UUID, MinionAttackInfo> minionProjectiles = new ConcurrentHashMap<>();
    private static final long MINION_ATTACK_WINDOW_MS = 500; // 500ms window for projectiles
    private static final long MINION_PROJECTILE_WINDOW_MS = 30_000;
    private static final NamespacedKey RPGITEM_SOURCE_ENTITY_KEY = new NamespacedKey("rpgitems", "rpgitem_source_entity");

    private static class MinionAttackInfo {
        final IMinion minion;
        final long timestamp;

        MinionAttackInfo(IMinion minion) {
            this.minion = minion;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid(long maxAgeMs) {
            return System.currentTimeMillis() - timestamp < maxAgeMs;
        }
    }

    @EventHandler
    public void onMinionAttack(MinionAttackEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem -> {
            Player onlinePlayer = player.getPlayer();
            // Set minion attack context before triggering powers
            // This allows us to track that any damage from this player during power execution
            // is actually from a minion attack
            LightContext.putTemp(onlinePlayer.getUniqueId(), MINION_ATTACK_CONTEXT, event.getMinion());

            // Also store in time-based cache for async damage (projectiles, delayed effects)
            recentMinionAttacks.put(onlinePlayer.getUniqueId(), new MinionAttackInfo(event.getMinion()));

            try {
                rpgitem.power(onlinePlayer, event.getItemStack(), event, BaseTrigger.MINION_ATTACK);
            } finally {
                LightContext.removeTemp(onlinePlayer.getUniqueId(), MINION_ATTACK_CONTEXT);
            }
        });
    }

    @EventHandler
    public void onMinionSpawn(MinionSpawnEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem ->{
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_SPAWN);
        });
    }

    @EventHandler
    public void onMinionMove(MinionMoveEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem ->{
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_MOVE);
        });
    }

    @EventHandler
    public void onTargetChange(MinionChangeTargetEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem ->{
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_CHANGE_TARGET);
        });
    }

    @EventHandler
    public void onAmbient(MinionAmbientEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem ->{
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_AMBIENT);
        });
    }

    /**
     * Trigger MINION_ATTACK_HIT powers on player equipment.
     * Called directly with EntityDamageByEntityEvent so it properly chains with HIT/HIT_GLOBAL.
     */
    private void triggerMinionAttackHit(Player player, IMinion minion, EntityDamageByEntityEvent evt) {
        // First, trigger powers on the minion's RPG item
        ItemStack minionItem = minion.getItemStack();
        Optional<RPGItem> minionRpgItem = ItemManager.toRPGItem(minionItem);
        minionRpgItem.ifPresent(rpgitem -> {
            Optional<Double> result = rpgitem.power(player, minionItem, evt, BaseTrigger.MINION_ATTACK_HIT);
            if (result.isPresent()) {
                double newDamage = result.get();
                if (newDamage == -1) {
                    evt.setCancelled(true);
                    return;
                }
                evt.setDamage(newDamage);
            }
        });
        if (evt.isCancelled()) {
            return;
        }

        // Then, trigger powers on the player's equipment
        PlayerInventory inventory = player.getInventory();
        ItemStack[] equipmentToCheck = new ItemStack[]{
            inventory.getItemInMainHand(),
            inventory.getItemInOffHand(),
            inventory.getHelmet(),
            inventory.getChestplate(),
            inventory.getLeggings(),
            inventory.getBoots()
        };

        for (ItemStack equipmentItem : equipmentToCheck) {
            if (evt.isCancelled()) break;
            if (equipmentItem == null || equipmentItem.getType().isAir()) continue;
            Optional<RPGItem> rpgItemOpt = ItemManager.toRPGItem(equipmentItem);
            if (!rpgItemOpt.isPresent()) continue;
            if (equipmentItem.equals(minionItem)) continue;

            RPGItem rpgItem = rpgItemOpt.get();
            Optional<Double> result = rpgItem.power(player, equipmentItem, evt, BaseTrigger.MINION_ATTACK_HIT);
            if (result.isPresent()) {
                double newDamage = result.get();
                if (newDamage == -1) {
                    evt.setCancelled(true);
                    break;
                }
                evt.setDamage(newDamage);
            }
        }
    }

    // Monitor damage events from players to detect minion attack hits
    // Uses HIGH priority (same as RPGItems) - since this plugin loads after RPGItems, it runs after HIT/HIT_GLOBAL
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDealDamage(EntityDamageByEntityEvent evt) {
        Entity damager = evt.getDamager();
        Player player = null;
        IMinion minion = null;

        // Check if damager is player directly or a projectile shot by player
        if (damager instanceof Player) {
            player = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            MinionAttackInfo projectileInfo = minionProjectiles.get(projectile.getUniqueId());
            if (projectileInfo != null) {
                if (projectileInfo.isValid(MINION_PROJECTILE_WINDOW_MS)) {
                    minion = projectileInfo.minion;
                    player = getOnlineOwner(minion);
                } else {
                    minionProjectiles.remove(projectile.getUniqueId());
                }
            }
            if (minion == null) {
                minion = getProjectileSourceMinion(projectile);
                if (minion != null) {
                    player = getOnlineOwner(minion);
                }
            }
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                player = (Player) shooter;
            }
        }

        if (player == null) return;
        if (evt.isCancelled()) return;

        // Check if this damage is from a minion attack context (synchronous)
        Optional<Object> minionContext = LightContext.getTemp(player.getUniqueId(), MINION_ATTACK_CONTEXT);

        if (minion == null && minionContext.isPresent() && minionContext.get() != null) {
            minion = (IMinion) minionContext.get();
        } else if (minion == null) {
            // Check time-based cache for async damage (projectiles)
            MinionAttackInfo attackInfo = recentMinionAttacks.get(player.getUniqueId());
            if (attackInfo != null && attackInfo.isValid(MINION_ATTACK_WINDOW_MS)) {
                minion = attackInfo.minion;
            }
        }

        if (minion == null) return;
        Entity target = evt.getEntity();

        // Don't trigger for damage to the minion's owner
        if (target.getUniqueId().equals(player.getUniqueId())) return;

        // Trigger MINION_ATTACK_HIT powers directly with the EntityDamageByEntityEvent
        // This allows proper chaining with HIT/HIT_GLOBAL damage modifiers
        triggerMinionAttackHit(player, minion, evt);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMinionProjectileLaunch(ProjectileLaunchEvent event) {
        IMinion minion = getProjectileMinion(event.getEntity());
        if (minion == null) {
            return;
        }
        UUID projectileId = event.getEntity().getUniqueId();
        minionProjectiles.put(projectileId, new MinionAttackInfo(minion));
        new BukkitRunnable() {
            @Override
            public void run() {
                minionProjectiles.remove(projectileId);
            }
        }.runTaskLater(MinionExtensionPlugin.plugin, MINION_PROJECTILE_WINDOW_MS / 50L);
    }

    private IMinion getProjectileMinion(Projectile projectile) {
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Entity) {
            IMinion minion = MinionManager.getInstance().toIMinion((Entity) shooter);
            if (minion != null) {
                return minion;
            }
        }
        if (shooter instanceof Player) {
            Optional<Object> minionContext = LightContext.getTemp(((Player) shooter).getUniqueId(), MINION_ATTACK_CONTEXT);
            if (minionContext.isPresent() && minionContext.get() instanceof IMinion) {
                return (IMinion) minionContext.get();
            }
        }
        return null;
    }

    private IMinion getProjectileSourceMinion(Projectile projectile) {
        String sourceId = projectile.getPersistentDataContainer().get(RPGITEM_SOURCE_ENTITY_KEY, PersistentDataType.STRING);
        if (sourceId == null) {
            return null;
        }
        try {
            Entity source = Bukkit.getEntity(UUID.fromString(sourceId));
            if (source == null) {
                return null;
            }
            return MinionManager.getInstance().toIMinion(source);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Player getOnlineOwner(IMinion minion) {
        OfflinePlayer owner = minion.getOwner();
        if (owner == null || !owner.isOnline()) {
            return null;
        }
        return owner.getPlayer();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMinionHurt(EntityDamageEvent evt){
        IMinion iMinion = MinionManager.getInstance().toIMinion(evt.getEntity());
        //adapt /minecraft:kill
        if (iMinion != null && !evt.getCause().equals(EntityDamageEvent.DamageCause.VOID)){
            evt.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMinionDeath(EntityDeathEvent evt){
        IMinion iMinion = MinionManager.getInstance().toIMinion(evt.getEntity());
        if (iMinion != null){
            evt.setDroppedExp(0);
            evt.getDrops().clear();
        }
    }


    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMinionHurt(EntityDamageByEntityEvent evt){
        IMinion iMinion = MinionManager.getInstance().toIMinion(evt.getDamager());
        if (iMinion!=null){
            OfflinePlayer owner = iMinion.getOwner();
            Entity entity = evt.getEntity();
            if (owner.getUniqueId().equals(entity.getUniqueId())){
                evt.setCancelled(true);
                return;
            }
            if (owner.isOnline()) {
                Player player = owner.getPlayer();
                evt.setCancelled(true);

                // Get LightContext from minion - this contains the actual damage from RPGItems powers
                Optional<Object> source = LightContext.getTemp(iMinion.getEntity().getUniqueId(), DAMAGE_SOURCE);
                Optional<Object> overridingDamage = LightContext.getTemp(iMinion.getEntity().getUniqueId(), OVERRIDING_DAMAGE);
                Optional<Object> supressMelee = LightContext.getTemp(iMinion.getEntity().getUniqueId(), SUPPRESS_MELEE);
                Optional<Object> sourceItem = LightContext.getTemp(iMinion.getEntity().getUniqueId(), DAMAGE_SOURCE_ITEM);

                // Transfer context to player
                source.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE, source.get());});
                overridingDamage.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), OVERRIDING_DAMAGE, overridingDamage.get());});
                supressMelee.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), SUPPRESS_MELEE, supressMelee.get());});
                if (sourceItem.isPresent()) {
                    LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE_ITEM, sourceItem.get());
                } else {
                    ItemStack minionItem = iMinion.getItemStack();
                    if (minionItem != null) {
                        LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE_ITEM, minionItem);
                    }
                }

                // Mark this redirected damage as a minion attack so MINION_ATTACK_HIT runs
                LightContext.putTemp(player.getUniqueId(), MINION_ATTACK_CONTEXT, iMinion);
                recentMinionAttacks.put(player.getUniqueId(), new MinionAttackInfo(iMinion));

                // Use the modified damage from event
                double damage = evt.getDamage();

                ((LivingEntity) entity).damage(damage, player);
                LightContext.clear();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent evt){
        if (!(evt.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player) evt.getEntity();
        Entity damager = evt.getDamager();
        notifyMinionsChangeTarget(player, damager);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamageOthers(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)){
            return;
        }
        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();
        notifyMinionsChangeTarget(player, entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSlotMax(MinionMaxEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = Database.getInstance().getPlayerData(player.getUniqueId());
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();
        ItemStack itemInOffHand = inventory.getItemInOffHand();
        ItemStack itemInMainHand = inventory.getItemInMainHand();
        AtomicInteger extraSlots = new AtomicInteger();
        Stream.concat(Stream.of(armorContents), Stream.of(itemInOffHand, itemInMainHand))
                .forEach(itemStack -> {
                    if (itemStack == null) {
                        return;
                    }
                    Optional<RPGItem> opt = ItemManager.toRPGItem(itemStack);
                    if (!opt.isPresent()) {
                        return;
                    }

                    RPGItem rgi = opt.get();
                    List<? extends ConditionedMarker<Integer>> markers = rgi.getMarker(MinionMax.class);
                    List<Condition<?>> conditions = rgi.getConditions();
                    Map<Condition<?>, PowerResult<?>> staticCondition = ConditionChecker.checkStaticCondition(player, itemStack, conditions, markers);
                    Map<PropertyHolder, PowerResult<?>> resultMap = new LinkedHashMap<>(staticCondition);
                    int sum = rgi.getMarker(MinionMax.class).stream()
                            .filter(marker -> ConditionChecker.checkConditions(player, itemStack, marker, conditions, resultMap) == null)
                            .mapToInt(MinionMax::getMax)
                            .sum();
                    extraSlots.addAndGet(sum);
                });
        event.setMax(extraSlots.get() + playerData.slotMax);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractAtEntityEvent event) {
        IMinion iMinion = MinionManager.getInstance().toIMinion(event.getRightClicked());
        if (iMinion == null){
            return;
        }
        event.setCancelled(true);
        iMinion.setStatus(MinionStatus.IDLE);
        iMinion.ambientAction();
        OfflinePlayer owner = iMinion.getOwner();
        if (owner.isOnline() && owner.getUniqueId().equals(event.getPlayer().getUniqueId())){
            final UUID uniqueId = iMinion.getEntity().getUniqueId();
            if (toRemove.contains(uniqueId)){
                MinionManager.getInstance().removeMinion(iMinion);
                return;
            }
            toRemove.add(uniqueId);
            new BukkitRunnable(){
                @Override
                public void run() {
                    toRemove.remove(uniqueId);
                }
            }.runTaskLater(MinionExtensionPlugin.plugin, 10);
        }
    }

    static List<UUID> toRemove = new ArrayList<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMount(EntityMountEvent event){
        MinionManager instance = MinionManager.getInstance();
        Entity entity = event.getEntity();
        Entity mount = event.getMount();
        IMinion entityMinion = instance.toIMinion(entity);
        IMinion mountMinion = instance.toIMinion(mount);
        if (entityMinion != null || mountMinion != null){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent event){
        removeEntitiesInChunk(event.getChunk());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event){
        removeEntitiesInChunk(event.getChunk());
    }

    private void removeEntitiesInChunk(Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        if (entities.length > 0){
            for (Entity entity : entities) {
                Set<String> scoreboardTags = entity.getScoreboardTags();
                if (scoreboardTags.contains(BaseMinion.TAG_MINION)){
                    // Try to clean up via MinionManager first
                    MinionManager.getInstance().removeMinion(entity.getUniqueId());
                    // Also forcefully remove the entity in case it's not tracked
                    // (e.g., persisted through restart or already removed from manager)
                    if (!entity.isDead()) {
                        entity.remove();
                    }
                }
            }
        }
    }

    private void notifyMinionsChangeTarget(Player player, Entity damager) {
        List<IMinion> minions = MinionManager.getInstance().getMinions(player);
        if (minions.isEmpty()){
            return;
        }
        minions.forEach(minion -> {
            // Use PLAYER_COMBAT priority - this will override AUTO targets
            // but not MANUAL targets (set via direct player interaction)
            if (minion.isValidTarget(damager)){
                minion.setTarget(damager, TargetPriority.PLAYER_COMBAT);
            }
        });
    }

    private void notifyMinionsStop(Player player){
        List<IMinion> minions = MinionManager.getInstance().getMinions(player);
        if (minions.isEmpty()){
            return;
        }
        minions.forEach(minion -> {
            minion.setStatus(MinionStatus.IDLE);
        });
    }
}
