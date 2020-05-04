package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionChangeTargetEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

public interface PowerMinionChangeTarget extends Pimpl {
    PowerResult<Void> onMinionChangeTarget(Player player, ItemStack itemStack, MinionChangeTargetEvent event);
}
