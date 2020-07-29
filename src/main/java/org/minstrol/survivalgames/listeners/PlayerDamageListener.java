package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameStatus;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class PlayerDamageListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(EntityDamageEvent event){


        if (!event.getEntity().getType().equals(EntityType.PLAYER))return;

        Player player = (Player) event.getEntity();
        PlayerManager playerManager = SurvivalGames.GetPlayerManager();

        SgPlayer sgPlayer = playerManager.getSgPlayer(player);

        if (sgPlayer == null)return;

        Game game = sgPlayer.getActiveGame();
        if (game == null)return;

        if (game.getGameStatus() != GameStatus.INGAME){
            event.setCancelled(true);
            return;
        }

        if (!game.isPlayersCanMove()){
            event.setCancelled(true);
            return;
        }

        //Player is about to die
        if (event.getDamage() > player.getHealth()){
            if (!sgPlayer.isAlive()) return;
            sgPlayer.setAlive(false);

            List<SgPlayer> alivePlayers
                    = game.getAlivePlayers();

            Location deathLocation = player.getLocation();
            deathLocation.getWorld().strikeLightningEffect(deathLocation);

            player.setHealth(20);
            player.setFoodLevel(20);
            player.setGameMode(GameMode.SPECTATOR);

            FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();

            if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();

                if (nEvent.getDamager() instanceof Player) {
                    Player killer = (Player) nEvent.getDamager();
                    SgPlayer sgKiller = playerManager.getSgPlayer(killer);

                    if (game != sgKiller.getActiveGame()) {
                        Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Player " + killer.getName() +
                                " has killed a player from another game! Check your maps are secure!");
                        return;
                    }

                    game.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.player-killed"),
                            new HashMap<String, String>(){{
                                put("%player%", player.getName());
                                put("%killer%", killer.getName());
                    }}));

                    killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, 8);

                    player.sendMessage(ParseConverter.StrTran(config.getString("events.game.player-killed-receiver"),
                            new HashMap<String, String>(){{
                                put("%player%", player.getName());
                                put("%killer%", killer.getName());
                            }}));

                    int kills = sgKiller.getKills();
                    sgKiller.setKills(++kills);
                }

                //There is one alive player left and so the game is stopped
                if (alivePlayers.size() <= 1) {
                    game.stop();
                }
                return;
            }

            game.broadcastMsg(ParseConverter.StrTran(config.getString("events.game.player-died"),
                    new HashMap<String, String>(){{ put("%player%", player.getName()); }}));
            //There is one alive player left and so the game is stopped
            if (alivePlayers.size() <= 1) {
                game.stop();
            }
        }
    }
}
