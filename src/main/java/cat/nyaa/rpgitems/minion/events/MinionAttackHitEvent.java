package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Event fired when a minion's attack actually hits an entity.
 * This is triggered after the attack lands, allowing powers to modify damage
 * or trigger effects on hit.
 */
public class MinionAttackHitEvent extends MinionEvent {
    private Entity target;
    private double damage;
    private final EntityDamageByEntityEvent originalEvent;

    private static final HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public MinionAttackHitEvent(IMinion minion, Entity target, double damage, EntityDamageByEntityEvent originalEvent) {
        super(minion);
        this.target = target;
        this.damage = damage;
        this.originalEvent = originalEvent;
    }

    public Entity getTarget() {
        return target;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public EntityDamageByEntityEvent getOriginalEvent() {
        return originalEvent;
    }
}
