package org.minstrol.survivalgames.listeners;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.lobby.Lobby;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Lobby lobby = SurvivalGames.GetLobby();
        if (lobby.getSpawnLocation() == null) return;

        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();


        boolean spawnOnJoin = config.getBoolean("lobby.teleport-players-to-lobby-on-join");

        if (spawnOnJoin) {
            player.teleport(lobby.getSpawnLocation());
            player.setGameMode(
                    GameMode.valueOf(config.getString("lobby.set-gamemode-on-join").toUpperCase()));
            player.setHealth(20);
            player.setFoodLevel(20);
        }
    }
}
