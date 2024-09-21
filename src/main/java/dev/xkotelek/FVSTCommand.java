package dev.xkotelek;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FVSTCommand implements CommandExecutor {

    // FVST Minecraft Plugin 1.0
    // by xKotelek @ https://kotelek.dev
    // https://github.com/fvstpl/minecraft-plugin/

    private final FVST plugin;

    public FVSTCommand(FVST plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§0§l[FVST] §rv1.0");
            sender.sendMessage("§dby xKotelek @ https://kotelek.dev");
            sender.sendMessage("");
            sender.sendMessage("§rAvailable commands:");
            sender.sendMessage("§f/fvst reload §0- §fReloads config.");
            sender.sendMessage("§f/fvst help §0- §fShows help.");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("fvst.reload")) {
                plugin.reloadConfig();
                sender.sendMessage("§0§l[FVST] §rThe config has been reloaded.");
            } else {
                sender.sendMessage("§0§l[FVST] §r§cYou don't have permissions to do that!");
            }
            return true;
        }

        sender.sendMessage("§0§l[FVST] §rUnknown command. Use /fvst help for available commands.");
        return true;
    }
}
