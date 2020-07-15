package org.minstrol.survivalgames.game;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.players.SgPlayer;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private int nSpawnLocations, nChestLocations;
    private Location[] spawnLocations, chestLocations;
    private Location lobbyLocation;

    private GameStatus gameStatus = GameStatus.STOPPED;

    private List<String> players;
    private String name;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name){
        this.spawnLocations = spawnLocations;
        this.chestLocations = chestLocations;
        this.lobbyLocation = lobbyLocation;
        this.name = name;

        //Initialise starting values of game instance
        this.players = new ArrayList<String>();
        this.nSpawnLocations = spawnLocations.length;
        this.nChestLocations = chestLocations.length;
    }

    public String getName() { return name; }

    public GameStatus getGameStatus() { return gameStatus; }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public List<SgPlayer> getPlayers() {
        //TODO
        return null;
    }

    private void restockChests(){
        //TODO
    }

    private void sendPlayersToSpawn(){
        //TODO
    }

    private void sendPlayersToLobby(){
        //TODO
    }

    public boolean playerJoin(Player player){
        //TODO
        return false;
    }

    public boolean playerLeave(Player player){
        //TODO
        return false;
    }
}
