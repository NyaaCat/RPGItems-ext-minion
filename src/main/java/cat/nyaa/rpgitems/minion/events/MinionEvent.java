package cat.nyaa.rpgitems.minion.events;

import cat.nyaa.rpgitems.minion.minion.IMinion;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

import java.util.Optional;

public abstract class MinionEvent extends Event {
    private final IMinion minion;
    private boolean canceled = false;

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    MinionEvent(IMinion minion){
        this.minion = minion;
    }

    public IMinion getMinion() {
        return minion;
    }

    public ItemStack getItemStack(){
        return minion.getItemStack();
    }

    public Optional<RPGItem> getRPGItem() {
        return ItemManager.toRPGItem(minion.getItemStack());
    }

    public OfflinePlayer getPlayer() {
        return minion.getOwner();
    }
}
