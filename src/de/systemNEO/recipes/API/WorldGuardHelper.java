package de.systemNEO.recipes.API;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import de.systemNEO.recipes.Constants;
import de.systemNEO.recipes.RUtils.Utils;

public abstract class WorldGuardHelper {
	
	/**
	 * @return
	 * 			Liefert das WorldGuardPlugin.
	 */
	public static WorldGuardPlugin getWgPlugin() {
		
		return Constants.getWorldGuardPlugin();
	}

	/**
	 * @param regionName
	 * 			Name der Region.
	 * @param world
	 * 			Welt der Region.
	 * @return
	 * 			Liefert die WorldGuard Region passend zum Namen und Welt,
	 * 			andernfalls null.
	 */
	public static ProtectedRegion getRegion(String regionName, World world) {
		
		if(!existsRegion(regionName, world)) return null;
		
		return getRegionManager(world).getRegion(regionName);
	}
	
	/**
	 * @param region
	 * 			Region dessen Manager zu ermitteln ist.
	 * @return
	 * 			Liefert den Regionmanager der Region, wenn gefunden, andernfalls
	 * 			null.
	 */
	public static RegionManager getRegionManager(ProtectedRegion region) {
		
		if(region == null) return null;
		
		List<World> worlds = Utils.getPlugin().getServer().getWorlds();
		
		RegionManager regionManager = null;
		
		for(World world : worlds) {
			
			if((regionManager = getRegionManager(world)) == null) continue;
			
			if(regionManager.hasRegion(region.getId())) return regionManager;
		}

		return null;
	}
	
	/**
	 * @param world
	 * 			Welt, dessen RegionManager zurueckgeliefert werden soll.
	 * @return
	 * 			Liefert RegionManager, wenn gefunden, andernfalls null.
	 */
	public static RegionManager getRegionManager(World world) {
		
		if(world == null) return null;
		
		return getWgPlugin().getRegionManager(world);
	}
	
	/**
	 * @param regionName
	 * 			Auf Existenz zu pruefender Regionen-Name.
	 * @param world
	 * 			Welt der auf Existenz zu pruefenden Region.
	 * @param player
	 * 			Optional: Wenn ein Spieler angegeben wird, wird eine Fehlermeldung im Fehlerfall ausgegeben.
	 * @param errorIfExists
	 * 			Optional: Wenn ein Spieler gesetzt ist, kann hier angegeben werden, wann ein Fehler auftritt.
	 * 			Wenn true und Spieler uebergeben und Area-Name existiert, wird eine Fehlermeldung an den 
	 * 			Spieler ausgegeben. Wenn false und Spieler uebergeben und Area-Name existiert nicht, wird eine
	 * 			Fehlermeldung an den Spieler ausgegeben.
	 * @return
	 * 			Liefert true, wenn der zu pruefende Regionen-Name existiert, andernfalls false.
	 */
	public static boolean existsRegion(String regionName, World world, Player player, Boolean errorIfExists) {
		
		ProtectedRegion region = getRegionManager(world).getRegion(regionName);
		
		if(region != null) {
			
			if(player != null && errorIfExists != null && errorIfExists) Utils.playerMessage(player, "&4WorldGuard region &6" + regionName + "&4 already exists.");
			
			return true;
		}
		
		if(player != null && errorIfExists != null && !errorIfExists) Utils.playerMessage(player, "&4WorldGuard region &6" + regionName + "&4 does not exist.");
		
		return false;			
	}
	
	/**
	 * @param regionName
	 * 			Auf Existenz zu pruefender Regionen-Name.
	 * @param world
	 * 			Welt der auf Existenz zu pruefenden Region.
	 * @return
	 * 			Liefert true, wenn der zu pruefende Regionen-Name existiert, andernfalls false.
	 */
	public static boolean existsRegion(String regionName, World world) {
		
		return existsRegion(regionName, world, null,null);
	}
	
	/**
	 * @param player
	 * 			Spieler, dessen WorldGuard Regionen ermittelt werden sollen.
	 * @return
	 * 			Liefert eine Liste mit WorldGuard Regionen zurueck, in denen sich
	 * 			der Spieler gerade aufhaelt. Werden keine gefunden, wird null
	 * 			zurueckgegeben.
	 */
	public static ArrayList<ProtectedRegion> getPlayerRegions(Player player) {
		
		return getLocationRegions(player.getLocation());
	}
	
	/**
	 * @param block
	 * 			Block, dessen WorldGuard Regionen ermittelt werden sollen.
	 * @return
	 * 			Liefert eine Liste mit WorldGuard Regionen zurueck, in denen sich
	 * 			der Block befindet. Werden keine gefunden, wird null
	 * 			zurueckgegeben.
	 */
	public static ArrayList<ProtectedRegion> getBlockRegions(Block block) {
		
		return getLocationRegions(block.getLocation());
	}
	
	/**
	 * @param location
	 * 			Location, dessen WorldGuard Regionen ermittelt werden sollen.
	 * @return
	 * 			Liefert eine Liste mit WorldGuard Regionen zurueck, in denen sich
	 * 			die Location befindet. Werden keine gefunden, wird null
	 * 			zurueckgegeben.
	 */
	public static ArrayList<ProtectedRegion> getLocationRegions(Location location) {
		
		RegionManager regionManager = getRegionManager(location.getWorld());
		if(regionManager == null) return null;
		
		ApplicableRegionSet applicableRegionSet = regionManager.getApplicableRegions(location);
		if(applicableRegionSet == null) return null;
		
		ArrayList<ProtectedRegion> regions = new ArrayList<ProtectedRegion>();
		
		for(ProtectedRegion region : applicableRegionSet) regions.add(region);
		if(regions.size() == 0) return null;
		
		return regions;
	}
	
	/**
	 * @param region
	 * 			Betreffende Region.
	 * @return
	 * 			Liefert alle Gruppen der Region zurueck, wenn keine gefunden, dann null.
	 */
	public static ArrayList<String> getGroupsOfRegion(ProtectedRegion region) {
		
		ArrayList<String> groups = new ArrayList<>();
		
		DefaultDomain members = region.getMembers();
		if(members != null) {
			
			Set<String> memberGroups = members.getGroups();
			if(memberGroups != null && !memberGroups.isEmpty()) groups.addAll(memberGroups);
		}
		
		DefaultDomain owners = region.getOwners();
		if(owners != null) {
			
			Set<String> ownerGroups = owners.getGroups();
			if(ownerGroups != null && !ownerGroups.isEmpty()) groups.addAll(ownerGroups);
		}
		
		if(groups.isEmpty()) return null;
		
		return groups;
	}
}
