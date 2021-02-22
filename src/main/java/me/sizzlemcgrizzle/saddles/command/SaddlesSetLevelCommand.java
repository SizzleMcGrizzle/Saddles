package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.AbstractHorseMount;
import me.sizzlemcgrizzle.saddles.Saddles;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class SaddlesSetLevelCommand extends SubCommand {
    private Saddles plugin;
    
    public SaddlesSetLevelCommand(Saddles plugin) {
        super("saddles.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], new String[]{"SPEED", "JUMP"});
        if (args.length == 3)
            return Utils.getMatches(args[2], new String[]{"1", "2", "3", "4", "5"});
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
        
        if (!plugin.hasSaddleUUID(item.getItemMeta().getPersistentDataContainer())) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must hold a saddle that already has a mount assigned.");
            return null;
        }
        
        AbstractHorseMount data = plugin.getMount(item, null);
        int index;
        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a valid level.");
            return null;
        }
        
        if (index < 1 || index > 5) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a number between 1 and 5");
            return null;
        }
        
        if (args[1].equalsIgnoreCase("SPEED"))
            data.setSpeedLevel(index);
        else if (args[1].equalsIgnoreCase("JUMP"))
            data.setJumpLevel(index);
        else {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "You must enter a category of level to change.");
            return null;
        }
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Changed " + args[1].toLowerCase() + " level of mount to " + index + ".");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
