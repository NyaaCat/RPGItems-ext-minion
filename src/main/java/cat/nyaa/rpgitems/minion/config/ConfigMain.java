package cat.nyaa.rpgitems.minion.config;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.rpgitems.minion.MinionExtensionPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigMain extends PluginConfigure {

    @Serializable(name = "slot.max.default")
    public int defaultSlotMax = 1;
    @Serializable(name = "interval.tick.minion")
    public int minionTickInterval = 20;
    @Serializable(name = "interval.tick.player")
    public int playerTickInterval = 20;


    @Override
    protected JavaPlugin getPlugin() {
        return MinionExtensionPlugin.plugin;
    }
}
