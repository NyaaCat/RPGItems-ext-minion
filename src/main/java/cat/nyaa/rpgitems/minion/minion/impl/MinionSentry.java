package cat.nyaa.rpgitems.minion.minion.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.minion.EntityInfo;
import cat.nyaa.rpgitems.minion.minion.MinionStatus;
import cat.nyaa.rpgitems.minion.power.impl.Sentry;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.item.RPGItem;

public class MinionSentry extends BaseMinion implements ISentry {
    private final Sentry power;

    public MinionSentry(Player player, Sentry power, ItemStack item){
        super(player, item);
        this.power = power;
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

    }

    @Override
    protected void onTargetChange(Location targetLocation) {

    }

    @Override
    public void attack(Location location) {
        new AttackTask(location).runTaskTimer(MinionExtensionPlugin.plugin, 0, 1);
    }

    @Override
    public void attack(Entity entity) {
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
            if (!checkRotation()){
                return;
            }
            OfflinePlayer owner = getOwner();
            if (owner.isOnline()) {
                broadcastAttack(owner.getPlayer());
            }
            this.cancel();
        }

        MinionStatus prevStatus;

        private void initialize() {
            initialized = true;
            prevStatus = getStatus();
            initializeRotateTask();
            MinionSentry.this.status = MinionStatus.ATTACK;
        }

        private void initializeRotateTask() {
            getRotater().setTarget(entity);

        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            status = prevStatus;
        }

        private boolean checkRotation() {
            return true;
        }

    }
    enum AttackMode{
        LOCATION, ENTITY
    }
}
