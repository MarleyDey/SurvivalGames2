package org.minstrol.survivalgames.game.util;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameManager;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GameLoader {

    private String gameName, configPath;
    private FileConfiguration gameConfig;

    public GameLoader(String name) {
        this.gameName = name;
        this.configPath = "games.maps." + name + ".";
        this.gameConfig = SurvivalGames.GetConfigManager().getGameConfig();
    }

    /**
     * This gets the dimensions of the game map from the config and loads them
     * into a single integer array
     *
     * @return array of cords of the game map
     */
    public static int[] getMapDimensions(FileConfiguration config, String gamePath) {
        int[] dimensions = new int[6];

        dimensions[0] = config.getInt(gamePath + "dimensions.x1");
        dimensions[1] = config.getInt(gamePath + "dimensions.x2");
        dimensions[2] = config.getInt(gamePath + "dimensions.y1");
        dimensions[3] = config.getInt(gamePath + "dimensions.y2");
        dimensions[4] = config.getInt(gamePath + "dimensions.z1");
        dimensions[5] = config.getInt(gamePath + "dimensions.z2");

        return dimensions;
    }

    /**
     * This will iterate through all the blocks of the games map and scans for chest
     * blocks to add to the chest locations and adds to the game.
     *
     * @param sender     The command sender to inform
     * @param world      The world the chests are located
     * @param dimensions The dimentions of the world
     * @return The locations of the chests in the game map
     */
    public static Location[] DetectChests(CommandSender sender, World world, int[] dimensions) {
        List<Location> chestLocations = new ArrayList<>();

        int lx, ux, ly, uy, lz, uz;

        //Upper and lower of x dimension
        lx = Math.min(dimensions[0], dimensions[1]);
        ux = Math.max(dimensions[0], dimensions[1]);

        //Upper and lower of y dimension
        ly = Math.min(dimensions[2], dimensions[3]);
        uy = Math.max(dimensions[2], dimensions[3]);

        //Upper and lower of z dimension
        lz = Math.min(dimensions[4], dimensions[5]);
        uz = Math.max(dimensions[4], dimensions[5]);

        int chestAmount = 1;

        //X dimension
        for (int x = lx; x < ux; x++) {

            //Y dimension
            for (int y = ly; y < uy; y++) {

                //Z dimension
                for (int z = lz; z < uz; z++) {

                    //Check for chest
                    Location location = new Location(world, x, y, z);
                    Block block = world.getBlockAt(location);
                    if (!block.getType().equals(Material.CHEST)) continue;

                    String locationString = ParseConverter.LocationToString(location);
                    chestLocations.add(location);

                    sender.sendMessage(ChatColor.GREEN + "[" + chestAmount + "] Chest Found! [" + locationString + "]");
                    chestAmount++;
                }
            }
        }

        return ParseConverter.LocationListToArray(chestLocations);
    }

    public static void DeleteGame(String name) {
        GameManager gameManager = SurvivalGames.GetGameManager();

        ConfigManager configManager = SurvivalGames.GetConfigManager();
        FileConfiguration gamesConfig = configManager.getGameConfig();

        gamesConfig.set("games.maps." + name.toUpperCase(), null);
        gameManager.removeGame(name);

    }

    /**
     * This will load an instance of a game from the game config given the name
     * to the game
     *
     * @return The loaded config game instance
     */
    public Game loadGame() {
        Location[] chestLocations = this.getChestLocations();

        if (chestLocations == null) {
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "The game [" + gameName +
                    "] could not be loaded due to one or more invalid chest locations. Try setting them up again!");
            return null;
        }

        Location[] spawnLocations = this.getSpawnLocations();

        if (spawnLocations == null) {
            Bukkit.getLogger().log(Level.SEVERE,"[SurvivalGames] " + "The game [" + gameName +
                    "] could not be loaded due to one or more invalid spawn locations. Try setting them up again!");
            return null;
        }

        Location lobbyLocation = this.getLobbyLocation();

        if (lobbyLocation == null) {
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "The game [" + gameName +
                    "] could not be loaded due to an invalid lobby location. Try setting the location again!");
            return null;
        }

        return new Game(spawnLocations, chestLocations, lobbyLocation, gameName, getMapDimensions(gameConfig, configPath), this.getMinPlayers(), this.getMaxPlayers());
    }

    /**
     * This gets the chest locations of the game from the game config
     *
     * @return Locations of chests
     */
    private Location[] getChestLocations() {
        return ConfigManager.GetLocations(gameConfig, configPath + "chests");
    }

    /**
     * This gets the spawn location of the game from the game config
     *
     * @return Locations of spawns
     */
    private Location[] getSpawnLocations() {
        return ConfigManager.GetLocations(gameConfig, configPath + "spawns");
    }

    /**
     * This gets the lobby location of the game
     *
     * @return Location of lobby
     */
    private Location getLobbyLocation() {
        return ConfigManager.GetLocation(gameConfig, configPath + "lobby-location");
    }

    /**
     * This gets the maximum players that can play this game
     *
     * @return Maximum amount of players
     */
    private int getMaxPlayers() {
        return gameConfig.getInt(configPath + ".options.max-players");
    }

    /**
     * This gets the minimum players to play the game
     *
     * @return Minimum players to start game
     */
    private int getMinPlayers() {
        return gameConfig.getInt(configPath + ".options.min-players");
    }
}
