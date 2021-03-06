package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface Targetable {
    Entity getTarget();
    void setTarget(Entity target);
    Location getTargetLocation();
    void setTargetLocation(Location location);
    TargetMode getTargetMode();
    boolean isTargetAutoLocked();

    void attack(Location location);
    void attack(Entity entity);
}
