package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dispenser;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.systemNEO.recipes.API.KSideHelper;
import de.systemNEO.recipes.RBlockDrops.RBlockDrops;
import de.systemNEO.recipes.RBlocks.RBlocks;
import de.systemNEO.recipes.RChunks.RChunks;
import de.systemNEO.recipes.RDrops.RDrop;
import de.systemNEO.recipes.RDrops.RDrops;
import de.systemNEO.recipes.RUtils.Utils;

/**
 * TODO Fuer RBlockDrops > Laub > die Saplings beruecksichtigen.
 * TODO XP als Zutat
 * TODO Bei Shift-Klick die Items automatisch im Inventarablegen!
 * TODO Bei Enchantments noch eine %-Chance hinzufuegen!
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
 * TODO Gruppen nochmal ueberarbeiten:
 *      1. Rankings spielen keine Rolle, einfach berechnen welches Rezept weniger kostet bzw. mehr
 *         bringt. Das gewinnt dann einfach. (Das Ergebnis cachen).
 *      2. Ggf. weiter PermissionPlugins supporten.
 * TODO Text fuer Lore nach [n] Stellen automatisch umbrechen.
 * TODO Beim "removen" der Rezepte auch die Anzahl des Results beruecksichtigen (29.01.2013: Muesste
 *      eigentlich schon so sein, ggf. nochmal pruefen).
 * TODO Beim Speichern der Rezepte nochmal aufraeumen, ggf. den Typ des Rezeptes zwischen Gruppe und
 *      Shape-String im Index mit aufnehmen, damit performanter insbesondere wenn es um die Wildcards
 *      in den Ingeredients geht.
 * TODO Wildcards auch fuer "remove"-Rezepte im Result ermoeglichen, um Config-Aufwand zu sparen.
 * TODO Fuer Leavings bei Tools die eine Durability haben es ermoeglichen das Item mit z. B. -20
 *      Durability wieder auszuspucken.
 * TODO Bei Leavings darauf achten, dass Namen und Lore, sowie ggf. Enchantments erhalten bleiben!
 * TODO Fuer Version 2.0 kompletter Rewrite, Rezepte als Objekt zugreifbar ueber Index fertig machen,
 *      Gruppenpruefung dann direkt im Rezept und nicht per Index, sowie Wildcards, usw. - ist derzeit
 *      ziemlicher Ranz, so wie es jetzt ist.
 * 
 * BEI UPDATES:
 * - Checken ob es neue Werkzeuge gibt und diese bei RBlockDrops ergaenzen!
 * - Checken ob es neue Erze gibt und diese bei RBlockDrops ergeanzen!
 * - Checken ob es neue Werkzeug-Enchantments gibt und diese bei RBlockDrops und Utils ergaenzen!
 *      
 * @author Hape
 * 
 */
public final class Recipes extends JavaPlugin implements Listener {	
	
	/** Liste an Bloecken die bei Beruehrung mit Wasser kaputt gehen und gecheckt werden sollen. */
	private Set<Material> waterBreakableItems_ = EnumSet.of(Material.CROPS, Material.CARROT, Material.POTATO, Material.WHEAT, Material.WEB);
	
	/** Liste an Wassermaterialien die das Zerstoeren eines Blocks bei Beruehrung ausloesen. */
	private Set<Material> waterMaterials_ = EnumSet.of(Material.WATER, Material.STATIONARY_WATER);
	
	@Override
	public void onEnable() {
		
		// Plugin in den Constanten setzen, damit man es nicht standig
		// uebergeben muss
		Constants.setPlugin(this);
		
		// Plugins checken
		if(!Utils.checkPlugins()) {
			
			Utils.logWarning("&4Plugin stopped. Check plugin dependencies, please.&r");
			
			this.setEnabled(false);
			
			return;
		}
		
		// Config einlesen und Rezepte erstellen
		if(!Config.loadRecipeConfig()) {
			
			Utils.onEnableError("Plugin stopped. Check error messages above, please.");
			
			return;
		}
		
		// Events der Klasse jetzt erst registrieren, da es Rezepte gibt und keine Fehler
		// vorliegen.
		getServer().getPluginManager().registerEvents(this, this);
		
		// TADAAA :)
		Utils.logInfo("&2Successfully loaded.&r");
	}
	
	

	/**
	 * Erstellt ein neues Rezept.
	 * @param stacks
	 * 			1 Resultstack plus 9 ItemStacks, je Position im Rezept.
	 * @param groups
	 * 			Einschraenkung auf Gruppe (Plugin PermissionEX) oder Koenigreich (Plugin KingdomSide).
	 * @param type
	 * 			Typ des Rezeptes.
	 * @param resultMessage
	 * 			Dem Spieler anzuzeigende Nachricht nach dem Craften.
	 * @param resultChance
	 * 			Chance zwischen 0 und 100 fuer das Rezeptergebnis.
	 * @param leavings
	 * 			Abfaelle / Ueberbleibsel nach dem Craften. Z. B. ein leere Milcheimer.
	 * @param leavingsChance
	 * 			Chance zwischen 0 und 100 fuer das jeweilige Leaving.
	 * @param shapeAlias
	 * 			Aliase mit z. B. Wildcards fuer ein Rezept.
	 */
	public static boolean createCustomRecipe(ItemStack[] stacks, ArrayList<String> groups, String type, String resultMessage, Integer resultChance, ArrayList<ItemStack> leavings, ArrayList<Integer> leavingsChance, String shapeAlias) {
		
		// Nix gescheites in der ItemStack[]-Liste, dann Abbruch.
		if(stacks == null || stacks.length < 10) return false;
		
		// Sicherstellen, dass der korrekte Typ an die Funktion uebergeben wurde.
		if(type == null) type = Constants.SHAPE_DEFAULT;
		
		// Den Shape nun vereinfachen, je nach Typ.
		// FIXED: Bleibt wie er ist.
		// VARIABLE: Die Form wird in die linke obere Ecke der Shapematrix verschoben
		// FREE: Die Items werden ihrer ID:SUBID nach sortiert in eine Reihe angeordnet.
		ItemStack[][] shape = null;
		
		if(type.equals(Constants.SHAPE_FIXED)) shape = Shapes.getFixedShape(stacks);
		
		if(type.equals(Constants.SHAPE_VARIABLE)) shape = Shapes.getVariableShape(stacks);
		
		if(type.equals(Constants.SHAPE_FREE)) shape = Shapes.getFreeShape(stacks);
		
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
		String shapeIndex =  Shapes.shapeToString(shape);
		
		//Utils.logInfo(shapeIndex);
		
		// Unter diesem String muessen jetzt mehrere Werte abgespeichert werden:
		// 1. Das Original-Rezept
		// 2. Das Result
		// 3. Der Shape
		// 4. Der Typ
		for(String group : groups) setRecipe(group, shapeIndex, stacks, type, shape, resultMessage, leavings, resultChance, leavingsChance, shapeAlias);
		
		// Alles okay
		return true;
	}
	
	/**
	 * Speichert Original-Rezept, Result, Shape und Typ des Rezeptes passend zu einer Gruppe.
	 * @param group
	 * 			Gruppe des Rezeptes.
	 * @param index
	 * 			Index des Rezeptes (abgeleitet vom Shape).
	 * @param stacks
	 * 			1 Resultstack plus 9 ItemStacks, je Position im Rezept.
	 * @param groups
	 * 			Einschraenkung auf Gruppe.
	 * @param type
	 * 			Typ des Rezeptes.
	 * @param resultMessage
	 * 			Dem Spieler anzuzeigende Nachricht nach dem Craften.
	 * @param resultChance
	 * 			Chance zwischen 0 und 100 fuer das Rezeptergebnis.
	 * @param leavings
	 * 			Abfaelle / Ueberbleibsel nach dem Craften. Z. B. ein leere Milcheimer.
	 * @param leavingsChance
	 * 			Chance zwischen 0 und 100 fuer das jeweilige Leaving.
	 * @param shapeAlias
	 * 			Aliase mit z. B. Wildcards fuer ein Rezept.
	 */
	public static void setRecipe(String group, String index, ItemStack[] stacks, String type, ItemStack[][] shape, String resultMessage, ArrayList<ItemStack> leavings, Integer resultChance, ArrayList<Integer> leavingsChance, String shapeAlias) {
	
		setRecipeOriginal(group, index, stacks);
		setRecipeType(group, index, type);
		Results.setRecipeResult(group, index, stacks[0]);
		Shapes.setRecipeShape(group, index, shape);
		setRecipeResultMessage(group, index, resultMessage);
		Results.setRecipeLeavings(group, index, leavings);
		setRecipeLeavingsChance(group, index, leavingsChance);
		setRecipeResultChance(group, index, resultChance);
		setRecipeAlias(group, index, shapeAlias);
	}
	
	/**
	 * Quick & Dirty Loesung fuer die WildCard-Loesung bei den Zutaten.
	 * @param group
	 * @param index
	 * @param newIndex
	 */
	public static void cloneRecipe(String group, String index, String newIndex, String shapeAlias) {
		
		String currentGroupIndex = group.toLowerCase() + "_" + index;
		
		String[] stacksDefinition = newIndex.split(",");
		ItemStack[] stacks = new ItemStack[10];
		ItemStack[] originalStacks = getRecipeOriginal(currentGroupIndex);
		
		for(int i = 0; i < 10; ++i) {
			
			// Sicherstellen, dass nicht auf etwas leeres zugegriffen wird.
			if(originalStacks == null || originalStacks[i] == null) {
				
				stacks[i] = new ItemStack(Material.AIR);
				
			} else {
			
				stacks[i] = new ItemStack(originalStacks[i]);
			}
		}
		
		int pos = 0;
		
		for(String stackDefinitionItem : stacksDefinition) {
			
			++pos;
			
			String[] stackDefinition = stackDefinitionItem.split(":");
			
			stacks[pos].setDurability((short) Integer.parseInt(stackDefinition[1]));
		}
		
		String type = getRecipeType(currentGroupIndex);
		ItemStack[][] shape = Stacks.getDefaultStack();
		
		if(type.equalsIgnoreCase(Constants.SHAPE_FIXED)) shape = Shapes.getFixedShape(stacks);
		if(type.equalsIgnoreCase(Constants.SHAPE_VARIABLE)) shape = Shapes.getVariableShape(stacks);
		if(type.equalsIgnoreCase(Constants.SHAPE_FREE)) shape = Shapes.getFreeShape(stacks);
		
		String newShapeIndex = Shapes.shapeToString(shape);
		
		setRecipe(
			group,
			newShapeIndex,
			stacks,
			type,
			shape,
			getRecipeResultMessage(currentGroupIndex),
			Results.getRecipeLeavings(currentGroupIndex),
			getRecipeResultChance(currentGroupIndex),
			getRecipeLeavingsChance(currentGroupIndex),
			shapeAlias);
	}
	
	public static void setRecipeAlias(String group, String index, String shapeAlias) {
		
		if(shapeAlias == null) return;
		
		group = group.toLowerCase();
		
		HashMap<String,String> aliasesByGroup = Constants.RECIPES_ALIAS.get(group);
		
		if(aliasesByGroup == null) aliasesByGroup = new HashMap<String,String>();
		
		aliasesByGroup.put(group + "_" + index, group + "_" + shapeAlias);
		
		Constants.RECIPES_ALIAS.put(group, aliasesByGroup);
	}
	
	public static HashMap<String,String> getRecipeAlias(String group) {
		
		group = group.toLowerCase();
		
		if(!Constants.RECIPES_ALIAS.containsKey(group)) return null;
		
		return Constants.RECIPES_ALIAS.get(group);
	}
	
	public static void setRecipeResultChance(String group, String index, Integer resultChance) {
		
		Constants.RECIPES_RESULTCHANCE.put(group.toLowerCase() + "_" + index, resultChance);
	}
	
	public static Integer getRecipeResultChance(String groupIndex) {
		
		return Constants.RECIPES_RESULTCHANCE.get(groupIndex);
	}
	
	public static void setRecipeLeavingsChance(String group, String index, ArrayList<Integer> leavingsChance) {
		
		Constants.RECIPES_LEAVINGSCHANCE.put(group.toLowerCase() + "_" + index, leavingsChance);
	}
	
	public static ArrayList<Integer> getRecipeLeavingsChance(String groupIndex) {
		
		return Constants.RECIPES_LEAVINGSCHANCE.get(groupIndex);
	}
	
	public static Integer getRecipeLeavingChance(Integer pos, String groupIndex) {
		
		ArrayList<Integer> leavingsChance = getRecipeLeavingsChance(groupIndex);
		
		if(leavingsChance == null || leavingsChance.isEmpty() || pos >= leavingsChance.size()) return 100;
		
		return leavingsChance.get(pos);
	}
	
	public static void setRecipeResultMessage(String group, String index, String resultMessage) {
		
		Constants.RECIPES_RESULTMESSAGE.put(group.toLowerCase() + "_" + index, resultMessage);
	}
	
	public static String getRecipeResultMessage(String groupIndex) {
		
		return Constants.RECIPES_RESULTMESSAGE.get(groupIndex);
	}
	
	public static void setRecipeOriginal(String group, String index, ItemStack[] stacks) {
		
		Constants.RECIPES_ORIGINAL.put(group.toLowerCase() + "_" + index, stacks);
	}
	
	public static ItemStack[] getRecipeOriginal(String groupIndex) {
		
		return Constants.RECIPES_ORIGINAL.get(groupIndex);
	}
	
	public static void setRecipeType(String group, String index, String type) {
		
		Constants.RECIPES_TYPE.put(group.toLowerCase() + "_" + index, type);
	}
	
	public static String getRecipeType(String groupIndex) {
		
		return Constants.RECIPES_TYPE.get(groupIndex);
	}
	
	public static String getRecipeAsString(String[] groups, String index) {
		
		// Wenn nix gefunden, dann aliase durchwuehlen.
		HashMap<String,String> aliases;
		String shapeIndexToTest;
		
		// Rezepte direkt durchsuchen.
		for(String group : groups) {
			
			if(isRecipe(group, index)) {
				
				return group.toLowerCase() + "_" + index;
				
			} else {
				
				// Aliase checken
				if((aliases = getRecipeAlias(group)) == null) continue;
				
				shapeIndexToTest = group.toLowerCase() + "_" + index;
				
				for(String keyShapeIndex : aliases.keySet()) {
					
					if(shapeIndexToTest.matches(aliases.get(keyShapeIndex))) {
						
						// Wenn ein Alias gefunden wurde, dann wird das Rezept zur
						// Laufzeit geklont, damit nicht wieder alle Aliase durchlaufen
						// werden muessen. Somit ist das Plugina anfaenglich etwas
						// langsamer, wird dann aber schneller mit der Zeit. Zudem
						// werden nur "sinnvolle" Rezepte in den Registern gemerkt,
						// da bei einem Werkzeug die Durability gar aber tausende
						// Rezepte anlegen muesste.
						String[] oldGroupIndex = keyShapeIndex.split("_");
						
						if(oldGroupIndex[1] != null && !oldGroupIndex[1].isEmpty()) {
							
							Utils.logInfo("&6Recipes::getRecipeAsString - Try to clone " + shapeIndexToTest + " - " + keyShapeIndex);
							
							cloneRecipe(group, oldGroupIndex[1], index, null);
							
						} else {
							
							Utils.logInfo("&4Recipes::getRecipeAsString - Group index was empty for " + shapeIndexToTest + " - " + keyShapeIndex);
						}
						
						return shapeIndexToTest;
					}
				}
			}
		}
		
		return null;
	}
	
	public static Boolean isRecipe(String group, String index) {
		
		return Constants.RECIPES_TYPE.containsKey(group.toLowerCase() + "_" + index);
	}
	
	@Override
	public void onDisable() {

		// Do something on DISABLE
		getServer().clearRecipes();
		
		// Info to console that the plugin is disabled now.
		getLogger().info("onDisable Recipes has been invoked!");
	}
	
	/**
	 * Entfernt an Hand eines Rezeptes
	 * @param resultStackToRemove
	 */
	public static boolean removeRecipe(ItemStack resultStackToRemove, ArrayList<String> groups) {
		
		List<Recipe> foundRecipes = Utils.getPlugin().getServer().getRecipesFor(resultStackToRemove);
		ShapedRecipe shapedRecipe;
		ShapelessRecipe shapelessRecipe;
		
		if(foundRecipes == null || foundRecipes.isEmpty()) {
			
			Utils.logInfo("&6Could not found recipes to remove for result " + Stacks.stackToString(resultStackToRemove) + "...");
			
			return false;
		}
		
		// Utils.logInfo(foundRecipes.size() + " found!");
		
		ItemStack[] shape;
		ItemStack[] ingredients;
		char[] line;
		boolean found;
		String type = Constants.SHAPE_DEFAULT;
		int pos;
		boolean returnFound = false;
		
		for(Recipe recipe : foundRecipes) {
			
			shape = new ItemStack[10];
			found = false;
			
			if(recipe instanceof ShapedRecipe) {
				
				found = true;
				type = Constants.SHAPE_VARIABLE;
				shapedRecipe = (ShapedRecipe) recipe;
				pos = 0;
				
				String[] shapeString = shapedRecipe.getShape();
				
				for(int row = 0; row < 3; ++row) {
					
					if((shapeString.length - 1) >= row) {
						line = shapeString[row].toCharArray();
					} else {
						line = new char[0];
					}
					
					for(int col = 0; col < 3; ++col) {
						
						++pos;
						
						if((line.length - 1) >= col) {
							shape[pos] = shapedRecipe.getIngredientMap().get(line[col]);
						} else {
							shape[pos] = Constants.AIR;
						}
					}
				}
				
			} else if(recipe instanceof ShapelessRecipe) {
				
				found = true;
				type = Constants.SHAPE_FREE;
				
				shapelessRecipe = (ShapelessRecipe) recipe;
				ingredients = shapelessRecipe.getIngredientList().toArray(new ItemStack[0]);
				
				for(int i = 0; i < 9; ++i) {
					
					pos = 1 + i;
					
					if(ingredients.length < (i + 1)) {
						shape[pos] = Constants.AIR;
					} else {
						
						shape[pos] = ingredients[i];
					}
				}
			}
			
			if(!found) continue;
			
			returnFound = true;
			
			shape[0] = new ItemStack(Material.AIR, 0);
			
			createCustomRecipe(shape, groups, type, null, null, null, null, null);
		}
		
		return returnFound;
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

			if(args.length == 0) {
				
				PluginDescriptionFile description = Constants.getPlugin().getDescription();
				
				Utils.playerMessage(player, "&6" + description.getName() + "&7 - &6" + description.getDescription()
						+ " &7Version &6" + description.getVersion() 
						+ " &7Depends &6" + StringUtils.join(description.getDepend(), "&7, &6")
						+ " &7Website &6" + description.getWebsite());
				
				return true;
			}
			
			if (args[0].equalsIgnoreCase("reload")) {

				if(!Config.loadRecipeConfig()) {
					
					if(isPlayer) {
						Utils.playerMessage(player, "[RECIPES] &2Plugin stopped. Check console, please.&r");
					} else {
						Utils.onEnableError("Plugin stopped. Check error messages above, please.");
					}
					
				} else {
					
					String message = "Successfully reloaded.";
					
					if(isPlayer) {
						Utils.playerMessage(player, "[RECIPES] &2" + message + "&r");
					} else {
						Utils.logInfo("&2" + message);
					}
				}
				
				return true;
			}
		}

		return false;
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		
		final Player player = (Player) event.getWhoClicked();
		
//		Utils.debug(player.getName() + " onCraft!");
		
		// Lagprotection: Falls aktiv, dann nochmal Inventory updaten und
		// gucken ob da nicht was zu aktuallisieren waere.
		Boolean isLagLock = (Boolean) Utils.getMetadata(player, "lagLock");
		if(isLagLock != null && isLagLock == true) Inventories.updateInventory(player);
		
		String currentRecipeIndex = (String) Utils.getMetadata(player, "currentRecipe");
		if(currentRecipeIndex == null || currentRecipeIndex.isEmpty()) return;
		
		// 1. Andernfalls, das Standard-Crafting-Event canceln, da onCustomCraftEvent von 
		// onInventoryItemEvent() gefeuert wird. Damit werden einige Probleme umgangen.
		event.setCancelled(true);
	}
	
	public void onCustomCraftEvent(CraftItemEvent event, InventoryClickEvent originalEvent) {
		
		final Player player = (Player) event.getWhoClicked();
		
//		Utils.debug(player.getName() + " onCustomCraftEvent!");
		
		// Wenn kein passendes Rezept gefunden wurde, dann in einem Tick nochmal UpdateInventory
		// ausfuehren, was dann nochmals prueft.
		String currentRecipeIndex = (String) Utils.getMetadata(player, "currentRecipe");

		Utils.setMetadata(player, "currentRecipe", null);
			
		if(currentRecipeIndex == null || currentRecipeIndex.isEmpty()) {
		
//			Utils.debug(player.getName() + " CALL setLagLockAfterClickOrDragInventory AND END!");
			
			// Lagprotection (Falls es laggt damit verhindern, dass trotz des verzoegerten
			// updateInventory-Calls keiner waehrend des Lags etwas rausnehmen kann).
			setLagLockAfterClickOrDragInventory(player);
			
			return;
		}
		
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
		ItemStack[] craftStacks  = Inventories.getCraftInventoryByType(craftInventory);
		Integer[][] slotMatrix = {{1,2,3},{4,5,6},{7,8,9}};
		
		// FIXED?
		if(currentRecipeType.equals(Constants.SHAPE_FIXED)) slotMatrix = Shapes.getFixedSlotMatrix();
		
		// VARIABLE?
		if(currentRecipeType.equals(Constants.SHAPE_VARIABLE)) slotMatrix = Shapes.getVariableSlotMatrix(craftStacks);
			
		// FREE?
		if(currentRecipeType.equals(Constants.SHAPE_FREE)) slotMatrix = Shapes.getFreeSlotMatrix(craftStacks, currentRecipeIndex);
		
		ItemStack[][] recipeShape = Shapes.getRecipeShape(currentRecipeIndex);
		ItemStack result = Results.getRecipeResult(currentRecipeIndex);
		
		// Das Original-Inventory-Click-Event canceln, damit nicht durch Shift-Click oder dergleichen
		// noch etwas aus Minecraft heraus gemacht wird.
		originalEvent.setCancelled(true);
		
		// Das Crafting-Event canceln, da nun Custom-Craft-Event stattfindet.
		event.setCancelled(true);
		
		// 1.1. Hat der Spieler bereits etwas im Cursor, muss das Item
		// mit dem Result identisch sein, sonst abbrechen. Spaeter wird
		// dann bei Klick mit gleichem Item, einfach addiert. (Auch auf
		// SubIds achten -> getDurability.)
		boolean isCurserItemTypeEqualToResultType = event.getCursor().getTypeId() != 0 && !Stacks.compareStacks(event.getCursor(), result);
		
		// Wenn Result gleich AIR ist, dann abbrechen, weil AIR wird nicht gecrafted!
		boolean isResultAir = result.getType().equals(Material.AIR);
		
		if(isCurserItemTypeEqualToResultType || isResultAir) {
			
//			Utils.debug(player.getName() + " isCurserItemTypeEqualToResultType || isResultAir END!");
			
			Inventories.updateInventory(player);
			
			return;
		}
		
		int resultAmount = result.getAmount();
		int resultCount = 1;
		int times = 1;
		
		// 2. Crafting-Grid entsprechend korrigieren, Kosten abziehen
		if(event.isShiftClick()) {
			
			// Hier muss der Stack der rausgegeben wird korrekt berechnet werden.
			// Einfach zusammenrechnen, wie oft ein Item pro Slot craftbar waere
			// und die geringste Zahl gewint.
			times = Results.calculateResults(craftStacks, recipeShape, slotMatrix);
			
//			Utils.debug(player.getName() + " TIMES " + times + " ON SHIFT CLICK!");
			
			// Bei ShiftClick muss das Click-Event abgebrochen werden, weil sonst
			// das CraftingEvent einfach ausgefuehrt wird und man nichts dagegen machen
			// koennte
			if(originalEvent != null) originalEvent.setCancelled(true);
		}
		
		// Wenn nix erstellt werden kann, dann resetten.
		if(times == 0) {
			
			// Da nix gefunden, das Result leeren.
			player.getOpenInventory().getTopInventory().setItem(0, null);
			
			Inventories.updateCompleteInventoryScheduled(player, 1);
			
			return;
		}
		
		if(event.isShiftClick()) {
			
			// Wenn SHIFT-Click aktiv, dann soviel craften wie nur geht und direkt ins Inventory
			// packen. Der Einfachheit halbe alles was nicht reinpasst vor die Fuesse schmeissen.
			Results.payResults(craftStacks, recipeShape, slotMatrix, times, craftInventory);
			
			resultCount = Chances.getResultAmountByChance((times * resultAmount), currentRecipeIndex, player);
			
//			Utils.debug(event.getWhoClicked().getName() + " SHIFT CLICK " + resultCount + " TIMES OF " + result.getType().toString());
			
			if(resultCount > 0) {
			
				ItemStack finalResultStack = result.clone();
			
				int maxStackAmount = finalResultStack.getMaxStackSize();
				int rest = resultCount % maxStackAmount;
				int stacks = (int) Math.floor((resultCount / maxStackAmount));
				ArrayList<ItemStack> itemsNotInChest = new ArrayList<ItemStack>();
				HashMap<Integer,ItemStack> drops = null;
				
				// Stacks in MaxStackSize ins Inventory packen.
				if(stacks > 0) {
					
					finalResultStack.setAmount(maxStackAmount);
					
					for(int i = 1; i <= stacks; ++i) {
						
						drops = player.getInventory().addItem(finalResultStack.clone());
						
						if(drops.isEmpty()) continue;
						
						itemsNotInChest.addAll(drops.values());
					}
				}
				
				// Stacks in Restgroesse ins Inventory packen.
				if(rest > 0) {
					
					finalResultStack.setAmount(rest);
					
					drops = player.getInventory().addItem(finalResultStack.clone());
					
					if(!drops.isEmpty()) itemsNotInChest.addAll(drops.values());
				}
				
				if(!itemsNotInChest.isEmpty()) {
					
					for(ItemStack itemToDrop : itemsNotInChest) player.getWorld().dropItem(player.getLocation(), itemToDrop);
					
					Utils.playerMessage(player, "&6Too many items for your inventory! Overage dropped around you.");
				}
			}
			
			Results.giveLeavings(times, currentRecipeIndex, player);
			
			// Noch eine Meldung ausgeben, falls vorhanden.
			String resultMessage = getRecipeResultMessage(currentRecipeIndex);
			if(resultMessage != null && !resultMessage.isEmpty()) Utils.playerMessage(player, resultMessage);
			
			
		} else {
			
			// Sicherstellen, dass nicht mehr gecraftet werden kann, als der Cursor
			// tragen kann. Dazu erst ausrechnen, wieviel maximal noch gecraftet
			// werden darf (maxStack). Dann schauen ob das Produkt aus dem Rezept 
			// weniger oder gleichviel maxStack ist, wenn nein, dann wird
			// maxStack als Limit genommen.
			int maxStack = result.getMaxStackSize() - event.getCursor().getAmount();
			
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
				
//				Utils.debug(event.getWhoClicked().getName() + " NO SHIFT CLICK " + times + " TIMES OF " + result.getType().toString());
				
				Results.payResults(craftStacks, recipeShape, slotMatrix, times, craftInventory);
				
				ItemStack finalResultStack = result.clone();
				
				// Hier nochmal den aktuellen Amount der Hand holen und resultCount per
				// Chance nochmal berechnen. Das erst jetzt, da die Zutaten ja so bezahlt
				// werden muessen, wie es waere wenn 100% der gecrafteten Items erstellt
				// wuerden, auch wenn es am Ende nur z. B. 70% sind...
				finalStack = event.getCursor().getAmount() + Chances.getResultAmountByChance(resultCount, currentRecipeIndex, player);
				
				if(finalStack > 0) {
					finalResultStack.setAmount(finalStack);
					player.setItemOnCursor(finalResultStack);
				}
				
				Results.giveLeavings(times, currentRecipeIndex, player);
				
				// Noch eine Meldung ausgeben, falls vorhanden.
				String resultMessage = getRecipeResultMessage(currentRecipeIndex);
				if(resultMessage != null && !resultMessage.isEmpty()) Utils.playerMessage(player, resultMessage);
			}
		}
		
		Inventories.updateCompleteInventoryScheduled(player, 1);
		
		// Inventory aktuallisieren, weil sonst ggf. update Probleme im Crafting
		// grid auftreten.
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {

				Inventories.updateInventory(player);
			}
		}, 1);
	}
	
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		
		//Utils.logInfo(event.getWhoClicked().getName() + " onInventoryClickEvent!");
		
		if(!Inventories.isWorkbenchOrCraftingInventory(event.getInventory())) return;
		
		// Crafting / Result?
		String slotType = event.getSlotType().toString();
		if(slotType != "CRAFTING" && slotType != "RESULT") return;
		
		if(slotType == "RESULT") {
			
			CraftItemEvent cieEvent = new CraftItemEvent(null, event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction()); 
			
			onCustomCraftEvent(cieEvent, event);
			
			return;
		}
		
		setLagLockAfterClickOrDragInventory((Player) event.getWhoClicked());
	}
	
	@EventHandler
	public void onInventoryDragEvent(InventoryDragEvent event) {
		
		if(!Inventories.isWorkbenchOrCraftingInventory(event.getInventory())) return;
		
		setLagLockAfterClickOrDragInventory((Player) event.getWhoClicked());
	}
	
	/**
	 * Nachdem im Inventory etwas angeklickt wurde oder eine Drag-Aktion durchgefuehrt wurde
	 * wird eine Lag-Sicherung gesetzt und dann im Inventory nachgeschaut, was da als Rezept
	 * bei heraus kam.
	 * 
	 * @param player
	 * 			Betreffender Spieler.
	 */
	public void setLagLockAfterClickOrDragInventory(final Player player) {
		
		// Utils.logInfo("setLagLockAfterClickOrDragInventory");
		
		// Evtl. LagLock-Task der noch aktiv ist abbrechen, damit nicht parallel
		// x-beliebig viele dabei rauskommen.
		Object lastLagLockTaskId = Utils.getMetadata(player, "lagLockTaskId");
		if(lastLagLockTaskId != null && lastLagLockTaskId instanceof Integer) {
			
			getServer().getScheduler().cancelTask((int) lastLagLockTaskId);
		}
		
		// Lagprotection (Falls es laggt damit verhindern, dass trotz des verzoegerten
		// updateInventory-Calls keiner waehrend des Lags etwas rausnehmen kann).
		Utils.setMetadata(player, "lagLock", true);
		
		// 1 Tick spaeter, weil man dann quasi nach dem Event ins Inventory schaut
		int lagLockTaskId = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				Inventories.updateInventory(player);				
			}
		}, 1);
		
		Utils.setMetadata(player, "lagLockTaskId", lagLockTaskId);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		
		if(event.isCancelled() || !RDrops.hasBlockDropRecipes()) return;
		
		int blocksToCheck = event.getLength() + 1;
		BlockFace direction = event.getDirection();
		Block lastBlock = event.getBlock();
		
		for(int i = 1; i <= blocksToCheck; i++) {
			
			lastBlock = lastBlock.getRelative(direction);
			
			// Drueber schauen
			RBlocks.checkBreakablesAboveMoved(direction, lastBlock, null);
			
			// Drumherum schauen
			RBlocks.checkBreakablesAroundMoved(lastBlock, null);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
		
		if(event.isCancelled() || !RDrops.hasBlockDropRecipes()) return;
		
		int blocksToCheck = 1;
		if(event.isSticky()) blocksToCheck = 2;
		
		BlockFace direction = event.getDirection();
		Block lastBlock = event.getBlock();
		
		for(int i = 1; i <= blocksToCheck; i++) {
			
			lastBlock = lastBlock.getRelative(direction);
			
			// Drueber schauen
			RBlocks.checkBreakablesAboveMoved(direction, lastBlock, null);
			
			// Drumherum schauen
			RBlocks.checkBreakablesAroundMoved(lastBlock, null);
		}
	}
	
	private Set<Material> checkDispenseItems_ = EnumSet.of(Material.WATER_BUCKET);
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDispenseEvent(BlockDispenseEvent event) {
		
		if(event.isCancelled() || !RDrops.hasBlockDropRecipes() || !checkDispenseItems_.contains(event.getItem().getType())) return;
		
		Block block = event.getBlock();
		
		Dispenser dispenser = (Dispenser) block.getState().getData();
		BlockFace face = dispenser.getFacing();
		
		Block blockInFront = block.getRelative(face);
		
		// Muss in der Breakableliste fuer Wasser stehen.
		if(!waterBreakableItems_.contains(blockInFront.getType())) return;
		
		BlockBreakEvent blockBreakEvent = new BlockBreakEvent(blockInFront, null);
		onBlockBreakEvent(blockBreakEvent);
		
		// Wenn der BreakEvent abgebrochen wurde, gabs Custom-Drops.
		if(blockBreakEvent.isCancelled()) blockInFront.setType(Material.AIR);
	}
	
	private Set<Material> checkBucketTypes_ = EnumSet.of(Material.WATER_BUCKET);
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		
		if(event.isCancelled() || !RDrops.hasBlockDropRecipes() || !checkBucketTypes_.contains(event.getBucket())) return;
		
		// Da man bei Klick nie die Plfanzen trifft, einen in die Richtung schauen, die angeklickt wurde.
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		if(block == null) return;
		
		// Muss in der Breakableliste fuer Wasser stehen.
		if(!waterBreakableItems_.contains(block.getType())) return;
		
		BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, null);
		onBlockBreakEvent(blockBreakEvent);
		
		// Wenn der BreakEvent abgebrochen wurde, gabs Custom-Drops.
		if(blockBreakEvent.isCancelled()) block.setType(Material.AIR);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		
		if(event.isCancelled()) return;
		
		if(RDrops.hasBlockDropRecipes()) RBlocks.checkBreakablesAroundPlaced(event.getBlockPlaced(), event.getPlayer());
		
		event.getBlock().setMetadata("lastSet", new FixedMetadataValue(Utils.getPlugin(), Utils.getCurrentServerTime()));
		event.getBlock().setMetadata("lastSetIdAndSubId", new FixedMetadataValue(Utils.getPlugin(), event.getBlockPlaced().getTypeId() + ":" + event.getBlockPlaced().getData()));
		
		RBlocks.setMetaData(event);
	}
	
	private Set<Material> fallingBlockTypes_ = EnumSet.of(Material.SAND, Material.GRAVEL);
	
	/**
	 * Sorgt dafuer, dass Sand und Gravel fuer Drop-Recipes beruecksichtigt
	 * werden.
	 * 
	 * 
	 * @param event
	 * 			ItemSpawnEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
		
		if(!RDrops.hasBlockDropRecipes() || event.isCancelled()) return;
		
		ItemStack item = event.getEntity().getItemStack();
		if(item == null) return;
		
		if(!fallingBlockTypes_.contains(item.getType())) return;
		
		if(event.getLocation().getBlock().getType().equals(Material.AIR)) return;
		
		Block fakeBlock = event.getLocation().getBlock().getRelative(BlockFace.UP);
		
		if(!fakeBlock.getType().equals(Material.AIR)) return;
		
		fakeBlock.setType(item.getType());
		
		BlockBreakEvent fakeEvent = new BlockBreakEvent(fakeBlock, null);
		
		if(onBlockBreakEvent(fakeEvent)) {
			
			event.setCancelled(true);
			
		} else {
			
			fakeBlock.setType(Material.AIR);
		}
	}
	
	/**
	 * @param event
	 * 			BlockBreakEvent
	 * @return
	 * 			Liefert true, wenn ein Custom-Rezept gefunden wurde, andernfalls false.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public static boolean onBlockBreakEvent(BlockBreakEvent event) {
		
		if(event.isCancelled()) return false;
		
		Block block = event.getBlock();
		
		if(block.isLiquid()) return false;
		
		Player player = event.getPlayer();
		
		// Nur wenn es besondere Block-Drop-Rezepte gibt muss geprueft werden ob solche Rezepte
		// betreffend weitere Events ausgeloest werden muessen, andernfalls nicht, um die Performance
		// zu schonen.
		if(RDrops.hasBlockDropRecipes()) {
		
			// Vorab noch schauen, ob der Block ueber dem aktuellen ggf. breakable Items hat.
			RBlocks.checkBreakablesAboveBrocken(block, player);
			
			// Vorab noch schauen, ob Bloecke drum herum ggf. "abfallen"...,
			// vorher noch pruefen ob der Block selbst kein Breakable ist, um moegliche
			// Rekursion zu verhindern.
			if(!RBlocks.isBreakableAroundBrocken(block)) RBlocks.checkBreakablesAroundBroken(block, player);
		}
		
		// Rausfinden ob es individuelle Drops gibt...
		Collection<ItemStack> drops = RDrops.calculateBlockDrops(block, event);
		
		// Rausfinden ob es besondere Displaynamen/Lore/Enchantments auf den Bloecken,
		// gab und ob MetaDaten zurueckzusetzen sind.
		drops = RBlocks.dropSpecialItem(block, event, drops);
		
		// Wenn der Event nicht abgebrochen wurde, dann gab es auch kein Custom-Rezept, also hier aussteigen.
		if(!event.isCancelled()) return false;
		
		block.setTypeId(0);
		
		// Wenn es keine Drops gibt oder der Spieler im GameMode Creative ist oder das Event
		// nicht gecancelt wurde, dann hier abbrechen, da es dann nix mehr zu tun gibt.
		//
		// HINWEIS: Den GameMode erst hier pruefen, damit in Blocks.dropSpecialItem zumindest noch die Meta-Daten
		// eines Blocks zurueck gesetzt werden, wenn ein Block abgebaut wird.
		boolean playerIsInCreative = player != null && player.getGameMode().equals(GameMode.CREATIVE);
		if(playerIsInCreative) return true;
		
		// Da der Event abgebrochen wurde muss der Damage auf dem Tool von Hand gesetzt werden.
		ItemStack tool = null;
		if(player != null) tool = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		
		if(tool != null && Utils.isTool(tool.getType())) {
			
			// Enchantments fuer unbreakable noch checken.
			int damage = 1;
			
			if(RBlockDrops.hasEnchantment(tool, Enchantment.DURABILITY)) {
				
				 double chance = (double) 1 / (double) (1 + RBlockDrops.getEnchantmentLevel(tool, Enchantment.DURABILITY));
				 
				 if(Math.random() > chance) damage = 0;
			} 
			
			short maxDur = tool.getType().getMaxDurability();
			
			tool.setDurability((short) (tool.getDurability() + damage));
			
			if(tool.getDurability() >= maxDur) {
			
				tool.setDurability(maxDur);
				player.setItemInHand(null);
				Utils.playFail(player.getLocation());
			}
			
			Inventories.updateCompleteInventoryScheduled(player, 1);
		}
				
		if(drops == null || drops.isEmpty()) return true;
		
		final Location finalLocation = block.getLocation();
		
		for(ItemStack drop : drops) {
			
			if(drop.getAmount() > 0) finalLocation.getWorld().dropItemNaturally(finalLocation, drop);
		}
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockFromToEvent (BlockFromToEvent event) {
		
		if(event.isCancelled() || !RDrops.hasBlockDropRecipes() || event.getToBlock().getType().equals(event.getBlock().getType())) return;
		
		// Muss in der Breakableliste stehen.
		if(!waterMaterials_.contains(event.getBlock().getType()) || !waterBreakableItems_.contains(event.getToBlock().getType())) return;
		
		BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getToBlock(), null);
		
		onBlockBreakEvent(blockBreakEvent);
		
		if(blockBreakEvent.isCancelled()) {
			
			event.getToBlock().setType(Material.AIR);
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDeathEvent(EntityDeathEvent event) {
		
		// Pruefen ob durch KingdomSide Drops verhindert werden,
		// wenn ja, dann sicherstellen, dass abgebrochen wird. Die Drops
		// und XP werden bereits im KingdomSide-Plugin zurueckgesetzt.
		if(KSideHelper.isPlugin() && event.getEntity().hasMetadata("KingdomSideNoDrops")) return;
		
		if(!RDrops.hasEntityDropRecipes()) return;
		
		RDrops.calculateEntityDrops(event.getEntity(), event);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
		
		if(event.isCancelled() || !RDrops.hasEntityDropRecipes()) return;
		
		Entity entity = event.getEntity();
		
		if(entity == null || !entity.getType().equals(EntityType.SHEEP)) return;
		
		Sheep sheep = (Sheep)entity;
		
		if(!sheep.isAdult() || sheep.isSheared()) return;
		
		String dropRecipeId = RDrops.getEntityRecipeId(entity.getType());
		if(!RDrops.isRawDropRecipe(dropRecipeId)) return;
		
		RDrop foundDropRecipe = RDrops.searchRecipe(event.getPlayer(), dropRecipeId);
		if(foundDropRecipe == null) return;
		
		ItemStack shear = event.getPlayer().getItemInHand();
		short maxDur = shear.getType().getMaxDurability();
		short newDur = (short) (shear.getDurability() + 1);
		
		if(newDur >= maxDur) {
			shear.setDurability(maxDur);
			event.getPlayer().setItemInHand(null);
			Utils.playFail(event.getPlayer().getLocation());
		} else {
			shear.setDurability((short) (newDur));
		}
		
		event.setCancelled(true);
		sheep.setSheared(true);
		
		ArrayList<ItemStack> drops = new ArrayList<>();
		
		if(foundDropRecipe.hasDrops()) {
			
			drops.addAll(RDrops.calculateCustomDrops(foundDropRecipe));
			
		} else {
			
			ArrayList<ItemStack> rawDrops = new ArrayList<>();
			DyeColor sheepColor = sheep.getColor();
			rawDrops.add(new ItemStack(Material.WOOL, (Utils.getRandom(2) + 1), (short) sheepColor.getWoolData()));
			
			drops.addAll(RDrops.calculateChanceDrops(foundDropRecipe, rawDrops));
		}
		
		if(drops == null || drops.isEmpty()) {
			
			sheep.damage(1, (Entity) event.getPlayer());
			
			sheep.setHealth(sheep.getHealth() + 1);
			
			Utils.playFail(sheep.getLocation());
			
			return;
		}
		
		for(ItemStack drop : drops) sheep.getLocation().getWorld().dropItem(sheep.getLocation(), drop);
	}
	
	@EventHandler
	public void onChunkLoadEvent(ChunkLoadEvent event) {
		
		RChunks.registerLoadedChunk(event.getChunk());
	}
	
	@EventHandler
	public void onChunkUnloadEvent(ChunkUnloadEvent event) {
		
		RChunks.unregisterLoadedChunk(event.getChunk());
	}
}