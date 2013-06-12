package de.systemNEO.recipes.API;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import de.systemNEO.recipes.Constants;

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
		
		int rank = 0;
		HashMap<Integer,String> groups = new HashMap<Integer,String>();
		
		for(PermissionGroup group : userGroups) {
			
			rank = group.getRank() * 100;
			
			if(groups.containsKey(rank)) {
			
				do {
					++rank;
				} while (groups.containsKey(rank));
			}
			
			groups.put(rank, group.getName().toLowerCase());
		}
		
		// Umso hoeher der Rang in PermissionsEX, um so "schlechter", daher werden
		// die globalen Rezepte als letztes einsortiert.
		groups.put(999999999, Constants.GROUP_GLOBAL.toLowerCase());
		
		ArrayList<Integer> groupRanks = new ArrayList<Integer>(groups.keySet());
		java.util.Collections.sort(groupRanks);
		
		int newPos = 0;
		String[] finalGroups = new String[groupRanks.size()];
		
		for(int groupRank : groupRanks) {
			finalGroups[newPos] = groups.get(groupRank);
			++newPos;
		}
		
		return finalGroups;
	}
}
