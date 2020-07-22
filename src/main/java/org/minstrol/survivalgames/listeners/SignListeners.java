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

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Sign)) return;

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

        game.playerJoin(player);
    }

    @EventHandler
    public void on(SignChangeEvent event){
        String[] signLines = event.getLines();

        if (!signLines[0].equals("[SG]"))return;


        //Check that the game exists
        Game game = SurvivalGames.GetGameManager().getGame(signLines[1]);
        if (game == null) {
            event.setLine(1, "Game not found!");
            event.getBlock().getState().update();
            return;
        }

        event.setLine(0, ChatColor.GOLD + "[SG]");
        event.setLine(1, game.getName());
        event.getBlock().getState().update();

        SignManager signManager = SurvivalGames.GetSignManager();
        signManager.addSign(event.getBlock().getLocation());

        event.getPlayer().sendMessage(ChatColor.GREEN + "You have set up a SG lobby sign!");
    }

    @EventHandler
    public void on(BlockBreakEvent event){
        Block block = event.getBlock();

        //Check it is a sign
        if (!(block.getState() instanceof Sign)) return;

        //Check the sign is a lobby sign
        SignManager signManager = SurvivalGames.GetSignManager();
        List<Location> signLocations = signManager.getSignLocations();

        if (signLocations == null)return;
        if (!signLocations.contains(block.getLocation()))return;

        signManager.removeSign(block.getLocation());
        event.getPlayer().sendMessage(ChatColor.YELLOW + "You have removed a lobby sign!");
    }
}
