package de.systemNEO.recipes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
		Constants.customRecipes = new HashMap<String, HashMap<Integer, ItemStack>>();
		Constants.RECIPES_ORIGINAL = new HashMap<String,ItemStack[]>();
		Constants.RECIPES_SHAPE = new HashMap<String,ItemStack[][]>();
		Constants.RECIPES_RESULT = new HashMap<String,ItemStack>();
		Constants.RECIPES_TYPE = new HashMap<String,String>();
		Constants.RECIPES_RESULTMESSAGE = new HashMap<String,String>(); 
		Constants.customConfig = null;
		
		Boolean loadRecipeConfigSuccessfull = true;
		Boolean recipesCreated = false;
		
		Utils.logInfo("Try to processing recipes from:");
		Utils.logInfo("" + Utils.getPlugin().getDataFolder() + "\\" + Constants.customConfigFileName);
		
		// Wenn die Rezepte spaeter Gruppen zugeordnet werden, kann man dem Index noch
		// den Gruppennamen zuordnen (Vorteil gleiches Basis-Rezept, andere Mengen als Input), 
		// oder in einer zweiten Tabelle das "Result" noch definieren,
		// wieviel sie da raus bekommen, was bei onCraft dann angepasst werden k�nnte (Vorteil
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
			ItemStack resultStack = Stacks.getItemStack(result, recipeKey, 0);
			if(resultStack == null) {
				Utils.prefixLog(recipeKey, "Material for recipe result not found. Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				continue;
			}
			
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
			
			// Optional: Nachricht fuer Rezept holen
			String resultMessage = recipeConfig.getString(recipeKey + ".resultMessage");
						
			// Ingredients ItemStack Liste erstellen, auf Position 0 ist das Result
			ArrayList<ItemStack> ingredientsStacks = new ArrayList<ItemStack>();
			ingredientsStacks.add(resultStack);
			
			int count = -1;
			boolean isRecipeCreated = false;
			
			for(String ingredient : ingredientList) {
				
				++count;
				
				ItemStack stack = Stacks.getItemStack(ingredient, recipeKey, count);
				
				if(stack == null) {
					
					Utils.prefixLog(recipeKey, "Material for ingredient '" + ingredient + "' on position " + count + " not found!");
					Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
					
					break;
				}
				
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
				resultMessage);
			
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