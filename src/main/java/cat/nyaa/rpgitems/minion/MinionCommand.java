package cat.nyaa.rpgitems.minion;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.power.impl.Ceasefire;
import cat.nyaa.rpgitems.minion.power.impl.Sentry;
import cat.nyaa.rpgitems.minion.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.Power;
import think.rpgitems.power.PowerManager;
import think.rpgitems.power.trigger.Trigger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class MinionCommand extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public MinionCommand(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "max", permission = "rpgitem.minion", tabCompleter = "maxCompleter")
    public void onMax(CommandSender sender, Arguments arguments){
        Player player = arguments.nextPlayer();
        int max = arguments.nextInt();
        PlayerData playerData = Database.getInstance().getPlayerData(player);
        playerData.slotMax = max;
        Database.getInstance().setPlayerData(playerData);
        msg(sender, I18n.format("max_change", player.getName(), max));
    }

    public List<String> maxCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                completeStr.addAll(Bukkit.getOnlinePlayers().stream().map(player -> player.getName()).collect(Collectors.toList()));
                break;
        }
        return Utils.filtered(arguments, completeStr);
    }

    public List<String> sampleCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        switch (arguments.remains()) {
            case 1:
                break;
        }
        return Utils.filtered(arguments, completeStr);
    }

    @SubCommand(value = "addceasefire", permission = "rpgitem.minion.admin", tabCompleter = "addCeasefireCompleter")
    public void onAddCeasefire(CommandSender sender, Arguments arguments) {
        String trigger = arguments.remains() > 0 ? arguments.nextString() : "SNEAK";
        int count = 0;

        NamespacedKey ceasefireKey = new NamespacedKey(MinionExtensionPlugin.plugin, "ceasefire");

        for (RPGItem item : ItemManager.items()) {
            // Only process items that have Sentry power
            boolean hasSentry = item.getPowers().stream()
                    .anyMatch(p -> p instanceof Sentry);
            if (!hasSentry) {
                continue;
            }

            // Remove existing Ceasefire power if present
            List<Power> existingCeasefire = item.getPowers().stream()
                    .filter(p -> p instanceof Ceasefire)
                    .collect(Collectors.toList());
            for (Power p : existingCeasefire) {
                item.removePower(p);
            }

            // Instantiate and configure new Ceasefire power
            Ceasefire ceasefire = PowerManager.instantiate(Ceasefire.class);
            ceasefire.setItem(item);
            ceasefire.init(new YamlConfiguration());

            // Set the trigger
            Trigger triggerObj = Trigger.valueOf(trigger);
            ceasefire.triggers = new HashSet<>();
            ceasefire.triggers.add(triggerObj);

            // Ensure showCooldownWarning is false
            ceasefire.showCooldownWarning(false);

            // Add power to item
            item.addPower(ceasefireKey, ceasefire);

            // Save item (refreshItem will be called once after the loop)
            ItemManager.save(item);

            count++;
        }

        // Refresh all items to apply changes
        ItemManager.refreshItem();

        msg(sender, "Added/updated ceasefire power on " + count + " items with Sentry power");
    }

    public List<String> addCeasefireCompleter(CommandSender sender, Arguments arguments) {
        List<String> completeStr = new ArrayList<>();
        if (arguments.remains() == 1) {
            completeStr.addAll(Arrays.asList("SNEAK", "RIGHT_CLICK", "LEFT_CLICK", "SPRINT"));
        }
        return Utils.filtered(arguments, completeStr);
    }

    @Override
    public String getHelpPrefix() {
        return null;
    }
}
