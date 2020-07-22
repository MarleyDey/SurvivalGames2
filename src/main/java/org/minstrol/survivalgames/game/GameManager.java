package org.minstrol.survivalgames.game;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.util.GameLoader;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class GameManager {

    private List<Game> games;

    public GameManager(){
        games = new ArrayList<>();

        //Load the games that are already present in the games config
        loadExistingGames();
    }

    /**
     * This checks if the game manager contains the game
     *
     * @param game Game instance
     * @return If game manager contains game
     */
    public boolean containsGame(Game game){
        for (Game gm : games){
            if (gm == null)continue;
            if (gm == game)return true;
        }
        return false;
    }

    /**
     * This checks if the game manager contains the game
     *
     * @param name Game name
     * @return If game manager contains game
     */
    public boolean containsGame(String name){
        for (Game gm : games){
            if (gm == null)continue;

            String gameName = gm.getName().toUpperCase();
            if (gameName.equals(name.toUpperCase()))return true;
        }
        return false;
    }

    /**
     * Adds a game to the game manager
     *
     * @param game Game instance to add
     */
    public void addGame(Game game){
        if (containsGame(game))return;
        games.add(game);
    }

    /**
     * Adds a game to the game manager
     *
     * @param name Name of game to add
     */
    public void addGame(String name){
        if (containsGame(name))return;

        GameLoader gameLoader = new GameLoader(name);
        Game game = gameLoader.loadGame();

        if (game == null){
            Bukkit.getLogger().log(Level.SEVERE, "The game " + name + " could not be loaded!");
            return;
        }

        game.waitForPlayers();

        Bukkit.getLogger().log(Level.INFO, "Game loaded: " + name + " was successful");

        games.add(game);
    }

    /**
     * Removes the game instance from the game manager
     *
     * @param game game instance to remove
     */
    public void removeGame(Game game){
        if (!containsGame(game))return;
        games.remove(game);
    }

    /**
     * Removes the game instance from the game manager
     *
     * @param name name of game to remove
     */
    public void removeGame(String name){
        if (!containsGame(name))return;

        games.remove(getGame(name));
    }

    /**
     * Gets the game instance from the game manager
     *
     * @param name name of the game to get
     * @return Game instance
     */
    public Game getGame(String name){
        for (Game gm : games){
            if (gm == null)continue;

            String gameName = gm.getName().toUpperCase();
            if (gameName.equalsIgnoreCase(name.toUpperCase()))return gm;
        }
        return null;
    }

    public String[] getGameNames(){
        FileConfiguration gameConfig
                = SurvivalGames.GetConfigManager().getGameConfig();

        if (gameConfig.get("games.maps") == null){
            Bukkit.getLogger().log(Level.WARNING, "No games were found in the game config to load!");
            return null;
        }

        Set<String> gameNames = gameConfig.getConfigurationSection("games.maps").getKeys(false);
        return ParseConverter.StringListToArray(new ArrayList<>(gameNames));
    }


    /**
     * This will load the games in the games config file
     */
    private void loadExistingGames(){
        String[] gameNames = getGameNames();

        if (gameNames == null)return;
        for (String name : gameNames){

            GameLoader gameLoader = new GameLoader(name);
            Game game = gameLoader.loadGame();

            if (game == null){
                Bukkit.getLogger().log(Level.SEVERE, "The game " + name + " could not be loaded!");
                continue;
            }

            games.add(game);
            Bukkit.getLogger().log(Level.INFO, "Game " + name + " has been found and loaded!");

            game.waitForPlayers();
        }
    }

    public void closeGames(){
        for (Game game : games){
            game.forceStop(); //TODO Do thread safe stuff
        }
    }

}
