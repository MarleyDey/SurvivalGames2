package org.minstrol.survivalgames;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.minstrol.survivalgames.commands.CommandManager;

public class SurvivalGames extends JavaPlugin implements Listener {

    private Listener[] listeners = new Listener[]{

    };

    private CommandManager commandManager
            = new CommandManager();

    private String[] commandNames = new String[]{
            "sgadmin",
            "sg"
    };

    @Override
    public void onEnable() {
        //Register game events
        registerEvents();

        //Register game command
        registerCommands();
    }

    @Override
    public void onDisable() {
        super.onDisable();
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
}
