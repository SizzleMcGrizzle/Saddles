package me.sizzlemcgrizzle.saddles.command;

import de.craftlancer.core.command.CommandHandler;
import me.sizzlemcgrizzle.saddles.Saddles;

public class SaddlesCommandHandler extends CommandHandler {
    public SaddlesCommandHandler(Saddles plugin) {
        super(plugin);
        
        registerSubCommand("createSaddle", new SaddlesCreateSaddleCommand(plugin));
        registerSubCommand("setLevel", new SaddlesSetLevelCommand(plugin));
        registerSubCommand("reload", new SaddlesReloadCommand(plugin));
    }
}
