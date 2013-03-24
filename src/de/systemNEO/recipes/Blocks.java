package de.systemNEO.recipes;

import java.util.Collection;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public abstract class Blocks {

	/**
	 * @param value
	 * 			Der Wert fuer das MetadataValue.
	 * @return
	 * 			Liefert ein MetadataValue.
	 */
	public static MetadataValue createMetaDataValue(final Object value) {
		
		final Plugin plugin = Utils.getPlugin();
		
		return new MetadataValue() {
			
			@Override
			public Object value() {
				
				return value;
			}
			
			@Override
			public void invalidate() {	
			}
			
			@Override
			public Plugin getOwningPlugin() {
				return plugin;
			}
			
			@Override
			public String asString() {
				return null;
			}
			
			@Override
			public short asShort() {
				return 0;
			}
			
			@Override
			public long asLong() {
				return 0;
			}
			
			@Override
			public int asInt() {
				return 0;
			}
			
			@Override
			public float asFloat() {
				return 0;
			}
			
			@Override
			public double asDouble() {
				return 0;
			}
			
			@Override
			public byte asByte() {
				return 0;
			}
			
			@Override
			public boolean asBoolean() {
				return false;
			}
		};
	}

	/**
	 * Prueft ob an der Position des Blocks MetaDaten vermerkt waren, wenn ja
	 * dann werden die Drops des gleichen Materials wie des damals gespeicherten
	 * Blocks mit den MetaDaten versehen und die MetaDaten in der Konfig resettet.
	 * 
	 * @param block
	 * 			Der betreffende Block.
	 * @param event
	 * 			Das betreffende BlockBreak-Event.
	 */
	public static void dropSpecialItem(Block block, BlockBreakEvent event) {
		
		Collection<ItemStack> drops = block.getDrops();
		
		// Gibt es keine Drops, dann Ende.
		if(drops == null || drops.isEmpty()) return;
		
		RChunk rChunk = RChunks.getRChunk(block);
		String blockRID = RChunks.getBlockRID(block);
		ItemStack blockItem = rChunk.getBlockItem(blockRID);
		
		// Gabs keine MetaDaten an der Position des Blocks, dann Ende.
		if(blockItem == null) return;
		
		for(ItemStack drop : drops) {
			
			if(drop.getType().equals(blockItem.getType())) {
				
				drop.setItemMeta(blockItem.getItemMeta());
			}
			
			block.getLocation().getWorld().dropItem(block.getLocation(), drop);
			
			block.setTypeId(0);
		}
		
		event.setCancelled(true);
		
		rChunk.resetMetaData(blockRID);
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
