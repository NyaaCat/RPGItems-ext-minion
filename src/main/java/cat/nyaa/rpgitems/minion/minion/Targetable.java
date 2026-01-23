package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface Targetable {
    Entity getTarget();
    void setTarget(Entity target);
    void setTarget(Entity target, TargetPriority priority);
    Location getTargetLocation();
    void setTargetLocation(Location location);
    TargetMode getTargetMode();
    boolean isTargetAutoLocked();
    TargetPriority getTargetPriority();

    void attack(Location location);
    void attack(Entity entity);
}
