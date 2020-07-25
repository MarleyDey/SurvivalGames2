package org.minstrol.survivalgames.game;

import com.google.common.collect.Range;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.minstrol.survivalgames.SurvivalGames;
import org.minstrol.survivalgames.util.ConfigManager;
import org.minstrol.survivalgames.util.ParseConverter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class MapEnvironment {

    /**
     * This will restock all the chests in the game map
     *
     * @param game The game with the chests to restock
     */
    public static void RestockChests(Game game) {
        ConfigManager configManager = SurvivalGames.GetConfigManager();
        FileConfiguration config = configManager.getConfig();

        Location[] chestLocations = game.getChestLocations();
        Random random = new Random();

        int maxItems = config.getInt("chests.max-items-in-chest");
        int minItems = config.getInt("chests.min-items-in-chest");

        int randomItemAmount = ThreadLocalRandom.current().nextInt(minItems, maxItems + 1);

        int itemsSize = GetProbabilityMapSize();
        if (itemsSize < minItems) randomItemAmount = itemsSize;

        for (Location chestLocation : chestLocations) {
            if (!ConfirmBlock(chestLocation, Material.CHEST)) {
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "When loading chests the chest at (" +
                        chestLocation.getBlockX() + "," +
                        chestLocation.getBlockY() + "," +
                        chestLocation.getBlockZ() + ") was not found");

                continue;
            }

            Block block = null;

            try {
                block = chestLocation.getWorld().getBlockAt(chestLocation);
            } catch (NullPointerException ex) {
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Block at [" + ParseConverter.LocationToString(chestLocation) + "] was null!");
            }

            if (block == null) {
                continue;
            }

            if (block.getType().isAir() || !block.getType().equals(Material.CHEST)) {
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Block at [" + ParseConverter.LocationToString(chestLocation) + "] is no longer a chest!");
                continue;
            }

            Chest chest = (Chest) chestLocation.getWorld().getBlockAt(chestLocation).getState();
            Inventory chestInv = chest.getBlockInventory();

            chestInv.clear();

            //Add a random amount of items between the specific range from the config file
            for (int i = 0; i < randomItemAmount; i++) {

                //If lands on same item, tries up to 5 times to randomly pick another item instead
                for (int attempt = 0; attempt < 5; attempt++) {

                    ItemStack itemToAdd = GetRandomChestItem(random);

                    if (itemToAdd == null) {
                        Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Chests could not be restocked due to a config error!");
                        return;
                    }

                    if (chestInv.contains(itemToAdd.getType()))continue;

                    chestInv.addItem(itemToAdd);
                    break;
                }
            }
        }
    }

    private static ItemStack GetRandomChestItem(Random random){
        Map<ItemStack, Range<Integer>> itemProbabiltyMap = GetItemProbabilityMap();

        if (itemProbabiltyMap == null){
            Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " +"Something went wrong with the chest items list!");
            return null;
        }

        int randomIndex = random.nextInt(99) + 1;

        for (ItemStack is : itemProbabiltyMap.keySet()){
            //Check probability of item
            if (!itemProbabiltyMap.get(is).contains(randomIndex)) continue;
            return is;
        }
        return null;
    }

    private static Map<ItemStack, Range<Integer>> GetItemProbabilityMap(){
        Map<ItemStack, Range<Integer>> itemProbabilityMap = new HashMap<>();

        ConfigManager configManager = SurvivalGames.GetConfigManager();
        FileConfiguration config = configManager.getConfig();

        if (config.get("chests.items") == null){
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "Chests have no items specified to fill with!");
            return null;
        }

        Set<String> itemDetailsSelections = config.getConfigurationSection("chests.items").getKeys(false);

        int probabilityTotal = 0;

        for (String itemDetailsIndex : itemDetailsSelections){

            if (config.get("chests.items." + itemDetailsIndex + ".material") == null){
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Chest item [" + itemDetailsIndex + "] has no material specified!");
                return null;
            }

            if (config.get("chests.items." + itemDetailsIndex + ".amount") == null){
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Chest item [" + itemDetailsIndex + "] has no amount specified!");
                return null;
            }

            if (config.get("chests.items." + itemDetailsIndex + ".probability") == null){
                Bukkit.getLogger().log(Level.WARNING, "[SurvivalGames] " + "Chest item [" + itemDetailsIndex + "] has no probability specified!");
                return null;
            }

            String materialString = config.getString("chests.items." + itemDetailsIndex + ".material");
            Material material = Material.getMaterial(materialString.toUpperCase());

            if (material == null){
                Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] " + "Material " + materialString + " does not exist from the config!");
                return null;
            }

            String amountString = config.getString("chests.items." + itemDetailsIndex + ".amount");

            try {
                Integer.valueOf(amountString);
            } catch (NumberFormatException ex){
                Bukkit.getLogger().log(Level.SEVERE,  "[SurvivalGames] " + amountString + " is not a parsable amount number from the config!");
                return null;
            }

            String probabilityString = config.getString("chests.items." + itemDetailsIndex + ".probability");

            try {
                Integer.valueOf(probabilityString);
            } catch (NumberFormatException ex){
                Bukkit.getLogger().log(Level.SEVERE,  "[SurvivalGames] " + probabilityString + " is not a parsable probability number from the config!");
                return null;
            }

            int amount = Integer.valueOf(amountString);
            int probability = Integer.valueOf(probabilityString);

            Range<Integer> probRange = Range.closed(probabilityTotal += 1, probabilityTotal += (probability - 1));

            ItemStack is = new ItemStack(material, amount);

            if (config.get("chests.items." + itemDetailsIndex + ".enchantments") != null){
                ItemMeta im = is.getItemMeta();

                List<String> enchantmentStrings = config.getStringList("chests.items." + itemDetailsIndex + ".enchantments");
                for (String enchantmentDetails : enchantmentStrings){
                    String[] details = enchantmentDetails.split(";");

                    if (Enchantment.getByName(details[0].toUpperCase()) == null){
                        Bukkit.getLogger().log(Level.SEVERE,  "[SurvivalGames] " + details[0] + " is not a valid enchantment!");
                        continue;
                    }

                    try {
                        Integer.valueOf(details[1]);
                    } catch (NumberFormatException ex){
                        Bukkit.getLogger().log(Level.SEVERE,  "[SurvivalGames] " + details[1] + " is not a parsable enchantment level number from the config!");
                        continue;
                    }

                    im.addEnchant(Enchantment.getByName(details[0].toUpperCase()), Integer.valueOf(details[1]), true);
                }

                is.setItemMeta(im);
            }

            itemProbabilityMap.put(is, probRange);
        }

        if (probabilityTotal != 100){
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] When determining the items for chests, the probability of all the" +
                    " items didnt add up to 100, instead " + probabilityTotal + ".");
            return null;
        }

        return itemProbabilityMap;
    }

    private static int GetProbabilityMapSize(){
        FileConfiguration config = SurvivalGames.GetConfigManager().getConfig();

        if (config.get("chests.items") == null){
            Bukkit.getLogger().log(Level.SEVERE, "[SurvivalGames] Chest item list not found!");
            return -1;
        }

        return config
                .getConfigurationSection("chests.items").getKeys(false).size();
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
