package cat.nyaa.rpgitems.minion.minion;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
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
    void setStatus(MinionStatus status);

    boolean isValidTarget(Entity target);
    void tick(int minionTick);
    void remove();
    boolean isRemoved();
}
