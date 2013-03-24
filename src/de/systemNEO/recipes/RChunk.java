package de.systemNEO.recipes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RChunk {

	/** Original Bukkit Chunk */
	private Chunk bukkitChunk_ = null;
	
	/** Konfigurationsname des aktuellen Chunks (WORLDID-X-Z.yml). */
	private String configFileName_ = null;
	
	/** Konfigurationsdatei des aktuellen Chunks. */
	private File configFile_ = null;
	
	/** YAML Konfiguration des aktuellen Chunks. */
	private YamlConfiguration config_ = null;
	
	/** Alle vermerkten Bloecke des Chunks die Metadaten haben. */
	private HashMap<String,ItemStack> blocks_ = new HashMap<String,ItemStack>();
	
	public RChunk(Chunk bukkitChunk) {
		
		bukkitChunk_ = bukkitChunk;
		
		configFileName_ = RChunks.getPathAndFileNameOfChunk(bukkitChunk);
	}
	
	/**
	 * @return Liefert die Recipes interne ID des Chunks.
	 */
	public String getId() {
		
		return configFileName_;
	}
	
	/**
	 * Versucht die Konfig passend zum Chunk zu laden und die darin enthaltenen
	 * Bloecke in den Cache  zu laden.
	 */
	private void loadConfig() {
		
		configFile_ = new File(RChunks.getChunkFolder(), configFileName_);
		
		config_ = YamlConfiguration.loadConfiguration(configFile_);
		
		blocks_.clear();
		
		Set<String> configKeys = config_.getKeys(false);
		
		if(configKeys.isEmpty()) return;
	    
	    for(String key : configKeys) blocks_.put(key, config_.getItemStack(key));
	}
	
	/**
	 * Speichert die Konfiguration des aktuellen Chunks.
	 */
	private void saveConfig() {
		
		if(blocks_.isEmpty()) {
			
			configFile_.delete();
			
		} else {
		
			try {
				config_.save(configFile_);
			} catch (IOException e) {
				Utils.logInfo("&4Could not save chunk data to " + configFileName_ + "!");
			}
		}
	}
	
	/**
	 * @return Liefert den Original Bukkit Chunk des Recipe-Chunk-Objektes.
	 */
	public Chunk getBukkitChunk() {
		
		return bukkitChunk_;
	}
	
	/**
	 * Setzt fuer die uebergebene Block-Recipe-ID die Metadaten zurueck.
	 * 
	 * @param blockRID
	 * 			Betreffende Block-Recipe-ID.
	 */
	public void resetMetaData(String blockRID) {
		
		if(!hasMetaData(blockRID)) return;
		
		checkConfig();
		
		blocks_.remove(blockRID);
		
		config_.set(blockRID, null);
		
		saveConfig();
	}
	
	/**
	 * Setzt fuer die uebergebene Block-Recipe-ID Metadaten an der Position des Blocks.
	 * 
	 * Werden keine Metadaten uebergeben, wird lediglich geprueft ob an der Position
	 * des Blocks zuvor Metadaten waren, wenn ja werden sie zurueckgesetzt.
	 * 
	 * @param block
	 * 			Betreffender Block.
	 * @param itemMeta
	 * 			Optional: Zu setzende Metadaten.
	 */
	public void setMetaData(Block block, ItemMeta itemMeta) {
		
		String blockRID = RChunks.getBlockRID(block);
		
		boolean hasMetaData = itemMeta != null && (itemMeta.hasDisplayName() || itemMeta.hasEnchants() || itemMeta.hasLore());

		if(!hasMetaData) {
			
			resetMetaData(blockRID);
			
			return;
		}

		checkConfig();
		
		ItemStack itemStack = new ItemStack(block.getType(), 1);
		itemStack.setItemMeta(itemMeta);
		
		blocks_.put(blockRID, itemStack);
		
		config_.set(blockRID, itemStack);
		
		saveConfig();
	}
	
	/**
	 * @param blockRID
	 * 			Betreffende Block-Recipe-ID.
	 * @return
	 * 			Liefert true, wenn am Platz des Blocks MetaDaten vorhanden sind oder nicht.
	 */
	public boolean hasMetaData(String blockRID) {
		
		checkConfig();
		
		return blocks_.containsKey(blockRID);
	}
	
	/**
	 * @param blockRID
	 * 			Betreffende Block-Recipe-ID.
	 * @return
	 * 			Liefert den fuer die Position des Blocks vermerkte ItemStack (im Fehlerfall null).
	 */
	public ItemStack getBlockItem(String blockRID) {
	
		checkConfig();
		
		return blocks_.get(blockRID);
	}
	
	private void checkConfig() {
		
		if(config_ == null) loadConfig();
	}
}
