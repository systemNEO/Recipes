package de.systemNEO.recipes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Config {
	
	public static boolean loadRecipeConfig() {
		
		// Fuer moeglichen Reload vorbereiten.
		Constants.customRecipes.clear();
		Constants.RECIPES_ORIGINAL.clear();
		Constants.RECIPES_SHAPE.clear();
		Constants.RECIPES_RESULT.clear();
		Constants.RECIPES_TYPE.clear();
		Constants.RECIPES_RESULTMESSAGE.clear();
		Constants.RECIPES_LEAVINGS.clear();
		Constants.CHANCES.clear();
		Constants.RECIPES_RESULTCHANCE.clear();
		Constants.RECIPES_LEAVINGSCHANCE.clear();
		Constants.customConfig = null;
		
		Boolean loadRecipeConfigSuccessfull = true;
		Boolean recipesCreated = false;
		Integer resultChance = 100;
		ArrayList<Integer> leavingsChance = new ArrayList<Integer>();
		Integer leaveChance;
		String leavePos;
		ItemStack leavingStack;
		
		Utils.logInfo("Try to processing recipes from:");
		Utils.logInfo("" + Utils.getPlugin().getDataFolder() + "\\" + Constants.customConfigFileName);
		
		// Wenn die Rezepte spaeter Gruppen zugeordnet werden, kann man dem Index noch
		// den Gruppennamen zuordnen (Vorteil gleiches Basis-Rezept, andere Mengen als Input), 
		// oder in einer zweiten Tabelle das "Result" noch definieren,
		// wieviel sie da raus bekommen, was bei onCraft dann angepasst werden könnte (Vorteil
		// nur ein Rezept, aber mehrere Ergebnisse). Beide Loesungen implementieren!
		FileConfiguration recipeConfig = Config.getCustomConfig();
		
		Set<String> recipeKeys = recipeConfig.getKeys(false);
		
		for (String recipeKey : recipeKeys) {
			
			// Result holen
			String result = recipeConfig.getString(recipeKey + ".result");
			if(result == null || result.equals("")) {
				Utils.prefixLog(recipeKey, "Recipe result not found.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
			// Resultstring in ITEM umwandeln.
			ItemStack resultStack = Stacks.getItemStack(result, recipeKey, "result");
			if(resultStack == null) {
				Utils.prefixLog(recipeKey, "Material for recipe result not found. Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
			// Resultchance ermitteln.
			resultChance = Chances.getChance(recipeKey + "_result");
			
			// Typ holen fixed, variable (default), free
			String shapeType = recipeConfig.getString(recipeKey + ".type");
			if(shapeType == null || shapeType.equals("") || !java.util.Arrays.asList(Constants.SHAPE_TYPES).contains(shapeType.toLowerCase())) {
				shapeType = Constants.SHAPE_DEFAULT;
			}
			
			// Gruppen holen
			List<String> rawGroups = recipeConfig.getStringList(recipeKey + ".groups");
			ArrayList<String> groups = new ArrayList<String>();
				
			for(String group : rawGroups) groups.add(group);
			
			// Bevor der Eintrag der Config als Rezept behandelt wird, vorab checken
			// ob der type ggf. "remove" ist und ein result angegeben ist, wenn
			// ja, dann kann das Rezept erst einmal komplett entfernt werden.
			if(shapeType.equals(Constants.SHAPE_REMOVE)) {
				
				if(Recipes.removeRecipe(resultStack, groups)) {
					Utils.prefixLog(recipeKey, Constants.MESSAGE_OK);
				} else {
					Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				}
				
				continue;
			}
			
			// Materialien fuer Shape holen
			String ingredients = recipeConfig.getString(recipeKey + ".ingredients");
			if(ingredients == null || ingredients.equals("")) {
				Utils.prefixLog(recipeKey, "Recipe ingredients not found.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
			// Shapes bestehen erst einmal aus immer 9 Feldern, auch fuer das 2x2 Crafting Grid werden
			// die Rezepte in diesem Raster angegeben.
			String[] ingredientList = ingredients.split(" ");
			if(ingredientList == null || ingredientList.length != 9) {
				Utils.prefixLog(recipeKey, "A recipe shape consists of 9 slots (currently used "+ingredientList.length+").");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
			// Hinweis: Auf "Luft" kann man keinen Displaynamen oder Lore setzen.
			if(resultStack.getTypeId() != 0) {
				
				// Optionalen Displaynamen fuer das Result holen
				String resultName = recipeConfig.getString(recipeKey + ".resultName");
				if(resultName != null && !resultName.isEmpty()) {
				
					// Da Resultname gefunden, den im ResultItem setzen
					ItemMeta resultMeta = resultStack.getItemMeta();
					resultMeta.setDisplayName(Utils.getChatColors(resultName + "&r"));
					resultStack.setItemMeta(resultMeta);
				}
				
				// Optionale Lore holen
				List<String> resultLores = recipeConfig.getStringList(recipeKey + ".lore");
				if(resultLores != null && resultLores.size() > 0) {
					
					String stringLore;
					
					for(int i = 0; i < resultLores.size(); ++i) {
						
						stringLore = resultLores.get(i);
						
						if(stringLore == null || stringLore.isEmpty()) continue;
						
						resultLores.set(i, Utils.getChatColors("&r" + stringLore + "&r"));
					}
					
					// Da Lore gefunden wurde, die im ResultItem setzen
					ItemMeta resultMeta = resultStack.getItemMeta();
					resultMeta.setLore(resultLores);
					resultStack.setItemMeta(resultMeta);
				}
			}
			
			// Optional: Nachricht fuer Rezept holen
			String resultMessage = recipeConfig.getString(recipeKey + ".resultMessage");
			
			// Ueberreste
			ArrayList<ItemStack> leavingsStacks = new ArrayList<ItemStack>();
			leavingsChance.clear();
			
			if(recipeConfig.isList(recipeKey + ".leavings")) {
				
				List<String> leavingsList = recipeConfig.getStringList(recipeKey + ".leavings");
				String leavingItem;
				
				for(int i = 0; i < leavingsList.size(); i++) {
					
					leavingItem = leavingsList.get(i);
					
					if(leavingItem == null || leavingItem.isEmpty()) {
						Utils.prefixLog(recipeKey, "&6Leaving item " + (i + 1) + " skipped because empty.&r");
						continue;
					}
					
					leavePos = "leaving list item " + (i + 1);
					leavingStack = Stacks.getItemStack(leavingItem, recipeKey, leavePos);
					
					// Resultchance ermitteln.
					leaveChance = Chances.getChance(leavePos);
					
					if(leavingStack == null) {
						Utils.prefixLog(recipeKey, "&6Material for leaving item " + (i + 1) + " not found. Leaving item skipped.&r");
						continue;
					}
					
					leavingsStacks.add(leavingStack);
					leavingsChance.add(leaveChance);
				}
			}
			
			// Ingredients ItemStack Liste erstellen, auf Position 0 ist das Result
			ArrayList<ItemStack> ingredientsStacks = new ArrayList<ItemStack>();
			ingredientsStacks.add(resultStack);
			
			int count = -1;
			boolean isRecipeCreated = false;
			ArrayList<String> isDoubeIngredient = new ArrayList<String>();
			String stackAsString;
			
			for(String ingredient : ingredientList) {
				
				++count;
				
				ItemStack stack = Stacks.getItemStack(ingredient, recipeKey, "ingredient " + count);
				
				if(stack == null) {
					
					Utils.prefixLog(recipeKey, "Material for ingredient '" + ingredient + "' on position " + count + " not found!");
					
					break;
				}
				
				// Wieder in vereinheitlichten formatierten String zurueckwandeln, damit der Double-Check
				// sauber laeuft.
				stackAsString = Stacks.stackToString(stack);
				
				if(shapeType.equals(Constants.SHAPE_FREE) && stack.getTypeId() != 0 && isDoubeIngredient.contains(stackAsString)) {
					
					Utils.prefixLog(recipeKey, "&6Duplicate ingredient '&f" + ingredient + "&6' on position &f" + count + "&6!");
					Utils.prefixLog(recipeKey, "Note: Duplicate ingredients are not allowed for recipes of type 'free'.");
					
					break;
				}
				
				isDoubeIngredient.add(stackAsString);
				
				ingredientsStacks.add(stack);
			}
			
			if(ingredientsStacks.size() != 10) {
				
				Utils.prefixLog(recipeKey, "Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				
				continue;
			}
			
			isRecipeCreated = Recipes.createCustomRecipe(new ItemStack[]{
					ingredientsStacks.get(0),
					ingredientsStacks.get(1),
					ingredientsStacks.get(2),
					ingredientsStacks.get(3),
					ingredientsStacks.get(4),
					ingredientsStacks.get(5),
					ingredientsStacks.get(6),
					ingredientsStacks.get(7),
					ingredientsStacks.get(8),
					ingredientsStacks.get(9),
				},
				groups,
				shapeType,
				resultMessage,
				resultChance,
				leavingsStacks,
				leavingsChance);
			
			if(!isRecipeCreated) {
				
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
			Utils.prefixLog(recipeKey, Constants.MESSAGE_OK);
			
			recipesCreated = true;
		}
		
		if (!recipesCreated) {
			
			Utils.logInfo("No valid recipes found.");
			Utils.logInfo("Check " + Constants.customConfigFile + ", please.");
			
			loadRecipeConfigSuccessfull = false;
		}
		 
		return loadRecipeConfigSuccessfull;
	}

	public static void reloadCustomConfig() {
		
		if(Constants.customConfigFile == null) Constants.customConfigFile = new File(Utils.getPlugin().getDataFolder(), Constants.customConfigFileName);
		
		Constants.customConfig = YamlConfiguration.loadConfiguration(Constants.customConfigFile);
		
		// Look for defaults in the jar
	    InputStream defConfigStream = Utils.getPlugin().getResource("recipes.yml");
	    
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        Constants.customConfig.setDefaults(defConfig);
	    }
	    
	    // Defaultdatei anlegen
	    if(!Constants.customConfigFile.exists()) {
	    	saveCustomConfig();
	    	Utils.logInfo("Recipe configuration was initally created in:");
	    	Utils.logInfo("" + Constants.customConfigFile);
	    	Utils.logInfo("You can now edit the file and configure your custom recipes.");
	    }
	}
	
	public static FileConfiguration getCustomConfig() {
		
		if(Constants.customConfig == null) reloadCustomConfig();
		
		return Constants.customConfig;
	}
	
	public static void saveCustomConfig() {
		
		if(Constants.customConfig == null || Constants.customConfigFile == null) return;
		
		try {
			
			getCustomConfig().save(Constants.customConfigFile);
			
		} catch (IOException ex) {
			
			Utils.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + Constants.customConfigFile, ex);
		}
	}
}
