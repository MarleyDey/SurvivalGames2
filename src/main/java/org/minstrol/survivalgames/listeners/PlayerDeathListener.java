package org.minstrol.survivalgames.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.players.SgPlayer;

import java.util.List;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();

        SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);
        if (sgPlayer == null) return;

        Game game = sgPlayer.getActiveGame();
        if (game == null) return;

        if (!sgPlayer.isAlive()) return;
        sgPlayer.setAlive(false);

        List<SgPlayer> alivePlayers
                = game.getAlivePlayers();

        Location deathLocation = player.getLocation();
        deathLocation.getWorld().strikeLightningEffect(deathLocation);

        event.setDeathMessage(null);

        //There is one alive player left and so the game is stopped
        if (alivePlayers.size() <= 1) {
            game.stop();
        }
    }
}