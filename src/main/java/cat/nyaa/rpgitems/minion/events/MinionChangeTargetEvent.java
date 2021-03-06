package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class MinionChangeTargetEvent extends MinionEvent {
    Entity source;
    Entity target = null;
    Location targetLocation = null;

    public MinionChangeTargetEvent(IMinion minion, Entity source, Entity target) {
        super(minion);
        this.source = source;
        this.target = target;
    }

    public MinionChangeTargetEvent(IMinion minion, Entity source, Location targetLocation) {
        super(minion);
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
