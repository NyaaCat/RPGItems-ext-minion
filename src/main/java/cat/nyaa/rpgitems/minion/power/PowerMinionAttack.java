package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

public interface PowerMinionAttack extends Pimpl {
    PowerResult<Void> onMinionAttack(Player player, ItemStack itemStack, MinionAttackEvent event);
}
