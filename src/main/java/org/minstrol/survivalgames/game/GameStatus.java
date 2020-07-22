package org.minstrol.survivalgames.game;

import org.bukkit.ChatColor;

public enum GameStatus {
    STOPPED("Stopped", ChatColor.RED + "Stopped", 1, "This game has been stopped!"),
    WAITING("Waiting", ChatColor.GREEN + "Waiting...", 2, "This game is waiting for more players!"),
    STARTING("Starting", ChatColor.YELLOW + "Starting...", 3, "This game is currently starting!"),
    INGAME("In-Game", ChatColor.RED + "In-Game", 4, "This game is currently in-game!"),
    RESETTING("Resetting", ChatColor.YELLOW + "Resetting...", 5, "This game is currently resetting the map!");

    private String name, formattedName, description;
    private int code;

    GameStatus(String name, String formattedName, int code, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.formattedName = formattedName;
    }

    public String getName() {
        return name;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}
