package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.entity.Entity;

public class EntitySpinner implements Spinnable {
    private Entity entity;
    private double speed;

    public EntitySpinner(){}

    @Override
    public double getSpinSpeed() {
        return speed;
    }

    @Override
    public void setSpinSpeed(double spinSpeed) {
        this.speed = spinSpeed;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void spin() {
        if (entity == null || entity.isDead()){
            return;
        }
        float yaw = entity.getLocation().getYaw();
        entity.setRotation((float) (yaw+speed/20), entity.getLocation().getPitch());
    }
}
