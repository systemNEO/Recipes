package de.systemNEO.recipes.API;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.systemNEO.KingdomSide.KingdomSide;
import de.systemNEO.KingdomSide.KsAPI.KsAPI;
import de.systemNEO.recipes.RUtils.Utils;

public abstract class KSideHelper {

	/** KingdomSide Plugin von Bukkit */
	private static KingdomSide kingdomSidePlugin_ = null;
	
	/** KingdomSide API */
	private static KsAPI ksAPI_ = null;
	
	/** Merker: Wurde das Plugin erfolgreich initialisiert? */
	private static boolean isInitialized = false;
	
	/** Merker: Gibt es ein KingdomSide Plugin? */
	private static boolean isPlugin = false;
	
	/** Prefix fuer KingdomSide-Gruppen */
	private static String kingdomSidePrefix_ = "kingdomSide___";
	
	/**
	 * @param player
	 * 			Betreffender Spieler.
	 * @return
	 * 			Liefert den Namen des Koenigreichs dem der Spieler angehoert, andernfalls null.
	 */
	public static String getPlayersKingdom(Player player) {
		
		if(initialize() == false) return null;
		
		return ksAPI_.getPlayersKingdom(player);
	}
	
	/**
	 * @param regions
	 * 			Betreffende WorldGuard-Regionen.
	 * @return
	 * 			Liefert bei Erfolg den Namen eines Koenigreiches zurueck, andernfalls null.
	 */
	public static String getKingdomByRegions(ArrayList<ProtectedRegion> regions) {
		
		if(initialize() == false) return null;
		
		return ksAPI_.getKingdomByRegions(regions);
	}
	
	/**
	 * @param regionName
	 * 			Betreffender WordGuard-Regionen-Name.
	 * @return
	 * 			Liefert bei Erfolg den Namen eines Koenigreiches zurueck, andernfalls null.
	 */
	public static String getKingdomByRegionName(String regionName) {
		
		return ksAPI_.getKingdomByRegionName(regionName);
	}
	
	/**
	 * Holt die KingdomSide-API, falls moeglich.
	 * 
	 * @return
	 * 			Liefert true zurueck, wenn die KingdomSide-API erfolgreich geholt werden konnte, andernfalls
	 * 			false.
	 */
	private static boolean initialize() {
		
		if(isInitialized) return isPlugin;
		
		isInitialized = true;
		
		kingdomSidePlugin_ = (KingdomSide) Utils.getPlugin().getServer().getPluginManager().getPlugin("KingdomSide");
		
		if(kingdomSidePlugin_ == null) return isPlugin;
		
		ksAPI_ = kingdomSidePlugin_.getAPI();
		
		if(ksAPI_ == null) return isPlugin;
		
		isPlugin = true;
		
		return isPlugin;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn das Plugin initialisiert wurde, andernfalls false.
	 */
	public static boolean isPlugin() {
		
		return initialize();
	}
	
	/**
	 * @return
	 * 			Liefert Prefix fuer KingdomSide-Gruppen (Gruppen im Sinne der Pruefung auf Gruppen
	 * 			im Recipe-Plugin).
	 */
	public static String getGroupPrefix() {
		
		return kingdomSidePrefix_;
	}
	
	/**
	 * @param kingdomName
	 * 			Name eines Koenigreiches.
	 * @return
	 * 			Liefert den Gruppennamen eines Koenigreiches (bzw. konvertiert diesen in einen Gruppennamen).
	 */
	public static String toGroupName(String kingdomName) {
		
		return kingdomSidePrefix_ + kingdomName.toLowerCase();
	}
	
	/**
	 * @return
	 * 			Liefert das KingdomSide Plugin, wenn vorhanden, andernfalls null.
	 */
	public static KingdomSide getPlugin() {
		
		if(initialize() == false) return null;
		
		return kingdomSidePlugin_;
	}
}
