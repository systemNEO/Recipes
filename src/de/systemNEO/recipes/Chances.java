package de.systemNEO.recipes;

import org.bukkit.entity.Player;

public abstract class Chances {
	
	/**
	 * Merkt sich eine Zahl (Chance) in einem temporaeren Register, welche mit getChance() solange
	 * abgerufen werden kann, bis rememberValidatedChance erneut aufgerufen wird.
	 * @param inputChance
	 * 			Chance als String.
	 * @param recipeName
	 * 			Rezeptname.
	 * @param ingredientPos
	 * 			Zutaten-Position.
	 * @return
	 * 			Liefert true, wenn aus dem String eine valide Chance geparst werden konnte,
	 * 			andernfalls false.
	 */
	public static boolean rememberValidatedChance(String inputChance, String recipeName, String ingredientPos) {
		
		String index = recipeName + "_" + ingredientPos;
		Integer percent = 100; 
		
		if(Constants.CHANCES.containsKey(index)) Constants.CHANCES.remove(index);
		
		try {
			
			percent = Integer.parseInt(inputChance);
			
		} catch (NumberFormatException ex) {
			
			if(recipeName != null && ingredientPos != null) Utils.logInfo("[" + recipeName + "] Wrong chance format on recipe position " + ingredientPos + "! Must be a value between 1 and 100.");
			
			return false;
		}
		
		if(percent < 1 || percent > 100) {
			
			if(recipeName != null && ingredientPos != null)  Utils.logInfo("[" + recipeName + "] Wrong chance value on recipe position " + ingredientPos + "! Must be a value between 1 and 100.");
		
			return false;
		}
		
		Constants.CHANCES.put(index, percent);
		
		return true;
	}
	
	/**
	 * @param index
	 * 			Index des Wertes.
	 * @return
	 * 			Liefert die zuletzt ueber rememberValidatedChance und dem uebergebenen index gespeicherte
	 * 			Zahl (die Chance), andernfalls 100.
	 */
	public static Integer getChance(String index) {
		
		if(Constants.CHANCES.containsKey(index)) return Constants.CHANCES.get(index);
		
		return 100;
	}
	
	/**
	 * @param amount
	 * @param groupIndex
	 * @param player
	 * @return
	 * 			Liefert die Anzahl berechnet an Hand der Chancenhoehe aus ueber
	 * 			groupIndex ermittelter Chance...
	 */
	public static Integer getAmountByChance(Integer amount, String groupIndex, Player player) {
		
		int resultAmount = 0;
		
		if(amount == 0) return 0;
		
		Integer chance = Recipes.getRecipeResultChance(groupIndex);
		
		if(chance == null || chance >= 100) return amount;
		
		for(int i = 1; i <= amount; ++i) {
			
			if((Math.random() * 100) <= chance) ++resultAmount;
		}
		
		if(player != null) {
			
			if(resultAmount == 0) {
				Utils.playerMessage(player, "&7The luck has left you! Everything has been broken.");
			} else if (resultAmount < amount) {
				Utils.playerMessage(player, "&7It seems that " + (amount - resultAmount) + " items are broken during crafting.");
			}
		}
		
		return resultAmount;
	}
}
