package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

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
	 * @param stacks 1 Resultstack plus 9 ItemStacks, je Position im Rezept.
	 * @param pexGroup Einschraenkung auf Gruppe.
	 */
	public static boolean createCustomRecipe(ItemStack[] stacks, ArrayList<String> groups, String type, String resultMessage) {
		
		// Nix gescheites in der ItemStack[]-Liste, dann Abbruch.
		if(stacks == null || stacks.length < 10) return false;
		
		// Sicherstellen, dass groups existiert
		if(groups == null || groups.size() == 0) {
			groups = new ArrayList<String>();
			groups.add(Constants.GROUP_GLOBAL);
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
	public static void setRecipe(String group, String index, ItemStack[] stacks, String type, ItemStack[][] shape, String resultMessage) {
	
		setRecipeOriginal(group, index, stacks);
		setRecipeType(group, index, type);
		Results.setRecipeResult(group, index, stacks[0]);
		Shapes.setRecipeShape(group, index, shape);
		setRecipeResultMessage(group, index, resultMessage);
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
		
		for(String group : groups) {
			
			if(isRecipe(group, index)) return group.toLowerCase() + "_" + index;
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
		
		ItemStack[] shape;
		ItemStack[] ingredients;
		char[] line;
		boolean found;
		String type = Constants.SHAPE_DEFAULT;
		int pos;
		
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
			
			shape[0] = new ItemStack(Material.AIR, 0);
			
			return createCustomRecipe(shape, groups, type, null);
		}
		
		return false;
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
			finalResultStack.setAmount(finalStack);
			
			player.setItemOnCursor(finalResultStack);
			
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
		
		// 1 Tick spaeter, weil man dann quasi nach dem Event ins Inventory schaut
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				
				Inventories.updateInventory(player);
			}
		}, 1);
	}
}