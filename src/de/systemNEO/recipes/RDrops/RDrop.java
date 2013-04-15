package de.systemNEO.recipes.RDrops;

import java.util.ArrayList;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class RDrop {

	/** Ausloesender Entity-Typ */
	private EntityType entity_ = null;
	
	/** Ausloesender ItemStack */
	private ItemStack block_ = null;
	
	/** Typ des Dropausloesers */
	private String type_ = "default";
	
	/** Drop-Chance des Result-Items (schliesst chance_ aus). */
	private ArrayList<RDropItem> drops_ = null;
	
	/** Globale Dropchance (schliesst drops_ aus). */
	private Double chance_ = 100.0;
	
	/** ID des Rezeptes */
	private String id_ = null;
	
	/** Zeigt an ob das Rezept individuelle Drops hat oder nicht. */
	private Boolean hasDrops_ = false;
	
	/** Zeigt an ob das Rezept eine globale Drop-Chance hat oder nicht. */
	private Boolean hasChance_ = false;
	
	private Boolean hasWildcard_ = false;
	
	/**
	 * Drop-Rezept mit Liste vorgegebener Drops.
	 * 
	 * @param drops
	 * 			Drops.
	 * @param by
	 * 			Ausloeser des Drops.
	 */
	public RDrop(ArrayList<RDropItem> drops, Object by, Boolean hasWildcard) {
		
		drops_ = drops;
		
		hasDrops_ = true;
		
		hasWildcard_ = hasWildcard;
		
		setBy(by);
	}
	
	/**
	 * Drop-Rezept mit globaler Drop-Chance.
	 * 
	 * @param chance
	 * 			Chance.
	 * @param by
	 * 			Ausloeser des Drops.
	 */
	public RDrop(Double chance, Object by, Boolean hasWildcard) {
		
		chance_ = chance;
		
		hasChance_ = true;
		
		hasWildcard_ = hasWildcard;
		
		setBy(by);
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn die SubID des Blocks keine Rolle spielt.
	 */
	public Boolean hasWildcard() {
		
		return hasWildcard_;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn das Drop-Rezept individuelle Drops hat, andernfalls false.
	 */
	public Boolean hasDrops() {
		
		return hasDrops_;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn das Drop-Rezept eine globale Drop-Chance hat, andernfalls false.
	 */
	public Boolean hasChance() {
		
		return hasChance_;
	}
	
	/**
	 * @return
	 * 			Liefert die globale Drop-Chance.
	 */
	public Double getChance() {
		
		return chance_;
	}
	
	/**
	 * Setzt die Quelle des Drops-Rezeptes, Block oder Entity.
	 * 
	 * @param by
	 * 			Ausloeser des Drops.
	 */
	private void setBy(Object by) {
		
		if(by instanceof EntityType) {
			
			setEntity((EntityType) by);
			
		} else if (by instanceof ItemStack) {
			
			setBlock((ItemStack) by);
		}
	}
	
	/**
	 * @return
	 * 			Liefert die ID des Drop-Rezeptes.
	 */
	public String getId() {
		
		return id_;
	}
	
	/**
	 * @return
	 * 			Liefert das Drops des Drop-Rezeptes.
	 */
	public ArrayList<RDropItem> getDrops() {
		
		return drops_;
	}
	
	/**
	 * Setzt einen Entity-Typen als Ursprung des Drops.
	 * 
	 * @param entity
	 * 			Betreffender Entity-Typ.
	 */
	public void setEntity(EntityType entity) {
		
		entity_ = entity;
		
		type_ = "entity";
		
		id_ = RDrops.getEntityRecipeId(entity_);
		
		RDrops.hasEntityDropRecipes(true);
	}
	
	/**
	 * @return
	 * 			Liefert, insofern vorhanden, den Entity-Typ als ausloesender Dropper, andernfalls null.
	 */
	public EntityType getEntity() {
		
		return entity_;
	}
		
	/**
	 * @return
	 * 			Liefert true, wenn der ausloesende Dropper ein Entity ist, andernfalls false.
	 */
	public boolean isEntity() {
		
		return type_.equalsIgnoreCase("entity");
	}
	
	/**
	 * Setzt einen ItemStack (Block) als Urpsrung des Drops.
	 * 
	 * @param itemStack
	 * 			Betreffender ItemStack.
	 */
	public void setBlock(ItemStack itemStack) {
		
		block_ = itemStack;
		
		type_ = "block";
		
		id_ = RDrops.getBlockRecipeId(itemStack, hasWildcard_);
		
		RDrops.hasBlockDropRecipes(true);
	}
	
	/**
	 * @return
	 * 			Liefert, insofern vorhanden, den ItemStack (Block) als ausloesender Dropper, andernfalls null.
	 */
	public ItemStack getBlock() {
		
		return block_;
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn der ausloesende Dropper ein ItemStack (Block) ist, andernfalls false.
	 */
	public boolean isBlock() {
		
		return type_.equalsIgnoreCase("block");
	}
	
	/**
	 * @return
	 * 			Liefert true, wenn das Drop-Rezept valide ist, andernfalls false.
	 */
	public boolean isValid() {
		
		return isBlock() || isEntity();
	}
}
