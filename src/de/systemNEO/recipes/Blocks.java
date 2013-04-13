package de.systemNEO.recipes;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.RChunks.RChunk;
import de.systemNEO.recipes.RChunks.RChunks;

public abstract class Blocks {

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
		RChunk rChunk = RChunks.getRChunk(block);
		
		rChunk.setMetaData(block, event.getItemInHand().getItemMeta());
	}
}
