package de.systemNEO.recipes;

import org.bukkit.inventory.ItemStack;

public abstract class Stacks {
	
	public static ItemStack[][] getDefaultStack() {
		
		// Default setzen
		ItemStack[][] shape = {
			{Constants.AIR, Constants.AIR, Constants.AIR},
			{Constants.AIR, Constants.AIR, Constants.AIR},
			{Constants.AIR, Constants.AIR, Constants.AIR}
		};
		
		return shape;
	}
	
	public static Boolean isStack(ItemStack stack) {
		
		if(stack == null || stack.getTypeId() == 0 || stack.getAmount() == 0) return false;
		
		return true;
	}
	
	public static Boolean[] getHasMat(ItemStack[] stacks) {
		
		Boolean[] isMat = new Boolean[10];
		
		for(int i = 1; i <= 9; ++i) isMat[i] = Stacks.isStack(stacks[i]);
		
		return isMat;
	}
	
	/**
	 * Erzeugt aus einem String, wie "<ItemID>[:<SubID>][,<Anzahl>]", einen ItemStack.
	 * @param ingredient String fuer Zutat im Format: "<ItemID>[:<SubID>][,<Anzahl>]"
	 * @param recipeName Name des Rezeptes zudem die Zutat gehoert.
	 * @param ingredientPos Position der Zutat im Shape des Rezeptes. 
	 * @return
	 */
	public static ItemStack getItemStack(String ingredient, String recipeName, String ingredientPos) {
		
		Integer materialId = 0;
		Short subId = 0;
		Integer amount = 1;
		
		if(ingredient == null || ingredient.isEmpty()) ingredient = "0";
		
		String[] materialAmount = ingredient.split(",");
		
		if(materialAmount.length == 2) {
			
			try {
				
				amount = Integer.parseInt(materialAmount[1]);
				
			} catch (NumberFormatException ex) {
				
				Utils.logInfo("[" + recipeName + "] Wrong amount format on recipe position " + ingredientPos + "!");
				
				return null;
			}
			
			// Sicherstellen, dass das Amount nicht 0 ist, sonst gleich unendlich
			if(amount < 1) amount = 1;
		}
		
		String[] materialSubId = materialAmount[0].split(":");
		
		if(materialSubId.length == 2) {
			
			try {
				
				subId = Short.parseShort(materialSubId[1]);
				
			} catch (NumberFormatException ex) {
				
				Utils.logInfo("[" + recipeName + "] Wrong subid format on recipe position " + ingredientPos + "!");
				
				return null;
			}
		}
		
		try {
			
			materialId = Integer.parseInt(materialSubId[0]);
			
		} catch (NumberFormatException ex) {
			
			Utils.logInfo("[" + recipeName + "] Wrong material id format on recipe position " + ingredientPos + "!");
			
			return null;
		}
		
		// Bei Luft, muss das Amount 0 sein
		if(materialId == 0) amount = 0;
		
		ItemStack stack = new ItemStack(materialId, amount, subId);
		
		if(!Stacks.stackExistsInMinecraft(stack)) {
			
			Utils.logInfo("[" + recipeName + "] Not in Minecraft existing material id on recipe position " + ingredientPos + "!");
			
			return null;
		}
		
		return stack;
	}
	
	public static String getStackString(int typeId, short subId) {
		
		return Utils.formatTypeId(typeId) + ":" + Utils.formatSubId(subId);
	}

	public static String stackToString(ItemStack stack) {
		
		int typeId = stack.getTypeId();
		short subId = stack.getDurability();
		if(subId < 0) subId = 0;
		
		return Stacks.getStackString(typeId, subId);
	}
	
	/**
	 * Aktuelle Recipes Regeln zum Vergleichen:
	 * - ItemID (TypeId)
	 * - SubID (Durability)
	 * - Displayname (ItemMeta :: DisplayName)
	 * 
	 * @param a
	 * 			Der mit b zu vergleichende ItemStack.
	 * @param b
	 * 			Der mit a zu vergleichende ItemStack.
	 * @return
	 * 			Liefert true, wenn die ItemStacks nach den Recipes-Regeln identisch sind.
	 */
	public static boolean compareStacks(ItemStack a, ItemStack b) {
		
		if(a.getTypeId() != b.getTypeId()) return false;
		
		if(a.getDurability() != b.getDurability()) return false;
		
		if(a.getItemMeta().hasDisplayName() != b.getItemMeta().hasDisplayName()) return false;
		
		if(!a.getItemMeta().getDisplayName().equals(b.getItemMeta().getDisplayName())) return false;
		
		return true;
	}
	
	/**
	 * @param stack
	 * 			Der zu validierende ItemStack.
	 * @return
	 * 			Liefert true, wenn der Stack in Minecraft als Item auch wirklich vorkommt,
	 * 			andernfalls false.
	 */
	public static boolean stackExistsInMinecraft(ItemStack stack) {
		
		if(stack == null) return false;
		
		if(stack.getTypeId() != stack.getData().getItemTypeId()) return false;
		
		return  true;
	}
}
