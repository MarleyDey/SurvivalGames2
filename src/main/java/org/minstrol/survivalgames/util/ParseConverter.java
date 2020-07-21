package org.minstrol.survivalgames.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.minstrol.survivalgames.players.SgPlayer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class ParseConverter {

    /**
     * This converts and parses a location to a string format
     *
     * @param location bukkit location
     *
     * @return The string version of the location
     */
    public static String LocationToString(Location location){
        return location.getWorld().getName() + ";" +
                location.getX() + ";" +
                location.getY() + ";" +
                location.getZ() + ";" +
                location.getPitch() + ";" +
                location.getYaw();
    }

    /**
     * This converts a string and parses it into a location instance
     *
     * @param locationString string of location
     *
     * @return bukkit location from string
     */
    public static Location StringToLocation(String locationString){
        String[] locStrs = locationString.split(";");

        try {
            return new Location(
                    Bukkit.getWorld(locStrs[0]),
                    Double.valueOf(locStrs[1]),
                    Double.valueOf(locStrs[2]),
                    Double.valueOf(locStrs[3]),
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

    /**
     * This converts a data instance into a string format using a simple date
     * format
     *
     * @param date Date to convert
     *
     * @return The string version of the date
     */
    public static String DateToString(Date date){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        return dateFormat.format(date);
    }


    /**
     * This converts a string into a date instance
     *
     * @param dateString The string version of a date
     *
     * @return The Date instance of the string
     */
    public static Date StringToDate(String dateString){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "String could not be parsed to date format", ex);
        }
        return null;
    }

    public static String[] StringListToArray(List<String> strings){
        String[] r = new String[strings.size()];

        for (int i = 0; i < strings.size(); i++){
            r[i] = strings.get(i);
        }

        return r;
    }

    public static Location[] LocationListToArray(List<Location> locations){
        Location[] r = new Location[locations.size()];

        for (int i = 0; i < locations.size(); i++){
            r[i] = locations.get(i);
        }

        return r;
    }

    public static SgPlayer[] SgPlayerListToArray(List<SgPlayer> players){
        SgPlayer[] r = new SgPlayer[players.size()];

        for (int i = 0; i < players.size(); i++){
            r[i] = players.get(i);
        }

        return r;
    }
}
