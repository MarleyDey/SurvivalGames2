package org.minstrol.survivalgames;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.minstrol.survivalgames.commands.CommandManager;
import org.minstrol.survivalgames.game.GameManager;
import org.minstrol.survivalgames.listeners.PlayerDeathListener;
import org.minstrol.survivalgames.listeners.PlayerMovingListener;
import org.minstrol.survivalgames.listeners.SignListeners;
import org.minstrol.survivalgames.lobby.Lobby;
import org.minstrol.survivalgames.lobby.SignManager;
import org.minstrol.survivalgames.players.PlayerManager;
import org.minstrol.survivalgames.util.ConfigManager;

public class SurvivalGames extends JavaPlugin implements Listener {

    private static ConfigManager configManager;
    private static PlayerManager playerManager;
    private static SignManager signManager;
    private static GameManager gameManager;

    private static Lobby lobby;

    private Listener[] listeners = new Listener[]{
            new PlayerMovingListener(),
            new PlayerDeathListener(),
            new SignListeners()
    };

    private CommandManager commandManager
            = new CommandManager();

    private String[] commandNames = new String[]{
            "sgadmin",
            "sg"
    };

    @Override
    public void onEnable() {
        //Initialise Managers
        configManager = new ConfigManager(this);
        signManager = new SignManager(this);
        playerManager = new PlayerManager();
        gameManager = new GameManager();

        lobby = new Lobby();

        //Register game events
        registerEvents();

        //Register game command
        registerCommands();
    }

    @Override
    public void onDisable() {
        GetGameManager().closeGames();
        GetSignManager().stopUpdatingSigns();
    }

    /**
     * Register the events of the game
     */
    private void registerEvents(){
        for (Listener listener : listeners){
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Register the commands of the game to the command manager
     */
    private void registerCommands(){
        for (String name : commandNames){
            Bukkit.getPluginCommand(name).setExecutor(commandManager);
        }
    }

    /**
     * Gets the static instance of the player manager
     *
     * @return player manager
     */
    public static PlayerManager GetPlayerManager(){
        return playerManager;
    }

    /**
     * Gets the static instance of the game manager
     *
     * @return game manager
     */
    public static GameManager GetGameManager() {
        return gameManager;
    }

    /**
     * Gets the static instance of the config manager
     *
     * @return config manager
     */
    public static ConfigManager GetConfigManager() {
        return configManager;
    }

    /**
     * This gets the static instance of the lobby class
     *
     * @return The lobby class
     */
    public static Lobby GetLobby() {
        return lobby;
    }

    public static SignManager GetSignManager() {
        return signManager;
    }
}
