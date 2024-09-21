package dev.xkotelek;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class FVSTTabCompleter implements TabCompleter {

    // FVST Minecraft Plugin 1.0
    // by xKotelek @ https://kotelek.dev
    // https://github.com/fvstpl/minecraft-plugin/

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
            if ("help".startsWith(args[0].toLowerCase())) {
                suggestions.add("help");
            }
        }

        return suggestions;
    }
}
