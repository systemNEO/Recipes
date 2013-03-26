package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
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
 *      
 * @author Hape
 * 
 */
public final class Recipes extends JavaPlugin implements Listener {	
	
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
		
		// Sicherstellen, dass groups existiert
		if(groups == null || groups.size() == 0) {
			groups = new ArrayList<String>();
			groups.add(Constants.GROUP_GLOBAL.toLowerCase());
		}
		
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
		
		for(int i = 0; i < 10; ++i) stacks[i] = new ItemStack(originalStacks[i]);
		
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
		
		// Rezepte direkt durchsuchen.
		for(String group : groups) {
			
			if(isRecipe(group, index)) return group.toLowerCase() + "_" + index;
		}
		
		// Wenn nix gefunden, dann aliase durchwuehlen.
		HashMap<String,String> aliases;
		String shapeIndexToTest;
		
		for(String group : groups) {
			
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

					cloneRecipe(group, oldGroupIndex[1], index, null);
					
					return shapeIndexToTest;
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
		
		Utils.logInfo(foundRecipes.size() + " found!");
		
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
			}
			
			if(recipe instanceof ShapelessRecipe) {
				
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

			if(args.length == 0) return false;
			
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
		
		String currentRecipeIndex = (String) Utils.getMetadata(player, "currentRecipe");
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
		
		// Das Crafting-Event canceln,
		event.setCancelled(true);
		
		// 1.1. Hat der Spieler bereits etwas im Cursor, muss das Item
		// mit dem Result identisch sein, sonst abbrechen. Spaeter wird
		// dann bei Klick mit gleichem Item, einfach addiert. (Auch auf
		// SubIds achten -> getDurability.)
		if(event.getCursor().getTypeId() != 0 && !Stacks.compareStacks(event.getCursor(), result)) return;
		
		int resultAmount = result.getAmount();
		int resultCount = 1;
		int times = 1;
		
		// 2. Crafting-Grid entsprechend korrigieren, Kosten abziehen
		if(event.isShiftClick()) {
			
			// Hier muss der Stack der rausgegeben wird korrekt berechnet werden.
			// Einfach zusammenrechnen, wie oft ein Item pro Slot craftbar waere
			// und die geringste Zahl gewint.
			times = Results.calculateResults(craftStacks, recipeShape, slotMatrix);
			
			// Bei ShiftClick muss das Click-Event abgebrochen werden, weil sonst
			// das CraftingEvent einfach ausgefuehrt wird und man nichts dagegen machen
			// koennte
			if(originalEvent != null) originalEvent.setCancelled(true);
		}
		
		// Wenn nix erstellt werden kann, dann resetten.
		if(times == 0) {
			
			// Da nix gefunden, das Result leeren.
			player.getOpenInventory().getTopInventory().setItem(0, null);
			
			Inventories.updateInventoryScheduled(player, 1);
			
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
		
		// Lagprotection (Falls es laggt damit verhindern, dass trotz des verzoegerten
		// updateInventory-Calls keiner waehrend des Lags etwas rausnehmen kann).
		Utils.setMetadata(player, "lagLock", true);
		
		// 1 Tick spaeter, weil man dann quasi nach dem Event ins Inventory schaut
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				Inventories.updateInventory(player);				
			}
		}, 1);
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event) {
		
		Blocks.setMetaData(event);
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		
		Block block = event.getBlock();
		
		if(block.isLiquid() || event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
		
		Blocks.dropSpecialItem(block, event);	
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