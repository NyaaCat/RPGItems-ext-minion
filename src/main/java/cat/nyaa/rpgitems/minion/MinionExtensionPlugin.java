package cat.nyaa.rpgitems.minion;

import cat.nyaa.rpgitems.minion.config.ConfigMain;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.power.trigger.BaseTrigger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.power.PowerManager;

public class MinionExtensionPlugin extends JavaPlugin {
    public static MinionExtensionPlugin plugin;
    MainEvents mainEvents;
    ConfigMain configMain;

    @Override
    public void onLoad() {
        super.onLoad();
        new BaseTrigger();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        mainEvents = new MainEvents();
        onReload();
        getServer().getPluginManager().registerEvents(mainEvents, this);
        PowerManager.registerPowers(this, "cat.nyaa.rpgitems.minion.power.impl");
        PowerManager.registerMarkers(this, "cat.nyaa.rpgitems.minion.power.markers");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinionManager.getInstance().clear();
    }

    public void onReload(){
        configMain = new ConfigMain();
        configMain.load();
    }
}
