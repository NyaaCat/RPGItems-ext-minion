package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface Targetable {
    Entity getTarget();
    void setTarget(Entity target);
    TargetMode getTargetMode();

    void attack(Location location);
    void attack(Entity entity);
}
