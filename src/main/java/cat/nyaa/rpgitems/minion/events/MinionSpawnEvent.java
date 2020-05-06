package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class MinionSpawnEvent extends MinionEvent {
    Location spawnLocation;

    public MinionSpawnEvent(IMinion minion, Location spawnLocation) {
        super(minion);
        this.spawnLocation = spawnLocation;
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
