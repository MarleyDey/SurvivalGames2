package org.minstrol.survivalgames.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class Game {

    private String name;
    private int[] dimensions;
    private Location lobbyLocation;
    private boolean playersCanMove = true;
    private int maxPlayers,
            minPlayers,
            waitingCountdown = 10,
            waitingCountdownTask = 0;
    private Location[] spawnLocations, chestLocations;
    private GameStatus gameStatus = GameStatus.STOPPED;
    private Plugin plugin;

    private PlayerManager playerManager;
    private List<String> players;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name,
                int[] dimensions, int minPlayers, int maxPlayers){

        plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
        this.playerManager = SurvivalGames.GetPlayerManager();
        this.spawnLocations = spawnLocations;
        this.chestLocations = chestLocations;
        this.lobbyLocation = lobbyLocation;
        this.name = name;
        this.dimensions = dimensions;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;

        //Initialise starting values of game instance
        this.players = new ArrayList<>();
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
     * This gets the spawn locations for the players in the game
     *
     * @return Array of spawn locations of players
     */
    public Location[] getSpawnLocations() {
        return spawnLocations;
    }

    /**
     * This gets the chest locations within the game
     *
     * @return The array of chest locations
     */
    public Location[] getChestLocations() {
        return chestLocations;
    }

    /**
     * This gets the lobby spawn location of the game
     *
     * @return The location of the game lobby
     */
    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    /**
     * Gets the dimension cords of the game map
     *
     * @return array of dimensions of map
     */
    public int[] getMapDimensions() {
        return dimensions;
    }

    /**
     * Gets the maximum amount of players the game can handle
     *
     * @return Max number of players
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Gets the minimum amount of players needed to start the game
     *
     * @return Minimum numbers of players to start game
     */
    public int getMinPlayers() {
        return minPlayers;
    }

    public boolean isPlayersCanMove() {
        return playersCanMove;
    }

    /**
     * Gets the Sg player instances of the game
     *
     * @return List of Sg players
     */
    public List<SgPlayer> getPlayers() {
        List<SgPlayer> sgPlayers = new ArrayList<>();
        for (String playerUuid : players){
            sgPlayers.add(playerManager.getSgPlayer(playerUuid));
        }

        return sgPlayers;
    }

    /**
     * Gets the Sg player that are alive in the game
     *
     * @return List of alive Sg players
     */
    public List<SgPlayer> getAlivePlayers() {
        List<SgPlayer> alivePlayers = new ArrayList<>();
        for (SgPlayer player : getPlayers()){
            if (player.isAlive())alivePlayers.add(player);
        }

        return alivePlayers;
    }

    public void waitForPlayers() {
        if (getGameStatus() == GameStatus.WAITING || getGameStatus() == GameStatus.INGAME) {
            Bukkit.getLogger().log(Level.WARNING, "Game is already running!");
            return;
        }

        Bukkit.getLogger().log(Level.INFO, "Game " + name + " is waiting for players...");

        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "The plugin instance could not be found!");

            forceStop();
            return;
        }

        //Start waiting for the players to join
        setGameStatus(GameStatus.WAITING);
    }

    private void attemptGameStart(){
        if (getGameStatus() != GameStatus.WAITING){
            Bukkit.getLogger().log(Level.WARNING, "Attempted to start game when it wasnt in waiting mode!");
            return;
        }
        //Waiting process is over, we can start the game
        setGameStatus(GameStatus.STARTING);

        broadcastMsg("Attempting to start the game..."); //TODO Make configurable
        Bukkit.getLogger().log(Level.INFO, "Attempting to start game: " + name);

        waitingCountdown = 10; //TODO Make configurable

        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0){
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);

                if (players.size() < minPlayers){
                    waitForPlayers();
                    return;
                }

                start();
                return;
            }

            if (waitingCountdown % 5 == 0 || waitingCountdown <= 3) {
                broadcastMsg("Starting game in " + waitingCountdown); //TODO  Make configurable
            }
            waitingCountdown--;
        }, 0L, 20L);
    }


    private void start(){
        restockChests(false);

        //Assign each player a spawn position
        assignPlayerSpawns();

        //Send players to their spawn positions
        playersCanMove = false;
        sendPlayersToSpawn();

        waitingCountdown = 5; //TODO Make configurable
        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0){
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);


                playersCanMove = true;
                setGameStatus(GameStatus.INGAME);
                return;
            }

            broadcastMsg("Prepare to run in " + waitingCountdown); //TODO  Make configurable
            waitingCountdown--;
        }, 0L, 20L);

        /**
         * This is the end of this method, instead the stop method is called
         * by the player death event when only one player is left
         */
    }

    public void stop(){
        Bukkit.getScheduler().cancelTask(waitingCountdownTask);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "The plugin instance could not be found!");

            forceStop();
            return;
        }

        if (getGameStatus() != GameStatus.INGAME){
            Bukkit.getLogger().log(Level.WARNING, "Attempted to stop a game while it is not in-game!");
            return;
        }

        displayLeaderboard();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {}, 60L);

        sendPlayersToGameLobby();

        //Remove all Sg Players from the game
        SurvivalGames.GetPlayerManager().clearGamePlayers(this);
        players.clear();

        //Start to reset the map
        setGameStatus(GameStatus.RESETTING);
        MapEnvironment.ClearDroppedItems(this);

        //Reset and restart the game
        restart();

    }

    public void forceStop(){
        Bukkit.getScheduler().cancelTask(waitingCountdownTask);

        setGameStatus(GameStatus.STOPPED);

        sendPlayersToGameLobby();

        SurvivalGames.GetPlayerManager().clearGamePlayers(this);
        players.clear();
    }

    private void restart(){
        if (getGameStatus() != GameStatus.RESETTING){
            Bukkit.getLogger().log(Level.WARNING, "Attempted to reset a game while it is not in resetting state!");
            return;
        }

        MapEnvironment.ClearDroppedItems(this);

        start();
    }

    private void displayLeaderboard(){
        broadcastMsg(ChatColor.BLUE + "----  " + ChatColor.YELLOW + "Leaderboard  " + ChatColor.BLUE  + "----");
        SgPlayer[] players = getLeaderboard();
        for (int i = 0; i < players.length; i++){
            broadcastMsg(ChatColor.YELLOW + "" + (i+1) + ChatColor.BLUE + " - " + ChatColor.WHITE + players[i].getName());
        }
    }

    /**
     * This will restock all the checks of the map
     */
    private void restockChests(boolean broadcast){
        MapEnvironment.RestockChests(this);
        if (broadcast) broadcastMsg("All chests have been restocked!"); //TODO Configurable

    }

    private void assignPlayerSpawns(){
        int i = 0;
        for (SgPlayer sgPlayer : getPlayers()){
            if (spawnLocations.length <= i){
                Bukkit.getLogger().log(Level.SEVERE, "There are more players than spawn points! Stopping game..");
                return;
            }

            sgPlayer.setSpawnLocation(spawnLocations[i]);
            i++;
        }
    }

    /**
     * This will send all the players of the game back to their spawn points
     */
    private void sendPlayersToSpawn(){
        for (SgPlayer sgPlayer : getPlayers()){
            Location spawnLocation = sgPlayer.getSpawnLocation();

            if (spawnLocation == null){
                Bukkit.getLogger().log(Level.SEVERE, "Player " + sgPlayer.getName() + " has no spawn point set! Stopping game..");

                //Force stop the game to prevent spawn error
                forceStop();
                return;
            }
            sgPlayer.getBukkitPlayer().teleport(spawnLocation);
        }
    }

    /**
     * This will send all the players of the game back to game lobby
     */
    private void sendPlayersToGameLobby(){
        for (SgPlayer sgPlayer : getPlayers()){
            Location spawnLocation = SurvivalGames.GetLobby().getSpawnLocation();

            if (spawnLocation == null){
                Bukkit.getLogger().log(Level.SEVERE, "The game lobby has no spawn point set! Stopping game..");
                return;
            }
            sgPlayer.getBukkitPlayer().teleport(spawnLocation);
        }
    }

    /**
     * This will send all the players of the game back to the lobby location
     */
    private void sendPlayersToLobby(){
        for (SgPlayer sgPlayer : getPlayers()){
            sgPlayer.getBukkitPlayer().teleport(lobbyLocation);
        }
    }

    public SgPlayer[] getLeaderboard(){
        List<SgPlayer> players = getPlayers();

        players.sort(Comparator.comparingInt(SgPlayer::getKills).reversed());
        return ParseConverter.SgPlayerListToArray(players);
    }

    public void broadcastMsg(String message){
        for (SgPlayer sgPlayer : getPlayers()){
            sgPlayer.getBukkitPlayer().sendMessage(message);
        }
    }

    /**
     * This will create a new instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player joined successfully
     */
    public void playerJoin(Player player){
        if (players.contains(player.getUniqueId().toString()))return;

        //Player joins while game is waiting for players
        if (getGameStatus() == GameStatus.WAITING){

            //Check that there is enough spaces left for player
            if (players.size() >= maxPlayers){
                player.sendMessage(ChatColor.RED + "The game you are trying to join is full!"); //TODO Make configurable
                return;
            }

            players.add(player.getUniqueId().toString());
            playerManager.addPlayer(player, this);

            broadcastMsg(ChatColor.AQUA + "[" + players.size() + "/" + maxPlayers + "] " + ChatColor.DARK_GREEN +
                    player.getName() + ChatColor.YELLOW + " has joined the game!");

            //Teleport player to waiting lobby
            player.teleport(lobbyLocation);

            //If the minimum players requireed to start the game is reached then attempt to start the game
            if (players.size() >= minPlayers) attemptGameStart();
            return;
        }

        player.sendMessage(ChatColor.RED + "This game is currently not joinable!");


    }

    /**
     * This will remove the instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player left successfully
     */
    public void playerLeave(Player player){
        playerManager.removePlayer(player);
        players.remove(player.getUniqueId().toString());
    }
}
