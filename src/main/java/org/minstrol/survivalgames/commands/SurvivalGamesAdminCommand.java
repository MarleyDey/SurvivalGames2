package org.minstrol.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SurvivalGamesAdminCommand extends SgCommand {

    private boolean settingLobby

    public String getCommand() {
        return "sgadmin";
    }

    public String getPermission() {
        return "survivalgames.admincommand";
    }

    public boolean isConsoleSupported() {
        return true;
    }

    public void execute(CommandSender sender, Command command, String s, String[] args) {

        //No arguments, suggest help command
        if (args.length == 0){
            sender.sendMessage(ChatColor.YELLOW + "Please use '/sgadmin help' to see all admin commands");
            return;
        }

        //Help sub-command
        if (args[0].equalsIgnoreCase("help")){
            sendHelpMessage(sender);
            return;
        }

        //Setup sub-command
        if (args[0].equalsIgnoreCase("setup")){

        }
    }

    private void setupGame(Player player, String name, int minPlayers, int maxPlayers){
        player.sendMessage("You are setting up game: " + name);
        player.sendMessage("1) Set up lobby spawn point - type '/sgadmin setup' to set lobby spawn location");

    }



    private void sendHelpMessage(CommandSender sender){
        sender.sendMessage(ChatColor.BLUE +         "-----------" + ChatColor.YELLOW + " SG Admin Help " + ChatColor.BLUE + "----------");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin setup [name] [min-players] [max-players]");
        sender.sendMessage(ChatColor.GRAY +         "    - Start the SG map setup process");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin delete [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Remove a SG map");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin modify [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Start the SG map modification process");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin forcestop [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Force stop the named game");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin forcestart [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Force start the named game");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin forcerestart [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Force restart the named game");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin forcerestock [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Force restock all the chests in named game");
        sender.sendMessage(ChatColor.BLUE +         "-------------------------------------------");
    }
}
