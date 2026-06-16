package cat.nyaa.rpgitems.minion;

import cat.nyaa.rpgitems.minion.config.ConfigMain;
import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.minion.MinionManager;
import cat.nyaa.rpgitems.minion.minion.impl.IntervalModifierService;
import cat.nyaa.rpgitems.minion.power.trigger.BaseTrigger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import think.rpgitems.event.RPGItemsReloadEvent;
import think.rpgitems.power.PowerManager;

import javax.xml.crypto.Data;
import java.sql.SQLException;

public class MinionExtensionPlugin extends JavaPlugin implements Listener {
    public static MinionExtensionPlugin plugin;
    MainEvents mainEvents;
    ConfigMain configMain;
    I18n i18n;
    MinionCommand minionCommand;

    @Override
    public void onLoad() {
        super.onLoad();
        new BaseTrigger();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        onReload();
        mainEvents = new MainEvents();
        MinionManager.getInstance().init();
        IntervalModifierService.getInstance().start();
        minionCommand = new MinionCommand(this, i18n);
        Bukkit.getServer().getPluginCommand("rpgitem-minion").setExecutor(minionCommand);
        getServer().getPluginManager().registerEvents(mainEvents, this);
        getServer().getPluginManager().registerEvents(this, this);
        registerPowers();
        try {
            Database instance = Database.getInstance();
            instance.load();
            instance.init();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onRPGItemsReload(RPGItemsReloadEvent event) {
        registerPowers();
    }

    private void registerPowers() {
        PowerManager.registerPowers(this, "cat.nyaa.rpgitems.minion.power.impl");
        PowerManager.registerMarkers(this, "cat.nyaa.rpgitems.minion.power.marker");
        PowerManager.registerConditions(this, "cat.nyaa.rpgitems.minion.power.condition");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        MinionManager.getInstance().clear();
        IntervalModifierService.getInstance().stop();
    }

    public void onReload(){
        configMain = new ConfigMain();
        configMain.load();
        i18n = new I18n();
        i18n.setLanguage(configMain.language);
        i18n.load();
    }

    public ConfigMain config() {
        return configMain;
    }
}
