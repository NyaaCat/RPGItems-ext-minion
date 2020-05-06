package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.Location;

public interface EntityHolderNonpersistent extends EntityHolder {
    void respawn(Location location);
    void despawn();
}
