package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionSpawnEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionSpawn;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerLocation;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

public class MinionSpawn extends Trigger<MinionSpawnEvent, PowerMinionSpawn, Void, Void> {

    public MinionSpawn(String name, Class<MinionSpawnEvent> minionSpawnEventClass, Class<PowerMinionSpawn> powerMinionSpawnClass, Class<Void> voidClass, Class<Void> voidClass2) {
        super(name, minionSpawnEventClass, powerMinionSpawnClass, voidClass, voidClass2);
        PowerManager.registerAdapter(PowerLocation.class, PowerMinionSpawn.class, (powerLocation) ->
                new PowerMinionSpawn() {
                    @Override
                    public PowerResult<Void> onMinionSpawn(Player player, ItemStack itemStack, MinionSpawnEvent event) {
                        return powerLocation.fire(player, itemStack, event.getSpawnLocation());
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
    public PowerResult<Void> run(PowerMinionSpawn powerMinionSpawn, Player player, ItemStack i, MinionSpawnEvent event) {
        return powerMinionSpawn.onMinionSpawn(player, i, event);
    }
}
