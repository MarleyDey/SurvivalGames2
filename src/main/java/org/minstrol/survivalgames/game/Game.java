package org.minstrol.survivalgames.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;

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
    private PlayerWaiter playerWaitingThread;

    private PlayerManager playerManager;

    private List<String> players;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name,
                int[] dimensions, int minPlayers, int maxPlayers){
        this.playerManager = SurvivalGames.GetPlayerManager();
        this.spawnLocations = spawnLocations;
        this.chestLocations = chestLocations;
        this.lobbyLocation = lobbyLocation;
        this.name = name;
        this.dimensions = dimensions;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;

        //Initialise starting values of game instance
        this.players = new ArrayList<String>();
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
        List<SgPlayer> sgPlayers = new ArrayList<SgPlayer>();
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


    public void start(){
        if (getGameStatus() != GameStatus.STOPPED || getGameStatus() != GameStatus.RESETTING){
            Bukkit.getLogger().log(Level.WARNING, "Game is already running!");
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "The plugin instance could not be found!");

            forceStop();
            return;
        }

        //Start waiting for the players to join
        setGameStatus(GameStatus.WAITING);

        waitingCountdown = 10; //TODO Make configurable
        boolean enoughPlayers = false;

        //Lobby waiting process
        while (!enoughPlayers) {

            playerWaitingThread = new PlayerWaiter(minPlayers);

            //Wait for enough players to join the game
            try {
                playerWaitingThread.getThread().join(); //I dont know if this will work
            } catch (InterruptedException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Waiting for players to join thread interrupted", ex);

                forceStop();
                return;
            }

            //Enough players have joined, start game countdown
            enoughPlayers = true;

            restockChests(false);
            broadcastMsg("Attempting to start the game..."); //TODO Make configurable


            waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                if (waitingCountdown <= 0){
                    Bukkit.getScheduler().cancelTask(waitingCountdownTask);
                    return;
                }

                broadcastMsg("Starting game in " + waitingCountdown); //TODO  Make configurable
                waitingCountdown--;
            }, 0L, 20L);

            if (players.size() < minPlayers){
                enoughPlayers = false;
            }
        }

        //Waiting process is over, we can start the game
        setGameStatus(GameStatus.STARTING);

        //Assign each player a spawn position
        assignPlayerSpawns();

        //Send players to their spawn positions
        playersCanMove = false;
        sendPlayersToSpawn();

        waitingCountdown = 5; //TODO Make configurable
        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0){
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);
                return;
            }

            broadcastMsg("Prepare to run in " + waitingCountdown); //TODO  Make configurable
            waitingCountdown--;
        }, 0L, 20L);

        playersCanMove = true;
        setGameStatus(GameStatus.INGAME);

        /**
         * This is the end of this method, instead the stop method is called
         * by the player death event when only one player is left
         */
    }

    public void stop(){
        playerWaitingThread.exit();

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

        //Start to reset the map
        setGameStatus(GameStatus.RESETTING);
        MapEnvironment.ClearDroppedItems(this);

        //Reset and restart the game
        restart();

    }

    public void forceStop(){
        playerWaitingThread.exit();
        setGameStatus(GameStatus.STOPPED);

        sendPlayersToGameLobby();

        SurvivalGames.GetPlayerManager().clearGamePlayers(this);
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

                //Force stop the game to prevent spawn error
                forceStop();
                return;
            }

            sgPlayer.setSpawnLocation(spawnLocations[i]);
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
            Location spawnLocation = SurvivalGames.getLobby().getSpawnLocation();

            if (spawnLocation == null){
                Bukkit.getLogger().log(Level.SEVERE, "The game lobby has no spawn point set! Stopping game..");

                //Force stop the game to prevent spawn error
                forceStop();
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
        return (SgPlayer[]) players.toArray();
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
        playerManager.addPlayer(player, this);

        //Player joins while game is waiting for players
        if (getGameStatus() == GameStatus.WAITING){
            broadcastMsg(ChatColor.AQUA + "[" + players.size() + "/" + maxPlayers + "] " + ChatColor.DARK_GREEN +
                    player.getName() + ChatColor.YELLOW + " has joined the game!");
        }
    }

    /**
     * This will remove the instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player left successfully
     */
    public void playerLeave(Player player){
        playerManager.removePlayer(player);
    }

    private class PlayerWaiter implements Runnable {
        private Thread thread;
        private int minPlayers;
        private boolean exit = false;

        public PlayerWaiter(int minPlayers){
            this.minPlayers = minPlayers;
            thread = new Thread(this, "PlayerWaiter - " + name);
            thread.start();
        }

        @Override
        public void run() {
            while (!exit &&(players.size() < minPlayers)){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                Thread.yield();
            }
        }

        public void exit(){
            exit = true;
        }

        public Thread getThread() {
            return thread;
        }
    }
}
