package org.minstrol.survivalgames.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.Collection;
import java.util.logging.Level;

public class MapEnvironment {

    /**
     * This will restock all the chests in the game map
     *
     * @param game The game with the chests to restock
     */
    public static void RestockChests(Game game) {
        Location[] chestLocations = game.getChestLocations();

        for (Location chestLocation : chestLocations) {
            if (!ConfirmBlock(chestLocation, Material.CHEST)) {
                Bukkit.getLogger().log(Level.WARNING, "When loading chests the chest at (" +
                        chestLocation.getBlockX() + "," +
                        chestLocation.getBlockY() + "," +
                        chestLocation.getBlockZ() + ") was not found");

                continue;
            }

            //Chest chest = (Chest) chestLocation.getWorld().getBlockAt(chestLocation);
            //TODO Restock the chest block

        }
    }

    /**
     * This will clear all the items on the ground of the arena of the survival game
     *
     * @param game Game instance to clear the map of
     */
    public static void ClearDroppedItems(Game game) {
        World world = game.getLobbyLocation().getWorld();
        int[] mapDim = game.getMapDimensions();

        //Iterate through every block in game map arena
        for (int x = mapDim[0]; x < mapDim[1]; x++) {
            for (int z = mapDim[4]; z < mapDim[5]; z++) {
                for (int y = mapDim[2]; y < mapDim[3]; y++) {
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
