package org.minstrol.survivalgames.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.game.GameStatus;
import org.minstrol.survivalgames.lobby.SignManager;

import java.util.List;
import java.util.logging.Level;

public class SignListeners implements Listener {


    @EventHandler
    public void on(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction().equals(Action.LEFT_CLICK_AIR))return;

        player.sendMessage("Clicked block");

        Block block = event.getClickedBlock();
        player.sendMessage("type: " + block.getType().name());

        if (!(block.getState() instanceof Sign)) {
            player.sendMessage("not sign");

        }

        player.sendMessage("Clicked sign");

        Sign sign = (Sign) block.getState();
        String[] signLines = sign.getLines();

        if (!signLines[0].equals(ChatColor.GOLD + "[SG]")) return;

        player.sendMessage("has sg on top");

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
    public void on(SignChangeEvent event){
        String[] signLines = event.getLines();

        if (!signLines[0].equals("[SG]"))return;

        event.getPlayer().sendMessage("has SG");

        //Check that the game exists
        Game game = SurvivalGames.GetGameManager().getGame(signLines[1]);
        if (game == null) {
            event.getBlock().getState().update();
            return;
        }

        event.setLine(0, ChatColor.GOLD + "[SG]");
        event.setLine(1, game.getName());
        event.getBlock().getState().update();

        SurvivalGames.GetSignManager().addSign(event.getBlock().getLocation());
        event.getPlayer().sendMessage(ChatColor.GREEN + "You have set up a SG lobby sign!");
    }

    @EventHandler
    public void on(BlockBreakEvent event){
        Block block = event.getBlock();

        //Check it is a sign
        if (!block.getType().name().contains("SIGN"))return;

        //Check the sign is a lobby sign
        SignManager signManager = SurvivalGames.GetSignManager();
        List<Location> signLocations = signManager.getSignLocations();

        if (!signLocations.contains(block.getLocation()))return;

        signManager.removeSign(block.getLocation());
        event.getPlayer().sendMessage(ChatColor.YELLOW + "You have removed a lobby sign!");
    }
}
