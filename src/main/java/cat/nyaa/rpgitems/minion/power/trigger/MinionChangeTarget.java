package cat.nyaa.rpgitems.minion.power.trigger;

import cat.nyaa.rpgitems.minion.events.MinionChangeTargetEvent;
import cat.nyaa.rpgitems.minion.power.PowerMinionChangeTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerLivingEntity;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.trigger.Trigger;

public class MinionChangeTarget extends Trigger<MinionChangeTargetEvent, PowerMinionChangeTarget, Void, Void> {
    public MinionChangeTarget(String name, Class<MinionChangeTargetEvent> minionChangeTargetEventClass, Class<PowerMinionChangeTarget> powerMinionChangeTargetClass, Class<Void> voidClass, Class<Void> voidClass2) {
        super(name, minionChangeTargetEventClass, powerMinionChangeTargetClass, voidClass, voidClass2);
        PowerManager.registerAdapter(PowerLivingEntity.class, PowerMinionChangeTarget.class, (powerLivingEntity) -> new PowerMinionChangeTarget() {
            @Override
            public PowerResult<Void> onMinionChangeTarget(Player player, ItemStack itemStack, MinionChangeTargetEvent event) {
                Entity source = event.getSource();
                if (source instanceof LivingEntity){
                    return powerLivingEntity.fire(player, itemStack, (LivingEntity) source, 0d);
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
    public PowerResult<Void> run(PowerMinionChangeTarget powerMinionChangeTarget, Player player, ItemStack i, MinionChangeTargetEvent event) {
        return powerMinionChangeTarget.onMinionChangeTarget(player, i, event);
    }
}
