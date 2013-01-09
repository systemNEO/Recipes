package de.systemNEO.recipes;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Results {
	
	public static void payResults(ItemStack[] craftStacks, ItemStack[][] recipeShape, Integer[][] slotMatrix, Integer amount, Inventory craftInventory) {
		
		int craftAmount = 0;
		int recipeAmount = 0;
		int rest = 0;
		int slot = 0;
		ItemStack craftStack; 
		
		// Jetzt, nachdem das Rezept gefunden wurde, checken, ob die Anzahl passt.
		for(int row = 0; row <= 2; ++row) {
			for(int col = 0; col <= 2; ++col) {
				
				slot = slotMatrix[row][col];
				if(slot == -1) continue;
				
				craftStack   = craftStacks[slot];
				craftAmount  = craftStack.getAmount();
				recipeAmount = recipeShape[row][col].getAmount();
				
				if(recipeAmount == 0) continue;
				
				rest = craftAmount - (recipeAmount * amount);
				
				if(rest <= 0) {
					craftInventory.setItem(slot, null);
				} else {
					craftInventory.setItem(slot, new ItemStack(craftStack.getTypeId(), rest, craftStack.getDurability()));
				}
			}
		}
	}
	
	public static void setRecipeResult(String group, String index, ItemStack result) {
		
		Constants.RECIPES_RESULT.put(group.toLowerCase() + "_" + index, result);
	}
	
	public static ItemStack getRecipeResult(String groupIndex) {
		
		return Constants.RECIPES_RESULT.get(groupIndex);
	}
	
	public static Integer calculateResults(ItemStack[] craftStacks, ItemStack[][] recipeShape, Integer[][] slotMatrix) {
		
		int minResult = 999;
		int foo = 0;
		int craftAmount = 0;
		int recipeAmount = 0;
		int slot;
		ItemStack craftStack;
		
		// Jetzt, nachdem das Rezept gefunden wurde, checken, ob die Anzahl passt.
		for(int row = 0; row <= 2; ++row) {
			for(int col = 0; col <= 2; ++col) {
				
				slot = slotMatrix[row][col];
				if(slot == -1) continue;
				
				craftStack = craftStacks[slot];
				
				craftAmount  = craftStack.getAmount();
				recipeAmount = recipeShape[row][col].getAmount();
				
				if(craftAmount == 0 || recipeAmount == 0) continue;
				
				foo = (int) Math.floor((craftAmount / recipeAmount));
				
				if(foo < minResult) minResult = foo;
				
				if(minResult == 0) return 0;
			}
		}
		
		// Default beruecksichtigen
		if(minResult == 999) return 0;
		
		return minResult;
	}
	
	public static Boolean setResultItem(ItemStack[][] craftShape, String[] userGroups, Inventory craftInventory, Player player, String type) {
		
		String craftIndex     = Shapes.shapeToString(craftShape);
		String recipeString   = Recipes.getRecipeAsString(userGroups, craftIndex);
		ItemStack resultStack = new ItemStack(Constants.AIR);
		
		if(recipeString != null && Recipes.getRecipeType(recipeString).equalsIgnoreCase(type)) {
			
			if(Shapes.compareShapes(Shapes.getRecipeShape(recipeString), craftShape)) {
				
				resultStack = Results.getRecipeResult(recipeString);
				
				// Am Spieler das Rezept merken, damit bei onCraft der ganze Kladderradatsch
				// nicht schon wieder berechnet werden muss.
				Utils.setMetadata(player, "currentRecipe", recipeString);
			}
			
			// Falls resultStack nicht gesetzt werden konnte, muss der Resultslot des Inventorys auf leer gesetzt werden,
			// da Rezepte einmalig sind und nicht mehrfach vorkommen koennen durfen pro Gruppe/Typ. Dabei wird immer
			// die Reihenfolge (vom Caller der aktuellen Funktion) FIXED -> VARIABLE -> FREE beachtet.
			craftInventory.setItem(0, resultStack);
			
			// Workaround: Inventory erzwungen updaten
			Inventories.updateInventoryScheduled(player, 1);
				
			return true;
		}
		
		return false;
	}
}
