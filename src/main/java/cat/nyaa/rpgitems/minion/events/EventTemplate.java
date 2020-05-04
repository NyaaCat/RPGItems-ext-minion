package cat.nyaa.rpgitems.minion.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EventTemplate extends Event {
    private static final HandlerList handlerList = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }
}
