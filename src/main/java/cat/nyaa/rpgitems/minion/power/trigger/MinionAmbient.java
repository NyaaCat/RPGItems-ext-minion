package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionAmbientEvent;
import cat.nyaa.rpgitems.minion.events.MinionAttackEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionAmbient;
import cat.nyaa.rpgitems.minion.power.PowerMinionAttack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerLivingEntity;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

public class MinionAmbient extends Trigger<MinionAmbientEvent, PowerMinionAmbient, Void, Void> {

    public MinionAmbient(String name, Class<MinionAmbientEvent> entitySpawnEventClass, Class<PowerMinionAmbient> powerAttackClass, Class<Void> voidClass, Class<Void> voidClass2) {
        super(name, entitySpawnEventClass, powerAttackClass, voidClass, voidClass2);
        PowerManager.registerAdapter(PowerLivingEntity.class, PowerMinionAmbient.class, powerLivingEntity ->
                new PowerMinionAmbient() {
                    @Override
                    public PowerResult<Void> onMinionAmbient(Player player, ItemStack itemStack, MinionAmbientEvent event) {
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
    public PowerResult<Void> run(PowerMinionAmbient powerMinionAmbient, Player player, ItemStack i, MinionAmbientEvent event) {
        return powerMinionAmbient.onMinionAmbient(player, i, event);
    }
}
