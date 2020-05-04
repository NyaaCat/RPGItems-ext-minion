package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionMoveEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionMove;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerLocation;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

public class MinionMove extends Trigger<MinionMoveEvent, PowerMinionMove, Void, Void> {
    public MinionMove(String name, Class<MinionMoveEvent> minionMoveEventClass, Class<PowerMinionMove> powerMinionMoveClass, Class<Void> voidClass, Class<Void> voidClass2) {
        super(name, minionMoveEventClass, powerMinionMoveClass, voidClass, voidClass2);
        PowerManager.registerAdapter(PowerLocation.class, PowerMinionMove.class, (powerLocation) ->
                new PowerMinionMove() {
                    @Override
                    public PowerResult<Void> onMinionMove(Player player, ItemStack itemStack, MinionMoveEvent event) {
                        return powerLocation.fire(player, itemStack, event.getOldLocation());
                    }

                    @Override
                    public Power getPower() {
                        return powerLocation.getPower();
                    }
                }
        );
        register(this);
    }

    @Override
    public PowerResult<Void> run(PowerMinionMove powerMinionMove, Player player, ItemStack i, MinionMoveEvent event) {
        return powerMinionMove.onMinionMove(player, i, event);
    }
}
