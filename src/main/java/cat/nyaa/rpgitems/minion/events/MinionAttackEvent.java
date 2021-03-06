package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class MinionAttackEvent extends MinionEvent {
    LivingEntity attacker;
    Entity source;
    Location fromLocation;
    Vector towards;

    private static final HandlerList handlerList = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public MinionAttackEvent(IMinion minion, LivingEntity attacker, Entity source, Location fromLocation, Vector towards) {
        super(minion);
        this.attacker = attacker;
        this.source = source;
        this.fromLocation = fromLocation;
        this.towards = towards;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public Entity getSource() {
        return source;
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public Vector getTowards() {
        return towards;
    }
}
