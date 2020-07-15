package org.minstrol.survivalgames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class SgCommand{

    public abstract String getCommand();

    public abstract String getPermission();

    public abstract boolean isConsoleSupported();

    public abstract void execute(CommandSender sender, Command command, String s, String[] args);
}
