package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.events.MinionSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Pimpl;
import think.rpgitems.power.PowerResult;

public interface PowerMinionSpawn extends Pimpl {
    PowerResult<Void> onMinionSpawn(Player player, ItemStack itemStack, MinionSpawnEvent event);
}
