package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.Config;
import me.sizzlemcgrizzle.saddles.Saddles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SaddlesReloadCommand extends SubCommand {
    private Saddles plugin;
    
    public SaddlesReloadCommand(Saddles plugin) {
        super("saddles.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.WARNING, "You do not have accesss to this command.");
            return null;
        }
        
        Config.load(plugin);
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Configuration reloaded.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
