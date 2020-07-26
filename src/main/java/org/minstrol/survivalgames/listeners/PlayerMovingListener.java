package org.minstrol.survivalgames.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.players.SgPlayer;

public class PlayerMovingListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(PlayerMoveEvent event){
        if (event.isCancelled())return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getX() != to.getX()) || (from.getZ() != to.getZ())) {
            Player player = event.getPlayer();
            SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);

            if (sgPlayer == null) return;
            if (sgPlayer.getActiveGame().isPlayersCanMove()) return;

            event.setCancelled(true);
        }
    }
}
