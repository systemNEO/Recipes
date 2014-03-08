package de.systemNEO.recipes.RUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.systemNEO.recipes.Constants;
import de.systemNEO.recipes.API.PEXHelper;

/**
 * Utilities
 * @author Hape
 *
 */
public abstract class Utils {

	/** Random Generator */
	private static Random randomGenerator_ = new Random(System.currentTimeMillis());
	
	/** Bekannte Tools */
	private static final Set<Material> tools_ = EnumSet.of(
			Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SPADE,
			Material.STONE_AXE, Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SPADE,
			Material.WOOD_AXE, Material.WOOD_HOE, Material.WOOD_PICKAXE, Material.WOOD_SPADE,
			Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_PICKAXE, Material.GOLD_SPADE,
			Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE,
			Material.FLINT_AND_STEEL, Material.FISHING_ROD, Material.SHEARS);
	
	/**
	 * @param material
	 * 			Betreffendes Material.
	 * @return
	 * 			Liefert true, wenn das Material ein Tool ist, andernfalls false.
	 */
	public static Boolean isTool(Material material) {
		
		return tools_.contains(material);
	}
	
	/**
	 * Liefert true, wenn alle benoetigten Plugins vorhanden sind.
	 * 
	 * Hinweis: Die Plugins werden in der Klasse Constants definiert.
	 * @return
	 */
	public static Boolean checkPlugins() {
		
		logInfo("Checking required plugins for " + getPlugin().getName() + ":");
		
		for(String pluginName : Constants.PluginDependencies.keySet()) {
			
			if(getPlugin().getServer().getPluginManager().getPlugin(pluginName) == null) {
				
				HashMap<String,String> messages = Constants.PluginDependencies.get(pluginName);
				
				logWarning("[" + pluginName + "] &4FAILED&r (Plugin is required)");
				
				for(String key : messages.keySet()) logInfo(key + ": " + messages.get(key));
				
				return false;
				
			} else {
				
				logInfo("[" + pluginName + "] &2OK&r");
			}
		}
		
		return true;
	}
	
	/**
	 * Liefert das aktuelle Plugin.
	 * WICHTIG: Dieser Wert muss in Constants gesetzt sein!
	 * @return
	 */
	public static Plugin getPlugin() {
		
		return Constants.getPlugin();
	}
	
	/**
	 * @param pluginName
	 * 			Name des zu liefernden Plugins.
	 * @return
	 * 			Liefert ein Plugin-Objekt passend zum uebergebenen Namen, bzw.
	 * 			null, wenn keines gefunden werden konnte.
	 */
	public static Plugin getPlugin(String pluginName) {
		
		return getPlugin().getServer().getPluginManager().getPlugin(pluginName);
	}
	
	/**
	 * Liefert einen mit ANSI Farben codierten String zurueck.
	 * @param message
	 *				Zu formatierender Text mit Farbcodes &0-&r.
	 * @return
	 */
	public static String getANSIColors(String message) {
		
		if(message == null) return null;
			
		for(String key : Constants.ANSICOLORS.keySet()) {
		
			message = message.replaceAll(key, Constants.ANSICOLORS.get(key));
		}
		
		return message;
	}
	
	/**
	 * Liefert einen mit Bukkit Chat Farben codierten String zurueck.
	 * @param message
	 *				Zu formatierender Text mit Farbcodes &0-&r.
	 * @return
	 */
	public static String getChatColors(String message) {
		
		if(message == null) return null;
		
		for(String key : Constants.CHATCOLORS.keySet()) {
		
			message = message.replaceAll(key, Constants.CHATCOLORS.get(key).toString());
		}
		
		return message;
	}
	
	/**
	 * Gibt eine Infomeldung in der Console aus.
	 * 
	 * @param message
	 * 				Die in der Console auszugebende Infomeldung. Unterstuetzt Farbcodes.
	 */
	public static void logInfo(String message) {
		
		getPlugin().getLogger().info(getANSIColors(message + "&r"));
	}
	
	/**
	 * Gibt eine Infomeldung in der Console aus (wird nur debug genannt, damit man es schneller
	 * im Quellcode wiederfindet...).
	 * 
	 * @param message
	 * 				Die in der Console auszugebende Infomeldung. Unterstuetzt Farbcodes.
	 */
	public static void debug(String message) {
		
		logInfo(message);
	}
	
	/**
	 * Gibt eine Warnmeldung in der Console aus.
	 * @param message
	 * 				Die in der Console auszugebende Warnmeldung. Unterstuetzt Farbcodes.
	 */
	public static void logWarning(String message) {
		
		getPlugin().getLogger().info(getANSIColors(message + "&r"));
	}
	
	/**
	 * Sendet dem uebergebenen Spieler eine Nachricht.
	 * @param player
	 * 				Spieler dem die Nachricht angezeigt werden soll.
	 * @param message
	 * 				Anzuzeigende Nachricht. Unterstuetzt Farbcodes.
	 */
	public static void playerMessage(Player player, String message) {
		
		if(player == null || message == null || message.isEmpty()) return;
		
		if(!player.isOnline()) return;
		
		player.sendMessage(getChatColors(message));
	}
	
	/**
	 * Sendet dem Spieler passend zum uebergebenen Spielernamen die uebergebene Nachricht.
	 * @param playerName
	 * 			Spielername des Nachrichtenempfaengers.
	 * @param message
	 * 			Anzuzeigende Nachricht. Unterstuetzt Farbcodes.
	 */
	public static void playerMessage(String playerName, String message) {
		
		playerMessage(getPlayer(playerName), message);
	}
	
	/**
	 * @param playerName
	 * 			Name des Spielers.
	 * @return
	 * 			Liefert passend zum Spielernamen das Playerobject.
	 */
	public static Player getPlayer(String playerName) {
		
		return Constants.getPlugin().getServer().getPlayerExact(playerName);
	}
	
	/**
	 * @see hasRight
	 * @param player
	 * @param right
	 * @return
	 */
	public static boolean hasRight(Player player, String right) {
		return hasRight(player, right, false);
	}
	
	/**
	 * Prueft ob der Spieler das entsprechende Recht hat und sendet dem Spieler eine Nachricht, wenn nicht.
	 * @param player
	 * 			Spieler dessen Rechte geprueft werden sollen.
	 * @param right
	 * 			Das zu pruefende Recht.
	 * @param throwMessage
	 * 			Wenn true wird dem Spieler eine Meldung angezeigt, falls er das Recht nicht hat.
	 * @return
	 */
	public static boolean hasRight(Player player, String right, boolean throwMessage) {
		
		PermissionUser user = PermissionsEx.getUser(player);
		
		if(user.has(right)) return true;
		
		if(throwMessage) playerMessage(player, "&4You have no permission to do that.");
		
		return false;
	}
	
	/**
	 * Prueft ob ein Kommando genuegend Argumente hat und sendet dem Spieler eine Nachricht, wenn nicht.
	 * @see hasCommandArguments(Player player, String[] args, int minCount, int maxCount, boolean throwMessage, String message)
	 * @param player
	 * 			Player dessen Kommando Argumente geprueft werden sollen. 
	 * @param args
	 * 			Argumente des Kommandos.
	 * @param minCount
	 * 			Geforderte Mindest-Anzahl der Argumente.
	 * @param message
	 * 			Individuelle zusaetzliche Meldung an den Spieler bei Fehlerfall.
	 * @return
	 */
	public static boolean hasCommandArguments(Player player, String[] args, int minCount, String message) {
		return hasCommandArguments(player, args, minCount, 999, true, message);
	}
	
	/**
	 * Prueft ob ein Kommando genuegend Argumente hat und sendet dem Spieler eine Nachricht, wenn nicht.
	 * @param player
	 * 			Player dessen Kommando Argumente geprueft werden sollen. 
	 * @param args
	 * 			Argumente des Kommandos.
	 * @param minCount
	 * 			Geforderte Mindest-Anzahl der Argumente.
	 * @param maxCount
	 * 			Geforderte Maximal-Anzahl der Argumente.
	 * @param message
	 * 			Individuelle zusaetzliche Meldung an den Spieler bei Fehlerfall.
	 * @return
	 */
	public static boolean hasCommandArguments(Player player, String[] args, int minCount, int maxCount, String message) {
		return hasCommandArguments(player, args, minCount, maxCount, true, message);
	}
	
	/**
	 * Prueft ob ein Kommando genuegend Argumente hat und sendet dem Spieler ggf. eine Nachricht, wenn nicht.
	 * @param player
	 * 			Player dessen Kommando Argumente geprueft werden sollen. 
	 * @param args
	 * 			Argumente des Kommandos.
	 * @param minCount
	 * 			Geforderte Mindest-Anzahl der Argumente.
	 * @param maxCount
	 * 			Geforderte Maximal-Anzahl der Argumente.
	 * @param throwMessage
	 * 			Wenn true wird dem Spieler eine Meldung angezeigt bei Fehlerfall.
	 * @param message
	 * 			Individuelle zusaetzliche Meldung an den Spieler bei Fehlerfall.
	 * @return
	 */
	public static boolean hasCommandArguments(Player player, String[] args, int minCount, int maxCount, boolean throwMessage, String message) {
		
		if(args.length >= minCount && args.length <= maxCount) return true;
		
		if(throwMessage) {
			
			if(args.length < minCount) {
				playerMessage(player, "&4Too few arguments.");
			} else {
				playerMessage(player, "&4Too many arguments.");
			}
			
			
			
			if(message != null && !message.isEmpty()) playerMessage(player, message);
		}
		
		return false;
	}
	
	/**
	 * Wandelt einen String in einen Integer um, insofern moeglich.
	 * @param value
	 * 			Der zu parsender Wert vom Typ String.
	 * @param player
	 * 			Optional: Wenn Spieler angegeben, wird im Fehlerfall eine entsprechende Meldung
	 *          ausgegeben.
	 * @return
	 */
	public static Integer parseInt(String value, Player player) {
		
		if(value == null || !(value instanceof String) || value.isEmpty()) {
			
			if(player != null) playerMessage(player, "&4Unknown value!");
			
			return null;
		}

		int intValue;
		
		try {
			
			intValue = Integer.parseInt(value);
			
		} catch (NumberFormatException ex) {
			
			if(player != null) {
				playerMessage(player, "&4The value &6" + value + "&4 is not valid!");
				playerMessage(player, "It must be an integer!");
			}
			
			return null;
		}
		
		return intValue;
	}
	
	/**
	 * Liefert an Hand von count den Plural oder Singular eines Wortes.
	 * @param count
	 * 			Auf Singular/Plural zu pruefende Anzahl.
	 * @param singular
	 * 			Wort als Singular.
	 * @param plural
	 * 			Wort als Plural.
	 * @return
	 */
	public static String getSingularPlural(Integer count, String singular, String plural) {
		
		if(count == 1) return singular;
		
		return plural;
	}
	
	/**
	 * @param seconds
	 * 			Die in Ticks umzurechnenden Sekunden.
	 * @return
	 * 			Liefert die berechneten Gameticks fuer die uebergebenen Sekunden.
	 */
	public static int getTicksPerSecons(Integer seconds) {
		
		if(seconds == null || seconds == 0) return 0;
		
		return Constants.TICKSPERSECOND * seconds;
	}
	
	/**
	 * @return Liefert den Bukkit Scheduler, um z. B. Tasks zu erstellen.
	 */
	public static BukkitScheduler getScheduler() {
		
		return Constants.getPlugin().getServer().getScheduler();
	}
	
	/**
	 * Stoppt alle aktiven Tasks dieses Plugins.
	 */
	public static void cancelTasks() {
		
		Utils.getScheduler().cancelTasks(Constants.getPlugin());
	}

	public static void prefixLog(String prefix, String message) {
		
		Utils.logInfo("[" + prefix + "] " + message);
	}
	
	public static void onEnableError(String errorMessage) {
		
		Utils.logWarning("&4" + errorMessage);
	}
	
	public static void setMetadata(Player player, String key, Object value) {
	
		player.setMetadata(key, new FixedMetadataValue(Utils.getPlugin(), value));
	}

	/**
	 * @param player
	 * 			Betreffender Spieler
	 * @param key
	 * 			Betreffender Key
	 * @return
	 * 			Liefert die angeforderten Metadaten, anderfnall null.
	 */
	public static Object getMetadata(Player player, String key) {

		List<MetadataValue> values = player.getMetadata(key);
		
		String pluginName = Utils.getPlugin().getDescription().getName();
		
		for (MetadataValue value : values) {
			if (value.getOwningPlugin().getDescription().getName().equals(pluginName)) return value.value();
		}

		return null;
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param key
	 * 			Betreffender Key
	 * @return
	 * 			Liefert die angeforderten MetadataValue, anderfnalls null.
	 */
	public static MetadataValue getMetadata(Block block, String key) {

		List<MetadataValue> values = block.getMetadata(key);

		String pluginName = Utils.getPlugin().getDescription().getName();
		
		for (MetadataValue value : values) {
			
			if(value.getOwningPlugin().getDescription().getName().equals(pluginName)) return value;
		}

		return null;
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param key
	 * 			Betreffender Key
	 * @return
	 * 			Liefert die angeforderten Metadaten vom Typ Long, anderfnalls 0.
	 */
	public static long getMetadataAsLong(Block block, String key) {

		MetadataValue value = getMetadata(block, key);
		
		if(value == null) return 0;
		
		return value.asLong();
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param key
	 * 			Betreffender Key
	 * @return
	 * 			Liefert die angeforderten Metadaten vom Typ Integer, anderfnalls 0.
	 */
	public static Integer getMetadataAsInt(Block block, String key) {

		MetadataValue value = getMetadata(block, key);
		
		if(value == null) return 0;
		
		return value.asInt();
	}
	
	/**
	 * @param block
	 * 			Betreffender Block.
	 * @param key
	 * 			Betreffender Key
	 * @return
	 * 			Liefert die angeforderten Metadaten vom Typ String, anderfnalls leer.
	 */
	public static String getMetadataAsString(Block block, String key) {

		MetadataValue value = getMetadata(block, key);
		
		if(value == null) return "";
		
		return value.asString();
	}
	
	public static String formatTypeId(Integer typeId) {
		
		return String.format("%05d", typeId);
	}
	
	public static String formatSubId(Short subId) {
		
		return String.format("%02d", subId);
	}
	
	/**
	 * @param player
	 * 			Betreffender Spieler.
	 * @return
	 * 			Liefert die Usergruppen (inkl. Koenigreiche) des Spielers nach Relevanz sortiert zurueck.
	 */
	public static String[] getPlayerGroups(Player player) {
		
		// Permission-EX Nutzergruppen holen
		String[] userGroups = PEXHelper.getUserGroups(player);
		
		return userGroups;
	}
	
	/**
	 * @param number
	 * 			Zahl, muss hoeher als 0 sein.
	 * @return
	 * 			Liefert eine Zufallszahl zwischen 0 (inklusiv) und der definierten
	 * 			Zahl (exklusiv) zurueck. 
	 */
	public static Integer getRandom(Integer number) {
		
		if(number == null || number <= 1) return 0;
		
		return randomGenerator_.nextInt(number);
	}
	
	/**
	 * Spielt einen bestimmten Sound ab, wenn etwas schief ging.
	 * 
	 * @param location
	 * 			Betreffende Position.
	 */
	public static void playFail(Location location) {
		
		location.getWorld().playSound(location, Sound.ITEM_BREAK, 1, 1);
	}
	
	/**
	 * @return
	 * 			Liefert die aktuelle Serverzeit in Millisekunden
	 */
	public static long getCurrentServerTime() {
		
		return System.currentTimeMillis();
	}
}