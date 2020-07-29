package org.minstrol.survivalgames.game;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.lobby.Lobby;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class Game {

    private String name;
    private int[] dimensions;
    private int
            maxPlayers,
            minPlayers,
            waitingCountdown = 10,
            waitingCountdownTask = 0;
    private Location[]
            spawnLocations,
            chestLocations;
    private Plugin plugin;
    private Game game = this;
    private List<String> players;
    private Location lobbyLocation;
    private boolean playersCanMove = true;
    private GameStatus gameStatus = GameStatus.STOPPED;

    public Game(Location[] spawnLocations, Location[] chestLocations, Location lobbyLocation, String name,
                int[] dimensions, int minPlayers, int maxPlayers) {

        this.plugin = Bukkit.getPluginManager().getPlugin("SurvivalGames");
        this.spawnLocations = spawnLocations;
        this.chestLocations = chestLocations;
        this.lobbyLocation = lobbyLocation;
        this.dimensions = dimensions;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.name = name;

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

    /**
     * Gets if the players should be able to move
     *
     * @return can players move
     */
    public boolean isPlayersCanMove() {
        return playersCanMove;
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
            SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(playerName, this);

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
            Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Game is already running!");
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

        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();

        this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.attempting-start"), null));
        this.playSoundToPlayers(Sound.ENTITY_CAT_BEG_FOR_FOOD, 10, 5);

        Bukkit.getLogger().log(Level.INFO, "[SurvivalGames] " + "Attempting to start game: [" + name + "]");

        waitingCountdown = config.getInt("events.game.waiting-countdown");
        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0) {
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);

                //Check there is still enough players to start the game
                if (players.size() < minPlayers) {
                    this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.insufficient-players"), null));
                    this.playSoundToPlayers(Sound.ENTITY_VILLAGER_NO, 10, 5);
                    this.waitForPlayers();
                    return;
                }

                //Still enough players to start the game
                this.start();
                return;
            }

            if (waitingCountdown % 5 == 0 || waitingCountdown <= 3) {
                this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.waiting-game-countdown"),
                        new HashMap<String, String>(){{put("%count%", "" + waitingCountdown);}}));

                this.playSoundToPlayers(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 7);
            }
            waitingCountdown--;
        }, 0L, 20L);
    }


    /**
     * Called to start the game, this will teleport players to their spawn
     * and begins the starting countdown.
     */
    private void start() {
        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();

        this.setGameStatus(GameStatus.INGAME);

        this.restockChests(config, false);
        //Assign each player a spawn position
        this.assignPlayerSpawns();
        //Send players to their spawn positions
        playersCanMove = false;
        this.sendPlayersToSpawn();

        this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.starting-message"), null));

        waitingCountdown = config.getInt("events.game.starting-countdown");
        waitingCountdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (waitingCountdown <= 0) {
                Bukkit.getScheduler().cancelTask(waitingCountdownTask);

                playersCanMove = true;
                this.playSoundToPlayers(Sound.ENTITY_PLAYER_LEVELUP, 10, 5);
                this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.start-message"), null));

                Bukkit.getLogger().log(Level.INFO, "[SurvivalGames] " + "Game: [" + name + "] has started w/ " + players.size() + " players!");
                return;
            }

            this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.starting-game-countdown"),
                    new HashMap<String, String>(){{put("%count%", "" + waitingCountdown);}}));

            waitingCountdown--;
        }, 20L, 20L);

        /*
          This is the end of this method, instead the stop method is called
          by the player death event when only one player is left
         */
    }

    /**
     * This will stop the game including any countdowns, display the leaderboard and
     * clear up the map.
     */
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

    /**
     * This will force stop the game and send all players back
     * to the lobby, and will not restart the game
     */
     void forceStop() {
        Bukkit.getScheduler().cancelTask(waitingCountdownTask);

        this.setGameStatus(GameStatus.STOPPED);

        Lobby lobby = SurvivalGames.GetLobby();
        if (lobby != null) {
            Location spawnLocation = lobby.getSpawnLocation();
            if (spawnLocation != null) {
                this.sendPlayersToGameLobby();
            }
        }

        SurvivalGames.GetPlayerManager().clearGamePlayers(this);
        players.clear();
    }

    /**
     * This will clear the map and restart the game to wait for
     * players to join
     */
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
    public void restockChests(FileConfiguration config, boolean broadcast) {
        MapEnvironment.RestockChests(this);
        if (broadcast)  this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.chest-restock"), null));

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

    /**
     * This will player a sound to all the players in the game
     *
     * @param sound sound to play
     * @param v1 Volume
     * @param v2 Pitch
     */
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

    /**
     * This will display the leaderboard of the last survivor and
     * the top three kills.
     */
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

    /**
     * This returns a sorted array of the kills player
     *
     * @return sorted array
     */
    private SgPlayer[] getLeaderboard() {
        List<SgPlayer> players = getPlayers(true);

        players.sort(Comparator.comparingInt(SgPlayer::getKills).reversed());
        return ParseConverter.SgPlayerListToArray(players);
    }

    /**
     * This will send a message to all the players in the game
     *
     * @param message The string message to send to players
     */
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
        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();
        if (players.contains(player.getName())) {
            player.sendMessage(ParseConverter.StrTran(config.getString("events.game.player-returning-error"),
                    null));
            return;
        }

        //Player joins while game is waiting for players
        if (this.getGameStatus() != GameStatus.WAITING && this.getGameStatus() != GameStatus.STARTING) {
            player.sendMessage(ParseConverter.StrTran(config.getString("events.game.not-joinable-error"),
                    null));
            return;
        }

        //Check that there is enough spaces left for player
        if (players.size() >= maxPlayers) {
            player.sendMessage(ParseConverter.StrTran(config.getString("events.game.full-game-error"),
                    null));
            return;
        }

        players.add(player.getName());
        SurvivalGames.GetPlayerManager().addPlayer(player, this);

        this.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.player-join"),
                new HashMap<String, String>(){{
                    put("%player_count%", "" + players.size());
                    put("%max_players%", "" + maxPlayers);
                    put("%player%", player.getName());
                }}));

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
        this.broadcastMsg(ParseConverter.StrTran(SurvivalGames.GetConfigManager().getConfig().getString("events.game.player-leave"),
                new HashMap<String, String>(){{ put("%player%", player.getName()); }}));

        PlayerManager playerManager = SurvivalGames.GetPlayerManager();

        if (this.getGameStatus() == GameStatus.INGAME) {
            SgPlayer sgPlayer = playerManager.getSgPlayer(player);
            sgPlayer.setAlive(false);
            sgPlayer.setGhost(true);

            if (getAlivePlayers().size() < 2) {
                stop();
            }
            return;
        }

        player.getInventory().clear();

        players.remove(player.getName());
        playerManager.removePlayer(player);

        Lobby lobby = SurvivalGames.GetLobby();

        if (lobby.getSpawnLocation() == null)return;
        player.teleport(SurvivalGames.GetLobby().getSpawnLocation());
    }
}
