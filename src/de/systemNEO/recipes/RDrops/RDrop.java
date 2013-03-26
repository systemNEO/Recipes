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
	
	/** Drop-Chance des Result-Items */
	private ArrayList<RDropItem> drops_ = null;
	
	/** ID des Rezeptes */
	private String id_ = null;
	
	/**
	 * Drop-Rezept.
	 * 
	 * @param drops
	 * 			Drops.
	 * @param by
	 * 			Ausloeser des Drops.
	 */
	public RDrop(ArrayList<RDropItem> drops, Object by) {
		
		drops_ = drops;
		
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
		
		id_ = RDrops.getBlockRecipeId(itemStack);
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
