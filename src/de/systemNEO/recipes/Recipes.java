package de.systemNEO.recipes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * TODO Standard Rezepte integrieren (brauch kein onInventoryClick usw, weil es Standardrezepte sind)
 * TODO Standard Rezepe "entfernen" per Result, ggf. neue YML-Datei als Configuration dafuer
 *      benutzen. (Derzeit dadurch loesbar das Rezept zu definieren und als Ergebnis AIR zurueck
 *      zu geben :D
 * TODO Standard Rezepte nur fuer bestimmte Nutzergruppen freigeben, bei dem prepareCraftEvent
 *      dann einfach pruefen ob das Standard-Rezept in der Gruppe erlaubt ist.
 * TODO Wenn ein Rezept "global" ueberschrieben wird, dann kann es von den Original-Rezepten auch
 *      entfernt werden. Hierbei darauf achten, dass bei einem Reload die Rezepte wieder hergestellt
 *      werden muessen. Damit wuerde der Fall umgangen, dass es doppelte Rezepte gibt, usw.
 * TODO Wenn ein custom Rezept keine Zutaten hat, die Amount = 1 sind, dann koennte man diese Rezepte
 *      ueber die internen Rezepte von Bukkit loesen, das waere ggf. Performance-sparender. Wichtig
 *      waere hier nur, dass das Result genauso manipulierbar ist, wie das des Plugins.
 * @author Hape
 * 
 */
public final class Recipes extends JavaPlugin implements Listener {

	public HashMap<String, HashMap<Integer, ItemStack>> customRecipes = new HashMap<String, HashMap<Integer, ItemStack>>();

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static final HashMap<String,ChatColor> CHATCOLORS = new HashMap<String,ChatColor>();
	static {
		CHATCOLORS.put("&0", ChatColor.BLACK);
		CHATCOLORS.put("&1", ChatColor.DARK_BLUE);
		CHATCOLORS.put("&2", ChatColor.DARK_GREEN);
		CHATCOLORS.put("&3", ChatColor.DARK_AQUA);
		CHATCOLORS.put("&4", ChatColor.DARK_RED);
		CHATCOLORS.put("&5", ChatColor.DARK_PURPLE);
		CHATCOLORS.put("&6", ChatColor.GOLD);
		CHATCOLORS.put("&7", ChatColor.GRAY);
		CHATCOLORS.put("&8", ChatColor.DARK_GRAY);
		CHATCOLORS.put("&9", ChatColor.BLUE);
		CHATCOLORS.put("&a", ChatColor.GREEN);
		CHATCOLORS.put("&b", ChatColor.AQUA);
		CHATCOLORS.put("&c", ChatColor.RED);
		CHATCOLORS.put("&d", ChatColor.LIGHT_PURPLE);
		CHATCOLORS.put("&e", ChatColor.YELLOW);
		CHATCOLORS.put("&f", ChatColor.WHITE);
		CHATCOLORS.put("&r", ChatColor.RESET);
	}
	
	public static final String GROUP_GLOBAL   = "_GLOBAL_";

	public static final String SHAPE_FIXED    = "fixed";
	public static final String SHAPE_VARIABLE = "variable";
	public static final String SHAPE_FREE     = "free";              // Bei freien Rezepten darf jede Zutat nur einmal vorkommen!
	public static final String SHAPE_DEFAULT  = SHAPE_VARIABLE;
	public static final String[] SHAPE_TYPES  = new String[] {SHAPE_FIXED, SHAPE_VARIABLE, SHAPE_FREE};
	
	public static HashMap<String,ItemStack[]> RECIPES_ORIGINAL = new HashMap<String,ItemStack[]>();
	public static HashMap<String,ItemStack[][]> RECIPES_SHAPE = new HashMap<String,ItemStack[][]>();
	public static HashMap<String,ItemStack> RECIPES_RESULT = new HashMap<String,ItemStack>();
	public static HashMap<String,String> RECIPES_TYPE = new HashMap<String,String>();
	public static HashMap<String,String> RECIPES_RESULTMESSAGE = new HashMap<String,String>(); 
	
	public static final String MESSAGE_FAILED = ANSI_RED + "FAILED" + ANSI_RESET;
	public static final String MESSAGE_OK = ANSI_GREEN + "OK" + ANSI_RESET;
	
	public static final ItemStack AIR = new ItemStack(Material.AIR);	
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private String customConfigFileName = "recipes.yml";
	
	
	
	@Override
	public void onEnable() {
		
		/*Iterator<org.bukkit.inventory.Recipe> recipes = Bukkit.recipeIterator();
		org.bukkit.inventory.Recipe recipe;

		while(recipes.hasNext()) {
			
			if((recipe = recipes.next()) != null) {
				this.getLogger().info("..." + recipe.getResult().getTypeId() + ":" + recipe.getResult().getDurability());
			}		
		}*/
		
		// Plugins checken
		if(!checkPlugins()) {
			
			onEnableError("Plugin stopped. Check plugin dependencies, please.");
			
			return;
		}
		
		// Config einlesen und Rezepte erstellen
		if(!loadRecipeConfig()) {
			
			onEnableError("Plugin stopped. Check error messages above, please.");
			
			return;
		}
		
		// Events der Klasse jetzt erst registrieren, da es Rezepte gibt und keine Fehler
		// vorliegen.
		getServer().getPluginManager().registerEvents(this, this);
		
		// TADAAA :)
		this.getLogger().info(ANSI_GREEN + "Successfully loaded." + ANSI_RESET);
	}

	public boolean loadRecipeConfig() {
		
		// Fuer moeglichen Reload vorbereiten.
		customRecipes = new HashMap<String, HashMap<Integer, ItemStack>>();
		RECIPES_ORIGINAL = new HashMap<String,ItemStack[]>();
		RECIPES_SHAPE = new HashMap<String,ItemStack[][]>();
		RECIPES_RESULT = new HashMap<String,ItemStack>();
		RECIPES_TYPE = new HashMap<String,String>();
		RECIPES_RESULTMESSAGE = new HashMap<String,String>(); 
		customConfig = null;
		
		Boolean loadRecipeConfigSuccessfull = true;
		Boolean recipesCreated = false;
		
		this.getLogger().info("Try to processing recipes from:");
		this.getLogger().info("" + getDataFolder() + "\\" + customConfigFileName);
		
		// Wenn die Rezepte spaeter Gruppen zugeordnet werden, kann man dem Index noch
		// den Gruppennamen zuordnen (Vorteil gleiches Basis-Rezept, andere Mengen als Input), 
		// oder in einer zweiten Tabelle das "Result" noch definieren,
		// wieviel sie da raus bekommen, was bei onCraft dann angepasst werden könnte (Vorteil
		// nur ein Rezept, aber mehrere Ergebnisse). Beide Loesungen implementieren!
		FileConfiguration recipeConfig = this.getCustomConfig();
		
		Set<String> recipeKeys = recipeConfig.getKeys(false);
		
		for (String recipeKey : recipeKeys) {
			
			// Materialien fuer Shape holen
			String ingredients = recipeConfig.getString(recipeKey + ".ingredients");
			if(ingredients == null || ingredients.equals("")) {
				prefixLog(recipeKey, "Recipe ingredients not found.");
				prefixLog(recipeKey, MESSAGE_FAILED);
				continue;
			}
			
			// Shapes bestehen erst einmal aus immer 9 Feldern, auch fuer das 2x2 Crafting Grid werden
			// die Rezepte in diesem Raster angegeben.
			String[] ingredientList = ingredients.split(" ");
			if(ingredientList == null || ingredientList.length != 9) {
				prefixLog(recipeKey, "A recipe shape consists of 9 slots (currently used "+ingredientList.length+").");
				prefixLog(recipeKey, MESSAGE_FAILED);
				continue;
			}
			
			// Result holen
			String result = recipeConfig.getString(recipeKey + ".result");
			if(result == null || result.equals("")) {
				prefixLog(recipeKey, "Recipe result not found.");
				prefixLog(recipeKey, MESSAGE_FAILED);
				continue;
			}
			
			// Resultstring in ITEM umwandeln.
			ItemStack resultStack = getItemStack(result, recipeKey, 0);
			if(resultStack == null) {
				prefixLog(recipeKey, "Material for recipe result not found. Recipe skipped because errors.");
				prefixLog(recipeKey, MESSAGE_FAILED);
				continue;
			}
			
			// Optionalen Displaynamen fuer das Result holen
			String resultName = recipeConfig.getString(recipeKey + ".resultName");
			if(resultName != null && !resultName.isEmpty()) {
			
				// Da Resultname gefunden, den im ResultItem setzen
				ItemMeta resultMeta = resultStack.getItemMeta();
				resultMeta.setDisplayName(getColoredMessage("&r" + resultName + "&r"));
				resultStack.setItemMeta(resultMeta);
			}
			
			// Optionale Lore holen
			List<String> resultLores = recipeConfig.getStringList(recipeKey + ".lore");
			if(resultLores != null && resultLores.size() > 0) {
				
				String stringLore;
				
				for(int i = 0; i < resultLores.size(); ++i) {
					
					stringLore = resultLores.get(i);
					
					if(stringLore == null || stringLore.isEmpty()) continue;
					
					resultLores.set(i, getColoredMessage("&r" + stringLore + "&r"));
				}
				
				// Da Lore gefunden wurde, die im ResultItem setzen
				ItemMeta resultMeta = resultStack.getItemMeta();
				resultMeta.setLore(resultLores);
				resultStack.setItemMeta(resultMeta);
			}
			
			// Gruppen holen
			List<String> rawGroups = recipeConfig.getStringList(recipeKey + ".groups");
			ArrayList<String> groups = new ArrayList<String>();
				
			for(String group : rawGroups) groups.add(group);
			
			// Typ holen fixed, variable (default), free
			String shapeType = recipeConfig.getString(recipeKey + ".type");
			if(shapeType == null || shapeType.equals("") || !java.util.Arrays.asList(SHAPE_TYPES).contains(shapeType.toLowerCase())) {
				shapeType = SHAPE_DEFAULT;
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
				
				ItemStack stack = getItemStack(ingredient, recipeKey, count);
				
				if(stack == null) {
					
					prefixLog(recipeKey, "Material for ingredient '" + ingredient + "' on position " + count + " not found!");
					prefixLog(recipeKey, MESSAGE_FAILED);
					
					break;
				}
				
				ingredientsStacks.add(stack);
			}
			
			if(ingredientsStacks.size() != 10) {
				
				prefixLog(recipeKey, "Recipe skipped because errors.");
				prefixLog(recipeKey, MESSAGE_FAILED);
				
				continue;
			}
			
			isRecipeCreated = createCustomRecipe(new ItemStack[]{
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
				
				prefixLog(recipeKey, MESSAGE_FAILED);
				continue;
			}
			
			prefixLog(recipeKey, MESSAGE_OK);
			
			recipesCreated = true;
		}
		
		if (!recipesCreated) {
			
			this.getLogger().info("No valid recipes found.");
			this.getLogger().info("Check " + customConfigFile + ", please.");
			
			loadRecipeConfigSuccessfull = false;
		}
		 
		return loadRecipeConfigSuccessfull;
	}
	
	public void prefixLog(String prefix, String message) {
		
		this.getLogger().info("[" + prefix + "] " + message + ANSI_RESET);
	}
	
	public void onEnableError(String errorMessage) {
		
		this.getLogger().warning(ANSI_RED + errorMessage + ANSI_RESET);
	}
	
	public Boolean checkPlugins() {
		
		// Pruefen ob benoetigte Plugins vorhanden sind
		Boolean pluginSuccessfullyLoaded = true;
				
		this.getLogger().info("Checking required plugins for Recipes:");
		
		// PermissionsEX
		// API: https://github.com/PEXPlugins/PermissionsEx/wiki/Native-API-example#wiki-pex-api-example
		if(getServer().getPluginManager().getPlugin("PermissionsEx") == null) {
			this.getLogger().warning("PermissionsEX - " + ANSI_RED + "FAILED" + ANSI_RESET + " (Plugin is required)");
			this.getLogger().info("Download: http://webbukkit.org/jenkins/packages/PermissionsEx/");
			this.getLogger().info("WIKI: http://github.com/PEXPlugins/PermissionsEx/wiki");
			this.getLogger().info("@Bukkit: http://dev.bukkit.org/server-mods/permissionsex/");
			this.getLogger().info("Note: This version of Recipes was coded with PermissionsEX v1.19.5");
			pluginSuccessfullyLoaded = false;
		} else {
			this.getLogger().info("PermissionsEX - " + ANSI_GREEN + "OK" + ANSI_RESET);
		}
		
		return pluginSuccessfullyLoaded;
	}
	
	/**
	 * Erzeugt aus einem String, wie "<ItemID>[:<SubID>][,<Anzahl>]", einen ItemStack.
	 * @param ingredient String fuer Zutat im Format: "<ItemID>[:<SubID>][,<Anzahl>]"
	 * @param recipeName Name des Rezeptes zudem die Zutat gehoert.
	 * @param ingredientPos Position der Zutat im Shape des Rezeptes. 
	 * @return
	 */
	public ItemStack getItemStack(String ingredient, String recipeName, int ingredientPos) {
		
		Integer materialId = 0;
		Short subId = 0;
		Integer amount = 1;
		
		if(ingredient == null || ingredient.isEmpty()) ingredient = "0";
		
		String[] materialAmount = ingredient.split(",");
		
		if(materialAmount.length == 2) {
			
			try {
				
				amount = Integer.parseInt(materialAmount[1]);
				
			} catch (NumberFormatException ex) {
				
				this.getLogger().info("[" + recipeName + "] Wrong amount format on recipe position " + ingredientPos + "!");
				
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
				
				this.getLogger().info("[" + recipeName + "] Wrong subid format on recipe position " + ingredientPos + "!");
				
				return null;
			}
		}
		
		try {
			
			materialId = Integer.parseInt(materialSubId[0]);
			
		} catch (NumberFormatException ex) {
			
			this.getLogger().info("[" + recipeName + "] Wrong material id format on recipe position " + ingredientPos + "!");
			
			return null;
		}
		
		// Bei Luft, muss das Amount 0 sein
		if(materialId == 0) amount = 0;
		
		return new ItemStack(materialId, amount, subId);
	}

	/**
	 * Erstellt ein neues Rezept.
	 * @param stacks 1 Resultstack plus 9 ItemStacks, je Position im Rezept.
	 * @param pexGroup Einschraenkung auf Gruppe.
	 */
	public boolean createCustomRecipe(ItemStack[] stacks, ArrayList<String> groups, String type, String resultMessage) {
		
		// Nix gescheites in der ItemStack[]-Liste, dann Abbruch.
		if(stacks == null || stacks.length < 10) return false;
		
		// Sicherstellen, dass groups existiert
		if(groups == null || groups.size() == 0) {
			groups = new ArrayList<String>();
			groups.add(GROUP_GLOBAL);
		}
		
		// Sicherstellen, dass der korrekte Typ an die Funktion uebergeben wurde.
		if(type == null) type = SHAPE_DEFAULT;
		
		// Den Shape nun vereinfachen, je nach Typ.
		// FIXED: Bleibt wie er ist.
		// VARIABLE: Die Form wird in die linke obere Ecke der Shapematrix verschoben
		// FREE: Die Items werden ihrer ID:SUBID nach sortiert in eine Reihe angeordnet.
		ItemStack[][] shape = null;
		
		if(type.equals(SHAPE_FIXED)) shape = getFixedShape(stacks);
		
		if(type.equals(SHAPE_VARIABLE)) shape = getVariableShape(stacks);
		
		if(type.equals(SHAPE_FREE)) shape = getFreeShape(stacks);
		
		// Wenn nix Sinnvolles dabei rauskamm, Abbruch.
		if(shape == null) return false;
		
		// Jetzt haben wir einen optimierten Shape und das Ursprungsrezept. Jetzt muss das Rezept
		// gespeichert werden, sowie die Gruppenzuordnung.
		// Beim Speichern muss darauf geachtet werden, dass Rezepte nicht doppelt je Gruppe vorkommen
		// duerfen, jedes Rezept muss je Gruppe einzigartig sein.
		// Zudem muss das Rezept je Typ abgespeichert werden und auch darin einzigartig sein.
		// [GRUPPE][TYP][REZEPT] = ...
		// Da ein Rezept mehreren Gruppen zugeoehren kann oder global sein kann, wuerden so Rezepte
		// doppelt hinterlegt, besser waehre es ein Rezept zu holen und zu schauen, ob es fuer
		// die Gruppe erlaubt ist, dies wiederum ist Problematisch, wenn das selbe Rezept mit
		// anderen Amounts in anderen Gruppen existiert. Im Grunde waere das okay, dann muesste man
		// aus der Gruppe die jeweils richtige Version holen. Hm, alles kompliziert, gehen wir
		// erstmal den dreckigen Weg ;)
		
		// String aus dem Shape erstellen (da der Shape auch Informationen fuer die Anzahl
		// enthaelt kann er schlecht als vergleichspattern dienen, deshalb der String).
		String shapeIndex =  shapeToString(shape);
		
		// Unter diesem String muessen jetzt mehrere Werte abgespeichert werden:
		// 1. Das Original-Rezept
		// 2. Das Result
		// 3. Der Shape
		// 4. Der Typ
		for(String group : groups) setRecipe(group, shapeIndex, stacks, type, shape, resultMessage);
		
		// Alles okay
		return true;
	}
	
	/**
	 * Speichert Original-Rezept, Result, Shape und Typ des Rezeptes passend zu einer Gruppe.
	 * @param group
	 * @param index
	 * @param stacks
	 * @param type
	 * @param shape
	 */
	public void setRecipe(String group, String index, ItemStack[] stacks, String type, ItemStack[][] shape, String resultMessage) {
	
		setRecipeOriginal(group, index, stacks);
		setRecipeType(group, index, type);
		setRecipeResult(group, index, stacks[0]);
		setRecipeShape(group, index, shape);
		setRecipeResultMessage(group, index, resultMessage);
	}
	
	public void setRecipeResultMessage(String group, String index, String resultMessage) {
		
		RECIPES_RESULTMESSAGE.put(group.toLowerCase() + "_" + index, resultMessage);
	}
	
	public String getRecipeResultMessage(String groupIndex) {
		
		return RECIPES_RESULTMESSAGE.get(groupIndex);
	}
	
	public void setRecipeOriginal(String group, String index, ItemStack[] stacks) {
		
		RECIPES_ORIGINAL.put(group.toLowerCase() + "_" + index, stacks);
	}
	
	public ItemStack[] getRecipeOriginal(String groupIndex) {
		
		return RECIPES_ORIGINAL.get(groupIndex);
	}
	
	public void setRecipeType(String group, String index, String type) {
		
		RECIPES_TYPE.put(group.toLowerCase() + "_" + index, type);
	}
	
	public String getRecipeType(String groupIndex) {
		
		return RECIPES_TYPE.get(groupIndex);
	}
	
	public void setRecipeResult(String group, String index, ItemStack result) {
		
		RECIPES_RESULT.put(group.toLowerCase() + "_" + index, result);
	}
	
	public ItemStack getRecipeResult(String groupIndex) {
		
		return RECIPES_RESULT.get(groupIndex);
	}
	
	public void setRecipeShape(String group, String index, ItemStack[][] shape) {
		
		RECIPES_SHAPE.put(group.toLowerCase() + "_" + index, shape);
	}
	
	public ItemStack[][] getRecipeShape(String groupIndex) {
		
		return RECIPES_SHAPE.get(groupIndex);
	}
	
	public String getRecipeAsString(String[] groups, String index) {
		
		for(String group : groups) {
			
			if(isRecipe(group, index)) return group.toLowerCase() + "_" + index;
		}
		
		return null;
	}
	
	public Boolean isRecipe(String group, String index) {
		
		return RECIPES_TYPE.containsKey(group.toLowerCase() + "_" + index);
	}
	
	/**
	 * 
	 * @param shape
	 * @return
	 */
	public String shapeToString(ItemStack[][] shape) {
		
		StringBuilder shapeString = new StringBuilder();
		
		for(int row = 0; row <= 2; ++row) {
		
			for(int column = 0; column <= 2; ++column) {
				
				if(shapeString.length() > 0) shapeString.append(",");
				shapeString.append(stackToString(shape[row][column]));
			}
		}
		
		return shapeString.toString();
	}
	
	public String stackToString(ItemStack stack) {
		
		int typeId = stack.getTypeId();
		short subId = stack.getDurability();
		if(subId < 0) subId = 0;
		
		return getStackString(typeId, subId);
	}
	
	public ItemStack[][] getFixedShape(ItemStack[] stacks) {
		
		ItemStack[][] shape = {
				{stacks[1],stacks[2],stacks[3]},
				{stacks[4],stacks[5],stacks[6]},
				{stacks[7],stacks[8],stacks[9]}
		};
		
		return shape;
	}
	
	public ItemStack[][] getVariableShape(ItemStack[] stacks) {
		
		// Um Resourcen zu sparen, Werte der Stacks einmal merken.
		Boolean[] isMat = getHasMat(stacks);
		
		// Start und Ende der Rezepte berechnen, leere Zeilen sollen
		// moeglichst ignoriert werden.
		int startCount = getStartCount(isMat);
		int endCount   = getEndCount(isMat);
		
		// Default setzen
		ItemStack[][] shape = getDefaultStack();
		
		//Startwerte fuer Zeile und Spalte initieren
		int row = 0;
		int column = 0;
		
		for(int i = startCount; i <= endCount; ++i) {
			
			shape[row][column] = stacks[i];
			
			++column;
			
			// Letzte Spalte erreicht? Dann auf erste zuruecksetzen und naechste Zeile setzen.
			if(column == 3) {
				column = 0;
				++row;
			}
		}
		
		return shape;
	}
	
	public ItemStack[][] getFreeShape(ItemStack[] stacks) {
		
		ItemStack[][] shape = getDefaultStack();
		
		List<String> liste = new ArrayList<String>();
		HashMap<String,ItemStack> merker = new HashMap<String,ItemStack>();
		
		short subId = 0;
		String stackString = "";
		
		for (int i = 1; i <= 9; ++i) {
		
			if(!isStack(stacks[i])) continue;
			
			subId = stacks[i].getDurability();
			if(subId < 0) subId = 0;
			
			stackString = getStackString(stacks[i].getTypeId(), subId) + "-" + i;
			
			merker.put(stackString, stacks[i]);
			liste.add(stackString);
		}
		
		Collections.sort(liste);
		
		int row = 0;
		int column = 0;
		
		for(String index : liste) {
			
			shape[row][column] = merker.get(index);
			
			++column;
			
			// Letzte Spalte erreicht? Dann auf erste zuruecksetzen und naechste Zeile setzen.
			if(column == 3) {
				column = 0;
				++row;
			}
		}
		
		return shape;
	}
	
	public Integer getStartCount(Boolean[] isMat) {
		
		int startCount = 1;
		
		// Erste Zeile leer?
		if(!isMat[1] && !isMat[2] && !isMat[3]) {
			
			startCount = startCount + 3;
			
			// Mittlere Zeile leer (aber nur checken, wenn die Erste schon leer war)?
			if(!isMat[4] && !isMat[5] && !isMat[6]) startCount = startCount + 3;
		}
		
		// Erste Spalte leer?
		if(!isMat[1] && !isMat[4] && !isMat[7]) {
			
			startCount = startCount + 1;
			
			// Mittlere Spalte leer (aber nur checken, wenn die Erste schon leer war)?
			if(!isMat[2] && !isMat[5] && !isMat[8]) startCount = startCount + 1;
		}
		
		return startCount;
	}
	
	public Integer getEndCount(Boolean[] isMat) {
		
		int endCount = 9;
		
		// Letzte Zeile leer?
		if(!isMat[7] && !isMat[8] && !isMat[9]) {
			
			endCount = endCount - 3;
			
			// Mittlere Zeile leer (aber nur checken, wenn die Letzte schon leer war)?
			if(!isMat[4] && !isMat[5] && !isMat[6]) endCount = endCount - 3;
		}
		
		// Letzte Spalte leer?
		if(!isMat[3] && !isMat[6] && !isMat[9]) {
			
			endCount = endCount - 1;
			
			// Mittlere Spalte leer (aber nur checken, wenn die Letzte schon leer war)?
			if(!isMat[2] && !isMat[5] && !isMat[8]) endCount = endCount - 1;
		}
		
		return endCount;
	}
	
	public Boolean[] getHasMat(ItemStack[] stacks) {
		
		Boolean[] isMat = new Boolean[10];
		
		for(int i = 1; i <= 9; ++i) isMat[i] = isStack(stacks[i]);
		
		return isMat;
	}
	
	public ItemStack[][] getDefaultStack() {
		
		// Default setzen
		ItemStack[][] shape = {
			{AIR, AIR, AIR},
			{AIR, AIR, AIR},
			{AIR, AIR, AIR}
		};
		
		return shape;
	}
	
	public Boolean isStack(ItemStack stack) {
		
		if(stack == null || stack.getTypeId() == 0 || stack.getAmount() == 0) return false;
		
		return true;
	}
	
	public StringBuilder getFreeShapeString(StringBuilder foo, int count, ItemStack[] stacks) {
		
		/*
		 * 1. Alle TypeIDs:SubIds muessen zu einer Zahl umgewandelt werden, z. B. 5:4 zu 54, 54:0 zu 540,
		 *    usw., dabei beachten, dass die SubId 2 stellig sein kann, daher einer 4 eine 0 vorangestellt
		 *    werden muss
		 */
		List<String> liste = new ArrayList<String>();
		short subId = 0;
		int typeId = 0;
		int skipCount = 0;
		
		for (ItemStack stack : stacks) {
		
			++skipCount;
			
			if(skipCount <= count) continue;
			
			typeId = stack.getTypeId();
			
			// Leere Stacks interessieren uns nicht...
			if(typeId == 0 || stack.getAmount() == 0) continue;
			
			subId = stack.getDurability();
			if(subId < 0) subId = 0;
			
			liste.add(getStackString(typeId, subId));
		}
		
		Collections.sort(liste);
		
		for(String index : liste) {
			
			if (foo.length() > 0) foo = foo.append(",");
			foo.append(index);
		}
		
		return foo;
	}
	
	public StringBuilder getVariableShapeString(StringBuilder foo, int count, ItemStack[] stacks) {
		
		/*
		 * Um die Zutatenvorgabe gedanklich nach links oben zu verschieben erstmal ein Array aufbauen.
		 * 
		 * 0 0 0    1 1 0    Wenn spaeter mal das 2x2 Craftingfeld gecheckt wird, dann muesste man
		 * 0 1 1 -> 1 1 0 -> die restlichen Slots auffuellen.
		 * 0 1 1    0 0 0
		 */
		Boolean[] isMat = new Boolean[9];
		int e = 0;
		int endCount = count + 9;
		
		// Erstmal stacks vereinfachen
		for(int i = count; i < stacks.length; ++i) { 
			
			if(stacks[i].getTypeId() == 0) {
				isMat[e] = false;
			} else {
				isMat[e] = true;
			}
			
			++e;
		}
		
		// ECKE LINKS OBEN
		
		// Erste Zeile checken
		if(!isMat[0] && !isMat[1] && !isMat[2]) {
			
			count = count + 3;
			
			// Mittlere Zeile checken, aber nur wenn erste Zeile schon leer war.
			// sonst funzt das verschieben nicht
			if(!isMat[3] && !isMat[4] && !isMat[5]) count = count + 3;
		}
		
		// Erste Spalte checken
		if(!isMat[0] && !isMat[3] && !isMat[6]) {
			
			count = count + 1;
			
			// Zweite Spalte checken, aber nur, wenn die erste schon leer war.
			if(!isMat[1] && !isMat[4] && !isMat[7]) count = count + 1;
		}
		
		// ECKE RECHTS UNTEN
		
		// Letzte Zeile checken
		if(!isMat[6] && !isMat[7] && !isMat[8]) {
			
			endCount = endCount - 3;
			
			// Mittlere Zeile checken, aber nur wenn die letzte Zeile schon leer war.
			if(!isMat[3] && !isMat[4] && !isMat[5]) endCount = endCount - 3;
		}
		
		// Letzte Spalte checken
		if(!isMat[2] && !isMat[5] && !isMat[8]) {
			
			endCount = endCount - 1;
			
			// Mittlere Spalte checken, aber nur, wenn die letzte Spalte schon leer war.
			if(!isMat[1] && !isMat[4] && !isMat[7]) endCount = endCount - 1;
		}
		
		short subId  = 0;
		int typeId   = 0;
		
		for(int i = count; i < endCount; ++i) { 

			subId = stacks[i].getDurability();
			if(subId < 0) subId = 0;
			
			typeId = stacks[i].getTypeId();
			
			if (foo.length() > 0) foo = foo.append(",");
			foo = foo.append(getStackString(typeId, subId));
		}
				
		return foo;
	}
	
	public StringBuilder getFixedShapeString(StringBuilder foo, int count, ItemStack[] stacks) {
		
		short subId = 0;
		
		for(int i = count; i < stacks.length; ++i) { 

			if (foo.length() > 0) foo = foo.append(",");
			subId = stacks[i].getDurability();
			if(subId < 0) subId = 0;
			foo = foo.append(getStackString(stacks[i].getTypeId(), subId));
		}
		
		return foo;
	}
	
	public String formatTypeId(Integer typeId) {
		
		return String.format("%05d", typeId);
	}
	
	public String formatSubId(Short subId) {
		
		return String.format("%02d", subId);
	}
	
	public String getStackString(int typeId, short subId) {
		return formatTypeId(typeId) + ":" + formatSubId(subId);
	}

	@Override
	public void onDisable() {

		// Do something on DISABLE
		getServer().clearRecipes();

		// Info to console that the plugin is disabled now.
		getLogger().info("onDisable Recipes has been invoked!");
	}

	/**
	 * 
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		boolean isPlayer = true;
		Player player = null;
		
		// Commands are only accepted by Players not by console or so.
		if(sender instanceof Player) {
			
			player = (Player)sender;
			PermissionUser user = PermissionsEx.getUser(player);
			
			if(!user.has("recipes.admin")) return false;
			
		} else {
			
			isPlayer = false;
		}
		
		if (cmd.getName().equalsIgnoreCase("recipes")) {

			if(args.length == 0) return false;
			
			if (args[0].equalsIgnoreCase("reload")) {

				if(!loadRecipeConfig()) {
					
					if(isPlayer) {
						player.sendMessage(getColoredMessage("[RECIPES] &2Plugin stopped. Check console, please.&r"));
					} else {
						onEnableError("Plugin stopped. Check error messages above, please.");
					}
					
				} else {
					
					String message = "Successfully reloaded.";
					
					if(isPlayer) {
						player.sendMessage(getColoredMessage("[RECIPES] &2" + message + "&r"));
					} else {
						this.getLogger().info(ANSI_GREEN + message + ANSI_RESET);
					}
				}
				
				return true;
			}
		}

		return false;
	}

	public void debugMessage(Player player, String message) {

		if (!(Boolean) getMetadata(player, "debugMode")) return;

		player.sendMessage("DEBUG: " + message);
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		
		final Player player = (Player) event.getWhoClicked();
		
		String currentRecipeIndex = (String) getMetadata(player, "currentRecipe");
		if(currentRecipeIndex == null || currentRecipeIndex.isEmpty()) return;
		
		// 1. Andernfalls, das Standard-Crafting-Event canceln, da onCustomCraftEvent von 
		// onInventoryItemEvent() gefeuert wird. Damit werden einige Probleme umgangen.
		event.setCancelled(true);
	}
	
	public void onCustomCraftEvent(CraftItemEvent event, InventoryClickEvent originalEvent) {
		
		final Player player = (Player) event.getWhoClicked();
		
		String currentRecipeIndex = (String) getMetadata(player, "currentRecipe");
		if(currentRecipeIndex == null || currentRecipeIndex.isEmpty()) return;
		
		String currentRecipeType = getRecipeType(currentRecipeIndex);
		
		// Das Rezept is da, das Inventory muss noch geholt werden, um davon
		// die Kosten fuer die Items abzuziehen. Wichtig dabei ist, dass das
		// aktuelle Inventory in einen assoziativen Shape verwandelt wird, welches
		// sich statt der Stacks die Slots merkt, um davon dann korrekt die
		// Werte abzuziehen.
		//
		// Bei FIXED bleibt alles wie es ist.
		//
		// Bei VARIABLE ist die Form noch da, es muss da nur ab dem ersten Vorkommen berechnet
		// werden.
		//
		// Bei FREE darf jedes Material nur einmal vorkommen, von daher muss nur das Material
		// gesucht werden, um sich den Slot zu merken.

		Inventory craftInventory = player.getOpenInventory().getTopInventory();
		ItemStack[] craftStacks  = getCraftInventoryByType(craftInventory);
		Integer[][] slotMatrix = {{1,2,3},{4,5,6},{7,8,9}};
		
		// FIXED?
		if(currentRecipeType.equals(SHAPE_FIXED)) slotMatrix = getFixedSlotMatrix();
		
		// VARIABLE?
		if(currentRecipeType.equals(SHAPE_VARIABLE)) slotMatrix = getVariableSlotMatrix(craftStacks);
			
		// FREE?
		if(currentRecipeType.equals(SHAPE_FREE)) slotMatrix = getFreeSlotMatrix(craftStacks, currentRecipeIndex);
		
		ItemStack[][] recipeShape = getRecipeShape(currentRecipeIndex);
		ItemStack result = getRecipeResult(currentRecipeIndex);
		int resultAmount = result.getAmount();
		int resultCount = 1;
		int times = 1;
		
		// 1. Andernfalls, das Crafting-Event canceln,
		event.setCancelled(true);
		
		// 1.1. Hat der Spieler bereits etwas im Cursor, muss das Item
		// mit dem Result identisch sein, sonst abbrechen. Spaeter wird
		// dann bei Klick mit gleichem Item, einfach addiert. (Auch auf
		// SubIds achten -> getDurability.)
		
		if(event.getCursor().getTypeId() != 0 && (event.getCursor().getTypeId() != result.getTypeId() || event.getCursor().getDurability() != result.getDurability())) return;
				
		// 2. Crafting-Grid entsprechend korrigieren, Kosten abziehen
		if(event.isShiftClick()) {
			
			// Hier muss der Stack der rausgegeben wird korrekt berechnet werden.
			// Einfach zusammenrechnen, wie oft ein Item pro Slot craftbar waere
			// und die geringste Zahl gewint.
			times = calculateResults(craftStacks, recipeShape, slotMatrix);
			
			// Bei ShiftClick muss das Click-Event abgebrochen werden, weil sonst
			// das CraftingEvent einfach ausgefuehrt wird und man nichts dagegen machen
			// koennte
			if(originalEvent != null) originalEvent.setCancelled(true);
		}
		
		// Wenn nix erstellt werden kann, dann resetten.
		if(times == 0) {
			
			// Da nix gefunden, das Result leeren.
			player.getOpenInventory().getTopInventory().setItem(0, null);
			
			updateInventoryScheduled(player, 1);
			
			return;
		}
		
		// Sicherstellen, dass nicht mehr gecraftet werden kann, als der Cursor
		// tragen kann. Dazu erst ausrechnen, wieviel maximal noch gecraftet
		// werden darf (maxStack). Dann schauen ob das Produkt aus dem Rezept 
		// weniger oder gleichviel maxStack ist, wenn nein, dann wird
		// maxStack als Limit genommen.
		int maxStack = 64 - event.getCursor().getAmount();
		
		// Jetzt die Anzahl des Ergebnisses damit multiplizieren, wie oft das Rezept craftbar ist,
		// das ergibt dann die Items fuer den Cursor.
		resultCount = times * resultAmount;
		
		// Ist das Ergebnis hoeher als noch auf den Cursor passt, dann berechnen wieviel mal
		// das Rezeptergebniss auf den Cursor passt.
		if(resultCount > maxStack) resultCount = ((int)Math.floor(maxStack / resultAmount)) * resultAmount;
		
		int finalStack = event.getCursor().getAmount() + resultCount;
		
		if (resultAmount != 0 && resultCount != 0) {
			times = resultCount / resultAmount;
		} else {
			times = 0;
		}
		
		if(times > 0) {
			
			payResults(craftStacks, recipeShape, slotMatrix, times, craftInventory);
			
			ItemStack finalResultStack = result.clone();
			finalResultStack.setAmount(finalStack);
			
			player.setItemOnCursor(finalResultStack);
			
			// Noch eine Meldung ausgeben, falls vorhanden.
			String resultMessage = getRecipeResultMessage(currentRecipeIndex);
			if(resultMessage != null && !resultMessage.isEmpty()) player.sendMessage(getColoredMessage(resultMessage));
			
		}
		
		// Inventory aktuallisieren, weil sonst ggf. update Probleme im Crafting
		// grid auftreten.
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {

				updateInventory(player);
			}
		}, 1);
	}
	
	public Integer[][] getFixedSlotMatrix() {
		
		Integer[][] slotMatrix = {
			{1,2,3},
			{4,5,6},
			{7,8,9}
		};
		
		return slotMatrix;
	}
	
	public Integer[][] getVariableSlotMatrix(ItemStack[] craftStacks) {
		
		Integer[][] slotMatrix = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
		
		// Um Resourcen zu sparen, Werte der Stacks einmal merken.
		Boolean[] isMat = getHasMat(craftStacks);
		
		// Start und Ende der Rezepte berechnen, leere Zeilen sollen
		// moeglichst ignoriert werden.
		int startCount = getStartCount(isMat);
		int endCount   = getEndCount(isMat);
		int row        = 0;
		int col        = 0;
		
		for(int i = startCount; i <= endCount; ++i) {
			
			slotMatrix[row][col] = i;
			
			++col;
			
			if(col == 3) {
				col = 0;
				++row;
			}
		}
		
		return slotMatrix;
	}
	
	public Integer[][] getFreeSlotMatrix(ItemStack[] craftStacks, String recipeIndex) {
		
		// Da jede Zutat nur einmal vorkommen darf, muss im Grunde nur vom Rezeptshape aus
		// gesehen in craftStacks geschaut werden, wo sich die jeweilige Zutat befindet.
		Integer[][] slotMatrix = {{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}};
		HashMap<String,Integer> translation = new HashMap<String,Integer>();
		
		for(int i = 1; i <= 9; ++i) translation.put(stackToString(craftStacks[i]), i);
		
		ItemStack[][] recipeShape = getRecipeShape(recipeIndex);
		ItemStack shapeStack;
		
		for(int row = 0; row <= 2; ++row) {
			for(int col = 0; col <= 2; ++col) {
				
				shapeStack = recipeShape[row][col];
				
				if(shapeStack.getTypeId() == 0) continue;
				
				slotMatrix[row][col] = translation.get(stackToString(shapeStack));
			}
		}
		
		return slotMatrix;
	}
	
	public ItemStack[] getCraftInventoryByType(Inventory craftInventory) {
		
		InventoryType invType = craftInventory.getType();
		ItemStack[] craftStacks  = null;
		
		if (invType == InventoryType.WORKBENCH) {
			
			// 0 = Result, 1-9 = Craftingslots
			craftStacks = craftInventory.getContents();
			
		} else if (invType == InventoryType.CRAFTING) {
			
			// 0 = Result, 1-4 = Craftingslots, Rest auffuellen
			ItemStack[] stacks = craftInventory.getContents();
			craftStacks = new ItemStack[]{stacks[0],stacks[1],stacks[2],stacks[3],stacks[4],AIR,AIR,AIR,AIR,AIR};		
		}
		
		return craftStacks;
	}
	
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {

		InventoryType inventoryType = event.getInventory().getType();
		
		// Workbench / Inventory Crafting?
		if(inventoryType != InventoryType.WORKBENCH && inventoryType != InventoryType.CRAFTING) return;
		
		// Crafting / Result?
		String slotType = event.getSlotType().toString();
		if(slotType != "CRAFTING" && slotType != "RESULT") return;
		
		if(slotType == "RESULT") {
			
			CraftItemEvent cieEvent = new CraftItemEvent(null, event.getView(), event.getSlotType(), event.getSlot(), event.isRightClick(), event.isShiftClick());
			onCustomCraftEvent(cieEvent, event);
			return;
		}
		
		final Player player = (Player) event.getWhoClicked(); 
		
		// 1 Tick spaeter, weil man dann quasi nach dem Event ins Inventory schaut
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				updateInventory(player);
			}
		}, 1);
	}
	
	public boolean compareShapes(ItemStack[][] recipeShape, ItemStack[][] craftShape) {
		
		ItemStack craftStack = null;
		ItemStack recipeStack = null;
		
		// Passend zum recipeShape und craftingShape muss nun nachgeschaut
		// werden ob genuegend Items vorhanden sind.
		for(int row = 0; row <= 2; ++row) {
			for(int column = 0; column <= 2; ++column) {
				
				craftStack = craftShape[row][column];
				recipeStack = recipeShape[row][column];
				
				// Wenn im Rezept nix an dem Slot ist (Luft = nix), dann skippen
				if(!isStack(recipeStack)) continue;
				
				// Rezept Amount muss hoeher gleich dem Craft Amount sein
				if(recipeStack.getAmount() > craftStack.getAmount()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Hinweis: Das Result des Inventory wird immer zurueckgesetzt, wenn der Server
	 * kein Original-Rezept findet, daher muss dies hier nicht zusaetzlich gemacht
	 * werden.
	 * @param player
	 */
	public void updateInventory(Player player) {
		
		// Da nix gefunden wurde, am Spieler dies merken...
		setMetadata(player, "currentRecipe", null);
				
		// Rausfinden ob ein Rezept passt...
		Inventory craftInventory = player.getOpenInventory().getTopInventory();
		InventoryType invType    = craftInventory.getType();
		ItemStack[] craftStacks  = getCraftInventoryByType(craftInventory);
		
		// Sichergehen, dass was drin steht, ansonsten Abbruch
		if(craftStacks == null) return;
		
		String[] userGroups = getUserGroups(player); 
		
		// 1. FIXED checken, ggf. bei Fund setzen und fertig (nur checken bei WORKBENCH)
		if(invType == InventoryType.WORKBENCH && setResultItem(getFixedShape(craftStacks), userGroups, craftInventory, player, SHAPE_FIXED)) return;
		
		// 2. VARIABLE checken, ggf. bei Fund setzen und fertig
		if(setResultItem(getVariableShape(craftStacks), userGroups, craftInventory, player, SHAPE_VARIABLE)) return;
		
		// 3. FREE checken, ggf. bei Fund setzen und fertig
		if(setResultItem(getFreeShape(craftStacks), userGroups, craftInventory, player, SHAPE_FREE)) return;
		
		// Da nix gefunden wurde, am Spieler dies merken...
		setMetadata(player, "currentRecipe", null);
	}
	
	public Boolean setResultItem(ItemStack[][] craftShape, String[] userGroups, Inventory craftInventory, Player player, String type) {
		
		String craftIndex     = shapeToString(craftShape);
		String recipeString   = getRecipeAsString(userGroups, craftIndex);
		ItemStack resultStack = new ItemStack(AIR);
		
		if(recipeString != null && getRecipeType(recipeString).equalsIgnoreCase(type)) {
			
			if(compareShapes(getRecipeShape(recipeString), craftShape)) {
				
				resultStack = getRecipeResult(recipeString);
				
				// Am Spieler das Rezept merken, damit bei onCraft der ganze Kladderradatsch
				// nicht schon wieder berechnet werden muss.
				setMetadata(player, "currentRecipe", recipeString);
			}
			
			// Falls resultStack nicht gesetzt werden konnte, muss der Resultslot des Inventorys auf leer gesetzt werden,
			// da Rezepte einmalig sind und nicht mehrfach vorkommen koennen durfen pro Gruppe/Typ. Dabei wird immer
			// die Reihenfolge (vom Caller der aktuellen Funktion) FIXED -> VARIABLE -> FREE beachtet.
			craftInventory.setItem(0, resultStack);
			
			// Workaround: Inventory erzwungen updaten
			updateInventoryScheduled(player, 1);
				
			return true;
		}
		
		return false;
	}
	
	/**
	 * DONE: Die Usergruppen muessen noch per Rank sortiert werden, damit ein Admin-Rezept (Rang 1) Vorrang
	 * vor einem User-Rezept (Rang 100) hat.
	 * @param player
	 * @return
	 */
	public String[] getUserGroups(Player player) {
		
		PermissionUser user = PermissionsEx.getUser(player);
		PermissionGroup[] userGroups = user.getGroups();
		
		HashMap<Integer,String> groups = new HashMap<Integer,String>();
		ArrayList<Integer> groupRanks = new ArrayList<Integer>();
		
		int lastRank = 999999999;
		
		groups.put(lastRank, GROUP_GLOBAL.toLowerCase());
		groupRanks.add(lastRank);
		
		int rank;
		
		for(PermissionGroup group : userGroups) {
			
			rank = group.getRank();
			
			if(rank == 0) {
				--lastRank;
				rank = lastRank;
			}
			
			groupRanks.add(rank);
			groups.put(rank, group.getName().toLowerCase());
		}
		
		java.util.Collections.sort(groupRanks);
		
		int len = groups.size();
		int newPos = 0;
		String[] finalGroups = new String[len];
		
		for(int groupRank : groupRanks) {
			finalGroups[newPos] = groups.get(groupRank);
			++newPos;
		}
		
		return finalGroups;
	}
	
	public void updateInventoryScheduled(Player player, int ticks) {
		
		final Player tmpPlayer = player;
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
		
				tmpPlayer.updateInventory();
			}
		}, ticks);
	}
	
	public Integer calculateResults(ItemStack[] craftStacks, ItemStack[][] recipeShape, Integer[][] slotMatrix) {
		
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
	
	public void payResults(ItemStack[] craftStacks, ItemStack[][] recipeShape, Integer[][] slotMatrix, Integer amount, Inventory craftInventory) {
		
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

	public void setMetadata(Player player, String key, Object value) {
		player.setMetadata(key, new FixedMetadataValue(this, value));
	}

	public Object getMetadata(Player player, String key) {

		List<MetadataValue> values = player.getMetadata(key);

		for (MetadataValue value : values) {
			if (value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())) {
				return value.value();
			}
		}

		return false;
	}
	
	public void reloadCustomConfig() {
		
		if(customConfigFile == null) customConfigFile = new File(getDataFolder(), customConfigFileName);
		
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
		
		// Look for defaults in the jar
	    InputStream defConfigStream = this.getResource("recipes.yml");
	    
	    if (defConfigStream != null) {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        customConfig.setDefaults(defConfig);
	    }
	    
	    // Defaultdatei anlegen
	    if(!customConfigFile.exists()) {
	    	this.saveCustomConfig();
	    	this.getLogger().info("Recipe configuration was initally created in:");
	    	this.getLogger().info("" + customConfigFile);
	    	this.getLogger().info("You can now edit the file and configure your custom recipes.");
	    }
	}
	
	public FileConfiguration getCustomConfig() {
		
		if(customConfig == null) this.reloadCustomConfig();
		
		return customConfig;
	}
	
	public void saveCustomConfig() {
		
		if(customConfig == null || customConfigFile == null) return;
		
		try {
			
			getCustomConfig().save(customConfigFile);
			
		} catch (IOException ex) {
			
			this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
		}
	}
	
	public String getColoredMessage(String message) {
		
		if(message == null) return null;
		
		for(String key : CHATCOLORS.keySet()) {
		
			message = message.replaceAll(key, CHATCOLORS.get(key).toString());
		}
		
		return message;
	}
}