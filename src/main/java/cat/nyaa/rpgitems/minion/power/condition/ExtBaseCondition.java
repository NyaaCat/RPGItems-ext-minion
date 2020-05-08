package cat.nyaa.rpgitems.minion.power.condition;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.cond.BaseCondition;

public abstract class ExtBaseCondition<T> extends BaseCondition<T> {
    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }
}
