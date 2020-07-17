package org.minstrol.survivalgames.game.util;

import org.bukkit.Location;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.List;

public class GameLoader {

    private String gameName, configPath;
    private ConfigManager configManager;

    public GameLoader(String name){
        this.gameName = name;
        this.configPath = "games.maps." + name + ".";

        this.configManager = SurvivalGames.GetConfigManager();
    }

    public boolean loadGame(){




    }

    private Location[] getChestLocations(){
        List<String> locationStrs
                = configManager.getGameConfig().getStringList(gameName + "chests");

        if (locationStrs.isEmpty())return null;

        for (String locStr : locationStrs){
            Location chestLocation = ParseConverter.StringToLocation(locStr);

            if (chestLocation == null)return null;
        }

    }

}
