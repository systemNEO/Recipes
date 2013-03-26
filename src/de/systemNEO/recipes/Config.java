package de.systemNEO.recipes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.systemNEO.recipes.API.KSideHelper;
import de.systemNEO.recipes.RDrops.RDropItem;
import de.systemNEO.recipes.RDrops.RDrops;

public abstract class Config {
	
	public static void getDropRecipe(FileConfiguration recipeConfig, String recipeKey, ArrayList<String> groups) {
		
		Object by = null;
		ArrayList<RDropItem> drops = new ArrayList<RDropItem>();
		
		if(recipeConfig.isString(recipeKey + ".entity")) {
			
			String entityName = recipeConfig.getString(recipeKey + ".entity");
			by = EntityType.fromName(entityName);
			
			if(by == null) {
				
				Utils.prefixLog(recipeKey, "Entity for recipe not found. Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				return;
			}
			
		} else if(recipeConfig.isString(recipeKey + ".block")) {
			
			by = Stacks.getItemStack(recipeConfig.getString(recipeKey + ".block"), recipeKey, "block");
			
			if(by == null) {
				
				Utils.prefixLog(recipeKey, "Block for recipe not found. Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				return;
			}
		
		} else {
			
			Utils.prefixLog(recipeKey, "Please set a block or entity!");
			Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
			return;
		}
		
		if(recipeConfig.isList(recipeKey + ".drops")) {
			
			List<String> dropItems = recipeConfig.getStringList(recipeKey + ".drops");
			int pos = 0;
			
			for(String dropItem : dropItems) {
				
				++pos;
				
				ItemStack dropStack = Stacks.getItemStack(dropItem, recipeKey, "drop");
				
				if(dropStack == null) {
					Utils.prefixLog(recipeKey, "Material for drop on position " + pos + " not found. Recipe skipped because errors.");
					Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
					return;
				}
				
				RDropItem rDropItem = new RDropItem(dropStack, Chances.getChance(recipeKey + "_drop"));
				drops.add(rDropItem);
			}	
		}
		
		if(RDrops.addDropRecipe(drops, groups, by)) {
			Utils.prefixLog(recipeKey, Constants.MESSAGE_OK);
		} else {
			Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
		}
	}
	
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
		Constants.RECIPES_ALIAS.clear();
		Constants.CUSTOMSTACKMETADATA.clear();
		Constants.customConfig = null;
		RDrops.reset();
		
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
			
			// Typ holen fixed, variable (default), free
			String shapeType = recipeConfig.getString(recipeKey + ".type");
			if(shapeType == null || shapeType.equals("") || !java.util.Arrays.asList(Constants.SHAPE_TYPES).contains(shapeType.toLowerCase())) {
				shapeType = Constants.SHAPE_DEFAULT;
			}
			
			// Gruppen holen
			ArrayList<String> groups = new ArrayList<String>();
			if(recipeConfig.isList(recipeKey + ".groups")) groups.addAll(recipeConfig.getStringList(recipeKey + ".groups"));
				
			// Kingdoms holen
			if(recipeConfig.isList(recipeKey + ".kingdoms")) {
				
				ArrayList<String> kingdoms = new ArrayList<String>(recipeConfig.getStringList(recipeKey + ".kingdoms"));
				
				for(String kingdomName : kingdoms) groups.add(KSideHelper.getGroupPrefix() + kingdomName);
			}
			
			// Sicherstellen, dass groups existiert
			if(groups == null || groups.size() == 0) {
				groups = new ArrayList<String>();
				groups.add(Constants.GROUP_GLOBAL.toLowerCase());
			}
			
			if(shapeType.equalsIgnoreCase(Constants.SHAPE_DROP)) {
				
				getDropRecipe(recipeConfig, recipeKey, groups);
				
				continue;
			}
			
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
			
			// Resultchance ermitteln (wurde in Stacks.getItemStack gesetzt).
			resultChance = Chances.getChance(recipeKey + "_result");
			
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
				
				// Optionale Enchantments
				// http://jd.bukkit.org/rb/apidocs/org/bukkit/enchantments/Enchantment.html
				List<String> resultEnchants = recipeConfig.getStringList(recipeKey + ".enchantments");
				if(resultEnchants != null && resultEnchants.size() > 0) {
					
					String enchantEntry;
					String[] enchantNameAndPower;
					String enchantName;
					Integer enchantPower;
					Enchantment enchant;
					
					for(int i = 0; i < resultEnchants.size(); ++i) {
						
						enchantEntry = resultEnchants.get(i);
						
						if(enchantEntry == null || enchantEntry.isEmpty()) continue;
						
						enchantNameAndPower = enchantEntry.split(" ");
						
						if(enchantNameAndPower.length != 2) {
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because invalid defenition.&r"));
							
							continue;
						}
						
						enchantName = enchantNameAndPower[0];
						enchant = Enchantment.getByName(enchantName.toUpperCase());
						if(enchant == null) {
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because enchantment named " + enchantName + " not found!&r"));
							
							continue;
						}
						
						if(!enchant.canEnchantItem(resultStack)) {
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because result item can not enchanted with " + enchantName + "!&r"));
							
							continue;
						}
						
						ItemMeta resultMeta = resultStack.getItemMeta();
						boolean errorFound = false;
						
						if(resultMeta.hasEnchants()) {
							
							Map<Enchantment, Integer> existingEnchants = resultMeta.getEnchants();
							
							for(Enchantment existingEnchant : existingEnchants.keySet()) {
								
								if(existingEnchant.getName().equalsIgnoreCase(enchant.getName())) {
									
									Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because result item allready has enchantment " + enchantName + "!&r"));
									
									errorFound = true;

									break;
								}
								
								if(enchant.conflictsWith(existingEnchant)) {
									
									Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because conflicts with existing enchantment " + existingEnchant.getName() + "!&r"));
									
									errorFound = true;

									break;
								}
							}
						}
						
						if(errorFound) continue;
						
						enchantPower = Utils.parseInt(enchantNameAndPower[1], null);
						
						if(enchantPower == null) {
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Enchantment " + i + " skipped because enchantment level must be a number!&r"));
							
							continue;
						}
						
						if(enchantPower < enchant.getStartLevel()) {
							
							enchantPower = enchant.getStartLevel();
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Note: Enchantment level of enchantment " + enchant.getName() + " was set to minimum level " + enchant.getStartLevel() + ".&r"));
						}
						
						if(enchantPower > enchant.getMaxLevel()) {
							
							enchantPower = enchant.getMaxLevel();
							
							Utils.prefixLog(recipeKey, Utils.getANSIColors("&6Note: Enchantment level of enchantment " + enchant.getName() + " was set to maximum level " + enchant.getMaxLevel() + ".&r"));
						}
						
						resultMeta.addEnchant(enchant, enchantPower, true);
						resultStack.setItemMeta(resultMeta);
					}
				}
			}
			
			// Optional: Nachricht fuer Rezept holen
			String resultMessage = recipeConfig.getString(recipeKey + ".resultMessage");
			
			// "Ueberreste" / Leavings
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
					
					// Resultchance ermitteln (wurde in Stacks.getItemStack gesetzt)
					leaveChance = Chances.getChance(recipeKey + "_" + leavePos);
					
					if(leavingStack == null) {
						Utils.prefixLog(recipeKey, "&6Material for leaving item " + (i + 1) + " not found. Leaving item skipped.&r");
						continue;
					}
					
					leavingsStacks.add(leavingStack);
					leavingsChance.add(leaveChance);
				}
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
			
			// Ingredients ItemStack Liste erstellen, auf Position 0 ist das Result
			ArrayList<ItemStack> ingredientsStacks = new ArrayList<ItemStack>();
			ingredientsStacks.add(resultStack);
			
			int count = 0;
			boolean isRecipeCreated = false;
			ArrayList<String> isDoubleIngredient = new ArrayList<String>();
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
				
				if(shapeType.equals(Constants.SHAPE_FREE) && stack.getTypeId() != 0 && isDoubleIngredient.contains(stackAsString)) {
					
					Utils.prefixLog(recipeKey, "&6Duplicate ingredient '&f" + ingredient + "&6' on position &f" + count + "&6!");
					Utils.prefixLog(recipeKey, "Note: Duplicate ingredients are not allowed for recipes of type 'free'.");
					
					break;
				}
				
				isDoubleIngredient.add(stackAsString);
				
				ingredientsStacks.add(stack);
			}
			
			if(ingredientsStacks.size() != 10) {
				
				Utils.prefixLog(recipeKey, "Recipe skipped because errors.");
				Utils.prefixLog(recipeKey, Constants.MESSAGE_FAILED);
				
				continue;
			}
			
			// Nun noch eine AliasMap fuer die SubIds = * Wildcards erstellen
			// und mitgeben. Dabei beachten, dass 0 = Result ist, und 1 - 9
			// die Ingredients.
			Boolean isWildcard;
			boolean hasAlias = false;
			StringBuilder shapeAlias = new StringBuilder();
			String finalShapeAlias = null;

			for(int i = 1; i <= 9; ++i) {
				
				if(i > 1) shapeAlias.append(",");
				
				if((isWildcard = (Boolean) Stacks.getCustomStackMetadata("wildcard", recipeKey, "ingredient " + i)) == null || !isWildcard) {
					
					shapeAlias.append(Stacks.stackToString(ingredientsStacks.get(i)));
					
				} else {
					
					shapeAlias.append(Utils.formatTypeId(ingredientsStacks.get(i).getTypeId()) + ":(\\d{0,4})");
					
					hasAlias = true;
				}
			}
			
			if(hasAlias) finalShapeAlias = shapeAlias.toString();
			
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
				leavingsChance,
				finalShapeAlias);
			
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
