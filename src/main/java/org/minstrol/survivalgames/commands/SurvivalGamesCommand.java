package org.minstrol.survivalgames.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SurvivalGamesCommand extends SgCommand {

    public String getCommand() {
        return "sg";
    }

    public String getPermission() {
        return "survivalgames.sgcommannd";
    }

    public boolean isConsoleSupported() {
        return true;
    }

    public void execute(CommandSender sender, Command command, String s, String[] args) {

    }
}
