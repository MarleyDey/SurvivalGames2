package org.minstrol.survivalgames.game;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private List<Game> games;

    public GameManager(){
        games = new ArrayList<Game>();
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

        //TODO Create game instance
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

}
