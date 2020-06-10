package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.nyaacore.utils.NmsUtils;
import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.events.MinionAmbientEvent;
import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.events.MinionChangeTargetEvent;
import cat.nyaa.rpgitems.minion.minion.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Utils;
import think.rpgitems.utils.cast.RangedDoubleValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BaseMinion implements IMinion {
    private OfflinePlayer owner;
    private ItemStack fromItem;
    private boolean removed = false;
    protected MinionStatus status = MinionStatus.IDLE;
    protected Entity trackedEntity;
    protected EntityRotater rotater = new EntityRotater();
    protected EntitySpinner spinner = new EntitySpinner();
    protected Location lastTrackedLocation;
    protected Entity target;
    protected Location targetLocation;
    protected double targetingRange = 24;
    protected boolean autoAttack = true;
    protected int nearbyRange = 12;
    protected int slotCost = 1;
    protected TargetMode targetMode = TargetMode.MOBS;
    protected int attackInterval = 20;
    protected int ttl = 1;
    protected double damage = 1;
    protected String display = "";
    protected boolean isTargetAutoLocked = false;
    protected double ranAtkIntFac = 0.1;
    protected SpinMode spinMode = SpinMode.OFF;
    protected double spinSpeed = 0;
    protected double spinSpeedMax = 0;


    protected int attackCooldown = 0;

    public BaseMinion(Player owner, ItemStack fromItem) {
        this.owner = owner;
        this.fromItem = fromItem.clone();
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public void setOwner(OfflinePlayer owner) {
        this.owner = owner;
    }

    @Override
    public void ambientAction() {
        if (spinMode.equals(SpinMode.AMBIENT)){
            selfSpin();
        }
        if (status.equals(MinionStatus.IDLE)){
            if (!rotater.isRotating()){
                if (autoAttack){
                    Optional<Entity> nearestValidTarget = getNearestValidTarget(trackedEntity, targetingRange);
                    if (nearestValidTarget.isPresent()) {
                        autoLockTarget(nearestValidTarget.get());
                        return;
                    }
                }
                if (ThreadLocalRandom.current().nextDouble(1) < 0.5){
                    MinionAmbientEvent minionAmbientEvent = new MinionAmbientEvent(this, getEntity());
                    Bukkit.getPluginManager().callEvent(minionAmbientEvent);
                    if (minionAmbientEvent.isCanceled()){
                        return;
                    }
                    Optional<Player> nearestPlayer = getNearestPlayer(trackedEntity, nearbyRange);
                    nearestPlayer.ifPresent(this::lookAtPlayer);
                }else if (ThreadLocalRandom.current().nextDouble(1) < 0.5){
                    MinionAmbientEvent minionAmbientEvent = new MinionAmbientEvent(this, getEntity());
                    Bukkit.getPluginManager().callEvent(minionAmbientEvent);
                    if (minionAmbientEvent.isCanceled()){
                        return;
                    }
                    lookAround();
                }
            }
        }
    }

    private void selfSpin() {
        spinner.spin();
    }

    private void autoLockTarget(Entity target) {
        setTarget(target);
        this.isTargetAutoLocked = true;
    }

    private void lookAround() {
        Location selfLocation = getSelfLocation(trackedEntity);
        double x = ThreadLocalRandom.current().nextDouble(1);
        double y = ThreadLocalRandom.current().nextDouble(1);
        double z = ThreadLocalRandom.current().nextDouble(1);
        getRotater()
                .rotateToLocation(selfLocation.add(x,y,z))
                .speed(50)
                .commitRotating();
    }

    private Optional<Entity> getNearestValidTarget(Entity trackedEntity, double range) {
        Location location = trackedEntity.getLocation();
        return trackedEntity.getNearbyEntities(range, range, range).stream()
                .filter(this::isValidTarget)
                .min(Comparator.comparingDouble(player -> getSelfLocation(player).distance(location)));
    }

    private void lookAtPlayer(Player nearestPlayer) {
        rotater.rotateTo(nearestPlayer)
                .speed(50)
                .commitRotating();
    }

    public static Location getSelfLocation(Entity entity) {
        Location selfLocation;
        if (entity instanceof LivingEntity){
            selfLocation = ((LivingEntity) entity).getEyeLocation();
        }else {
            selfLocation = entity.getLocation();
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
        lastTrackedLocation = trackedEntity.getLocation();
        if (target != null){
            if(!target.isDead()){
                setStatus(MinionStatus.ATTACK);
            }else {
                setStatus(MinionStatus.IDLE);
            }
        }

        if (getStatus().equals(MinionStatus.ATTACK)){
            if (target != null){
                attack(target);
            }else if (targetLocation !=null){
                attack(targetLocation);
            }else {
                setStatus(MinionStatus.IDLE);
            }
        }
        if (this.spinMode.equals(SpinMode.ALWAYS)){
            rotater.setPitchOnly(true);
            selfSpin();
        }else {
            rotater.setPitchOnly(false);
        }
        attackCooldown--;
    }

    @Override
    public void remove() {
        removed = true;
        despawn();
    }

    @Override
    public boolean isRemoved() {
        return removed;
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
        Set<String> scoreboardTags = trackedEntity.getScoreboardTags();
        if (!scoreboardTags.contains(Utils.INVALID_TARGET)){
            trackedEntity.addScoreboardTag(Utils.INVALID_TARGET);
            trackedEntity.addScoreboardTag("rpgitems-minion");
        }
        if (trackedEntity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) this.trackedEntity;
            livingEntity.setAI(false);
            if (livingEntity instanceof ArmorStand) {
                if (((ArmorStand) livingEntity).isMarker()){
                    Location clone = location.clone();
                    Block relative = location.getBlock().getRelative(BlockFace.DOWN);
                    double dx = clone.getX() - clone.getBlockX();
                    double dy = clone.getY() - clone.getBlockY();
                    double dz = clone.getZ() - clone.getBlockZ();
                    int xSign = dx < 0 ? -1: 1;
                    int ySign = dy < 0 ? -1: 1;
                    int zSign = dz < 0 ? -1: 1;

                    clone.setX(clone.getBlockX() + (xSign * Math.max(0.5, Math.min(Math.abs(dx), 0.7))));
                    clone.setZ(clone.getBlockZ() + (zSign * Math.max(0.5, Math.min(Math.abs(dz), 0.7))));

                    double dy1 = ySign * Math.max(0.5, Math.min(Math.abs(dy), 0.7));
                    if (relative.getType().isAir()) {
                        clone.setY(clone.getBlockY() + dy1);
                    }else {
                        clone.setY(clone.getBlockY() + dy1 + 1);
                    }

                    livingEntity.teleport(clone, PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            }
        }
        if (!tags.isEmpty()){
            tags.forEach(s -> trackedEntity.addScoreboardTag(s));
        }
        if (!display.equals("")){
            trackedEntity.setCustomName(ChatColor.translateAlternateColorCodes('&', display));
            trackedEntity.setCustomNameVisible(true);
        }
        rotater.setTrackedEntity(trackedEntity);
        spinner.setEntity(trackedEntity);
        spinner.setSpinSpeed(spinSpeed);
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
        spinner.setEntity(null);
        spinner.setSpinSpeed(spinSpeed);
    }

    @Override
    public Entity getTarget() {
        return target;
    }

    @Override
    public void setTarget(Entity target) {
        if (target == trackedEntity){
            target = null;
        }
        MinionChangeTargetEvent minionChangeTargetEvent = new MinionChangeTargetEvent(this, trackedEntity, target);
        Bukkit.getPluginManager().callEvent(minionChangeTargetEvent);
        this.target = target;
        if (target == null){
            targetLocation = null;
        }else {
            targetLocation = getSelfLocation(target);
        }
        isTargetAutoLocked = false;
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

    @Override
    public void setStatus(MinionStatus status) {
        this.status = status;
    }

    @Override
    public boolean isValidTarget(Entity target){
        IMinion iMinion = MinionManager.getInstance().toIMinion(target);
        if (iMinion != null){
            return false;
        }
        switch (getTargetMode()){
            case ALL:
                return true;
            case MOBS:
                return target instanceof Mob;
            case PLAYERS:
                return target instanceof Player;
            case PASSIVE:
                return target instanceof Animals;
            default:
                return true;
        }
    }

    protected abstract void onTargetChange(Entity target);
    protected abstract void onTargetChange(Location targetLocation);

    protected void broadcastAttack(Player commander){
        Location selfLocation = getSelfLocation(trackedEntity);
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
    public boolean isTargetAutoLocked() {
        return isTargetAutoLocked;
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
