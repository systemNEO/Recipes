package de.systemNEO.recipes;

import java.io.File;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class Constants {

	// Farbcodes fuer ANSI Text
	public static final HashMap<String,String> ANSICOLORS = new HashMap<String,String>();
	static {
		ANSICOLORS.put("&0", "\u001B[30m");
		ANSICOLORS.put("&1", "\u001B[34m");
		ANSICOLORS.put("&2", "\u001B[32m");
		ANSICOLORS.put("&3", "\u001B[36m");
		ANSICOLORS.put("&4", "\u001B[31m");
		ANSICOLORS.put("&5", "\u001B[35m");
		ANSICOLORS.put("&6", "\u001B[33m");
		ANSICOLORS.put("&7", "\u001B[37m");
		ANSICOLORS.put("&8", "\u001B[37m");
		ANSICOLORS.put("&9", "\u001B[34m");
		ANSICOLORS.put("&a", "\u001B[32m");
		ANSICOLORS.put("&b", "\u001B[36m");
		ANSICOLORS.put("&c", "\u001B[31m");
		ANSICOLORS.put("&d", "\u001B[35m");
		ANSICOLORS.put("&e", "\u001B[33m");
		ANSICOLORS.put("&f", "\u001B[37m");
		ANSICOLORS.put("&r", "\u001B[0m");
	}
	
	// Farbcodes fuer Spielernachrichten
	public static final HashMap<String,ChatColor> CHATCOLORS = new HashMap<String,ChatColor>();
	static {
		CHATCOLORS.put("&0", ChatColor.BLACK);
		CHATCOLORS.put("&1", ChatColor.DARK_BLUE);
		CHATCOLORS.put("&2", ChatColor.DARK_GREEN);
		CHATCOLORS.put("&3", ChatColor.DARK_AQUA);
		CHATCOLORS.put("&4", ChatColor.DARK_RED);
		CHATCOLORS.put("&5", ChatColor.DARK_PURPLE);
		CHATCOLORS.put("&6", ChatColor.GOLD);
		CHATCOLORS.put("&7", ChatColor.GRAY);
		CHATCOLORS.put("&8", ChatColor.DARK_GRAY);
		CHATCOLORS.put("&9", ChatColor.BLUE);
		CHATCOLORS.put("&a", ChatColor.GREEN);
		CHATCOLORS.put("&b", ChatColor.AQUA);
		CHATCOLORS.put("&c", ChatColor.RED);
		CHATCOLORS.put("&d", ChatColor.LIGHT_PURPLE);
		CHATCOLORS.put("&e", ChatColor.YELLOW);
		CHATCOLORS.put("&f", ChatColor.WHITE);
		CHATCOLORS.put("&r", ChatColor.RESET);
	}
	
	private static Plugin PLUGIN;
	public static int TICKSPERSECOND = 20;
	
	// Definierte Plugins bzw. Abhaengigkeiten
	public static final HashMap<String,HashMap<String,String>> PluginDependencies = new HashMap<String,HashMap<String,String>>();
	static {
		HashMap<String,String> PermissionsEX = new HashMap<String,String>();
		PermissionsEX.put("Download", "http://webbukkit.org/jenkins/packages/PermissionsEx/");
		PermissionsEX.put("WIKI", "http://github.com/PEXPlugins/PermissionsEx/wiki");
		PermissionsEX.put("Bukkit", "http://dev.bukkit.org/server-mods/permissionsex/");
		PermissionsEX.put("Version", "This version of the plugin was coded with PermissionsEX v1.19.5");
		PluginDependencies.put("PermissionsEx", PermissionsEX);
	}
	
	public static FileConfiguration customConfig = null;
	public static File customConfigFile = null;
	public static final String customConfigFileName = "recipes.yml";
	
	public static final String MESSAGE_FAILED = "&4FAILED&r";
	public static final String MESSAGE_OK = "&2OK&r";
	
	public static final String GROUP_GLOBAL   = "_GLOBAL_";
	
	public static final ItemStack AIR = new ItemStack(Material.AIR);
	
	public static final String SHAPE_FIXED    = "fixed";
	public static final String SHAPE_VARIABLE = "variable";
	public static final String SHAPE_FREE     = "free";              // Bei freien Rezepten darf jede Zutat nur einmal vorkommen!
	public static final String SHAPE_REMOVE   = "remove";
	public static final String SHAPE_DEFAULT  = SHAPE_VARIABLE;
	public static final String[] SHAPE_TYPES  = new String[] {SHAPE_FIXED, SHAPE_VARIABLE, SHAPE_FREE, SHAPE_REMOVE};
	
	public static HashMap<String, HashMap<Integer, ItemStack>> customRecipes = new HashMap<String, HashMap<Integer, ItemStack>>();
	
	public static HashMap<String,ItemStack[]> RECIPES_ORIGINAL = new HashMap<String,ItemStack[]>();
	public static HashMap<String,ItemStack[][]> RECIPES_SHAPE = new HashMap<String,ItemStack[][]>();
	public static HashMap<String,ItemStack> RECIPES_RESULT = new HashMap<String,ItemStack>();
	public static HashMap<String,String> RECIPES_TYPE = new HashMap<String,String>();
	public static HashMap<String,String> RECIPES_RESULTMESSAGE = new HashMap<String,String>(); 
	
	/**
	 * Setzt das uebergebene Plugin als Konstante.
	 * @param plugin
	 * 			Das zu setzende Plugin.
	 */
	public static void setPlugin(Plugin plugin) {
		
		PLUGIN = plugin;
	}
	
	/**
	 * Liefert die Konstante PLUGIN.
	 * @return
	 */
	public static Plugin getPlugin() {
		
		return PLUGIN;
	}
}