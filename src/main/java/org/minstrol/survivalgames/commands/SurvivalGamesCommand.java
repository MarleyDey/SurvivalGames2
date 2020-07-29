package org.minstrol.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameManager;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.HashMap;

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

        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();

        //No arguments, suggest help command
        if (args.length == 0){
            sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.zero-arguments"), null));
            return;
        }

        if (args[0].equalsIgnoreCase("help")){
            displayHelp(sender);
            return;
        }

        PlayerManager playerManager = SurvivalGames.GetPlayerManager();

        //The join sub-command
        if (args[0].equalsIgnoreCase("join")){
            if (sender instanceof ConsoleCommandSender){
                sender.sendMessage(ChatColor.RED + "The join sub-command is not supported by the console!");
                return;
            }

            //Not enough arguments
            if (args.length < 2){
                sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.specify-game"), null));
                return;
            }

            Player player = (Player) sender;
            SgPlayer sgPlayer = playerManager.getSgPlayer(player);

            if (sgPlayer != null){

                if (sgPlayer.getActiveGame() == null)return;
                sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.already-in-game"), null));
                return;
            }

            Game game = SurvivalGames.GetGameManager().getGame(args[1]);
            if (game == null){
                sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.game-does-not-exist"),
                        new HashMap<String, String>(){{ put("%game%", args[0]); }}));
                return;
            }

            //Attempt to join the game through the command
            game.playerJoin((Player) sender);
            return;
        }

        //The leave sub-command
        if (args[0].equalsIgnoreCase("leave") ||
                args[0].equalsIgnoreCase("quit")){
            if (sender instanceof ConsoleCommandSender){
                sender.sendMessage(ChatColor.RED + "The leave sub-command is not supported by the console!");
                return;
            }

            Player player = (Player) sender;
            SgPlayer sgPlayer = playerManager.getSgPlayer(player);

            if (sgPlayer == null){
                sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.not-in-game"), null));
                return;
            }

            Game game = sgPlayer.getActiveGame();

            if (game == null){
                sender.sendMessage(ParseConverter.StrTran(config.getString("commands.sg.not-in-game"), null));
                return;
            }

            //Attempt to leave the game through the command
            game.playerLeave(player);
            return;
        }
    }

    private void displayHelp(CommandSender sender){
        sender.sendMessage(ChatColor.BLUE +         "-----------" + ChatColor.YELLOW + " SG Help " + ChatColor.BLUE + "----------");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sg join [name]");
        sender.sendMessage(ChatColor.GRAY +         "    - Join [name] game");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sg leave/quit");
        sender.sendMessage(ChatColor.GRAY +         "    -  Leave current game");
        sender.sendMessage(ChatColor.BLUE +         "-------------------------------------------");
    }
}
