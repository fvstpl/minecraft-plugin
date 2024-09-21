package dev.xkotelek;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public final class FVST extends JavaPlugin {

    // FVST Minecraft Plugin 1.0
    // by xKotelek @ https://kotelek.dev
    // https://github.com/fvstpl/minecraft-plugin/

    @Override
    public void onEnable() {
        saveDefaultConfig();
        checkAndFixConfig();

        getCommand("fvst").setExecutor(new FVSTCommand(this));
        getCommand("fvst").setTabCompleter(new FVSTTabCompleter());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, PurchaseManager::checkForPurchases, 0, 100);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void checkAndFixConfig() {
        FileConfiguration config = getConfig();
        boolean modified = false;

        if (!config.isSet("debug") || !config.get("debug").getClass().equals(Boolean.class)) {
            config.set("debug", false);
            modified = true;
        }

        if (!config.isSet("shopId") || !(config.get("shopId") instanceof String)) {
            config.set("shopId", "");
            modified = true;
        }

        if (!config.isSet("serverId") || !(config.get("serverId") instanceof String)) {
            config.set("serverId", "");
            modified = true;
        }

        if (!config.isSet("apiKey") || !(config.get("apiKey") instanceof String)) {
            config.set("apiKey", "");
            modified = true;
        }

        if (!config.isSet("boughtMessage") || !(config.get("boughtMessage") instanceof Iterable<?>)) {
            config.set("boughtMessage", Arrays.asList(
                    "&lGRACZ &0&l%player%&r&l ZAKUPIŁ",
                    "",
                    "  &0&l%item%",
                    ""
            ));
            modified = true;
        }

        if (modified) {
            System.out.println("[FVST] Modifying config with default values...");
            saveConfig();
            reloadConfig();
            System.out.println("[FVST] Config updated. Reloading and verifying values...");

            StringBuilder configBuilder = new StringBuilder();
            configBuilder.append("# FVST Configuration @ https://github.com/fvstpl/minecraft-plugin/\n# by xKotelek        @ https://kotelek.dev\n\n");
            configBuilder.append("debug: ").append(config.getBoolean("debug")).append("\n");
            configBuilder.append("shopId: '").append(config.getString("shopId")).append("'\n");
            configBuilder.append("serverId: '").append(config.getString("serverId")).append("'\n");
            configBuilder.append("apiKey: '").append(config.getString("apiKey")).append("'\n\n");
            configBuilder.append("# Use %item% for item name and %player% for player name.\n");
            configBuilder.append("boughtMessage:\n");

            for (String message : config.getStringList("boughtMessage")) {
                configBuilder.append("  - '").append(message).append("'\n");
            }

            try {
                File configFile = new File(getDataFolder(), "config.yml");
                FileWriter writer = new FileWriter(configFile);
                writer.write(configBuilder.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[FVST] Config is already up-to-date.");
        }
    }
}