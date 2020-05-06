package cat.nyaa.rpgitems.minion;

import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.power.trigger.BaseTrigger;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MainEvents implements Listener {

    @EventHandler
    public void onMinionAttack(MinionAttackEvent event){
        OfflinePlayer player = event.getPlayer();
        if (!player.isOnline()) return;
        event.getRPGItem().ifPresent(rpgitem -> {
            rpgitem.power(player.getPlayer(), event.getItemStack(), event, BaseTrigger.MINION_ATTACK);
        });
    }
}
