package org.minstrol.survivalgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameManager;
import org.minstrol.survivalgames.game.util.GameLoader;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

public class SurvivalGamesAdminCommand extends SgCommand {

    private boolean
            settingGame = true,
            settingLobby = false,
            settingPlayerSpawns = false,
            settingBoundsCorner1 = false,
            settingBoundsCorner2 = false,
            settingDetectionChests = false;

    private String settingUpGameName = "";

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

        //Lobby sub-command
        if (args[0].equalsIgnoreCase("lobby")){
            if (args.length == 1){
                sendLobbyHelpMessage(sender);
                return;
            }

            if (args[1].equalsIgnoreCase("setspawn")){
                if (sender instanceof ConsoleCommandSender){
                    sender.sendMessage(ChatColor.RED + "The set spawn lobby sub-command is not supported by the console!");
                    return;
                }
                Player player = (Player) sender;

                SurvivalGames.GetLobby().setSpawnLocation(player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "You have set the lobby spawn point!");
                return;
            }
        }

        //Remove sub-command
        if (args[0].equalsIgnoreCase("remove")){
            if (args.length == 1){
                sender.sendMessage(ChatColor.RED + "Please specify the game you would like to remove!");
                return;
            }

            GameManager gameManager = SurvivalGames.GetGameManager();
            Game game = gameManager.getGame(args[1]);

            if (game == null){
                sender.sendMessage(ChatColor.RED + "The game " + args[1] + " does not exist!");
                return;
            }

            gameManager.removeGame(game);
        }

        //Setup sub-command
        if (args[0].equalsIgnoreCase("setup")){

            if (sender instanceof ConsoleCommandSender){
                sender.sendMessage(ChatColor.RED + "The setup sub-command is not supported by the console!");
                return;
            }

            String gamePath = "games.maps." + settingUpGameName.toUpperCase() + ".";

            ConfigManager configManager = SurvivalGames.GetConfigManager();
            FileConfiguration gamesConfig = configManager.getGameConfig();

            if (settingGame){
                if (args.length == 1){
                    sender.sendMessage(ChatColor.RED + "To setup a game please use: " + ChatColor.LIGHT_PURPLE + "\n- /sgadmin setup [name] [min-players] [max-players]");
                    return;
                }

                if (args.length < 4){
                    sender.sendMessage(ChatColor.RED + "Invalid amount of arguments, please specify the games name, minimum players and maximum players!");
                    return;
                }

                //Check name argument
                if (SurvivalGames.GetGameManager().getGame(args[1]) != null){
                    sender.sendMessage(ChatColor.RED + "This game already exists!");
                    return;
                }

                //Check min-players argument
                if (!validInteger(args[2])){
                    sender.sendMessage(ChatColor.RED + args[2] + " is not a valid whole number!");
                    return;
                }

                //Check max-players argument
                if (!validInteger(args[3])){
                    sender.sendMessage(ChatColor.RED + args[3] + " is not a valid whole number!");
                    return;
                }

                int minPlayers = Integer.parseInt(args[2]);
                int maxPlayers = Integer.parseInt(args[3]);

                if (minPlayers > maxPlayers){
                    sender.sendMessage(ChatColor.RED + "The minimum players required to start the game cannot be " +
                            "larger than the maximum amount of players to start the game!");
                    return;
                }

                settingUpGameName = args[1].toUpperCase();

                gamePath = "games.maps." + settingUpGameName.toUpperCase() + ".";

                gamesConfig.set(gamePath + "options.max-players", maxPlayers);
                gamesConfig.set(gamePath + "options.min-players", minPlayers);

                configManager.saveGameConfig();

                settingGame = false;
                settingBoundsCorner1 = true;

                sender.sendMessage(ChatColor.GREEN + "You have created the game " + settingUpGameName + "!\n" +
                        ChatColor.AQUA + "Next you need to set the maps boundaries!\n" +
                        ChatColor.GRAY + "To set the boundaries of the map you will need to set\n" +
                                         "the location of the two furthest corners! To do this go\n " +
                                         "to one edge of the map cube and\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to set corner 1's location");
                return;
            }

            //Setting the 1st corner of map
            if (settingBoundsCorner1){
                //Check name argument
                if (!checkGameExists(gamesConfig, settingUpGameName)){
                    sender.sendMessage(ChatColor.RED + "The game " + settingUpGameName + " does not exist!");
                    return;
                }

                Player player = (Player) sender;
                Location location = player.getLocation();

                gamesConfig.set(gamePath + "dimensions.x1", location.getBlockX());
                gamesConfig.set(gamePath + "dimensions.y1", location.getBlockY());
                gamesConfig.set(gamePath + "dimensions.z1", location.getBlockZ());

                configManager.saveGameConfig();

                settingBoundsCorner1 = false;
                settingBoundsCorner2 = true;

                sender.sendMessage(ChatColor.GREEN + " \nYou have set corner 1 location!\n" +
                        ChatColor.AQUA + "Next you need to set corner 2 location!\n" +
                        ChatColor.GRAY + "To set the boundaries of the map you will need to set\n" +
                        "the location of the two furthest corners! To do this go\n " +
                        "to the other edge of the map cube and\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to set corner 2's location");
                return;
            }

            //Setting the 2nd corner of map
            if (settingBoundsCorner2){
                //Check name argument
                if (!checkGameExists(gamesConfig, settingUpGameName)){
                    sender.sendMessage(ChatColor.RED + "The game " + settingUpGameName + " does not exist!");
                    return;
                }

                Player player = (Player) sender;
                Location location = player.getLocation();

                gamesConfig.set(gamePath + "dimensions.x2", location.getBlockX());
                gamesConfig.set(gamePath + "dimensions.y2", location.getBlockY());
                gamesConfig.set(gamePath + "dimensions.z2", location.getBlockZ());

                configManager.saveGameConfig();

                settingBoundsCorner2 = false;
                settingLobby = true;

                sender.sendMessage(ChatColor.GREEN + " \nYou have created the map boundaries!\n" +
                        ChatColor.AQUA + "Next you need to set the games waiting lobby spawn point!\n" +
                        ChatColor.GRAY + "This is the waiting lobby where players wait for the game to\n" +
                                         "start. To set this location\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to set the location");
                return;
            }

            //Set the waiting lobby location
            if (settingLobby){
                //Check name argument
                if (!checkGameExists(gamesConfig, settingUpGameName)){
                    sender.sendMessage(ChatColor.RED + "The game " + settingUpGameName + " does not exist!");
                    return;
                }

                Player player = (Player) sender;
                Location location = player.getLocation();

                gamesConfig.set(gamePath + "lobby-location", ParseConverter.LocationToString(location));
                configManager.saveGameConfig();

                settingLobby = false;
                settingPlayerSpawns = true;

                int maxPlayers = gamesConfig.getInt(gamePath + "options.max-players");

                sender.sendMessage(ChatColor.GREEN + " \nYou have set the waiting lobby spawn!\n" +
                        ChatColor.AQUA + "Next you need to set the players spawn points for the game!\n" +
                        ChatColor.GRAY + "This is where players are sent at the beginning of the game,\n" +
                                         "usually in a big circle.\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to set the spawn location (1/" + maxPlayers + ")");
                return;
            }

            //Set the players spawn points
            if (settingPlayerSpawns){

                //Check name argument
                if (!checkGameExists(gamesConfig, settingUpGameName)){
                    sender.sendMessage(ChatColor.RED + "The game " + settingUpGameName + " does not exist!");
                    return;
                }

                Location[] currentSpawnLocations = null;
                if (gamesConfig.get(gamePath + ".spawns") != null) {
                    currentSpawnLocations = ConfigManager.GetLocations(gamesConfig, gamePath + ".spawns");
                }

                Player player = (Player) sender;
                Location location = player.getLocation();

                int spawnLocs = 0;
                if (currentSpawnLocations != null) spawnLocs = currentSpawnLocations.length;

                int maxPlayers = gamesConfig.getInt(gamePath +  "options.max-players");

                ConfigManager.AddLocationToLocationList(gamesConfig, gamePath + ".spawns", location);
                configManager.saveGameConfig();

                //Is last spawn to set
                if (spawnLocs >= (maxPlayers - 1)){

                    settingPlayerSpawns = false;
                    settingDetectionChests = true;

                    sender.sendMessage(ChatColor.GREEN + " \nYou have set the player spawn points!\n" +
                            ChatColor.AQUA + "Next the game cube needs to be scanned for chests!\n" +
                            ChatColor.GRAY + "All the blocks in the map will be scanned to check for\n" +
                                             "chests in the arena, once scanned, chests placed will\n" +
                                             "not be stocked at games start. To you can rescan later\n" +
                                             "with '/sgadmin setup [game] rescan'. To scan for chests\n" +
                            ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to scan for chests in map arena");
                    return;
                }

                sender.sendMessage(ChatColor.GREEN + " \nYou have set a player spawn point!\n" +
                        ChatColor.AQUA + "Next you need to set another player spawn point!\n" +
                        ChatColor.GRAY + "This is where players are sent at the beginning of the game,\n" +
                        "usually in a big circle.\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin setup' to set the spawn location (" + spawnLocs + "/" + maxPlayers + ")");
                return;

            }

            //Detect for chests in game map arena
            if (settingDetectionChests){

                //Check name argument
                if (!checkGameExists(gamesConfig, settingUpGameName)){
                    sender.sendMessage(ChatColor.RED + "The game " + settingUpGameName + " does not exist!");
                    return;
                }

                sender.sendMessage(ChatColor.YELLOW + "Detecting chests in map area...");

                Location lobbyLocation = ConfigManager.GetLocation(gamesConfig, gamePath + ".lobby-location");
                Location[] chestLocations = GameLoader.DetectChests(sender, lobbyLocation.getWorld(), GameLoader.getMapDimensions(gamesConfig, gamePath));

                ConfigManager.SetLocations(gamesConfig, gamePath + ".chests", chestLocations);
                configManager.saveGameConfig();

                settingDetectionChests = false;
                settingGame = true;

                sender.sendMessage(ChatColor.GREEN + " \nChest detection complete!\n" +
                        ChatColor.AQUA + "The game id now set up!\n" +
                        ChatColor.GRAY + "You can now set up SG signs to join this game, if you would\n" +
                                         "like to change any other settings you must delete the game\n" +
                                         "and set it up again, to remove the game\n" +
                        ChatColor.LIGHT_PURPLE + "  Type: '/sgadmin remove " + settingUpGameName +"' to remove the game");


                sender.sendMessage(" \n" +
                        ChatColor.YELLOW + "Attempting to load the game!");
                //Attempt to load the game
                SurvivalGames.GetGameManager().addGame(settingUpGameName);

                settingUpGameName = "";
            }
        }
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

    private void sendLobbyHelpMessage(CommandSender sender){
        sender.sendMessage(ChatColor.BLUE +         "-----------" + ChatColor.YELLOW + " SG Admin Help " + ChatColor.BLUE + "----------");
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "/sgadmin lobby setspawn");
        sender.sendMessage(ChatColor.GRAY +         "    - Sets the lobby spawn location");
        sender.sendMessage(ChatColor.BLUE +         "-------------------------------------------");
    }

    private boolean validInteger(String arg){
        try {
            int d = Integer.parseInt(arg);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private boolean checkGameExists(FileConfiguration gamesConfig, String name){
        return gamesConfig.get("games.maps." + name.toUpperCase()) != null;
    }
}
