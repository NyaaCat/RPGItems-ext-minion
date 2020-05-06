package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;

public interface Movable {
    void moveTo(Location location);
    void teleportTo(Location location);
}
