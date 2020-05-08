package cat.nyaa.rpgitems.minion.power.marker;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.marker.BaseMarker;

public abstract class ExtBaseMarker extends BaseMarker {
    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }
}
