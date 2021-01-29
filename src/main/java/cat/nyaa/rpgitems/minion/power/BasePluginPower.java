package cat.nyaa.rpgitems.minion.power;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.BasePower;

public abstract class BasePluginPower extends BasePower {
    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }
}
