package cat.nyaa.rpgitems.minion.power.impl;

import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.NamespacedKey;
import think.rpgitems.power.*;
import think.rpgitems.power.trigger.Trigger;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class containing common methods and fields.
 */
public abstract class BasePower extends BasePropertyHolder implements Serializable, Power {
    @Property
    int cooldown = 0;
    @Property
    int cost = 0;
    @Property
    public String displayName;
    @Property
    @AcceptedValue(preset = Preset.TRIGGERS)
    public Set<Trigger> triggers = Power.getDefaultTriggers(this.getClass());
    @Property
    public Set<String> selectors = new HashSet<>();
    @Property
    public Set<String> conditions = new HashSet<>();
    @Property
    public String requiredContext;

    public int getCooldown() {
        return cooldown;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public NamespacedKey getNamespacedKey() {
        return new NamespacedKey(MinionExtensionPlugin.plugin, getName());
    }

    @Override
    public Set<Trigger> getTriggers() {
        return Collections.unmodifiableSet(triggers);
    }

    @Override
    public Set<String> getSelectors() {
        return Collections.unmodifiableSet(selectors);
    }

    @Override
    public Set<String> getConditions() {
        return Collections.unmodifiableSet(conditions);
    }

    @Override
    public String requiredContext() {
        return requiredContext;
    }

    @Override
    public final String getPropertyHolderType() {
        return "power";
    }
}