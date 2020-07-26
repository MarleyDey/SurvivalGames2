package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.players.SgPlayer;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void on(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        String format = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());

        PlayerManager playerManager = SurvivalGames.GetPlayerManager();
        SgPlayer sgPlayer = playerManager.getSgPlayer(player);

        if (event.isCancelled())return;
        event.setCancelled(true);

        if (sgPlayer != null) {
            Game game = sgPlayer.getActiveGame();

            if (game != null) {
            /* Player is in a game and so if they talk in chat,
            only the players in the game should see it. */

                game.broadcastMsg(ChatColor.AQUA + "[SG] " + ChatColor.RESET + format); //TODO Configure this from config
                return;
            }
        }

        //Send messages amongst lobby players
        for (Player lobbyPlayer : Bukkit.getOnlinePlayers()){
            if (playerManager.getSgPlayer(lobbyPlayer) != null)continue;

            lobbyPlayer.sendMessage(format);
        }

    }
}
