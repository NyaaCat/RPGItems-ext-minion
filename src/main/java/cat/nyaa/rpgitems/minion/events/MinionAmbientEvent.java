package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

public class MinionAmbientEvent extends MinionEvent {
    Entity source;

    public MinionAmbientEvent(IMinion minion, Entity source){
        super(minion);
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
