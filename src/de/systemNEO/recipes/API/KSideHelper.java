package de.systemNEO.recipes.API;

import org.bukkit.entity.Player;

import de.systemNEO.KingdomSide.KingdomSide;
import de.systemNEO.KingdomSide.KsAPI.KsAPI;
import de.systemNEO.recipes.Utils;

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
}
