package org.minstrol.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ParseConverter;

public class CommandManager implements CommandExecutor {

    //This contains all the SgCommand instances to be called
    private SgCommand[] commands = new SgCommand[]{
            new SurvivalGamesCommand(),
            new SurvivalGamesAdminCommand()
    };

    /**
     * This will trigger the on command method once any command with a valid
     * registered command name is called, this will check compatability and
     * permission for each command before proceeding and calling the SgCommand
     *
     * @param commandSender The sender of the command, console or player
     * @param command The command being sent
     * @param s The raw input command
     * @param strings The raw arguments
     * @return false
     */
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (SgCommand sgCommand : commands){
            if (!sgCommand.getCommand().equalsIgnoreCase(command.getName())) continue;

            if (commandSender instanceof ConsoleCommandSender && (!sgCommand.isConsoleSupported())){
                commandSender.sendMessage(ChatColor.RED + "This command is not supported by the console!");
                continue;
            }

            if (!commandSender.hasPermission(sgCommand.getPermission())){
                commandSender.sendMessage(ParseConverter.StrTran(SurvivalGames.GetConfigManager().getConfig()
                                .getString("events.game.player-died"),
                        null));
                continue;
            }

            sgCommand.execute(commandSender, command, s, strings);
        }
        return false;
    }
}
