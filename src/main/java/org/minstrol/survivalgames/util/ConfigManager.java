package org.minstrol.survivalgames.util;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    private FileConfiguration gameConfig, playerConfig, lobbyConfig;
    private File gameConfigFile, playerConfigFile, lobbyConfigFile;
    private String
            playerFileName = "players.yml",
            gamesFileName = "games.yml",
            lobbyFileName = "lobby.yml";

    private Plugin plugin;

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * This gets an array of Locations from the config given a
     * config file and the path to the location lists
     *
     * @param config The config containing the locations list
     * @param path   The path in the config to the locations
     * @return The array of found locations
     */
    public static Location[] GetLocations(FileConfiguration config, String path) {
        List<String> locationStrs
                = config.getStringList(path);

        if (locationStrs.isEmpty()) return null;

        Location[] locations = new Location[locationStrs.size()];

        int i = 0;
        for (String locStr : locationStrs) {
            Location foundLocation = ParseConverter.StringToLocation(locStr);

            if (foundLocation == null) return null;

            locations[i] = foundLocation;
            i++;
        }

        return locations;
    }

    /**
     * This will get the string list at a path in a config file and add the string location
     * to the list and set the list back into the config file
     *
     * @param config The config containing the list
     * @param path The path to the list
     * @param location The Location to add
     */
    public static void AddLocationToLocationList(FileConfiguration config, String path, Location location) {
        List<String> locationStrs;

        if (config.get(path) != null) {
            locationStrs
                    = config.getStringList(path);
        } else {
            locationStrs = new ArrayList<>();
        }

        locationStrs.add(ParseConverter.LocationToString(location));
        config.set(path, locationStrs);

    }

    /**
     * This adds a string list of the locations to a given config file
     *
     * @param config The config file to add locations list to
     * @param path The Path to where to store the location in the config
     * @param locations The locations to save to the config
     */
    public static void SetLocations(FileConfiguration config, String path, Location[] locations) {
        List<String> locationStrs;

        if (config.get(path) != null) {
            locationStrs
                    = config.getStringList(path);
        } else {
            locationStrs = new ArrayList<>();
        }

        for (Location location : locations) {
            locationStrs.add(ParseConverter.LocationToString(location));
        }

        config.set(path, locationStrs);
    }

    /**
     * This gets a location in the config from a given path
     *
     * @param config The config containing the location
     * @param path   The path to the location
     * @return The location in the config
     */
    public static Location GetLocation(FileConfiguration config, String path) {
        String locationStr = config.getString(path);

        if (locationStr == null || locationStr.isEmpty()) {
            return null;
        }

        return ParseConverter.StringToLocation(locationStr);
    }

    /**
     * This gets a date in the config from a given path
     *
     * @param config The config containing the date
     * @param path   The path to the date
     * @return The date in the config
     */
    public static Date GetDate(FileConfiguration config, String path) {
        String dateStr = config.getString(path);

        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        return ParseConverter.StringToDate(dateStr);
    }

    /**
     * Reloads the custom game config file
     */
    public void reloadGameConfig() {
        gameConfigFile = checkConfigLocation(gameConfigFile, gamesFileName);
        gameConfig = rldCustomConfig(gameConfigFile, gamesFileName);
    }

    /**
     * Reloads the custom players config file
     */
    public void reloadPlayerConfig() {
        playerConfigFile = checkConfigLocation(playerConfigFile, playerFileName);
        playerConfig = rldCustomConfig(playerConfigFile, playerFileName);
    }

    /**
     * Reloads the custom lobby config file
     */
    public void reloadLobbyConfig() {
        lobbyConfigFile = checkConfigLocation(lobbyConfigFile, lobbyFileName);
        lobbyConfig = rldCustomConfig(lobbyConfigFile, lobbyFileName);
    }

    /**
     * Saves the custom game config file
     */
    public void saveGameConfig() {
        saveCustomConfig(checkConfigLocation(gameConfigFile, gamesFileName), getGameConfig());
    }

    /**
     * Saves the custom players config file
     */
    public void savePlayerConfig() {
        saveCustomConfig(checkConfigLocation(playerConfigFile, playerFileName), getPlayerConfig());
    }

    /**
     * Saves the custom lobby config file
     */
    public void saveLobbyConfig() {
        saveCustomConfig(checkConfigLocation(lobbyConfigFile, lobbyFileName), getLobbyConfig());
    }

    /**
     * Gets the custom game config file as a bukkit configuration
     *
     * @return bukkit game config
     */
    public FileConfiguration getGameConfig() {
        if (gameConfig == null) {
            reloadGameConfig();
        }
        return gameConfig;
    }

    /**
     * Gets the custom players config file as a bukkit configuration
     *
     * @return bukkit players config
     */
    public FileConfiguration getPlayerConfig() {
        if (playerConfig == null) {
            reloadPlayerConfig();
        }
        return playerConfig;
    }

    /**
     * Gets the custom lobby config file as a bukkit configuration
     *
     * @return bukkit lobby config
     */
    public FileConfiguration getLobbyConfig() {
        if (lobbyConfig == null) {
            reloadLobbyConfig();
        }
        return lobbyConfig;
    }

    /**
     * Reloads a custom configuration given the file location and file name and converts
     * it into a bukkit configuration format
     *
     * @param configFile     The file location
     * @param configFileName The config file name
     * @return The bukkit configuration file
     */
    private FileConfiguration rldCustomConfig(File configFile, String configFileName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        Reader defConfigStream = null;
        try {
            defConfigStream = new InputStreamReader(
                    plugin.getResource(configFileName),
                    "UTF8");

        } catch (UnsupportedEncodingException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not find " + configFileName + " config resource in jar", ex);
        }
        if (defConfigStream != null) {
            YamlConfiguration defConfig
                    = YamlConfiguration.loadConfiguration(defConfigStream);

            config.setDefaults(defConfig);
        }

        return config;
    }

    /**
     * This will save a given file path and config file
     *
     * @param configFile The file path to config
     * @param config     The bukkit configuration file
     */
    private void saveCustomConfig(File configFile, FileConfiguration config) {
        if (config == null || configFile == null) {
            return;
        }
        try {
            config.save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save game config to " + configFile, ex);
        }
    }

    /**
     * This will check if there is a valid file location to the specific config file
     *
     * @param file           config file location
     * @param configFileName The name of the config file
     * @return The file location of the config
     */
    private File checkConfigLocation(File file, String configFileName) {
        if (file == null) {
            return new File(plugin.getDataFolder(), configFileName);
        }
        return file;
    }
}
