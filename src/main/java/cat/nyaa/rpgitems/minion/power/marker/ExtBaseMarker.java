package cat.nyaa.rpgitems.minion.power.marker;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;

public abstract class ExtBaseMarker extends ConditionedMarker<Integer> {
    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }
}
