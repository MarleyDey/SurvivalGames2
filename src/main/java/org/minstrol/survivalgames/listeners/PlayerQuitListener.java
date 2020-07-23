package org.minstrol.survivalgames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;

public class PlayerQuitListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(PlayerQuitEvent event){
        Player player = event.getPlayer();

        PlayerManager playerManager = SurvivalGames.GetPlayerManager();
        SgPlayer sgPlayer = playerManager.getSgPlayer(player);

        if (sgPlayer != null){
            sgPlayer.getActiveGame().playerLeave(player);
        }
    }

}
