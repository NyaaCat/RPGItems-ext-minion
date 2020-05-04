package cat.nyaa.rpgitems.minion.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionChangeTargetEvent extends Event {
    Entity source;
    Entity target = null;
    Location targetLocation = null;

    public MinionChangeTargetEvent(Entity source, Entity target) {
        super();
        this.source = source;
        this.target = target;
    }

    public MinionChangeTargetEvent(Entity source, Location targetLocation) {
        super();
        this.source = source;
        this.targetLocation = targetLocation;
    }

    public Entity getSource() {
        return source;
    }

    public Entity getTarget() {
        return target;
    }

    public Location getTargetLocation() {
        return targetLocation;
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
