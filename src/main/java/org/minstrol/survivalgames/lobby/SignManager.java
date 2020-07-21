package org.minstrol.survivalgames.lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.game.Game;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class SignManager {

    private Plugin plugin;
    private ConfigManager configManager;
    private FileConfiguration lobbyConfig;
    private int signUpdatingTask = 0;
    private boolean isUpdatingTaskActive = false;

    public SignManager(Plugin plugin) {
        this.plugin = plugin;

        configManager = SurvivalGames.GetConfigManager();
        lobbyConfig = configManager.getLobbyConfig();


        //Start updating the lobby signs every half-second
        updateSignsTask();
    }

    public List<Location> getSignLocations() {
        Location[] locationStrs = ConfigManager.GetLocations(lobbyConfig, "lobby.signs");

        if (locationStrs == null) {
            Bukkit.getLogger().log(Level.WARNING, "No sign location were found in the lobby!");
            return null;
        }

        return Arrays.asList(locationStrs);
    }

    private void updateSignsTask() {
        isUpdatingTaskActive = true;
        signUpdatingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            List<Location> signLocations = getSignLocations();

            if (signLocations == null) {
                stopUpdatingSigns();
                return;
            }

            for (Location signLoc : signLocations) {
                World world = signLoc.getWorld();

                Block block = world.getBlockAt(signLoc);
                if (!(block.getState() instanceof Sign)) {
                    SurvivalGames.GetSignManager().removeSign(signLoc);
                    continue;
                }

                Sign sign = (Sign) block.getState();

                String[] signLines = sign.getLines();

                if (!signLines[0].equals(ChatColor.GOLD + "[SG]")) {
                    removeSign(signLoc);
                    continue;
                }

                Game game = SurvivalGames.GetGameManager().getGame(signLines[1]);
                if (game == null) {
                    Bukkit.getLogger().log(Level.WARNING, "On an attempt to update a sign, the game " +
                            signLines[1] +
                            " could not be found!");
                    removeSign(signLoc);
                    continue;
                }

                sign.setLine(2, game.getGameStatus().getFormattedName());
                sign.setLine(3, "[" + game.getPlayers().size() + "/" + game.getMaxPlayers() + "]");

                sign.update();
            }
        }, 0L, 10L);
    }

    public void stopUpdatingSigns() {
        Bukkit.getScheduler().cancelTask(signUpdatingTask);
        isUpdatingTaskActive = false;
    }

    public void addSign(Location location) {
        if (lobbyConfig.get("lobby.signs") == null) {
            List<String> signLocs = new ArrayList<>();
            signLocs.add(ParseConverter.LocationToString(location));

            lobbyConfig.set("lobby.signs", signLocs);
            configManager.saveLobbyConfig();
            return;
        }

        List<String> signLocs = lobbyConfig.getStringList("lobby.signs");
        signLocs.add(ParseConverter.LocationToString(location));
        lobbyConfig.set("lobby.signs", signLocs);
        configManager.saveLobbyConfig();

        if (!isUpdatingTaskActive) updateSignsTask();
    }

    public void removeSign(Location location) {
        if (lobbyConfig.get("lobby.signs") == null) return;
        List<String> signLocs = lobbyConfig.getStringList("lobby.signs");

        String locStr = ParseConverter.LocationToString(location);
        if (!signLocs.contains(locStr)) return;
        signLocs.remove(locStr);

        lobbyConfig.set("lobby.signs", signLocs);
        configManager.saveLobbyConfig();

        //Check if any lobby signs remain to save processing power
        if (getSignLocations() == null)stopUpdatingSigns();
    }

    public boolean isUpdatingTaskActive() {
        return isUpdatingTaskActive;
    }
}