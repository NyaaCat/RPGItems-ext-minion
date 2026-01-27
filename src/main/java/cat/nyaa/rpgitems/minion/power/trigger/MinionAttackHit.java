package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionAttackHitEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionAttackHit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerHit;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

import java.util.Optional;

/**
 * Trigger for when a minion's attack hits an entity.
 * Adapts PowerHit to PowerMinionAttackHit so all HIT-compatible powers
 * automatically work with MINION_ATTACK_HIT.
 */
public class MinionAttackHit extends Trigger<MinionAttackHitEvent, PowerMinionAttackHit, Double, Optional<Double>> {

    public MinionAttackHit(String name, Class<MinionAttackHitEvent> eventClass, Class<PowerMinionAttackHit> powerClass, Class<Double> resultClass, Class<Optional> aggregatorClass) {
        super(name, eventClass, powerClass, resultClass, (Class<Optional<Double>>) (Class<?>) aggregatorClass);

        // Register adapter for PowerHit -> PowerMinionAttackHit
        // This allows all powers implementing PowerHit to automatically support MINION_ATTACK_HIT
        PowerManager.registerAdapter(PowerHit.class, PowerMinionAttackHit.class, powerHit ->
                new PowerMinionAttackHit() {
                    @Override
                    public PowerResult<Double> onMinionAttackHit(Player player, ItemStack itemStack, MinionAttackHitEvent event) {
                        Entity target = event.getTarget();
                        if (target instanceof LivingEntity) {
                            return powerHit.hit(player, itemStack, (LivingEntity) target,
                                    event.getDamage(), event.getOriginalEvent());
                        }
                        return PowerResult.ok(event.getDamage());
                    }

                    @Override
                    public Power getPower() {
                        return powerHit.getPower();
                    }
                });
        register(this);
    }

    @Override
    public PowerResult<Double> run(PowerMinionAttackHit power, Player player, ItemStack itemStack, MinionAttackHitEvent event) {
        return power.onMinionAttackHit(player, itemStack, event);
    }

    @Override
    public Optional<Double> def(Player player, ItemStack i, MinionAttackHitEvent event) {
        return Optional.of(event.getDamage());
    }

    @Override
    public Optional<Double> next(Optional<Double> a, PowerResult<Double> b) {
        if (!b.isOK()) {
            return a;
        }
        return Optional.of(b.data());
    }
}
