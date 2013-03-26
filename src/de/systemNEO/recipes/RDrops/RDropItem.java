package de.systemNEO.recipes.RDrops;

import org.bukkit.inventory.ItemStack;

public class RDropItem {

	private ItemStack dropItem_ = null;
	
	private Integer dropChance_ = 100;
	
	public RDropItem(ItemStack dropItem, Integer dropChance) {
		
		dropItem_ = dropItem;
		
		dropChance_ = dropChance;
	}
	
	public ItemStack getItem() {
		
		return dropItem_;
	}
	
	public Integer getDropChance() {
		
		return dropChance_;
	}
}
