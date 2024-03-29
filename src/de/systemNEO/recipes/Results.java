package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.RUtils.Utils;

public abstract class Results {
	
	/** Ubersetzungstabelle der 3x3 Slots zu 2x2 */
	private static HashMap<Integer,Integer> workbenchToCrafting_ = new HashMap<Integer,Integer>();
	static {
		workbenchToCrafting_.put(1, 1);
		workbenchToCrafting_.put(2, 2);
		workbenchToCrafting_.put(4, 3);
		workbenchToCrafting_.put(5, 4);		
	}
	
	@SuppressWarnings("deprecation")
	public static void payResults(ItemStack[] craftStacks, ItemStack[][] recipeShape, Integer[][] slotMatrix, Integer amount, Inventory craftInventory) {
		
		int craftAmount = 0;
		int recipeAmount = 0;
		int rest = 0;
		int slot = 0;
		int maxRow = 2; // 3x3 Grid
		int maxCol = 2; // 3x3 Grid
		boolean isCraftingGrid = craftInventory.getType().equals(InventoryType.CRAFTING);
		ItemStack craftStack; 
		
		if(isCraftingGrid) {
			
			maxRow = 1;
			maxCol = 1;	
		}
		
		// Jetzt, nachdem das Rezept gefunden wurde, checken, ob die Anzahl passt.
		for(int row = 0; row <= maxRow; ++row) {
			for(int col = 0; col <= maxCol; ++col) {
				
				slot = slotMatrix[row][col];
				if(slot == -1) continue;
				
				craftStack   = craftStacks[slot];
				craftAmount  = craftStack.getAmount();
				recipeAmount = recipeShape[row][col].getAmount();
				
				if(recipeAmount == 0) continue;
				
				rest = craftAmount - (recipeAmount * amount);
				
				// Uebersetzung fuer Crafting-Grid (2x2)
				if(isCraftingGrid) slot = workbenchToCrafting_.get(slot);
				
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
				
				// Gibt es ueberhaupt eine Anforderung?, wenn nicht skippen.
				if(recipeAmount == 0) continue;
				
				// Es gibt eine Anforderung, aber es war 0 drin, um einen error
				// DivisionByZero zu verhindern, gleich auf 0 setzen.
				if(craftAmount == 0) {
					
					foo = 0;
					
				} else {
				
					foo = (int) Math.floor((craftAmount / recipeAmount));
				}
				
				if(foo < minResult) minResult = foo;
				
				if(minResult == 0) return 0;
			}
		}
		
		// Default beruecksichtigen
		if(minResult == 999) return 0;
		
		return minResult;
	}
	
	public static Boolean setResultItemInCraftingInventory(ItemStack[][] craftShape, String[] userGroups, Inventory craftInventory, Player player, String type) {
		
		String craftIndex     = Shapes.shapeToString(craftShape);
		String recipeString   = Recipes.getRecipeAsString(userGroups, craftIndex);
		ItemStack resultStack = new ItemStack(Constants.AIR);
		
		if(recipeString != null) {
			
			String recipeType = Recipes.getRecipeType(recipeString);
			
			if(recipeType != null && recipeType.equalsIgnoreCase(type)) {
			
				if(Shapes.compareShapes(Shapes.getRecipeShape(recipeString), craftShape)) {
					
					resultStack = Results.getRecipeResult(recipeString);
					
					// Am Spieler das Rezept merken, damit bei onCraft der ganze Kladderradatsch
					// nicht schon wieder berechnet werden muss.
					Utils.setMetadata(player, "currentRecipe", recipeString);
				}
				
				craftInventory.setItem(0, resultStack);
				
				// Workaround: Inventory erzwungen updaten
				Inventories.updateCompleteInventoryScheduled(player, 1);
					
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Merkt sich zu einem Rezept (differzenziert nach Gruppe) moegliche Ueberreste eines Rezeptes.
	 * @param group
	 * 			Name der Gruppe.
	 * @param index
	 * 			In des Rezeptes.
	 * @param leavings
	 * 			Liste an Ueberresten.
	 */
	public static void setRecipeLeavings(String group, String index, ArrayList<ItemStack> leavings) {
		
		if(leavings == null) return;
		
		Constants.RECIPES_LEAVINGS.put(group.toLowerCase() + "_" + index, leavings);
	}
	
	/**
	 * @param groupIndex
	 * 			Gruppen-Index.
	 * @return
	 * 			Liefert leavings, falls vorhanden, andernfalls null.
	 */
	public static ArrayList<ItemStack> getRecipeLeavings(String groupIndex) {
		
		if(!Constants.RECIPES_LEAVINGS.containsKey(groupIndex)) return null;
		
		return Constants.RECIPES_LEAVINGS.get(groupIndex);
	}
	
	/**
	 * Gibt dem uebergebenen Spieler die evtl. vorhandenen Ueberreste eines Rezeptes ins
	 * Inventar.
	 * @param times
	 * 			Multiplyer fuer die leavings. Z. B. bei Shift-Click wird ja versucht soviel
	 * 			wie moeglich an ResultItems herzustellen, dies muss hier natuerlich
	 *			beruecksichtigt werden.
	 * @param recipeString
	 * 			Rezept-Index (inkl. Gruppe).
	 * @param player
	 * 			Spieler, dem Ueberreste gegeben werden sollen.
	 */
	public static void giveLeavings(Integer times, String recipeString, Player player) {
		
		if(times == null || times == 0) return;
		
		if(!Constants.RECIPES_LEAVINGS.containsKey(recipeString)) return;
		
		ArrayList<ItemStack> leavingStacks = Constants.RECIPES_LEAVINGS.get(recipeString);
		
		if(leavingStacks == null || leavingStacks.size() == 0) return;
		
		HashMap<Integer,ItemStack> itemsThatCannotGiveToInventory;
		ItemStack stack;
		int amountByChance;
		int pos = -1;
		
		for(ItemStack leavingItem : leavingStacks) {
			
			++pos;
			
			stack = leavingItem.clone();
			amountByChance = Chances.getLeaveAmountByChance((leavingItem.getAmount() * times), pos, recipeString);
			if(amountByChance == 0) continue;
			stack.setAmount(amountByChance);
			
			itemsThatCannotGiveToInventory = player.getInventory().addItem(stack);
			
			if(itemsThatCannotGiveToInventory.size() == 0) continue;
			
			for(ItemStack itemToDrop : itemsThatCannotGiveToInventory.values()) player.getWorld().dropItem(player.getLocation(), itemToDrop);
		}
	}
}
