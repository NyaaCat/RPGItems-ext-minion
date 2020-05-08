package cat.nyaa.rpgitems.minion.power.condition;

import cat.nyaa.rpgitems.minion.minion.MinionManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.power.Meta;
import think.rpgitems.power.PowerResult;
import think.rpgitems.power.Property;
import think.rpgitems.power.PropertyHolder;

import java.util.Map;

@Meta(marker = true)
public class MinionMaxCondition extends ExtBaseCondition<Void> {
    @Property
    int max = 1;
    @Property
    String id;
    @Property
    boolean isStatic;
    @Property
    boolean critical;

    public int getMax() {
        return max;
    }

    public String getId() {
        return id;
    }


    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public boolean isCritical() {
        return critical;
    }

    @Override
    public PowerResult<Void> check(Player player, ItemStack stack, Map<PropertyHolder, PowerResult<?>> context) {
        int minionMax = MinionManager.getInstance().getMinionMax(player);
        if (minionMax < getMax()){
            return PowerResult.ok();
        }else {
            return PowerResult.fail();
        }
    }

    @Override
    public String getName() {
        return "minionmax";
    }
}
