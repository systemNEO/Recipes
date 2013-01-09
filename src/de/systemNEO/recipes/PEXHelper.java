package de.systemNEO.recipes;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public abstract class PEXHelper {
	
	/**
	 * DONE: Die Usergruppen muessen noch per Rank sortiert werden, damit ein Admin-Rezept (Rang 1) Vorrang
	 * vor einem User-Rezept (Rang 100) hat.
	 * @param player
	 * @return
	 */
	public static String[] getUserGroups(Player player) {
		
		PermissionUser user = PermissionsEx.getUser(player);
		PermissionGroup[] userGroups = user.getGroups();
		
		HashMap<Integer,String> groups = new HashMap<Integer,String>();
		ArrayList<Integer> groupRanks = new ArrayList<Integer>();
		
		int lastRank = 999999999;
		
		groups.put(lastRank, Constants.GROUP_GLOBAL.toLowerCase());
		groupRanks.add(lastRank);
		
		int rank;
		
		for(PermissionGroup group : userGroups) {
			
			rank = group.getRank();
			
			if(rank == 0) {
				--lastRank;
				rank = lastRank;
			}
			
			groupRanks.add(rank);
			groups.put(rank, group.getName().toLowerCase());
		}
		
		java.util.Collections.sort(groupRanks);
		
		int len = groups.size();
		int newPos = 0;
		String[] finalGroups = new String[len];
		
		for(int groupRank : groupRanks) {
			finalGroups[newPos] = groups.get(groupRank);
			++newPos;
		}
		
		return finalGroups;
	}
}
