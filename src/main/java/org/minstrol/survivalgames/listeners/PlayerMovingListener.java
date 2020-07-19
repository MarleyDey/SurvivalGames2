package org.minstrol.survivalgames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.SgPlayer;

public class PlayerMovingListener implements Listener {

    @EventHandler
    public void on(PlayerMoveEvent event){
        if (event.isCancelled())return;

        Player player = event.getPlayer();
        SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);

        if (sgPlayer == null)return;

        if (!sgPlayer.getActiveGame().isPlayersCanMove()){
            event.setCancelled(true);
        }
    }
}
