package de.systemNEO.recipes;

import java.util.Collection;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	 * Prueft ob der Block Metadaten hatte, wenn ja, dann muss ein Item mit diesen Metadaten
	 * als ItemMeta-Daten gedropped werden, um zu vermeiden, dass Lore und Displaynames verloren
	 * gehen.
	 * @param block
	 * 			Der betreffende Block.
	 * @param event
	 * 			Das betreffende BlockBreak-Event.
	 */
	public static void dropSpecialItem(Block block, BlockBreakEvent event) {
		
		Collection<ItemStack> drops = event.getBlock().getDrops();
		
		if(drops == null || drops.isEmpty()) return;
		
		for(ItemStack drop : drops) {
			
			ItemMeta md = drop.getItemMeta();
			
			if(block.hasMetadata("displayName")) {
				
				String displayName = (String) block.getMetadata("displayName").get(0).value();
				
				if(displayName != null && displayName instanceof String) md.setDisplayName(displayName);
			}
			
			if(block.hasMetadata("lore")) {
				
				@SuppressWarnings("unchecked")
				List<String> lore = (List<String>) block.getMetadata("lore").get(0).value();
				
				if(lore != null && lore instanceof List<?>) md.setLore(lore);
			}
			
			drop.setItemMeta(md);
			
			block.getLocation().getWorld().dropItem(block.getLocation(), drop);
			
			block.setTypeId(0);
		}
		
		event.setCancelled(true);	
	}
	
	/**
	 * Prueft ob ein zu setzender Block ItemMeta-Daten hat, wenn ja werden
	 * diese in dem Block als interne Metadaten gespeichert werden, damit man
	 * bei BlockBreakEvent diese Werte wieder herstellen kann.
	 * @param event
	 * 			Betreffender BlockPlaceEvent.
	 */
	public static void setMetaData(BlockPlaceEvent event) {
		
		Block block = event.getBlockPlaced();
		
		ItemMeta itemMeta = event.getItemInHand().getItemMeta();
		
		if(itemMeta == null) return;
		
		if(itemMeta.hasDisplayName()) block.setMetadata("displayName", createMetaDataValue(itemMeta.getDisplayName()));
		
		if(itemMeta.hasLore()) block.setMetadata("lore", createMetaDataValue(itemMeta.getLore()));
	}
}
