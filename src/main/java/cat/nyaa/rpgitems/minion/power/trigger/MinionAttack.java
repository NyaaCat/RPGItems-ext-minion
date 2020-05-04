package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionAttack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerLivingEntity;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

public class MinionAttack extends Trigger<MinionAttackEvent, PowerMinionAttack, Void, Void> {

    public MinionAttack(String name, Class<MinionAttackEvent> minionAttackEventClass, Class<PowerMinionAttack> powerMinionAttackClass, Class<Void> voidClass, Class<Void> voidClass2) {
        super(name, minionAttackEventClass, powerMinionAttackClass, voidClass, voidClass2);
        PowerManager.registerAdapter(PowerLivingEntity.class, PowerMinionAttack.class, powerLivingEntity ->
                new PowerMinionAttack() {
                    @Override
                    public PowerResult<Void> onMinionAttack(Player player, ItemStack itemStack, MinionAttackEvent event) {
                        Entity source = event.getSource();
                        if (source instanceof LivingEntity){
                            return powerLivingEntity.fire(player, itemStack, ((LivingEntity) source), 0d);
                        }
                        return PowerResult.ok();
                    }

                    @Override
                    public Power getPower() {
                        return powerLivingEntity.getPower();
                    }
                });
        register(this);
    }

    @Override
    public PowerResult<Void> run(PowerMinionAttack powerMinionAttack, Player player, ItemStack i, MinionAttackEvent event) {
        return powerMinionAttack.onMinionAttack(player, i, event);
    }
}
