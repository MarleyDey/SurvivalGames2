package org.minstrol.survivalgames.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ConfigManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SignManager {

    private List<Location> signLocations;
    private ConfigManager configManager;
    private FileConfiguration lobbyConfig;

    public SignManager(){
        configManager = SurvivalGames.GetConfigManager();
        lobbyConfig = configManager.getLobbyConfig();

        signLocations = Arrays.asList();

    }

    private List<String> getSignLocations(){
        List<String> locationStrings = new ArrayList<>();

        Location[] locationStrs = ConfigManager.GetLocations(lobbyConfig, "lobby.sign");

        if (locationStrs == null){
            Bukkit.getLogger().log(Level.WARNING, "No sign location were found in the lobby!");
            return null;
        }

        locationStrings = (List<String>) Arrays.asList(locationStrs);

        return



    }

}
