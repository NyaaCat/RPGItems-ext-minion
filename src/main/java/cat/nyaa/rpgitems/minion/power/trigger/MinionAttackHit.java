package cat.nyaa.rpgitems.minion.power.trigger;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.PowerHit;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

import java.util.Optional;

import static think.rpgitems.power.Utils.maxWithCancel;

/**
 * Trigger for when a minion's attack hits an entity.
 * Uses EntityDamageByEntityEvent directly (like HIT_GLOBAL) so it properly
 * chains with other damage modifiers.
 */
public class MinionAttackHit extends Trigger<EntityDamageByEntityEvent, PowerHit, Double, Optional<Double>> {

    @SuppressWarnings("unchecked")
    public MinionAttackHit() {
        super("MINION_ATTACK_HIT", EntityDamageByEntityEvent.class, PowerHit.class, Double.class, (Class<Optional<Double>>) (Class<?>) Optional.class);
        register(this);
    }

    @Override
    public PowerResult<Double> run(PowerHit power, Player player, ItemStack itemStack, EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            return power.hit(player, itemStack, (LivingEntity) event.getEntity(),
                    event.getDamage(), event);
        }
        return PowerResult.ok(event.getDamage());
    }

    @Override
    public Optional<Double> def(Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> next(Optional<Double> a, PowerResult<Double> b) {
        return b.isOK() ? Optional.ofNullable(maxWithCancel(a.orElse(null), b.data())) : a;
    }

    @Override
    public PowerResult<Double> warpResult(PowerResult<Void> overrideResult, PowerHit power, Player player, ItemStack i, EntityDamageByEntityEvent event) {
        return overrideResult.with(event.getDamage());
    }
}
