package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionAttackHitEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

/**
 * Power interface for MINION_ATTACK_HIT trigger.
 * Called when a minion's attack hits an entity.
 * Returns PowerResult<Double> to allow damage modification.
 */
public interface PowerMinionAttackHit extends Pimpl {
    PowerResult<Double> onMinionAttackHit(Player player, ItemStack itemStack, MinionAttackHitEvent event);
}
