package org.minstrol.survivalgames.players.inventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryHold{

    private Map<Integer, ItemStack> itemsMap;
    private String playerName;
    private int size;

    public InventoryHold(String playerName){
        this.playerName = playerName;
    }

    public void storeInventory(Inventory inventory){
        Map<Integer, ItemStack> itemsstackMap = new HashMap<>();

        this.size = inventory.getSize();

        for (int i = 0; i < size; i++){
            ItemStack is = inventory.getItem(i);

            itemsstackMap.put(i, is);
        }

        itemsMap = itemsstackMap;
    }

    public void retrieveInventory(Player player){
        if (itemsMap == null) return;

        Inventory inventory = player.getInventory();
        for (int i = 0; i < size; i++){
            ItemStack is = itemsMap.get(i);
            inventory.setItem(i, is);
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public boolean equals(Object o){
        if (!(o instanceof InventoryHold))return false;

        InventoryHold other = (InventoryHold) o;
        if (!this.playerName.equals(other.getPlayerName()))return false;
        return true;
    }
}
