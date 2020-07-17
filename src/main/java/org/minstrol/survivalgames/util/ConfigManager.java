package org.minstrol.survivalgames.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.logging.Level;

public class ConfigManager {

    private FileConfiguration gameConfig, playerConfig;
    private File gameConfigFile, playerConfigFile;
    private String
            playerFileName = "players.yml",
            gamesFileName = "games.yml";

    private Plugin plugin;

    public ConfigManager(Plugin plugin){
        this.plugin = plugin;
    }

    /**
     * Reloads the custom game config file
     */
    public void reloadGameConfig(){
        gameConfigFile = checkConfigLocation(gameConfigFile, gamesFileName);
        gameConfig = rldCustomConfig(gameConfigFile, gamesFileName);
    }

    /**
     * Reloads the custom players config file
     */
    public void reloadPlayerConfig(){
        playerConfigFile = checkConfigLocation(playerConfigFile, playerFileName);
        playerConfig = rldCustomConfig(playerConfigFile, playerFileName);
    }

    /**
     * Saves the custom game config file
     */
    public void saveGameConfig(){
        saveCustomConfig(checkConfigLocation(gameConfigFile, gamesFileName), getGameConfig());
    }

    /**
     * Saves the custom players config file
     */
    public void savePlayerConfig(){
        saveCustomConfig(checkConfigLocation(playerConfigFile, playerFileName), getPlayerConfig());
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
     * Reloads a custom configuration given the file location and file name and converts
     * it into a bukkit configuration format
     *
     * @param configFile The file location
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
     * @param config The bukkit configuration file
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
     * @param file config file location
     * @param configFileName The name of the config file
     * @return The file location of the config
     */
    private File checkConfigLocation(File file, String configFileName){
        if (file == null) {
            return new File(plugin.getDataFolder(), configFileName);
        }
        return file;
    }


}
