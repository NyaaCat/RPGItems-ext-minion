package cat.nyaa.rpgitems.minion.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionAmbientEvent extends Event {
    Entity source;

    public MinionAmbientEvent(Entity source){
        super();
        this.source = source;
    }

    public Entity getSource() {
        return source;
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
