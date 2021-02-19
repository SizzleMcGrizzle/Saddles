package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.Saddles;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SaddlesCreateSaddleCommand extends SubCommand {
    private Saddles plugin;
    
    public SaddlesCreateSaddleCommand(Saddles plugin) {
        super("saddles.admin", plugin, false);
        
        this.plugin = plugin;
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
        
        
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Saddles.SADDLE_KEY, PersistentDataType.STRING, "true");
        item.setItemMeta(meta);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully edited your saddle.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
