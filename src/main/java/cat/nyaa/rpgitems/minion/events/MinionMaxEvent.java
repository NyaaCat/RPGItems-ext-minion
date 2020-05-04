package cat.nyaa.rpgitems.minion.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionMaxEvent extends Event {
    private int max = 1;

    public MinionMaxEvent(int max){
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
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
