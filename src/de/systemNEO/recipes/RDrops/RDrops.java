package de.systemNEO.recipes.RDrops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.systemNEO.recipes.Constants;
import de.systemNEO.recipes.API.KSideHelper;
import de.systemNEO.recipes.API.WorldGuardHelper;
import de.systemNEO.recipes.RBlockDrops.RBlockDrops;
import de.systemNEO.recipes.RUtils.Utils;

public abstract class RDrops {

	/** Register aller Drop-Rezepte inkl. Alliasen fuer Gruppen */
	private static HashMap<String,RDrop> dropRecipes_ = new HashMap<String,RDrop>();
	
	/** Register fuer alle Drop-Rezepte ohne Gruppen-Zuordnung. */
	private static HashMap<String,RDrop> existingDropRecipes_ = new HashMap<String,RDrop>();
	
	/** Zeigt an, ob es Drop-Rezepte fuer Bloecke gibt... */
	private static Boolean hasBlockDropRecipes_ = false;
	
	/** Zeight an, ob es Drop-Rezepte fuer Entities gibt... */
	private static Boolean hasEntityDropRecipes_ = false;
	
	/** 10 Minuten Zeit einen Block erneut zu zerbrechen... */
	private static final long rebreakTime_ = 1000 * 60 * 10;
	
	/**
	 * @return
	 * 			Liefert die ReBreak-Time fuer Drop-Rezepte betroffene Bloecke.
	 */
	public static long getReBreakTime() {
		
		return rebreakTime_;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn es Drop-Rezepte fuer Bloecke gibt.
	 */
	public static Boolean hasBlockDropRecipes() {
		
		return hasBlockDropRecipes_;
	}
	
	/**
	 * Setzt den Merker ob es Drop-Rezepte fuer Bloecke gibt.
	 * 
	 * @param trueFalse
	 * 			true oder false
	 */
	public static void hasBlockDropRecipes(Boolean trueFalse) {
		
		hasBlockDropRecipes_ = trueFalse;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn es Drop-Rezepte fuer Enities gibt.
	 */
	public static Boolean hasEntityDropRecipes() {
		
		return hasEntityDropRecipes_;
	}
	
	/**
	 * Setzt den Merker ob es Drop-Rezepte fuer Entities gibt.
	 * 
	 * @param trueFalse
	 * 			true oder false
	 */
	public static void hasEntityDropRecipes(Boolean trueFalse) {
		
		hasEntityDropRecipes_ = trueFalse;
	}
	
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
	 * @param hasWildcard
	 * 			Wenn true, wird keine Ruecksicht auf SubIDs genommen.
	 * @return
	 * 			Liefert true, wenn das Drop-Rezept erfolgreich angelegt werden konnte, andernfalls false.
	 */
	public static boolean addDropRecipe(ArrayList<RDropItem> drops, ArrayList<String> groups, Object by, Double mainDropChance, Boolean hasWildcard) {
		
		if(by == null) return false;
		
		RDrop dropRecipe;
		
		if(drops == null || drops.isEmpty()) {
			dropRecipe = new RDrop(mainDropChance, by, hasWildcard);
		} else {
			dropRecipe = new RDrop(drops, by, hasWildcard);
		}
		
		if(!dropRecipe.isValid()) return false;
		
		for(String groupName : groups) dropRecipes_.put(groupName.toLowerCase() + "-" + dropRecipe.getId(), dropRecipe);
		
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
		
		// Erst ohne Wildcard versuchen.
		String dropRecipeId = getBlockRecipeId(block, false);
		if(!isRawDropRecipe(dropRecipeId)) {
			
			// Jetzt noch mit WildCard versuchen
			dropRecipeId = getBlockRecipeId(block, true);
			if(!isRawDropRecipe(dropRecipeId)) return null;
		}
		
		RDrop foundDropRecipe;
		
		if(player == null) {
			
			foundDropRecipe = searchRecipe(block.getLocation(), dropRecipeId);
			
		} else {
			
			foundDropRecipe = searchRecipe(player, dropRecipeId);
		}
		
		if(foundDropRecipe == null) return null;
		
		event.setCancelled(true);
		
		// Dropliste oder Chance?
		if(foundDropRecipe.hasDrops()) {
			
			// Hinweis: bei CustomDrops wird nicht auf ReBreak geachtet, da davon ausgegangen wird, dass CustomDrops
			// sowieso etwas anderes droppen lassen als der Block selber ist.
			return calculateCustomDrops(foundDropRecipe);
		
		} else {
			
			Collection<ItemStack> drops;
			
			if(player != null) {
				
				// Beachtet Tools und Verzauberungen
				// In Bukkit noch kaputt: https://bukkit.atlassian.net/browse/BUKKIT-4094
				// Nachgebaut: http://www.minecraftwiki.net/wiki/Enchanting#Tools
				drops = RBlockDrops.getDrops(block, player.getItemInHand());
				
				int xpToDrop = event.getExpToDrop();
				
				if(xpToDrop > 0) {
					
					ExperienceOrb exp = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
					exp.setExperience(xpToDrop);
				}
			
				
			} else {
				
				drops = block.getDrops();
			}
			
			// Unter der Bedingung, dass der Block das gleiche dropped was er selber ist,
			// kann ein Block zu 100% wieder abgebaut werden, wenn die Platzierung nicht laenger als
			// 10 Minuten her ist.
			if(checkReBreak(block, drops)) return drops;
			
			return calculateChanceDrops(foundDropRecipe, drops);
		}
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param drops
	 * 			Betreffende Drops.
	 * @return
	 * 			Liefert true, wenn der Block innerhalb der letzten 10 Minuten gesetzt wurde und die drops
	 * 			dem Block entsprechen, anderfnalls false.
	 */
	public static boolean checkReBreak(Block block, Collection<ItemStack> drops) {
		
		// 1. Checken ob es MetaDaten fuer den Block gibt.
		if(!block.hasMetadata("lastSet")) return false;
		
		List<MetadataValue> metadatas = block.getMetadata("lastSet");
		long lastSet = 0;
		
		for(MetadataValue metadata : metadatas) {
			
			if(!metadata.getOwningPlugin().equals(Utils.getPlugin())) continue;
			
			lastSet = metadata.asLong();
			break;
		}
		
		// 1.1. Sicherstellen, dass die Meta-Daten entfernt werden.
		block.removeMetadata("lastSet", Utils.getPlugin());
		
		if(drops == null || drops.isEmpty()) return false;
		
		// 2. Bevor der Zeitcheck kommt, erstmal dropliste checken.
		for(ItemStack drop : drops) {
			
			if(drop.getTypeId() != block.getTypeId() || drop.getDurability() != (short) block.getData()) return false;
		}
		
		// 3. Jetzt Zeit checken.
		long maxPastTime = Utils.getCurrentServerTime() - getReBreakTime();
		
		return lastSet >= maxPastTime;
	}
	
	/**
	 * @param entity
	 * 			Betreffendes Entity.
	 * @param event
	 * 			Entity-Death-Event.
	 */
	public static void calculateEntityDrops(Entity entity, EntityDeathEvent event) {
		
		String dropRecipeId = getEntityRecipeId(entity.getType());
		if(!isRawDropRecipe(dropRecipeId)) return;
		
		LivingEntity livingEntity = (LivingEntity) entity;
		Player player = livingEntity.getKiller();
		RDrop foundDropRecipe;
		
		if(player != null) {
		
			foundDropRecipe = searchRecipe(player, dropRecipeId);
			
		} else {
			
			foundDropRecipe = searchRecipe(entity.getLocation(), dropRecipeId);
		}
		
		if(foundDropRecipe == null) return;
		
		List<ItemStack> rawDrops = new ArrayList<ItemStack>(event.getDrops());
		event.getDrops().clear();
		
		if(foundDropRecipe.hasDrops()) {
			
			event.getDrops().addAll(calculateCustomDrops(foundDropRecipe));
			
		} else {
			
			event.getDrops().addAll(calculateChanceDrops(foundDropRecipe, rawDrops));
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
			
		Collection<ItemStack> calculatedDrops = new ArrayList<ItemStack>();
		if(chance == 0) return calculatedDrops;
		
		int amount = 0;
		int resultAmount = 0;
		
		for(ItemStack rawDrop : rawDrops) {
			
			amount = rawDrop.getAmount();
			if(amount == 0) continue;
			
			resultAmount = 0;
			
			for(int pos = 1; pos <= amount; ++pos) {
				
				double tmpChance = Math.random() * 100;
				
				if(tmpChance <= chance) ++resultAmount;
			}
			
			if(resultAmount == 0) continue;
			
			ItemStack resultItem = rawDrop.clone();
			resultItem.setAmount(resultAmount);
			calculatedDrops.add(resultItem);
		}
		
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
			
			if(dropRecipes_.containsKey(groupName.toLowerCase() + "-" + dropRecipeId)) {
				
				return dropRecipes_.get(groupName.toLowerCase() + "-" + dropRecipeId);
			}
		}
		
		return null;
	}
	
	/**
	 * TODO Prioritaet nochmals bei FromToBlock checken, um nicht wieder Gefahr zu laufen andere
	 * Plugins wie WorldGuard oder dergleichen auszuhebeln.
	 * 
	 * @param location
	 * 			Betreffende Position.
	 * @param dropRecipeId
	 * 			Betreffendes Drop-Rezept bzw. dessen ID.
	 * @return
	 * 			Liefert bei Erfolg ein Rezept passend zur Position (WorldGuardRegion -> Kingdoms/Usergruppen),
	 * 			andernfalls null.
	 */
	public static RDrop searchRecipe(Location location, String dropRecipeId) {
		
		// 1. Erstmal die Regionen des Blocks holen (falls vorhanden).
		ArrayList<ProtectedRegion> locationRegions = WorldGuardHelper.getLocationRegions(location);
		
		HashSet<String> foundGroups = new HashSet<>();
		
		if(locationRegions != null && !locationRegions.isEmpty()) {
		
			// Standard PermissionEX Modus
			for(ProtectedRegion locationRegion : locationRegions) {
				
				ArrayList<String> groupsOfRegion = WorldGuardHelper.getGroupsOfRegion(locationRegion);
				if(groupsOfRegion == null || groupsOfRegion.isEmpty()) continue;
				
				foundGroups.addAll(groupsOfRegion);
			}
		
			// KingdomSide-Modus
			if(KSideHelper.isPlugin()) {
				
				String kingdomName = KSideHelper.getKingdomByRegions(locationRegions);
				if(kingdomName != null && !kingdomName.isEmpty()) foundGroups.add(KSideHelper.toGroupName(kingdomName));	
			}
		}
		
		// Default adden
		foundGroups.add(Constants.GROUP_GLOBAL.toLowerCase());
		
		for(String foundGroup : foundGroups) {
			
			if(dropRecipes_.containsKey(foundGroup.toLowerCase() + "-" + dropRecipeId)) {
				
				return dropRecipes_.get(foundGroup.toLowerCase() + "-" + dropRecipeId);
			}
		}
		
		return null;
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param hasWildcard
	 * 			True, wenn SubID keine Rolle spielt.
	 * @return
	 * 			Liefert eine aus Block-ID und SubID zusammengesetzte ID zurueck.
	 */
	public static String getBlockRecipeId(Block block, Boolean hasWildcard) {
		
		return getBlockRecipeId(block.getTypeId(), (short) block.getData(), hasWildcard);
	}
	
	/**
	 * @param itemStack
	 * 			Betreffender ItemStack.
	 * @param hasWildcard
	 * 			True, wenn SubID keine Rolle spielt.
	 * @return
	 * 			Liefert eine aus ItemStack-ID und SubID zusammengesetzte ID zurueck.
	 */
	public static String getBlockRecipeId(ItemStack itemStack, Boolean hasWildcard) {
		
		return getBlockRecipeId(itemStack.getTypeId(), itemStack.getDurability(), hasWildcard);
	}
	
	/**
	 * @param itemId
	 * 			ItemID.
	 * @param subId
	 * 			SubID.
	 * @param hasWildcard
	 * 			True, wenn SubID keine Rolle spielt.
	 * @return
	 * 			Erstellt aus ItemID und SubID eine zusammengesetzte konforme ID.
	 */
	public static String getBlockRecipeId(Integer itemId, Short subId, Boolean hasWildcard) {
		
		if(hasWildcard != null && hasWildcard) {
			
			return Utils.formatTypeId(itemId) + "-*";
			
		} else {
			
			return Utils.formatTypeId(itemId) + "-" + Utils.formatSubId(subId);
		}
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
