package org.minstrol.survivalgames.game.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.util.ConfigManager;

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

        return new Game(chestLocations, spawnLocations, lobbyLocation, gameName, getMapDimensions());
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

    /**
     * This gets the dimensions of the game map from the config and loads them
     * into a single integer array
     *
     * @return array of cords of the game map
     */
    private int[] getMapDimensions(){
        int[] dimensions = new int[6];

        dimensions[0] = gameConfig.getInt(configPath + "dimensions.x1");
        dimensions[1] = gameConfig.getInt(configPath + "dimensions.x2");
        dimensions[2] = gameConfig.getInt(configPath + "dimensions.y1");
        dimensions[3] = gameConfig.getInt(configPath + "dimensions.y2");
        dimensions[4] = gameConfig.getInt(configPath + "dimensions.z1");
        dimensions[5] = gameConfig.getInt(configPath + "dimensions.z2");

        return dimensions;
    }

}
