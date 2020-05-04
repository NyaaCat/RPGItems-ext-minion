package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionAmbientEvent;
import cat.nyaa.rpgitems.minion.events.MinionMoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

public interface PowerMinionMove extends Pimpl {
    PowerResult<Void> onMinionMove(Player player, ItemStack itemStack, MinionMoveEvent event);
}
