package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.nyaacore.utils.NmsUtils;
import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.events.MinionChangeTargetEvent;
import cat.nyaa.rpgitems.minion.minion.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseMinion implements IMinion {
    private OfflinePlayer owner;
    private ItemStack fromItem;
    protected MinionStatus status = MinionStatus.IDLE;
    protected Entity trackedEntity;
    protected EntityRotater rotater = new EntityRotater();
    protected Location lastTrackedLocation;
    protected Entity target;
    protected Location targetLocation;
    protected int nearbyRange = 24;
    protected int slotCost = 1;
    protected TargetMode targetMode = TargetMode.MOBS;

    public BaseMinion(Player owner, ItemStack fromItem) {
        this.owner = owner;
        this.fromItem = fromItem;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    @Override
    public void ambientAction() {
        if (status.equals(MinionStatus.IDLE)){
            if (!rotater.isRotating()){
                Optional<Player> nearestPlayer = getNearestPlayer(trackedEntity, nearbyRange);
                nearestPlayer.ifPresent(this::lookAtPlayer);
            }
        }
    }

    private void lookAtPlayer(Player nearestPlayer) {
        rotater.rotateTo(nearestPlayer);
    }

    private Location getSelfLocation() {
        Location selfLocation;
        if (trackedEntity instanceof LivingEntity){
            selfLocation = ((LivingEntity) trackedEntity).getEyeLocation();
        }else {
            selfLocation = trackedEntity.getLocation();
        }
        return selfLocation.clone();
    }

    public static Optional<Player> getNearestPlayer(Entity target, double range) {
        Location location = target.getLocation();
        return target.getNearbyEntities(range, range, range).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> ((Player) entity))
                .filter(player -> player.hasLineOfSight(target))
                .min(Comparator.comparingDouble(player -> player.getEyeLocation().distance(location)));
    }

    @Override
    public MinionStatus getStatus() {
        return status;
    }

    @Override
    public void tick(int minionTick) {
        if (trackedEntity == null || trackedEntity.isDead()){
            respawn(lastTrackedLocation);
        }
        if (minionTick % 40 == 0 && getStatus().equals(MinionStatus.IDLE)){
            this.ambientAction();
        }
        //todo
        lastTrackedLocation = trackedEntity.getLocation();
    }

    @Override
    public int getSlotCost() {
        return slotCost;
    }

    @Override
    public Entity getEntity() {
        return trackedEntity;
    }

    protected abstract EntityInfo getEntityInfo();

    @Override
    public void respawn(Location location) {
        EntityInfo entityInfo = getEntityInfo();
        EntityType entityType = entityInfo.getType();
        String nbt = entityInfo.getNbt();
        List<String> tags = entityInfo.getTags();
        World world = location.getWorld();
        if (world == null){
            return;
        }
        UUID oldUid = trackedEntity == null ? null : trackedEntity.getUniqueId();
        despawn();
        trackedEntity = world.spawnEntity(location, entityType);
        MinionManager.getInstance().replaceEntity(oldUid, trackedEntity.getUniqueId(), this);

        if (nbt != null && !nbt.equals("")){
            NmsUtils.setEntityTag(trackedEntity, nbt);
        }
        trackedEntity.setInvulnerable(true);
        if (!tags.isEmpty()){
            tags.forEach(s -> trackedEntity.addScoreboardTag(s));
        }
        rotater.setTrackedEntity(trackedEntity);
        if (lastTrackedLocation != null){
            trackedEntity.setRotation(lastTrackedLocation.getYaw(), lastTrackedLocation.getPitch());
        }
        lastTrackedLocation = trackedEntity.getLocation();
    }

    @Override
    public void despawn() {
        if (trackedEntity == null){
            return;
        }
        trackedEntity.remove();
        rotater.setTrackedEntity(null);
    }

    @Override
    public Entity getTarget() {
        return target;
    }

    @Override
    public void setTarget(Entity target) {
        MinionChangeTargetEvent minionChangeTargetEvent = new MinionChangeTargetEvent(this, trackedEntity, target);
        Bukkit.getPluginManager().callEvent(minionChangeTargetEvent);
        this.target = target;
        targetLocation = target.getLocation();
        onTargetChange(target);
    }

    @Override
    public Location getTargetLocation() {
        return targetLocation;
    }

    @Override
    public void setTargetLocation(Location location) {
        MinionChangeTargetEvent minionChangeTargetEvent = new MinionChangeTargetEvent(this, trackedEntity, location);
        Bukkit.getPluginManager().callEvent(minionChangeTargetEvent);
        target = null;
        targetLocation = location;
        onTargetChange(location);
    }

    protected abstract void onTargetChange(Entity target);
    protected abstract void onTargetChange(Location targetLocation);

    protected void broadcastAttack(Player commander){
        Location selfLocation = getSelfLocation();
        MinionAttackEvent minionAttackEvent = new MinionAttackEvent(this, commander, trackedEntity, selfLocation, selfLocation.getDirection());
        Bukkit.getPluginManager().callEvent(minionAttackEvent);
    }

    public EntityRotater getRotater() {
        return rotater;
    }

    @Override
    public TargetMode getTargetMode() {
        return targetMode;
    }

    @Override
    public ItemStack getItemStack() {
        return fromItem;
    }

    @Override
    public Optional<RPGItem> getRPGItem() {
        return ItemManager.toRPGItem(fromItem);
    }
}
