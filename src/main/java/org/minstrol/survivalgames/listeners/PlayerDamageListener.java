package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameStatus;
import org.minstrol.survivalgames.players.SgPlayer;

import java.util.logging.Level;

public class PlayerDamageListener implements Listener {

    @EventHandler
    public void on(EntityDamageByEntityEvent event){
        if (!event.getEntity().getType().equals(EntityType.PLAYER))return;
        if (!event.getDamager().getType().equals(EntityType.PLAYER))return;

        Player player = (Player) event.getEntity();
        Player killer = (Player) event.getDamager();

        SgPlayer sgPlayer = SurvivalGames.GetPlayerManager().getSgPlayer(player);
        SgPlayer sgKiller = SurvivalGames.GetPlayerManager().getSgPlayer(killer);

        if (sgPlayer == null || sgKiller == null)return;

        Game game = sgPlayer.getActiveGame();
        if (game == null)return;

        if (game.getGameStatus() != GameStatus.INGAME){
            event.setCancelled(true);
            return;
        }

        if (player.isDead()){
            if (game != sgKiller.getActiveGame()){
                Bukkit.getLogger().log(Level.WARNING, "Player " + killer.getName() +
                        " has killed a player from another game! Check your maps are secure!");
                return;
            }

            game.broadcastMsg(player.getName() + " was killed by " + ChatColor.RED + killer.getName()); //TODO Customise this
            int kills = sgKiller.getKills();
            sgKiller.setKills(++kills);
        }
    }
}
