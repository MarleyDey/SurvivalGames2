package org.minstrol.survivalgames.players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.minstrol.survivalgames.game.Game;

public class SgPlayer {

    private Game activeGame = null;
    private String uuid, name;
    private int kills = 0;

    public SgPlayer(Game game, String uuid, String name){
        this.activeGame = game;
        this.uuid = uuid;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public int getKills() {
        return kills;
    }

    public Game getActiveGame() {
        return activeGame;
    }

    public void setKills(int kills){
        this.kills = kills;
    }

    public void setActiveGame(Game activeGame) {
        this.activeGame = activeGame;
    }

    public Player getBukkitPlayer(){
        for (Player pl : Bukkit.getOnlinePlayers()){
            if (pl.getUniqueId().toString().equalsIgnoreCase(uuid))return pl;
        }
        return null;
    }

}
