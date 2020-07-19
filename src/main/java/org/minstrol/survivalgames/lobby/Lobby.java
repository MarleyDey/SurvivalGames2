package org.minstrol.survivalgames.lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.logging.Level;

public class Lobby {

    private Location spawnLocation;
    private ConfigManager configManager = SurvivalGames.GetConfigManager();
    private FileConfiguration lobbyConfig = configManager.getLobbyConfig();

    public Lobby(){
        spawnLocation = getSpawnLocation();
    }

    public Location getSpawnLocation() {
        if (spawnLocation != null)return spawnLocation;

        if (lobbyConfig.get("lobby.spawn") == null){
            Bukkit.getLogger().log(Level.SEVERE, "Lobby spawn location has not been set!");
            return null;
        }

        String spawnLocString = lobbyConfig.getString("lobby.spawn");
        if (ParseConverter.StringToLocation(spawnLocString) == null){
            Bukkit.getLogger().log(Level.SEVERE, "Lobby spawn location string was not valid!");
            return null;
        }

        spawnLocation = ParseConverter.StringToLocation(spawnLocString);
        return spawnLocation;
    }

    public void setSpawnLocation(Location location){
        spawnLocation = location;

        lobbyConfig.set("lobby.spawn", ParseConverter.LocationToString(location));
        configManager.saveLobbyConfig();
    }
}
