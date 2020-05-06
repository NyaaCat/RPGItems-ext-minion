package cat.nyaa.rpgitems.minion.minion;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class EntityRotater implements Rotatable {
    Entity trackedEntity;
    Entity target;
    Location location;
    private double speed;

    public Entity getTrackedEntity() {
        return trackedEntity;
    }

    public void setTrackedEntity(Entity trackedEntity) {
        this.trackedEntity = trackedEntity;
    }

    @Override
    public void speed(double speed) {
        this.speed = speed;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public void reset() {
        trackedEntity = null;
        target = null;
        location = null;
        speed = 1d;
    }

    BukkitTask bukkitTask;

    @Override
    public void commitRotating() {
        bukkitTask = new RotateTask().runTaskTimer(MinionExtensionPlugin.plugin, 0, 1);
    }

    @Override
    public void cancelRotating() {
        bukkitTask.cancel();
    }

    @Override
    public boolean isRotating() {
        return false;
    }

    public void rotateTo(Entity nearestPlayer) {
        reset();
        setTarget(nearestPlayer);
        commitRotating();
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public void setLocation(Location location){
        this.location = location;
    }

    public void rotateToLocation(Location location, double speed) {
        reset();
        setLocation(location);
        commitRotating();
    }


    private void internalRotate(){
        Location selfLocation = getSelfLocation();
        Location eyeLocation = location;
        Vector targetDirection = eyeLocation.clone().toVector().subtract(selfLocation.toVector());
        float toRotate = targetDirection.angle(selfLocation.getDirection());
        float pitch = targetDirection.clone().setY(0).angle(targetDirection);
        //todo
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

    class RotateTask extends BukkitRunnable{

        @Override
        public void run() {

        }
    }
}
