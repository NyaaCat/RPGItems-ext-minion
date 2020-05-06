package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class MinionMoveEvent extends MinionEvent {
    Entity entity;
    Location oldLocation;
    Location newLocation;

    public MinionMoveEvent(IMinion minion, Entity entity, Location oldLocation, Location newLocation) {
        super(minion);
        this.entity = entity;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
    }

    public Entity getEntity() {
        return entity;
    }

    public Location getOldLocation() {
        return oldLocation;
    }

    public Location getNewLocation() {
        return newLocation;
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
