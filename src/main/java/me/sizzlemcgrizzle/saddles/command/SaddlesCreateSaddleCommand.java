package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.Saddles;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SaddlesCreateSaddleCommand extends SubCommand {
    private Saddles plugin;
    
    public SaddlesCreateSaddleCommand(Saddles plugin) {
        super("saddles.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], new String[]{"DONKEY", "HORSE", "SKELETON_HORSE", "ZOMBIE_HORSE", "LLAMA"});
        if (args.length == 3)
            return Utils.getMatches(args[2], new String[]{"RANDOM", "1", "2", "3", "4", "5"});
        if (args.length == 4)
            return Utils.getMatches(args[3], new String[]{"RANDOM", "1", "2", "3", "4", "5"});
        if (!args[1].equalsIgnoreCase("HORSE"))
            return Collections.emptyList();
        if (args.length == 5) {
            List<String> l = Arrays.stream(Horse.Color.values()).map(Horse.Color::name).collect(Collectors.toList());
            l.add("RANDOM");
            return Utils.getMatches(args[4], l);
        }
        if (args.length == 6)
            return Utils.getMatches(args[5], Arrays.stream(Material.values()).filter(m -> m == Material.AIR || m.name().contains("_HORSE_ARMOR")).map(Material::name).collect(Collectors.toList()));
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.WARNING, "You do not have accesss to this command.");
            return null;
        }
        
        Player player = (Player) sender;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.SADDLE) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must hold a saddle.");
            return null;
        }
        
        if (plugin.hasSaddleKey(item.getItemMeta().getPersistentDataContainer())) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "This saddle is already created!");
            return null;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a mount type.");
            return null;
        }
        
        String t = args[1].toUpperCase();
        
        if (!t.equals("HORSE") && !t.equals("DONKEY") && !t.equals("SKELETON_HORSE") && !t.equals("ZOMBIE_HORSE") && !t.equals("LLAMA")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid mount type.");
            return null;
        }
        
        EntityType entityType = EntityType.valueOf(t);
        
        if (entityType == EntityType.DONKEY && args.length < 4) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a speed and jump level, or RANDOM for random.");
            return null;
        }
        
        if (entityType == EntityType.HORSE && args.length < 6) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a type, speed level, jump level, color, and armor.");
            return null;
        }
        
        int speedLevel;
        if (args[2].equalsIgnoreCase("RANDOM"))
            speedLevel = -1;
        else
            try {
                speedLevel = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid speed level.");
                return null;
            }
        
        if (speedLevel != -1 && speedLevel < 1 || speedLevel > 5) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a number between 1 and 5");
            return null;
        }
        
        int jumpLevel;
        if (args[3].equalsIgnoreCase("RANDOM"))
            jumpLevel = -1;
        else
            try {
                jumpLevel = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid jump level.");
                return null;
            }
        
        if (jumpLevel != -1 && jumpLevel < 1 || jumpLevel > 5) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a number between 1 and 5");
            return null;
        }
        
        if (entityType == EntityType.HORSE) {
            if (Arrays.stream(Horse.Color.values()).noneMatch(c -> args[4].equalsIgnoreCase("RANDOM") || c.name().equals(args[4].toUpperCase()))) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid horse color.");
                return null;
            }
            if (Arrays.stream(Material.values()).noneMatch(m -> m.name().equals(args[5].toUpperCase()))
                    || (Material.valueOf(args[5].toUpperCase()) != Material.AIR && !args[5].toUpperCase().contains("_HORSE_ARMOR"))) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid material");
                return null;
            }
        }
        
        
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Saddles.SADDLE_TYPE_KEY, PersistentDataType.STRING, t);
        meta.getPersistentDataContainer().set(Saddles.SADDLE_SPEED_LEVEL_KEY, PersistentDataType.INTEGER, speedLevel);
        meta.getPersistentDataContainer().set(Saddles.SADDLE_JUMP_LEVEL_KEY, PersistentDataType.INTEGER, jumpLevel);
        if (entityType == EntityType.HORSE) {
            if (!args[4].equalsIgnoreCase("RANDOM"))
                meta.getPersistentDataContainer().set(Saddles.SADDLE_COLOR_KEY, PersistentDataType.STRING, args[4].toUpperCase());
            meta.getPersistentDataContainer().set(Saddles.SADDLE_ARMOR_KEY, PersistentDataType.STRING, args[5].toUpperCase());
        }
        item.setItemMeta(meta);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully created mount.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
