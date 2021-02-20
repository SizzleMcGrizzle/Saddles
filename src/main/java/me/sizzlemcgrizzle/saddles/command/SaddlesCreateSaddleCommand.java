package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.Config;
import me.sizzlemcgrizzle.saddles.Saddles;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.List;

public class SaddlesCreateSaddleCommand extends SubCommand {
    private Saddles plugin;
    
    public SaddlesCreateSaddleCommand(Saddles plugin) {
        super("saddles.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("1-" + Config.REQUIRED_TICKS.length);
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
        
        if (plugin.hasSaddleUUID(item.getItemMeta().getPersistentDataContainer())) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "This saddle already has a mount!");
            return null;
        }
        
        int index = 0;
        if (args.length > 1) {
            try {
                index = Integer.parseInt(args[1]) - 1;
            } catch (NumberFormatException e) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid level.");
                return null;
            }
            
            if (index < 1 || index > Config.REQUIRED_TICKS.length) {
                MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a number between 1 and " + Config.REQUIRED_TICKS.length);
                return null;
            }
        }
        
        
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Saddles.SADDLE_KEY, PersistentDataType.INTEGER, index);
        item.setItemMeta(meta);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully edited your saddle.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
