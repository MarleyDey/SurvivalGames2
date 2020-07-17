package org.minstrol.survivalgames.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.logging.Level;

public class ParseConverter {

    public static String LocationToString(Location location){
        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getPitch() + ";" +
                location.getYaw();
    }

    public static Location StringToLocation(String locationString){
        String[] locStrs = locationString.split(";");

        try {
            return new Location(
                    Bukkit.getWorld(locStrs[0]),
                    Integer.getInteger(locStrs[1]),
                    Integer.getInteger(locStrs[2]),
                    Integer.getInteger(locStrs[3]),
                    Float.valueOf(locStrs[4]),
                    Float.valueOf(locStrs[5]));
        } catch (Exception ex){
            Bukkit.getLogger().log(Level.SEVERE, "String could not be parsed into a location " +
                    "(world:" + locStrs[0] +
                    " x:" + locStrs[1] + "" +
                    " y:" + locStrs[2] + "" +
                    " z:" + locStrs[3] + "" +
                    " pitch:" + locStrs[4] + "" +
                    " yaw:" + locStrs[5], ex);
            return null;
        }
    }
}
