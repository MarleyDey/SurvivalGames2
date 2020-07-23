package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.players.SgPlayer;

import java.util.List;
import java.util.logging.Level;

public class PlayerDeathListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
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

        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();

            if (nEvent.getDamager() instanceof Player) {
                Player killer = (Player) nEvent.getDamager();
                SgPlayer sgKiller = SurvivalGames.GetPlayerManager().getSgPlayer(killer);

                if (game != sgKiller.getActiveGame()) {
                    Bukkit.getLogger().log(Level.WARNING, "Player " + killer.getName() +
                            " has killed a player from another game! Check your maps are secure!");
                    return;
                }

                game.broadcastMsg(ChatColor.GREEN + player.getName() + ChatColor.GRAY + " was killed by " + ChatColor.RED + killer.getName()); //TODO Customise this
                int kills = sgKiller.getKills();
                sgKiller.setKills(++kills);
            }

            //There is one alive player left and so the game is stopped
            if (alivePlayers.size() <= 1) {
                game.stop();
            }
            return;
        }

        game.broadcastMsg(ChatColor.GREEN + player.getName() + ChatColor.YELLOW + " has died!");
        //There is one alive player left and so the game is stopped
        if (alivePlayers.size() <= 1) {
            game.stop();
        }
    }
}