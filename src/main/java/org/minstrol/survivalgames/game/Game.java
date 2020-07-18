package org.minstrol.survivalgames.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private int nSpawnLocations, nChestLocations;
    private Location[] spawnLocations, chestLocations;
    private Location lobbyLocation;

    private PlayerManager playerManager;

    private GameStatus gameStatus = GameStatus.STOPPED;

    private List<String> players;
    private String name;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name){
        this.playerManager = SurvivalGames.GetPlayerManager();
        this.spawnLocations = spawnLocations;
        this.chestLocations = chestLocations;
        this.lobbyLocation = lobbyLocation;
        this.name = name;

        //Initialise starting values of game instance
        this.players = new ArrayList<String>();
        this.nSpawnLocations = spawnLocations.length;
        this.nChestLocations = chestLocations.length;
    }

    /**
     * Gets the name of the game
     *
     * @return name of the game
     */
    public String getName() { return name; }

    /**
     * Gets the game status of the game
     *
     * @return status of the game
     */
    public GameStatus getGameStatus() { return gameStatus; }

    /**
     * This sets the status of the game
     *
     * @param gameStatus The new game status
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    /**
     * Gets the Sg player instances of the game
     *
     * @return List of Sg players
     */
    public List<SgPlayer> getPlayers() {
        List<SgPlayer> sgPlayers = new ArrayList<SgPlayer>();
        for (String playerUuid : players){
            sgPlayers.add(playerManager.getSgPlayer(playerUuid));
        }

        return sgPlayers;
    }

    /**
     * This will restock all the checks of the map
     */
    private void restockChests(){
        //TODO
    }

    /**
     * This will send all the players of the game back to their spawn points
     */
    private void sendPlayersToSpawn(){
        //TODO
    }

    /**
     * This will send all the players of the game back to the lobby location
     */
    private void sendPlayersToLobby(){
        //TODO
    }

    /**
     * This will create a new instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player joined successfully
     */
    public boolean playerJoin(Player player){
        //TODO
        return false;
    }

    /**
     * This will remove the instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player left successfully
     */
    public boolean playerLeave(Player player){
        //TODO
        return false;
    }
}
