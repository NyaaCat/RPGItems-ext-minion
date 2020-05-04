package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionAmbientEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

public interface PowerMinionAmbient extends Pimpl {
    PowerResult<Void> onMinionAmbient(Player player, ItemStack itemStack, MinionAmbientEvent event);
}
