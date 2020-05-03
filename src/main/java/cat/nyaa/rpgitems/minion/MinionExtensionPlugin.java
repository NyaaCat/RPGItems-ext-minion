package cat.nyaa.rpgitems.minion;

import org.bukkit.plugin.java.JavaPlugin;
import think.rpgitems.power.PowerManager;

public class MinionExtensionPlugin extends JavaPlugin {
    public static MinionExtensionPlugin plugin;
    MainEvents mainEvents;

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        mainEvents = new MainEvents();
        getServer().getPluginManager().registerEvents(mainEvents, this);
        PowerManager.registerPowers(this, "cat.nyaa.rpgitems.minion.power");
    }
}
