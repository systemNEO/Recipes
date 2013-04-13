package de.systemNEO.recipes.RChunks;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.block.Block;

import de.systemNEO.recipes.RUtils.Utils;

public abstract class RChunks {

	/** Name des Chunk-Ordners. */
	private static String chunkFolder_ = "chunks";
	
	/** Chunkordner */
	private static File chunkDir_ = null;
	
	/** Register aller aktuell geladenen Chunks */
	private static HashMap<String,RChunk> loadedChunks_ = new HashMap<String,RChunk>();
	
	/**
	 * @return
	 * 			Liefert den Ordner fuer die Chunk-Dateien zurueck.
	 */
	public static File getChunkFolder() {
		
		if(chunkDir_ == null) {
			
			chunkDir_ = new File(Utils.getPlugin().getDataFolder() + "/" + chunkFolder_);
			
			if(!chunkDir_.exists()) chunkDir_.mkdir();
		}
		
		return chunkDir_;
	}
	
	/**
	 * @param chunk
	 * 			Betreffender Chunk.
	 * @return
	 * 			Liefert den YML-Dateinamen des Chunks zurueck.
	 */
	public static String getPathAndFileNameOfChunk(Chunk chunk) {
		
		return chunk.getWorld().getUID().toString() + "-" + chunk.getX() + "-" + chunk.getZ() + ".yml";
	}
	
	/**
	 * Registriert einen BukkitChunk in Recipes.
	 * 
	 * @param chunk
	 * 			Betreffender Bukkit-Chunk.
	 */
	public static void registerLoadedChunk(Chunk chunk) {
		
		RChunk rChunk = new RChunk(chunk);
		
		loadedChunks_.put(rChunk.getId(), rChunk);
	}
	
	/**
	 * Hebt die Registrierng des BukkitChunks in Recipes wieder auf.
	 * 
	 * @param chunk
	 * 			Betreffender Bukkit-Chunk.
	 */
	public static void unregisterLoadedChunk(Chunk chunk) {
		
		loadedChunks_.remove(getPathAndFileNameOfChunk(chunk));
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @return
	 * 			Liefert den Recipe-Chunk zum Block zurueck.
	 */
	public static RChunk getRChunk(Block block) {
		
		Chunk chunk = block.getChunk();
		
		String chunkName = getPathAndFileNameOfChunk(block.getChunk());
		
		// Sicherstellen, dass der Chunk nach einem Reload-Befehl in Bukkit auch
		// wirklich da ist.
		if(!loadedChunks_.containsKey(chunkName) && chunk.isLoaded()) registerLoadedChunk(block.getChunk());
		
		return loadedChunks_.get(chunkName);
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @return
	 * 			Liefert die Recipe-Block-ID eines Bukkit-Blocks.
	 */
	public static String getBlockRID(Block block) {
		
		return block.getX() + "-" + block.getY() + "-" + block.getZ();
	}
}
