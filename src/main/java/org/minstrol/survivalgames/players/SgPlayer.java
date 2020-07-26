package org.minstrol.survivalgames.players;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.game.Game;

public class SgPlayer {

    private Game activeGame;
    private String uuid, name;
    private int kills = 0, deaths = 0;
    private Location spawnLocation;
    private boolean
            isAlive = true,
            ghost = false;

    public SgPlayer(Game game, String uuid, String name){
        this.activeGame = game;
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Gets the name of the player
     *
     * @return name of bukkit player
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the uuid of the player
     *
     * @return uuid of player
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Gets the kills of the players
     *
     * @return kills of player
     */
    public int getKills() {
        return kills;
    }

    /**
     * Gets the deaths of the players
     *
     * @return deaths of player
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * Gets the active game of the player
     *
     * @return players active game
     */
    public Game getActiveGame() {
        return activeGame;
    }

    /**
     * This gets whether a player is declared as alive in
     * a game
     *
     * @return Whether a player is alive in the game
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * This sets whether a player is alive in a game
     *
     * @param alive Player is alive or has 'died'
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    /**
     * This sets the kills of the player
     *
     * @param kills Kills of the player
     */
    public void setKills(int kills){
        this.kills = kills;
    }


    /**
     * This sets the deaths of the player
     *
     * @param deaths deaths of the player
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * This sets the active game instance of the player
     *
     * @param activeGame Game instance player is in
     */
    public void setActiveGame(Game activeGame) {
        this.activeGame = activeGame;
    }

    /**
     * This will set the spawn location of the SG Player
     *
     * @param location Spawn location of player
     */
    public void setSpawnLocation(Location location){
        this.spawnLocation = location;
    }

    /**
     * This gets the spawn location of the SG player
     *
     * @return spawn location of player
     */
    public Location getSpawnLocation(){
        return this.spawnLocation;
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }

    /**
     * Gets the bukkit player instance of the sg player
     *
     * @return Bukkit player of Sg Player
     */
    public Player getBukkitPlayer(){
        for (Player pl : Bukkit.getOnlinePlayers()){
            if (pl.getUniqueId().toString().equalsIgnoreCase(uuid))return pl;
        }
        return null;
    }

}
