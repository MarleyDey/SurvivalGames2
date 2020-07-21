package org.minstrol.survivalgames.game.util;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GameLoader {

    private String gameName, configPath;
    private FileConfiguration gameConfig;

    public GameLoader(String name){
        this.gameName = name;
        this.configPath = "games.maps." + name + ".";
        this.gameConfig = SurvivalGames.GetConfigManager().getGameConfig();
    }

    public Game loadGame(){
        Location[] chestLocations = getChestLocations();

        if (chestLocations == null){
            Bukkit.getLogger().log(Level.SEVERE, "The game " + gameName +
                    " could not be loaded due to one or more invalid chest locations. Try setting them up again!");
            return null;
        }

        Location[] spawnLocations = getSpawnLocations();

        if (spawnLocations == null){
            Bukkit.getLogger().log(Level.SEVERE, "The game " + gameName +
                    " could not be loaded due to one or more invalid spawn locations. Try setting them up again!");
            return null;
        }

        Location lobbyLocation = getLobbyLocation();

        if (lobbyLocation == null){
            Bukkit.getLogger().log(Level.SEVERE, "The game " + gameName +
                    " could not be loaded due to an invalid lobby location. Try setting the location again!");
            return null;
        }

        return new Game(chestLocations, spawnLocations, lobbyLocation, gameName, getMapDimensions(gameConfig, configPath), getMinPlayers(), getMaxPlayers());
    }

    private Location[] getChestLocations(){
        return ConfigManager.GetLocations(gameConfig,  configPath + "chests");
    }

    private Location[] getSpawnLocations(){
        return ConfigManager.GetLocations(gameConfig,  configPath + "spawns");
    }

    private Location getLobbyLocation(){
        return ConfigManager.GetLocation(gameConfig, configPath + "lobby-location");
    }

    private int getMaxPlayers(){
        return gameConfig.getInt(configPath + ".options.max-players");
    }

    private int getMinPlayers(){
        return gameConfig.getInt(configPath + ".options.min-players");
    }

    /**
     * This gets the dimensions of the game map from the config and loads them
     * into a single integer array
     *
     * @return array of cords of the game map
     */
    public static int[] getMapDimensions(FileConfiguration config, String gamePath){
        int[] dimensions = new int[6];

        dimensions[0] = config.getInt(gamePath + "dimensions.x1");
        dimensions[1] = config.getInt(gamePath + "dimensions.x2");
        dimensions[2] = config.getInt(gamePath + "dimensions.y1");
        dimensions[3] = config.getInt(gamePath + "dimensions.y2");
        dimensions[4] = config.getInt(gamePath + "dimensions.z1");
        dimensions[5] = config.getInt(gamePath + "dimensions.z2");

        return dimensions;
    }

    public static Location[] DetectChests(CommandSender sender, World world, int[] dimensions){
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
        for (int x = lx; x < ux; x++){

            //Y dimension
            for (int y = ly; y < uy; y++){

                //Z dimension
                for (int z = lz; z < uz; z++){

                    //Check for chest
                    Location location = new Location(world, x, y, z);
                    Block block = world.getBlockAt(location);
                    if (!block.getType().equals(Material.CHEST))continue;

                    String locationString = ParseConverter.LocationToString(location);
                    chestLocations.add(location);

                    sender.sendMessage(ChatColor.GREEN + "[" + chestAmount + "] Chest Found! [" + locationString + "]");
                    chestAmount++;
                }
            }
        }

        return  ParseConverter.LocationListToArray(chestLocations);
    }
}
