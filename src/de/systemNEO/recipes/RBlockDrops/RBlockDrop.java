package de.systemNEO.recipes.RBlockDrops;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import de.systemNEO.recipes.RUtils.Utils;

public class RBlockDrop {

	/** Drop. */
	private ItemStack drop_ = null;
	
	/** Dropchance. */
	private Double chance_ = null;
	
	/** Anzeiger fuer Random-Items. */
	private Integer randomCount_ = 0;
	
	/** Anzeiger fuer SafeItem. */
	private Integer safeCount_ = 0;
	
	private ArrayList<Double> luckChances_ = null;
	
	/**
	 * Erstellt ein Drop-Objekt mit individueller Dropchance.
	 * 
	 * @param drop
	 * 			Drop.
	 * @param chance
	 * 			Dropchance.
	 */
	public RBlockDrop(ItemStack drop, Double chance) {
		
		if(chance >= 100.0) setDrop(drop, drop.getAmount(), 0);
		
		drop_ = drop;
		
		chance_ = chance;
	}
	
	/**
	 * Erstellt ein Drop-Objekt mit individueller Dropchance.
	 * 
	 * @param material
	 * 			Material.
	 * @param subId
	 * 			SubId.
	 * @param chance
	 * 			Dropchance.
	 */
	public RBlockDrop(Material material, int subId, Double chance) {
		
		this(new ItemStack(material, 1, (short) subId), chance);
	}
	
	/**
	 * Erstellt ein Drop-Objekt mit individueller Dropchance.
	 * 
	 * @param material
	 * 			Material.
	 * @param chance
	 * 			Dropchance.
	 */
	public RBlockDrop(Material material, Double chance) {
		
		this(new ItemStack(material, 1, (short) 0), chance);
	}
	
	public RBlockDrop(Material material, Double chance, Double luckChanceLvl1, Double luckChanceLvl2, Double luckChanceLvl3) {
		
		this(new ItemStack(material, 1, (short) 0), chance);
		
		luckChances_ = new ArrayList<>();
		
		luckChances_.add(chance);
		luckChances_.add(luckChanceLvl1);
		luckChances_.add(luckChanceLvl2);
		luckChances_.add(luckChanceLvl3);
	}
	
	/**
	 * Erstellt ein Drop-Objekt mit 100% Dropchance.
	 * 
	 * @param drop
	 * 			Drop.
	 */
	public RBlockDrop(ItemStack drop) {
		
		setDrop(drop, drop.getAmount(), 0);
	} 
	
	/**
	 * Erstellt ein Drop-Objekt mit 100% Dropchance.
	 * 
	 * @param material
	 * 			Material.
	 * @param subId
	 * 			SubId.
	 */
	public RBlockDrop(Material material, int subId) {
		
		setDrop(new ItemStack(material, 1, (short) subId), 1, 0);
	}
	
	/**
	 * Erstellt ein Drop-Objekt mit 100% Dropchance.
	 * 
	 * @param material
	 * 			Material.
	 */
	public RBlockDrop(Material material) {
		
		this(new ItemStack(material, 1, (short) 0));
	}
	
	/**
	 * Erstellt ein Drop-Objekt mit 100% Dropchance.
	 * 
	 * @param material
	 * 			Material.
	 */
	public RBlockDrop(Material material, int subId, int safeCount, int randomCount) {
		
		setDrop(new ItemStack(material, 1, (short) subId), safeCount, randomCount);
	}
	
	public RBlockDrop(Material material, int safeCount, int randomCount) {
		
		setDrop(new ItemStack(material, 1, (short) 0), safeCount, randomCount);
	}
	
	public RBlockDrop(ItemStack drop, int safeCount, int randomCount) {
		
		setDrop(drop, safeCount, randomCount);
	}
	
	public void setDrop(ItemStack drop, int safeCount, int randomCount) {
		
		drop_ = drop;
		
		if(safeCount > 0) safeCount_ = safeCount;
		
		if(randomCount > 0) randomCount_ = randomCount;
	}
	
	/**
	 * @return
	 * 			Liefert den Drop, ggf. nach Dropchance berechnet.
	 */
	public ItemStack getDrop(ItemStack tool) {
		
		if(chance_ != null) {
			
			double chance = chance_;
			
			if(luckChances_ != null && !luckChances_.isEmpty()) {
				
				Integer enchantmentLvl = RBlockDrops.getEnchantmentLevel(tool, Enchantment.LOOT_BONUS_BLOCKS);
			
				if(enchantmentLvl != null && enchantmentLvl > 0) luckChances_.get(enchantmentLvl);
			}
			
			if(drop_.getAmount() <= 1) {
				
				if((Math.random() * 100) <= chance) {
					return new ItemStack(drop_);
				} else {
					return null;
				}
				
			} else if(drop_.getAmount() > 1) {
				
				int resultAmount = 0;
				
				for(int i = 1; i <= drop_.getAmount(); i++) {
					
					if((Math.random() * 100) <= chance) ++resultAmount;
				}
				
				ItemStack resultStack = new ItemStack(drop_);
				resultStack.setAmount(resultAmount);
				return resultStack;
			}
			
			return null;
			
		} else {
			
			int resultAmount = safeCount_;
			
			if(randomCount_ > 0) resultAmount = resultAmount + Utils.getRandom(randomCount_);
			
			ItemStack resultStack = new ItemStack(drop_);
			resultStack.setAmount(resultAmount);
			return resultStack;
		}
	}
}
