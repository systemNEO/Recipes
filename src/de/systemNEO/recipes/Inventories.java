package de.systemNEO.recipes;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Inventories {
	
	/**
	 * Hinweis: Das Result des Inventory wird immer zurueckgesetzt, wenn der Server
	 * kein Original-Rezept findet, daher muss dies hier nicht zusaetzlich gemacht
	 * werden.
	 * @param player
	 */
	public static void updateInventory(Player player) {
		
		// Da nix gefunden wurde, am Spieler dies merken...
		Utils.setMetadata(player, "currentRecipe", null);
				
		// Rausfinden ob ein Rezept passt...
		Inventory craftInventory = player.getOpenInventory().getTopInventory();
		InventoryType invType    = craftInventory.getType();
		ItemStack[] craftStacks  = getCraftInventoryByType(craftInventory);
		
		// Sichergehen, dass was drin steht, ansonsten Abbruch
		if(craftStacks == null) return;
		
		String[] userGroups = PEXHelper.getUserGroups(player); 
		
		// 1. FIXED checken, ggf. bei Fund setzen und fertig (nur checken bei WORKBENCH)
		if(invType == InventoryType.WORKBENCH && Results.setResultItem(Shapes.getFixedShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_FIXED)) return;
		
		// 2. VARIABLE checken, ggf. bei Fund setzen und fertig
		if(Results.setResultItem(Shapes.getVariableShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_VARIABLE)) return;
		
		// 3. FREE checken, ggf. bei Fund setzen und fertig
		if(Results.setResultItem(Shapes.getFreeShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_FREE)) return;
		
		// Da nix gefunden wurde, am Spieler dies merken...
		Utils.setMetadata(player, "currentRecipe", null);
	}
	
	public static void updateInventoryScheduled(Player player, int ticks) {
		
		final Player tmpPlayer = player;
		
		Utils.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(Utils.getPlugin(), new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
		
				tmpPlayer.updateInventory();
			}
		}, ticks);
	}
	
	public static ItemStack[] getCraftInventoryByType(Inventory craftInventory) {
		
		InventoryType invType = craftInventory.getType();
		ItemStack[] craftStacks  = null;
		
		if (invType == InventoryType.WORKBENCH) {
			
			// 0 = Result, 1-9 = Craftingslots
			craftStacks = craftInventory.getContents();
			
		} else if (invType == InventoryType.CRAFTING) {
			
			// 0 = Result, 1-4 = Craftingslots, Rest auffuellen
			ItemStack[] stacks = craftInventory.getContents();
			craftStacks = new ItemStack[]{stacks[0],stacks[1],stacks[2],stacks[3],stacks[4],Constants.AIR,Constants.AIR,Constants.AIR,Constants.AIR,Constants.AIR};		
		}
		
		return craftStacks;
	}
}