package de.systemNEO.recipes.RDrops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.RUtils.Utils;

public abstract class RDrops {

	/** Register aller Drop-Rezepte inkl. Alliasen fuer Gruppen */
	private static HashMap<String,RDrop> dropRecipes_ = new HashMap<String,RDrop>();
	
	/** Register fuer alle Drop-Rezepte ohne Gruppen-Zuordnung. */
	private static HashMap<String,RDrop> existingDropRecipes_ = new HashMap<String,RDrop>();
	
	/**
	 * Setzt das Register aller Drop-Rezepte zurueck.
	 */
	public static void reset() {
		
		dropRecipes_.clear();
		existingDropRecipes_.clear();
	}
	
	/**
	 * Erstellt ein Drop-Rezept und registriert dies im Plugin fuer die Ueberwachung.
	 * 
	 * @param drops
	 * 			Drops (schliesst mainDropChance aus).
	 * @param groups
	 * 			Erlaubte Gruppen.
	 * @param by
	 * 			Verursacher (Block oder Entity).
	 * @param mainDropChance
	 * 			Dropchance fuer alle Items des Rezeptes (schliesst drops aus).
	 * @return
	 * 			Liefert true, wenn das Drop-Rezept erfolgreich angelegt werden konnte, andernfalls false.
	 */
	public static boolean addDropRecipe(ArrayList<RDropItem> drops, ArrayList<String> groups, Object by, Double mainDropChance) {
		
		if(by == null) return false;
		
		RDrop dropRecipe;
		
		if(mainDropChance != null && mainDropChance != 100.0) {
			dropRecipe = new RDrop(mainDropChance, by);
		} else {
			dropRecipe = new RDrop(drops, by);
		}
		
		if(!dropRecipe.isValid()) return false;
		
		for(String groupName : groups) dropRecipes_.put(groupName + "-" + dropRecipe.getId(), dropRecipe);
		
		existingDropRecipes_.put(dropRecipe.getId(), dropRecipe);
		
		return true;
	}
	
	/**
	 * @param id
	 * 			Zu testende Drop-Rezept ID.
	 * @return
	 * 			Liefert true, wenn ein Drop-Rezept zu dieser ID existiert, andernfalls false.
	 */
	public static boolean isRawDropRecipe(String id) {
		
		return existingDropRecipes_.containsKey(id);
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param event
	 * 			Block-Break-Event.
	 * @return
	 * 			Liefert null, wenn es kein Drop-Rezept zum Block gab, andernfalls die zu ersetzenden Drops.
	 */
	public static Collection<ItemStack> calculateBlockDrops(Block block, BlockBreakEvent event) {
		
		Player player = event.getPlayer();
		
		String dropRecipeId = getBlockRecipeId(block);
		
		if(!isRawDropRecipe(dropRecipeId)) return null;
		
		RDrop foundDropRecipe;
		if(player == null) {
			foundDropRecipe = searchRecipe(block.getLocation(), dropRecipeId);
		} else {
			foundDropRecipe = searchRecipe(player, dropRecipeId);
		}
		if(foundDropRecipe == null) return null;
		
		event.setCancelled(true);
		
		if(foundDropRecipe.hasDrops()) {
			
			return calculateCustomDrops(foundDropRecipe);
		
		} else {
			
			return calculateChanceDrops(foundDropRecipe, block.getDrops());
		}
	}
	
	/**
	 * @param entity
	 * 			Betreffendes Entity.
	 * @param event
	 * 			Entity-Death-Event.
	 */
	public static void calculateEntityDrops(Entity entity, EntityDeathEvent event) {
		
		LivingEntity livingEntity = (LivingEntity) entity;
		Player player = livingEntity.getKiller();
		
		if(player == null) return;
		
		String dropRecipeId = getEntityRecipeId(entity.getType());
		if(!isRawDropRecipe(dropRecipeId)) return;
		
		RDrop foundDropRecipe = searchRecipe(player, dropRecipeId);
		if(foundDropRecipe == null) return;
		
		event.getDrops().clear();
		
		if(foundDropRecipe.hasDrops()) {
			
			event.getDrops().addAll(calculateCustomDrops(foundDropRecipe));
			
		} else {
			
			event.getDrops().addAll(calculateChanceDrops(foundDropRecipe, event.getDrops()));
		}
	}
	
	/**
	 * @param foundDropRecipe
	 * 			Betreffendes Rezept.
	 * @return
	 * 			Liefert eine Liste an Drops aus dem uebergebenen Rezept, inkl. Chancen-Berechnung.
	 */
	public static Collection<ItemStack> calculateCustomDrops(RDrop foundDropRecipe) {
		
		ArrayList<RDropItem> recipeDrops = foundDropRecipe.getDrops();
		Collection<ItemStack> calculatedDrops = new ArrayList<ItemStack>();
		
		for(RDropItem recipeDrop : recipeDrops) {
			
			ItemStack resultItem = recipeDrop.getItem().clone();
			int amount = resultItem.getAmount();
			int dropChance = recipeDrop.getDropChance();
			
			if(dropChance != 100) {
				
				int resultAmount = 0;
				
				for(int i = 1; i <= amount; ++i) {
					
					if((Math.random() * 100) <= dropChance) ++resultAmount;
				}
				
				amount = resultAmount;
			}
			
			if(amount == 0) continue;
			
			resultItem.setAmount(amount);
			
			calculatedDrops.add(resultItem);
		}
		
		return calculatedDrops;
	}
	
	/**
	 * @param foundDropRecipe
	 * 			Betreffendes Drop-Rezept.
	 * @param rawDrops
	 * 			Urspruengliche Drop-Liste.
	 * @return
	 * 			Liefert die Original-Dropliste nachberechnet per Drop-Chance.
	 */
	public static Collection<ItemStack> calculateChanceDrops(RDrop foundDropRecipe, Collection<ItemStack> rawDrops) {
		
		Double chance = foundDropRecipe.getChance();
		if(chance == null || chance == 100.0 || rawDrops == null || rawDrops.isEmpty()) return rawDrops;
			
		Utils.logInfo("1");
		
		Collection<ItemStack> calculatedDrops = new ArrayList<ItemStack>();
		if(chance == 0) return calculatedDrops;
		
		Utils.logInfo("2");
		
		int amount = 0;
		int resultAmount = 0;
		
		for(ItemStack rawDrop : rawDrops) {
			
			amount = rawDrop.getAmount();
			if(amount == 0) continue;
			
			resultAmount = 0;
			
			Utils.logInfo(amount + " anzahl " + rawDrop.getType().toString());
			
			for(int pos = 1; pos <= amount; ++pos) {
				
				double tmpChance = Math.random() * 100;
				
				Utils.logInfo(tmpChance + "%" + chance);
				
				if(tmpChance <= chance) {
					
					Utils.logInfo("YEAH");
					++resultAmount;
				}
			}
			
			if(resultAmount == 0) continue;
			
			ItemStack resultItem = rawDrop.clone();
			resultItem.setAmount(resultAmount);
			calculatedDrops.add(resultItem);
		}
		
		Utils.logInfo(calculatedDrops.size() + "ha?");
		
		return calculatedDrops;
	}
	
	/**
	 * @param player
	 * 			Betreffender Spieler.
	 * @param dropRecipeId
	 * 			Basis-Drop-Rezept.
	 * @return
	 * 			Liefert passend zu den Spielergruppen ein ggf. vorhandenes Drop-Rezept, andernfalls null.
	 */
	public static RDrop searchRecipe(Player player, String dropRecipeId) {
		
		String[] playerGroups = Utils.getPlayerGroups(player);
		
		for(String groupName : playerGroups) {
			
			if(dropRecipes_.containsKey(groupName + "-" + dropRecipeId)) {
				
				return dropRecipes_.get(groupName + "-" + dropRecipeId);
			}
		}
		
		return null;
	}
	
	/**
	 * TODO Hier muss einmal nachgeschaut werden, welche Regionen WorldGuard fuer die Position
	 * ausspuckt, dann muss entweder aus der Region die Permissions und darueber die PEX-Gruppen
	 * gefunden werden bzw. alternativ das Koenigreich.
	 * 
	 * TODO Nachdokumentieren.
	 * 
	 * @param location
	 * @param dropRecipeId
	 * @return
	 */
	public static RDrop searchRecipe(Location location, String dropRecipeId) {
		
		return null;
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @return
	 * 			Liefert eine aus Block-ID und SubID zusammengesetzte ID zurueck.
	 */
	public static String getBlockRecipeId(Block block) {
		
		return getBlockRecipeId(block.getTypeId(), (short) block.getData());
	}
	
	/**
	 * @param itemStack
	 * 			Betreffender ItemStack.
	 * @return
	 * 			Liefert eine aus ItemStack-ID und SubID zusammengesetzte ID zurueck.
	 */
	public static String getBlockRecipeId(ItemStack itemStack) {
		
		return getBlockRecipeId(itemStack.getTypeId(), itemStack.getDurability());
	}
	
	/**
	 * @param itemId
	 * 			ItemID.
	 * @param subId
	 * 			SubID.
	 * @return
	 * 			Erstellt aus ItemID und SubID eine zusammengesetzte konforme ID.
	 */
	public static String getBlockRecipeId(Integer itemId, Short subId) {
		
		return Utils.formatTypeId(itemId) + "-" + Utils.formatSubId(subId);
	}
	
	/**
	 * @param entityType
	 * 			Betreffender EntityType.
	 * @return
	 * 			Liefert eine aus dem EntityType abgeleitete ID.
	 */
	public static String getEntityRecipeId(EntityType entityType) {
		
		return entityType.getName();
	}
}
