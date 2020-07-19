package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameStatus;
import org.minstrol.survivalgames.util.ConfigManager;

import java.util.logging.Level;

public class SignListeners implements Listener {


    @EventHandler
    public void on(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction().equals(Action.LEFT_CLICK_AIR))return;

        Block block = event.getClickedBlock();
        if (block == null)return;
        if (!block.getType().name().contains("SIGN"))return;

        Sign sign = (Sign) block.getState();

        String[] signLines = sign.getLines();

        if (!signLines[0].equals(ChatColor.GOLD + "[SG]")) return;

        //Check that the game exists
        Game game = SurvivalGames.GetGameManager().getGame(signLines[1]);
        if (game == null) {
            Bukkit.getLogger().log(Level.WARNING, "On player clicking lobby sign, the game " +
                    signLines[1] + " could not be found!");
            return;
        }

        //Players can only join in a waiting state
        if (game.getGameStatus() != GameStatus.WAITING)return;

        //Check game is not full
        if (game.getPlayers().size() >= game.getMaxPlayers()){
            player.sendMessage(ChatColor.RED + "The game you are trying to join is currently full!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Attempting to join " + ChatColor.YELLOW + game.getName());
        game.playerJoin(player);
    }

    @EventHandler
    public void on(BlockPlaceEvent event){
        Block block = event.getBlock();

        if (!block.getType().name().contains("SIGN"))return;

        Sign sign = (Sign) block.getState();
        String[] signLines = sign.getLines();

        if (!signLines[0].equals("[SG]"))return;

        //Check that the game exists
        Game game = SurvivalGames.GetGameManager().getGame(signLines[1]);
        if (game == null) {
            sign.setLine(1, "Game not found!");
            sign.update();
            return;
        }

        sign.setLine(0, ChatColor.GOLD + "[SG]");
        sign.setLine(1, game.getName());
        sign.update();

        SurvivalGames.GetSignManager().addSign(block.getLocation());
        event.getPlayer().sendMessage(ChatColor.GREEN + "You have set up a SG lobby sign!");
    }
}
