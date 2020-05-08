package cat.nyaa.rpgitems.minion;

import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.events.MinionMaxEvent;
import cat.nyaa.rpgitems.minion.minion.IMinion;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.minion.MinionStatus;
import cat.nyaa.rpgitems.minion.power.marker.MinionMax;
import cat.nyaa.rpgitems.minion.power.trigger.BaseTrigger;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityMountEvent;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.utils.LightContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static think.rpgitems.Events.*;
import static think.rpgitems.Events.DAMAGE_SOURCE_ITEM;

public class MainEvents implements Listener {

    @EventHandler
    public void onMinionAttack(MinionAttackEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem -> {
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_ATTACK);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMinionHit(EntityDamageByEntityEvent evt){
        IMinion iMinion = MinionManager.getInstance().toIMinion(evt.getDamager());
        if (iMinion!=null){
            OfflinePlayer owner = iMinion.getOwner();
            Entity entity = evt.getEntity();
            if (owner.getUniqueId().equals(entity.getUniqueId())){
                evt.setCancelled(true);
                return;
            }
            if (owner.isOnline()) {
                double damage = evt.getDamage();
                Player player = owner.getPlayer();
                evt.setCancelled(true);
                Optional<Object> source = LightContext.getTemp(iMinion.getEntity().getUniqueId(), DAMAGE_SOURCE);
                Optional<Object> overridingDamage = LightContext.getTemp(iMinion.getEntity().getUniqueId(), OVERRIDING_DAMAGE);
                Optional<Object> supressMelee = LightContext.getTemp(iMinion.getEntity().getUniqueId(), SUPPRESS_MELEE);
                Optional<Object> sourceItem = LightContext.getTemp(iMinion.getEntity().getUniqueId(), DAMAGE_SOURCE_ITEM);
                source.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE, source.get());});
                overridingDamage.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), OVERRIDING_DAMAGE, overridingDamage.get());});
                supressMelee.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), SUPPRESS_MELEE, supressMelee.get());});
                sourceItem.ifPresent(obj -> {LightContext.putTemp(player.getUniqueId(), DAMAGE_SOURCE_ITEM, sourceItem.get());});
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
                    int sum = rgi.getMarker(MinionMax.class).stream()
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

    private void notifyMinionsChangeTarget(Player player, Entity damager) {
        List<IMinion> minions = MinionManager.getInstance().getMinions(player);
        if (minions.isEmpty()){
            return;
        }
        minions.forEach(minion -> {
            if (minion.getStatus().equals(MinionStatus.IDLE)){
                if (minion.isValidTarget(damager)){
                    minion.setTarget(damager);
                }
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
