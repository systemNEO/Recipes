package de.systemNEO.recipes.RBlocks;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.systemNEO.recipes.Recipes;
import de.systemNEO.recipes.RChunks.RChunk;
import de.systemNEO.recipes.RChunks.RChunks;

public abstract class RBlocks {

	/**
	 * Liste an Materialien die um einen Block herum zerbrechen, wenn der Block platziert
	 * wird.
	 */
	private static final Set<Material> breakablesAroundPlacedBlock_ = EnumSet.of(Material.CACTUS, Material.SUGAR_CANE_BLOCK);
	
	/**
	 * Liste an Materialien die um einen Block herum zerbrechen, wenn der Block entfernt
	 * wird.
	 */
	private static final Set<Material> breakablesAroundBrokenBlock_ = EnumSet.of(Material.COCOA);
	
	/**
	 * Liste an Materialien die um einen bewegten Block herum zerbrechen, wenn der Block
	 * bewegt wird.
	 */
	private static final Set<Material> breakablesAroundMovedBlock_ = EnumSet.of(Material.CACTUS, Material.COCOA);
	
	/** 
	 * Liste an Materialien die oberhalb eines Blocks zerbrechen, wenn der Block darunter
	 * entfernt wurde.
	 */
	private static final Set<Material> generalBreakablesAboveMovedOrBrokenBlock_ = EnumSet.of(
		Material.CROPS,
		Material.CACTUS,
		Material.POTATO,
		Material.CARROT,
		Material.SUGAR_CANE_BLOCK
	);
	
	/** 
	 * Liste an Materialien die oberhalb eines Blocks zerbrechen, wenn der Block darunter
	 * bewegt wurde.
	 */
	private static final Set<Material> breakablesAboveMovedUpDownBlock_ = EnumSet.of(
		Material.CROPS,
		Material.CACTUS,
		Material.POTATO,
		Material.CARROT,
		Material.SUGAR_CANE_BLOCK,
		Material.MELON_BLOCK,
		Material.PUMPKIN
	);
	
	/**
	 * Liste an Seiten um einen Block herum, die rund herum fuer breakables geprueft werden sollen.
	 */
	private static final Set<BlockFace> aroundFaces_ = EnumSet.of(BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST);
	
	/**
	 * Liste an Seiten oberhalb eines Blocks, die fuer breakables geprueft werden sollen.
	 */
	private static final Set<BlockFace> aboveFaces_ = EnumSet.of(BlockFace.UP);
	
	/**
	 * Liste aller Richtungen fuer einen Hoch/Runter Bewegung.
	 */
	private static final Set<BlockFace> upDownFaces_ = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
	
	/**
	 * Prueft ob an der Position des Blocks MetaDaten vermerkt waren, wenn ja
	 * dann werden die Drops des gleichen Materials wie des damals gespeicherten
	 * Blocks mit den MetaDaten versehen und die MetaDaten in der Konfig resettet.
	 * 
	 * @param block
	 * 			Der betreffende Block.
	 * @param event
	 * 			Das betreffende BlockBreak-Event.
	 * @param drops
	 * 			Die zu pruefenden Drops.
	 */
	public static Collection<ItemStack> dropSpecialItem(Block block, BlockBreakEvent event, Collection<ItemStack> drops) {
		
		if(drops == null) drops = block.getDrops();
		
		// Gibt es keine Drops, dann Ende.
		if(drops == null || drops.isEmpty()) return drops;
		
		RChunk rChunk = RChunks.getRChunk(block);
		String blockRID = RChunks.getBlockRID(block);
		ItemStack blockItem = rChunk.getBlockItem(blockRID);
		
		// Gabs keine MetaDaten an der Position des Blocks, dann Ende.
		if(blockItem == null) return drops;
		
		for(ItemStack drop : drops) {
			
			if(drop.getType().equals(blockItem.getType())) {
				
				drop.setItemMeta(blockItem.getItemMeta());
			}
		}
		
		event.setCancelled(true);
		
		rChunk.resetMetaData(blockRID);
		
		return drops;
	}
	
	/**
	 * Prueft ob ein zu setzender Block ItemMeta-Daten hat, wenn ja werden
	 * diese in dem Block als interne Metadaten gespeichert werden, damit man
	 * bei BlockBreakEvent diese Werte wieder herstellen kann.
	 * 
	 * Falls ein Block-Metadaten hat, aber der neue zu setzende Block nicht, dann
	 * trotzdem die Metadaten loeschen, da ja ggf. vorher per WorldEdit was weg-
	 * gerissen wurde, was das Plugin nicht abfangen kann.
	 * 
	 * @param event
	 * 			Betreffender BlockPlaceEvent.
	 */
	public static void setMetaData(BlockPlaceEvent event) {
		
		Block block = event.getBlockPlaced();
		ItemStack itemInHand = event.getItemInHand();
		RChunk rChunk = RChunks.getRChunk(block);
		
		ItemMeta itemMeta = null;
		
		if(block.getType().equals(itemInHand.getType())) itemMeta = itemInHand.getItemMeta();
		
		rChunk.setMetaData(block, itemMeta);
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @return
	 * 			Liefert true, wenn der "zerbrochene" Block selbst ein umliegendes Breakable ist,
	 * 			andernfalls false.
	 */
	public static boolean isBreakableAroundBrocken(Block block) {
		
		return breakablesAroundBrokenBlock_.contains(block.getType());
	}
	
	/**
	 * Prueft oberhalb eines Blocks ob dort Materialien sind, die zerfallen, wenn der Block
	 * zerstoert wurde.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAboveBrocken(Block block, Player player) {
		
		checkBreakablesAround(generalBreakablesAboveMovedOrBrokenBlock_, aboveFaces_, block, player);
	}
	
	/**
	 * Prueft um einen Block herum ob dort Materialien sind, die zerfallen, wenn der Block
	 * zerstoert wurde.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAroundBroken(Block block, Player player) {

		checkBreakablesAround(breakablesAroundBrokenBlock_, aroundFaces_, block, player);
	}
	
	/**
	 * Prueft um einen Block herum ob dort Materialien sind, die zerfallen, wenn der Block
	 * platziert wurde.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAroundPlaced(Block block, Player player) {
		
		checkBreakablesAround(breakablesAroundPlacedBlock_, aroundFaces_, block, player);
	}
	
	/**
	 * Prueft um einen Block herum ob dort Materialien sind, die zerfallen, wenn der Block
	 * bewegt wurde.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAroundMoved(Block block, Player player) {
		
		checkBreakablesAround(breakablesAroundMovedBlock_, aroundFaces_, block, player);
	}

	/**
	 * Prueft oberhalb eines Blocks ob dort Materialien sind, die zerfallen, wenn der Block
	 * bewegt wurde.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAboveMoved(BlockFace direction, Block block, Player player) {
		
		if(upDownFaces_.contains(direction)) {
			
			// Hoch oder runter (Da gehen auch Pumpkins & Co kaputt).
			checkBreakablesAround(breakablesAboveMovedUpDownBlock_, aboveFaces_, block, player);
			
		} else {
			
			// Seitwaerts...
			checkBreakablesAround(generalBreakablesAboveMovedOrBrokenBlock_, aboveFaces_, block, player);
		}
	}
	
	/**
	 * Prueft um einen Block herum ob dort Materialien sind, die zerfallen, wenn der Block
	 * bewegt, platiert, entfernt wurde.
	 * 
	 * @param materials
	 * 			Relevante Materialien.
	 * @param directions
	 * 			Relevante Richtungen.
	 * @param block
	 * 			Betreffender Block.
	 * @param player
	 * 			Optional: Ausloesender Spieler.
	 */
	public static void checkBreakablesAround(Set<Material> materials, Set<BlockFace> directions, Block block, Player player) {
		
		for(BlockFace aroundFace : directions) {
			
			Block aroundBlock = block.getRelative(aroundFace);
			if(aroundBlock == null || !materials.contains(aroundBlock.getType())) continue;
			
			BlockBreakEvent blockBreakEventAround = new BlockBreakEvent(aroundBlock, player);
			
			Recipes.onBlockBreakEvent(blockBreakEventAround);
		}
	}
}
