package de.systemNEO.recipes;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.API.KSideHelper;
import de.systemNEO.recipes.API.PEXHelper;

public abstract class Inventories {
	
	/**
	 * Hinweis: Das Result des Inventory wird immer zurueckgesetzt, wenn der Server
	 * kein Original-Rezept findet, daher muss dies hier nicht zusaetzlich gemacht
	 * werden.
	 * @param player
	 */
	public static void updateInventory(Player player) {
		
		// Lagprotection wieder aufheben
		Utils.setMetadata(player, "lagLock", false);
		
		// Da nix gefunden wurde, am Spieler dies merken...
		Utils.setMetadata(player, "currentRecipe", null);
				
		// Rausfinden ob ein Rezept passt...
		Inventory craftInventory = player.getOpenInventory().getTopInventory();
		InventoryType invType    = craftInventory.getType();
		ItemStack[] craftStacks  = getCraftInventoryByType(craftInventory);
		
		// Sichergehen, dass was drin steht, ansonsten Abbruch
		if(craftStacks == null) return;
		
		// Permission-EX Nutzergruppen holen
		String[] userGroups = PEXHelper.getUserGroups(player);
		
		if(KSideHelper.isPlugin()) {
			
			String kingdomName = KSideHelper.getPlayersKingdom(player);
			
			if(kingdomName != null && !kingdomName.isEmpty()) {
				
				kingdomName = KSideHelper.getGroupPrefix() + kingdomName;
				
				if(userGroups.length > 0) {
					
					userGroups = (kingdomName + " " + StringUtils.join(userGroups, " ")).split(" ");
				
				} else {
					
					userGroups = new String[1];
					userGroups[0] = kingdomName;
				}				
			}	
		}
		
		// 1. FIXED checken, ggf. bei Fund setzen und fertig (nur checken bei WORKBENCH)
		if(invType == InventoryType.WORKBENCH && Results.setResultItemInCraftingInventory(Shapes.getFixedShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_FIXED)) return;
		
		// 2. VARIABLE checken, ggf. bei Fund setzen und fertig
		if(Results.setResultItemInCraftingInventory(Shapes.getVariableShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_VARIABLE)) return;
		
		// 3. FREE checken, ggf. bei Fund setzen und fertig
		if(Results.setResultItemInCraftingInventory(Shapes.getFreeShape(craftStacks), userGroups, craftInventory, player, Constants.SHAPE_FREE)) return;
		
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
			craftStacks = new ItemStack[]{stacks[0],stacks[1],stacks[2],Constants.AIR,stacks[3],stacks[4],Constants.AIR,Constants.AIR,Constants.AIR,Constants.AIR};		
		}
		
		return craftStacks;
	}
}
