package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import cat.nyaa.rpgitems.minion.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class EntityRotater implements Rotatable {
    Entity trackedEntity;
    Entity target;
    Location location;
    private double speed;
    private boolean pitchOnly = false;

    public boolean isPitchOnly() {
        return pitchOnly;
    }

    public void setPitchOnly(boolean pitchOnly) {
        this.pitchOnly = pitchOnly;
    }

    public Entity getTrackedEntity() {
        return trackedEntity;
    }

    public void setTrackedEntity(Entity trackedEntity) {
        this.trackedEntity = trackedEntity;
    }

    public EntityRotater speed(double speed){
        setSpeed(speed);
        return this;
    }

    @Override
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void reset() {
        target = null;
        location = null;
        speed = 1d;
        cancelRotating();
    }

    private RotateTask rotateTask;

    @Override
    public void commitRotating() {
        if (rotateTask != null && rotateTask.isRunning()){
            cancelRotating();
        }
        rotateTask = new RotateTask(trackedEntity, target, location, speed);
        rotateTask.runTaskTimer(MinionExtensionPlugin.plugin, 0, 1);
    }

    @Override
    public void cancelRotating() {
        if (rotateTask!= null){
            rotateTask.finish();
            rotateTask = null;
        }
    }

    @Override
    public boolean isRotating() {
        return rotateTask != null && rotateTask.isRunning();
    }

    public EntityRotater rotateTo(Entity nearestPlayer) {
        reset();
        setTarget(nearestPlayer);
        return this;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    public EntityRotater rotateToLocation(Location location) {
        reset();
        setLocation(location);
        return this;
    }

    private Location getSelfLocation(Entity trackedEntity) {
        Location selfLocation;
        if (trackedEntity instanceof LivingEntity){
            selfLocation = ((LivingEntity) trackedEntity).getEyeLocation();
        }else {
            selfLocation = trackedEntity.getLocation();
        }
        return selfLocation.clone();
    }

    class RotateTask extends BukkitRunnable{
        private final Entity trackedEntity;
        private final Entity target;
        private final Location location;
        private final double speed;
        boolean running = false;

        public RotateTask(Entity trackedEntity, Entity target, Location location, double speed) {
            this.trackedEntity = trackedEntity;
            this.target = target;
            this.location = location;
            this.speed = speed;
        }

        @Override
        public void run() {
            try {
                running = true;
                if (trackedEntity == null || trackedEntity.isDead()){
                    finish();
                    return;
                }
                Location selfLocation = getSelfLocation(trackedEntity);
                Location eyeLocation = location;
                if (target != null){
                    eyeLocation = getSelfLocation(target);
                }
                Vector targetDirection = eyeLocation.clone().toVector().subtract(selfLocation.toVector());

                double toRotate = Math.toDegrees(Utils.angle(
                        selfLocation.clone().getDirection().setY(0),
                        targetDirection.clone().setY(0))
                );
                if (toRotate > 180){
                    toRotate -= 360;
                }
                double toPitch = -selfLocation.getPitch() + Math.toDegrees(
                        Utils.angle(targetDirection.clone().setY(0), targetDirection)
                );

                int rSign = toRotate >= 0 ? 1 : -1;
                int pSign = toPitch >= 0 ? 1 : -1;

                double maxTickRotation = speed / 20;
                double maxTickPitch = maxTickRotation / 2;

                double dRotate = rSign * Math.min(Math.abs(maxTickRotation), Math.abs(toRotate));
                double dPitch = pSign * Math.min(Math.abs(maxTickPitch), Math.abs(toPitch));

                if (isPitchOnly()){
                    dRotate = 0;
                }

                float yaw = (float) (selfLocation.getYaw() + dRotate);
                float pitch = (float) (selfLocation.getPitch() + dPitch);
                if (Float.isInfinite(yaw)){
                    yaw = 0;
                }
                if (Float.isInfinite(pitch)){
                    pitch = 0;
                }
                trackedEntity.setRotation(yaw, pitch);

                if (Math.abs(dRotate) < Math.abs(maxTickRotation) && Math.abs(dPitch) < Math.abs(maxTickPitch)) {
                    finish();
                }
            }catch (Exception e){
                e.printStackTrace();
                finish();
            }
        }

        private void finish() {
            this.cancel();
            running = false;
        }

        public boolean isRunning() {
            return running;
        }
    }
}
