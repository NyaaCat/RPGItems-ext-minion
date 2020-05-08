package cat.nyaa.rpgitems.minion;

import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import cat.nyaa.rpgitems.minion.database.Database;
import cat.nyaa.rpgitems.minion.database.PlayerData;
import cat.nyaa.rpgitems.minion.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
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

    @SubCommand(value = "max", tabCompleter = "maxCompleter")
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

    @Override
    public String getHelpPrefix() {
        return null;
    }
}
