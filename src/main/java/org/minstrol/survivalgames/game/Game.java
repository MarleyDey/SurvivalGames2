package org.minstrol.survivalgames.game;

import org.bukkit.*;
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
    private boolean
            playersCanMove = true,
            gracePeriod = false;
    private int
            maxPlayers,
            minPlayers,
            waitingCountdown = 10,
            waitingCountdownTask = 0;
    private Location[]
            spawnLocations,
            chestLocations;
    private GameStatus gameStatus = GameStatus.STOPPED;
    private Plugin plugin;

    private PlayerManager playerManager;
    private List<String> players;
    private Game game = this;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name,
                int[] dimensions, int minPlayers, int maxPlayers) {

        this.plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
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
    public String getName() {
        return name;
    }

    /**
     * Gets the game status of the game
     *
     * @return status of the game
     */
    public GameStatus getGameStatus() {
        return gameStatus;
    }

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

    public boolean isGracePeriod() {
        return gracePeriod;
    }

    /**
     * Gets the Sg player instances of the game
     *
     * @param includeGhosts Should the list of players include players that are no longer in the game
     * @return List of Sg players
     */
    public List<SgPlayer> getPlayers(boolean includeGhosts) {
        List<SgPlayer> sgPlayers = new ArrayList<>();
        for (String playerName : players) {
            SgPlayer sgPlayer = playerManager.getSgPlayer(playerName, this);

            if (sgPlayer == null) continue;
            if (!includeGhosts && sgPlayer.isGhost()) continue;
            sgPlayers.add(sgPlayer);
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
        for (SgPlayer player : getPlayers(true)) {
            if (player.isAlive()) alivePlayers.add(player);
        }

        return alivePlayers;
    }

    /**
     * Wait for players to join the game through either signs or commands, also
     * known as the waiting process for the game and will allow the game to start
     * once the minimum player amount has joined
     */
    void waitForPlayers() {
        if (getGameStatus() == GameStatus.WAITING || getGameStatus() == GameStatus.INGAME) {
            Bukkit.getLogger().log(Level.WARNING, "Game is already running!");
            return;
        }

        Bukkit.getLogger().log(Level.INFO, "[SurvivalGames] " + "Game " + name + " is waiting for players...");

        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "The plugin instance could not be found!");

            this.forceStop();
            return;
        }

        //Start waiting for the players to join
        this.setGameStatus(GameStatus.WAITING);
    }

    /**
     * This is called from the waiting for players to join method and starts
     * to countdown to the start of the game, this will not proceed to the start
     * method unless there is still enough players to start the game.
     */
    private void attemptGameStart() {
        if (getGameStatus() != GameStatus.WAITING) return;

        //Waiting process is over, we can start the game
        this.setGameStatus(GameStatus.STARTING);

        this.broadcastMsg(ChatColor.GREEN + "" + ChatColor.BOLD + "Attempting to start the game..."); //TODO Make configurable
        this.playSoundToPlayers(Sound.ENTITY_CAT_BEG_FOR_FOOD, 10, 5);
        Bukkit.getLogger().log(Level.INFO, "[SurvivalGames] " + "Attempting to start game: [" + name + "]");

        waitingCountdown = 15; //TODO Make configurable

        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0) {
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);

                //Check there is still enough players to start the game
                if (players.size() < minPlayers) {
                    this.broadcastMsg(ChatColor.YELLOW + "Not enough players to start game! Waiting again...");
                    this.playSoundToPlayers(Sound.ENTITY_VILLAGER_NO, 10, 5);
                    this.waitForPlayers();
                    return;
                }

                //Still enough players to start the game
                this.start();
                return;
            }

            if (waitingCountdown % 5 == 0 || waitingCountdown <= 3) {
                this.broadcastMsg(ChatColor.YELLOW + "Starting game in " + ChatColor.GREEN + waitingCountdown); //TODO  Make configurable
                this.playSoundToPlayers(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 7);
            }
            waitingCountdown--;
        }, 0L, 20L);
    }


    private void start() {
        this.setGameStatus(GameStatus.INGAME);
        this.restockChests(false);

        //Assign each player a spawn position
        this.assignPlayerSpawns();

        //Send players to their spawn positions
        playersCanMove = false;
        this.sendPlayersToSpawn();

        this.broadcastMsg(ChatColor.AQUA + " \nWelcome to Survival Games! Have fun.\n ");

        waitingCountdown = 5; //TODO Make configurable
        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0) {
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);


                playersCanMove = true;
                this.playSoundToPlayers(Sound.ENTITY_PLAYER_LEVELUP, 10, 5);
                this.broadcastMsg(ChatColor.GREEN + "" + ChatColor.BOLD + "GO GO GO");

                Bukkit.getLogger().log(Level.INFO, "[SurvivalGames] " + "Game: [" + name + "] has started w/ " + players.size() + " players!");
                return;
            }

            broadcastMsg(ChatColor.YELLOW + "Prepare to run in " + ChatColor.RED + waitingCountdown); //TODO  Make configurable
            waitingCountdown--;
        }, 20L, 20L);

        /*
          This is the end of this method, instead the stop method is called
          by the player death event when only one player is left
         */
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(waitingCountdownTask);

        Plugin plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
        if (plugin == null) {
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "The plugin instance could not be found!");

            this.forceStop();
            return;
        }

        if (getGameStatus() != GameStatus.INGAME) {
            Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Attempted to stop a game while it is not in-game!");
            return;
        }

        this.displayLeaderboard();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            sendPlayersToGameLobby();

            //Remove all Sg Players from the game
            SurvivalGames.GetPlayerManager().clearGamePlayers(game);
            players.clear();

            //Start to reset the map
            setGameStatus(GameStatus.RESETTING);
            MapEnvironment.ClearDroppedItems(game);

            //Reset and restart the game
            restart();
        }, 80L);
    }

    public void forceStop() {
        Bukkit.getScheduler().cancelTask(waitingCountdownTask);

        this.setGameStatus(GameStatus.STOPPED);

        Location spawnLocation = SurvivalGames.GetLobby().getSpawnLocation();
        if (spawnLocation != null) {
            this.sendPlayersToGameLobby();
        }

        SurvivalGames.GetPlayerManager().clearGamePlayers(this);
        players.clear();
    }

    private void restart() {
        if (getGameStatus() != GameStatus.RESETTING) {
            Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Attempted to reset a game while it is not in resetting state!");
            return;
        }

        MapEnvironment.ClearDroppedItems(this);

        this.waitForPlayers();
    }

    /**
     * This will restock all the checks of the map
     */
    public void restockChests(boolean broadcast) {
        MapEnvironment.RestockChests(this);
        if (broadcast) this.broadcastMsg("All chests have been restocked!"); //TODO Configurable

    }

    private void assignPlayerSpawns() {
        int i = 0;
        for (SgPlayer sgPlayer : getPlayers(false)) {
            if (spawnLocations.length <= i) {
                Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "There are more players than spawn points! Stopping game..");
                return;
            }

            sgPlayer.setSpawnLocation(spawnLocations[i]);
            i++;
        }
    }

    /**
     * This will send all the players of the game back to their spawn points
     */
    private void sendPlayersToSpawn() {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            Location spawnLocation = sgPlayer.getSpawnLocation();

            if (spawnLocation == null) {
                Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "Player " + sgPlayer.getName() + " has no spawn point set! Stopping game..");

                //Force stop the game to prevent spawn error
                this.forceStop();
                return;
            }
            sgPlayer.getBukkitPlayer().teleport(spawnLocation);
        }
    }

    /**
     * This will send all the players of the game back to game lobby
     */
    private void sendPlayersToGameLobby() {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            if (sgPlayer == null) continue;
            Location spawnLocation = SurvivalGames.GetLobby().getSpawnLocation();

            if (spawnLocation == null) {
                Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "The game lobby has no spawn point set! Stopping game..");
                return;
            }

            if (sgPlayer.getBukkitPlayer() == null) continue;
            Player player = sgPlayer.getBukkitPlayer();

            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(spawnLocation);
            player.setFoodLevel(20);
            player.setHealth(20);
        }
    }

    /**
     * This will send all the players of the game back to the lobby location
     */
    private void sendPlayersToLobby() {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            if (sgPlayer == null) continue;
            sgPlayer.getBukkitPlayer().teleport(lobbyLocation);
        }
    }

    private void playSoundToPlayers(Sound sound, int v1, int v2) {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            Player player = sgPlayer.getBukkitPlayer();

            player.playSound(player.getLocation(), sound, v1, v2);
        }
    }

    /**
     * This sets the players gamemode
     *
     * @param gamemode gamemode of player
     */
    private void setPlayersGamemode(GameMode gamemode) {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            sgPlayer.getBukkitPlayer().setGameMode(gamemode);
        }
    }

    private void displayLeaderboard() {
        this.broadcastMsg(ChatColor.BLUE + "" + ChatColor.BOLD + " \n-------  " + ChatColor.YELLOW + "Leaderboard  " + ChatColor.BLUE + "-------\n \n" +
                ChatColor.GREEN + "" + ChatColor.BOLD + "     Final Survivor: " + ChatColor.YELLOW + this.getAlivePlayers().get(0).getName() + "\n ");
        SgPlayer[] players = this.getLeaderboard();
        for (int i = 0; i < (players.length < 3 ? players.length : 3); i++) {
            SgPlayer sgPlayer = players[i];
            if (sgPlayer.getKills() == 0)continue;

            this.broadcastMsg(ChatColor.YELLOW + "       " + ChatColor.BOLD + (i + 1) + ChatColor.BLUE + " - " + ChatColor.RESET + ChatColor.WHITE + sgPlayer.getName()
                    + ChatColor.YELLOW + " (" + ChatColor.AQUA + sgPlayer.getKills() + ChatColor.YELLOW + ") kill(s)");
        }
        this.broadcastMsg(ChatColor.BLUE + "" + ChatColor.BOLD + " \n-------------------------\n ");
    }

    private SgPlayer[] getLeaderboard() {
        List<SgPlayer> players = getPlayers(true);

        players.sort(Comparator.comparingInt(SgPlayer::getKills).reversed());
        return ParseConverter.SgPlayerListToArray(players);
    }

    public void broadcastMsg(String message) {
        for (SgPlayer sgPlayer : getPlayers(false)) {
            sgPlayer.getBukkitPlayer().sendMessage(message);
        }
    }

    /**
     * This will create a new instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player joined successfully
     */
    public void playerJoin(Player player) {
        if (players.contains(player.getName())) {
            player.sendMessage(ChatColor.RED + "You cannot return to this game once left!");
            return;
        }

        //Player joins while game is waiting for players
        if (this.getGameStatus() != GameStatus.WAITING && this.getGameStatus() != GameStatus.STARTING) {
            player.sendMessage(ChatColor.RED + "This game is currently not joinable!");
            return;
        }

        //Check that there is enough spaces left for player
        if (players.size() >= maxPlayers) {
            player.sendMessage(ChatColor.RED + "The game you are trying to join is full!"); //TODO Make configurable
            return;
        }

        players.add(player.getName());
        playerManager.addPlayer(player, this);

        this.broadcastMsg(ChatColor.AQUA + "[" + players.size() + "/" + maxPlayers + "] " + ChatColor.DARK_GREEN +
                player.getName() + ChatColor.YELLOW + " has joined the game!");

        //Teleport player to waiting lobby
        player.teleport(lobbyLocation);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.getInventory().clear();

        //If the minimum players requireed to start the game is reached then attempt to start the game
        if (players.size() >= minPlayers) attemptGameStart();

    }

    /**
     * This will remove the instance of Sg Player for this game
     *
     * @param player bukkit player
     * @return if the player left successfully
     */
    public void playerLeave(Player player) {
        this.broadcastMsg(ChatColor.DARK_GREEN + player.getName() + ChatColor.YELLOW + " has left the game!");

        if (this.getGameStatus() == GameStatus.INGAME) {
            SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);
            sgPlayer.setAlive(false);
            sgPlayer.setGhost(true);

            if (getAlivePlayers().size() < 2) {
                stop();
            }
            return;
        }

        players.remove(player.getName());
        SurvivalGames.GetPlayerManager().removePlayer(player);
    }
}
