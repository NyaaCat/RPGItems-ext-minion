package cat.nyaa.rpgitems.minion.power.marker;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.Property;
import think.rpgitems.power.marker.BaseMarker;

import java.util.HashSet;
import java.util.Set;

public abstract class ConditionedMarker<T> extends BaseMarker {
    @Property
    Set<String> conditions = new HashSet<>();

    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }

    public Set<String> getConditions() {
        return conditions;
    }

    public abstract T getValue();
}
