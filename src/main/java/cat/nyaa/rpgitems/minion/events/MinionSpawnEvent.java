package cat.nyaa.rpgitems.minion.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionSpawnEvent extends Event {
    Entity entity;
    Location spawnLocation;

    public MinionSpawnEvent(Entity entity, Location spawnLocation) {
        super();
        this.entity = entity;
        this.spawnLocation = spawnLocation;
    }

    public Entity getEntity() {
        return entity;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    private static final HandlerList handlerList = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
