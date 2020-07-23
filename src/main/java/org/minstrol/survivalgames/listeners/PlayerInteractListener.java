package org.minstrol.survivalgames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.SgPlayer;

public class PlayerInteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(PlayerInteractEvent event){
        Player player = event.getPlayer();
        SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);

        if (sgPlayer == null)return;

        if (!sgPlayer.getActiveGame().isPlayersCanMove()){
            event.setCancelled(true);
        }
    }
}
