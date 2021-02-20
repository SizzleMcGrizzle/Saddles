package me.sizzlemcgrizzle.saddles;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Config {
    
    public static String PREFIX;
    public static long[] REQUIRED_TICKS;
    public static long MOUNT_SPAWN_DELAY;
    
    public static void load(Saddles plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        
        if (!file.exists())
            plugin.saveResource(file.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        PREFIX = ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "§8[§9Saddles§8]"));
        MOUNT_SPAWN_DELAY = config.getLong("mountSpawnDelay", 30);
        
        long requiredTickMultiplier = config.getLong("requiredTickMultiplier", 2 * 40 * 60);
        
        REQUIRED_TICKS = new long[config.getInt("numberLevels", 30)];
        
        for (int i = 0; i < REQUIRED_TICKS.length; i++) {
            long ticks = i * requiredTickMultiplier;
            if (i != 0)
                ticks += REQUIRED_TICKS[i - 1];
            REQUIRED_TICKS[i] = ticks;
        }
    }
}
