package org.minstrol.survivalgames.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ConfigManager;

import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

public class MapEnvironment {

    /**
     * This will restock all the chests in the game map
     *
     * @param game The game with the chests to restock
     */
    public static void RestockChests(Game game) {
        Location[] chestLocations = game.getChestLocations();
        Random random = new Random();

        for (Location chestLocation : chestLocations) {
            if (!ConfirmBlock(chestLocation, Material.CHEST)) {
                Bukkit.getLogger().log(Level.WARNING, "When loading chests the chest at (" +
                        chestLocation.getBlockX() + "," +
                        chestLocation.getBlockY() + "," +
                        chestLocation.getBlockZ() + ") was not found");

                continue;
            }

            Chest chest = (Chest) chestLocation.getWorld().getBlockAt(chestLocation);
            Inventory chestInv = chest.getBlockInventory();

            Map<ItemStack, Double> itemProbabiltyMap = GetItemProbabilityMap();

            //TODO Restock the chest block

        }
    }

    private static Map<ItemStack, Double> GetItemProbabilityMap(){
        Map<ItemStack, Double> itemProbabilityMap = new HashMap<>();

        ConfigManager configManager = SurvivalGames.GetConfigManager();
        FileConfiguration config = configManager.getConfig();

        if (config.get("chests.items") == null){
            Bukkit.getLogger().log(Level.SEVERE, "Chests have no items specified to fill with!");
            return null;
        }

        List<String> itemProbStrings = config.getStringList("chests.items");
        double probabilityTotal = 0;

        for (String itemProb : itemProbStrings){
            String[] itemProbSplit = itemProb.split(";");

            Material material = Material.getMaterial(itemProbSplit[0].toUpperCase());

            if (material == null){
                Bukkit.getLogger().log(Level.SEVERE, "Material " + itemProbSplit[0] + " does not exist from the config!");
                return null;
            }

            try {
                Integer.valueOf(itemProbSplit[1]);
            } catch (NumberFormatException ex){
                Bukkit.getLogger().log(Level.SEVERE,  itemProbSplit[1] + " is not a parsable amount number from the config!");
                return null;
            }

            try {
                Double.valueOf(itemProbSplit[2]);
            } catch (NumberFormatException ex){
                Bukkit.getLogger().log(Level.SEVERE,  itemProbSplit[1] + " is not a parsable probability number from the config!");
                return null;
            }

            int amount = Integer.valueOf(itemProbSplit[1]);
            double probability = Double.valueOf(itemProbSplit[2]);

            probabilityTotal += probability;

            itemProbabilityMap.put(new ItemStack(material, amount), probability);
        }

        if (probabilityTotal != 100){
            Bukkit.getLogger().log(Level.SEVERE, "When determining the items for chests, the probability of all the" +
                    " items didnt add up to 100, instead " + probabilityTotal + ".");
            return null;
        }

        return itemProbabilityMap;
    }

    /**
     * This will clear all the items on the ground of the arena of the survival game
     *
     * @param game Game instance to clear the map of
     */
    public static void ClearDroppedItems(Game game) {
        World world = game.getLobbyLocation().getWorld();
        int[] dimensions = game.getMapDimensions();

        //Iterate through every block in game map arena
        int lx, ux, ly, uy, lz, uz;

        //Upper and lower of x dimension
        lx = Math.min(dimensions[0], dimensions[1]);
        ux = Math.max(dimensions[0], dimensions[1]);

        //Upper and lower of y dimension
        ly = Math.min(dimensions[2], dimensions[3]);
        uy = Math.max(dimensions[2], dimensions[3]);

        //Upper and lower of z dimension
        lz = Math.min(dimensions[4], dimensions[5]);
        uz = Math.max(dimensions[4], dimensions[5]);

        int chestAmount = 1;

        //X dimension
        for (int x = lx; x < ux; x++) {

            //Y dimension
            for (int y = ly; y < uy; y++) {

                //Z dimension
                for (int z = lz; z < uz; z++) {
                    Location loc = new Location(world, x + 0.5, y + 0.5, z + 0.5);

                    //Gets entites within the block, no added radius.
                    Collection<Entity> entities = world.getNearbyEntities(loc, 0.5, 0.5, 0.5);
                    for (Entity entity : entities) {
                        if (!entity.isOnGround()) continue;
                        entity.remove();
                    }
                }
            }
        }
    }

    /**
     * This confirms a blocks material and existence
     *
     * @param location Location of block
     * @param material The material of the block
     * @return Whether the block is confirmed
     */
    private static boolean ConfirmBlock(Location location, Material material) {
        if (location == null) return false;
        if (material == null) return false;

        World world = location.getWorld();
        Block block = world.getBlockAt(location);

        return block.getType() == material;
    }
}
