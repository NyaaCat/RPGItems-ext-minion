package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.minion.*;
import cat.nyaa.rpgitems.minion.power.impl.Sentry;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class MinionSentry extends BaseMinion implements ISentry {
    private final Sentry power;

    public MinionSentry(Player player, Sentry power, ItemStack item){
        super(player, item);
        this.power = power;
        initialize();
    }

    private void initialize() {
        Sentry sentryPower = getSentryPower();
        this.ttl = sentryPower.getTtl();
        this.damage = sentryPower.getDamage();
        this.attackInterval = sentryPower.getAttackInteval();
        this.slotCost = sentryPower.getSlotCost();
        this.targetingRange = sentryPower.getTargetRange();
        this.autoAttack = sentryPower.isAutoAttackTarget();
        this.display = sentryPower.getDisplay();
        this.ranAtkIntFac = sentryPower.getRanAtkIntFac();
        this.spinMode = sentryPower.getSpinMode();
        this.spinSpeed = sentryPower.getSpinSpeed().random();
        this.spinSpeedMax = sentryPower.getSpinSpeed().uniformed(1,1);
        new BukkitRunnable() {
            @Override
            public void run() {
                MinionManager.getInstance().removeMinion(MinionSentry.this);
            }
        }.runTaskLater(MinionExtensionPlugin.plugin, ttl);
    }

    @Override
    public Sentry getSentryPower() {
        return power;
    }

    @Override
    protected EntityInfo getEntityInfo() {
        Sentry sentryPower = getSentryPower();
        return new EntityInfo(sentryPower.getEntityType(), sentryPower.getTags(), sentryPower.getNbt());
    }

    @Override
    protected void onTargetChange(Entity target) {
        if (target == null){
            setStatus(MinionStatus.IDLE);
            return;
        }
        if (isValidTarget(target)){
            setStatus(MinionStatus.ATTACK);
        }
    }

    int currentTargetModified = 0;

    @Override
    protected void onTargetChange(Location targetLocation) {
        if (targetLocation == null){
            setStatus(MinionStatus.IDLE);
            return;
        }
        int taskId = ++currentTargetModified;
        setStatus(MinionStatus.ATTACK);
        new BukkitRunnable(){
            @Override
            public void run() {
                if (taskId == currentTargetModified) {
                    setStatus(MinionStatus.IDLE);
                }
            }
        }.runTaskLater(MinionExtensionPlugin.plugin, 200);
    }

    private boolean attacking = false;

    @Override
    public void tick(int minionTick) {
        super.tick(minionTick);
    }

    @Override
    public void attack(Location location) {
        if (target != null){
            if (target.isDead()){
                setTarget(null);
                setTargetLocation(null);
            }
        }
        if (attackCooldown > 0 || attacking){
            return;
        }
        new AttackTask(location).runTaskTimer(MinionExtensionPlugin.plugin, 0, 1);
    }

    @Override
    public void attack(Entity entity) {
        if (target != null){
            if (target.isDead()){
                setTarget(null);
            }
        }
        if (attackCooldown > 0 || attacking){
            return;
        }
        new AttackTask(entity).runTaskTimer(MinionExtensionPlugin.plugin, 0, 1);
    }

    class AttackTask extends BukkitRunnable {
        Location location;
        Entity entity;
        boolean initialized = false;

        AttackMode attackMode;

        public AttackTask(Location location) {
            super();
            this.attackMode = AttackMode.LOCATION;
            this.location = location;
        }

        public AttackTask(Entity target) {
            super();
            this.attackMode = AttackMode.ENTITY;
            this.entity = target;
        }

        @Override
        public void run() {
            if (!initialized){
                initialize();
            }
            if (getStatus().equals(MinionStatus.ATTACK)){
                this.cancel();
            }
            OfflinePlayer owner = getOwner();
            if (!owner.isOnline()) {
                this.cancel();
                return;
            }
            if (!MinionSentry.this.spinMode.equals(SpinMode.ALWAYS) && !checkRotation()){
                rotateToTarget();
                return;
            }
            if (isTargetAutoLocked()){
                if (entity!=null && entity.getLocation().distance(getEntity().getLocation()) > targetingRange){
                    this.cancel();
                    return;
                }
            }

            Location selfLocation = getSelfLocation(trackedEntity);
            Location target = targetLocation.clone();
            if (entity != null){
                target = getSelfLocation(entity);
            }
            Location tool = selfLocation.clone();
            Vector subtract = target.toVector().subtract(selfLocation.toVector());
            tool.setDirection(subtract);
            if (MinionSentry.this.spinMode.equals(SpinMode.ALWAYS)){
                trackedEntity.setRotation(tool.getYaw(), tool.getPitch());
            }
            broadcastAttack(owner.getPlayer());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (MinionSentry.this.spinMode.equals(SpinMode.ALWAYS)){
                        trackedEntity.setRotation(selfLocation.getYaw(), selfLocation.getPitch());
                    }
                }
            }.runTaskLater(MinionExtensionPlugin.plugin, 0);

            attackCooldown = attackInterval + randomInteval(ranAtkIntFac);
            this.cancel();
        }

        private void rotateToTarget() {
            EntityRotater rotater = getRotater();
            if (rotater.isRotating()){
                return;
            }
            if (entity != null){
                rotater.rotateTo(entity)
                        .speed(180)
                        .commitRotating();
            }else {
                rotater.rotateToLocation(location)
                        .speed(180)
                        .commitRotating();
            }
        }

        private int randomInteval(double factor){
            return (int) Math.round(ThreadLocalRandom.current().nextDouble(-factor, factor) * attackInterval);
        }

        private void initialize() {
            initialized = true;
            attacking = true;
            initializeRotateTask();
        }

        private void initializeRotateTask() {
            EntityRotater rotater = getRotater();
            if (entity != null){
                rotater.rotateTo(entity);
            }else if (location != null){
                rotater.rotateToLocation(location);
            }
            rotater.speed(180)
                    .commitRotating();
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            attacking = false;
        }

        private boolean checkRotation() {
            Location target = targetLocation.clone();
            if (entity != null){
                target = getSelfLocation(entity);
            }
            Location selfLocation = getSelfLocation(trackedEntity);
            Location subtract = target.subtract(selfLocation);

            double angle = Math.toDegrees(subtract.toVector().angle(selfLocation.getDirection()));
            return !getRotater().isRotating() && angle < 5;
        }

    }

    enum AttackMode{
        LOCATION, ENTITY
    }
}
