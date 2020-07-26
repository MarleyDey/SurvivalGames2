package org.minstrol.survivalgames.players;

import org.bukkit.entity.Player;
import org.minstrol.survivalgames.game.Game;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {

    private List<SgPlayer> players;

    public PlayerManager() {
        players = new ArrayList<>();
    }

    /**
     * Get if player manager contains the player
     *
     * @param sgPlayer The Survival games player instance
     * @return player manager contains player
     */
    private boolean containsPlayer(SgPlayer sgPlayer) {
        return players.contains(sgPlayer);
    }

    /**
     * Get if player manager contains the player
     *
     * @param bukkitPlayer Bukkit instance of player
     * @return player manager contains player
     */
    private boolean containsPlayer(Player bukkitPlayer) {
        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getBukkitPlayer() == null) continue;
            if (sgPlayer.getBukkitPlayer() == bukkitPlayer) return true;
        }
        return false;
    }

    /**
     * Get if player manager contains the player
     *
     * @param name uuid of player
     * @return player manager contains player
     */
    private boolean containsPlayer(String name) {
        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getBukkitPlayer() == null) continue;
            if (sgPlayer.getBukkitPlayer().getName().toUpperCase().equals(name.toUpperCase()))
                return true;
        }
        return false;
    }

    /**
     * Add player to the player manager
     *
     * @param sgPlayer SgPlayer instance
     */
    public void addPlayer(SgPlayer sgPlayer) {
        if (this.containsPlayer(sgPlayer)) return;
        players.add(sgPlayer);
    }

    /**
     * Add player to the player manager by creating a new
     * instance of a SgPlayer
     *
     * @param player bukkit player instance
     * @param game   the game the player is in
     */
    public void addPlayer(Player player, Game game) {
        players.add(new SgPlayer(game, player.getUniqueId().toString(), player.getName()));
    }

    /**
     * Remove a player from the player manager
     *
     * @param sgPlayer the sg player instance
     */
    public void removePlayer(SgPlayer sgPlayer) {
        if (!this.containsPlayer(sgPlayer)) return;
        players.remove(sgPlayer);
    }

    /**
     * Remove a player from the player manager
     *
     * @param player the bukkit player instance
     */
    public void removePlayer(Player player) {
        if (!this.containsPlayer(player)) return;
        players.remove(getSgPlayer(player));

    }

    public List<SgPlayer> getPlayers() {
        return players;
    }

    /**
     * Gets a player from the player manager
     *
     * @param player bukkit player
     * @return The SgPlayer instance of the bukkit player
     */
    public SgPlayer getSgPlayer(Player player) {
        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getBukkitPlayer() == null) continue;
            if (sgPlayer.getBukkitPlayer() != player) continue;
            if (sgPlayer.isGhost())continue;
                return sgPlayer;
        }
        return null;
    }

    /**
     * Gets a player from the player manager
     *
     * @param name bukkit player name string
     * @return The SgPlayer instance of the bukkit player
     */
    public SgPlayer getSgPlayer(String name) {
        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getBukkitPlayer() == null) continue;
            if (!sgPlayer.getBukkitPlayer().getName().toLowerCase().equals(name.toLowerCase()))continue;
            if (sgPlayer.isGhost())continue;
                return sgPlayer;
        }
        return null;
    }

    /**
     * Gets a player from the player manager, including
     * ghost players
     *
     * @param name bukkit player name string
     * @return The SgPlayer instance of the bukkit player
     */
    public SgPlayer getSgPlayer(String name, Game game) {
        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getBukkitPlayer() == null) continue;
            if (!sgPlayer.getBukkitPlayer().getName().toLowerCase().equals(name.toLowerCase()))continue;
            if (sgPlayer.getActiveGame() != game)continue;
            return sgPlayer;
        }
        return null;
    }

    /**
     * This clears the Sg Player instances from a game
     *
     * @param game Game of SgPlayers
     */
    public void clearGamePlayers(Game game) {
        if (players == null)return;
        List<SgPlayer> editPlayers = new ArrayList<>(players);

        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getActiveGame() == null)continue;
            if (sgPlayer.getActiveGame() != game)continue;

            editPlayers.remove(sgPlayer);
        }

        players = editPlayers;
    }

    /**
     * This gets the Sg Player instances from a game
     *
     * @param game Game of SgPlayers
     */
    public List<SgPlayer> getSgPlayersFromGame(Game game) {
        List<SgPlayer> gamePlayers = new ArrayList<>();

        for (SgPlayer sgPlayer : players) {
            if (sgPlayer == null) continue;
            if (sgPlayer.getActiveGame() != null && sgPlayer.getActiveGame() == game)
                gamePlayers.add(sgPlayer);
        }
        return gamePlayers;
    }
}
