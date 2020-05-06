package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.item.RPGItem;

import java.util.Optional;

public interface IMinion extends Targetable, EntityHolderNonpersistent {
    OfflinePlayer getOwner();
    void setOwner(OfflinePlayer player);
    int getSlotCost();
    ItemStack getItemStack();
    Optional<RPGItem> getRPGItem();

    void ambientAction();
    MinionStatus getStatus();

    void tick(int minionTick);

}
