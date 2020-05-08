package cat.nyaa.rpgitems.minion.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MinionMaxEvent extends Event {
    private Player player;
    private int max = 1;

    public MinionMaxEvent(Player player, int max){
        this.player = player;
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public Player getPlayer() {
        return player;
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
