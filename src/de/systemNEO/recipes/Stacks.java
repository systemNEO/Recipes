package de.systemNEO.recipes;

import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.RUtils.Utils;

@SuppressWarnings("deprecation")
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
	
	/**
	 * @param stack
	 * 			Zu pruefender Stack.
	 * @return
	 * 			Liefert true, wenn der Stack gueltig und nicht vom Typ "Luft" ist,
	 * 			andernfalls false.
	 */
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
	 * @param ingredient
	 * 			String fuer Zutat im Format: "<ItemID>[:<SubID>][,<Anzahl>][%<Chance>]"
	 * @param recipeName 
	 * 			Name des Rezeptes zudem die Zutat gehoert.
	 * @param ingredientPos 
	 * 			Position der Zutat im Shape des Rezeptes. 
	 * @return
	 * 			Erzeugt aus einem String, wie "<ItemID>[:<SubID>][,<Anzahl>][%<Chance>]", einen ItemStack.
	 */
	public static ItemStack getItemStack(String ingredient, String recipeName, String ingredientPos) {
		
		Integer materialId = 0;
		Short subId = 0;
		Integer amount = 1;
		
		if(ingredient == null || ingredient.isEmpty()) ingredient = "0";
		
		String[] chance = ingredient.split("%");
		
		if(chance.length == 2) {
			
			if(!Chances.rememberValidatedChance(chance[1], recipeName, ingredientPos)) return null;
		}
		
		String[] materialAmount = chance[0].split(",");
		
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
			
			if(materialSubId[1].equals("*")) {
				
				// SubID bleibt 0, allerdings wird sich fuer das aktuelle Rezept und der Ingredient
				// Position gemerkt, dass als SubID eine WildCard gesetzt wurde.
				Stacks.setCustomStackMetadata("wildcard", true, recipeName, ingredientPos);
				
			} else {
				
				try {
					
					subId = Short.parseShort(materialSubId[1]);
					
				} catch (NumberFormatException ex) {
					
					Utils.logInfo("[" + recipeName + "] Wrong subid format on recipe position " + ingredientPos + "!");
					
					return null;
				}
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
		short subId = stack.getData().getData();
		
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
		
		if(!a.getItemMeta().hasDisplayName() && !b.getItemMeta().hasDisplayName()) return true;
		
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
	
	/**
	 * Merkt sich fuer einen Stack eines bestimmten Rezeptes an einer bestimmten Position
	 * den uebergebenen Wert.
	 * @param key
	 * 			Name des Argumentes/Wertes.
	 * @param value
	 * 			Zu merkender Wert.
	 * @param recipeName
	 * 			Rezeptname.
	 * @param ingredientPos
	 * 			Zutatenposition innerhalb des Rezeptes.
	 */
	public static void setCustomStackMetadata(String key, Object value, String recipeName, String ingredientPos) {
		
		String index = recipeName + "_" + ingredientPos + "_" + key;
		
		if(value == null) {
			
			if(Constants.CUSTOMSTACKMETADATA.containsKey(index)) Constants.CUSTOMSTACKMETADATA.remove(index);
			
			return;
		}
		
		Constants.CUSTOMSTACKMETADATA.put(index, value);
	}
	
	/**
	 * @param key
	 * 			Name des Argumentes/Wertes.
	 * @param recipeName
	 * 			Rezeptname.
	 * @param ingredientPos
	 * 			Zutatenposition innerhalb des Rezeptes.
	 * @return
	 * 			Liefert einen Wert fuer einen Stack eines bestimmten Rezeptes an einer
	 * 			bestimmten Position.
	 */
	public static Object getCustomStackMetadata(String key, String recipeName, String ingredientPos) {
		
		String index = recipeName + "_" + ingredientPos + "_" + key;
		
		if(Constants.CUSTOMSTACKMETADATA.containsKey(index)) return Constants.CUSTOMSTACKMETADATA.get(index);
		
		return null;
	}
}
