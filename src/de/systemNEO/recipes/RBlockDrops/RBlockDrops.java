package de.systemNEO.recipes.RBlockDrops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


public abstract class RBlockDrops {
	
	private static final HashMap<String,ArrayList<RBlockDrop>> blockDrops_ = new HashMap<>();
	static {
		ArrayList<RBlockDrop> crops = new ArrayList<>();
		crops.add(new RBlockDrop(Material.WHEAT));
		crops.add(new RBlockDrop(Material.WHEAT, 25.0));
		crops.add(new RBlockDrop(Material.SEEDS));
		crops.add(new RBlockDrop(Material.SEEDS, 50.0));
		blockDrops_.put(Material.CROPS.toString() + "-" + 7, crops);
		
		ArrayList<RBlockDrop> potatos = new ArrayList<>();
		potatos.add(new RBlockDrop(Material.POTATO_ITEM, 1, 2));
		potatos.add(new RBlockDrop(Material.POISONOUS_POTATO, 10.0));
		blockDrops_.put(Material.POTATO.toString() + "-" + 7, potatos);
		
		ArrayList<RBlockDrop> carrots = new ArrayList<>();
		carrots.add(new RBlockDrop(Material.CARROT_ITEM, 1, 2));
		blockDrops_.put(Material.CARROT.toString() + "-" + 7, carrots);
		
		ArrayList<RBlockDrop> cocoa = new ArrayList<>();
		cocoa.add(new RBlockDrop(new ItemStack(351, 3, (short) 3)));
		blockDrops_.put(Material.COCOA.toString() + "-" + 8, cocoa);
		blockDrops_.put(Material.COCOA.toString() + "-" + 9, cocoa);
		blockDrops_.put(Material.COCOA.toString() + "-" + 10, cocoa);
		blockDrops_.put(Material.COCOA.toString() + "-" + 11, cocoa);
		
		ArrayList<RBlockDrop> apple = new ArrayList<>();
		apple.add(new RBlockDrop(Material.APPLE, 10.0));
		blockDrops_.put(Material.LEAVES.toString() + "-" + 0, apple);
		blockDrops_.put(Material.LEAVES.toString() + "-" + 8, apple);
		blockDrops_.put(Material.LEAVES.toString() + "-" + 12, apple);
		
		ArrayList<RBlockDrop> glowstoneDust = new ArrayList<>();
		glowstoneDust.add(new RBlockDrop(Material.GLOWSTONE_DUST, 2, 2));
		blockDrops_.put(Material.GLOWSTONE.toString(), glowstoneDust);
		
		ArrayList<RBlockDrop> gravel = new ArrayList<>();
		gravel.add(new RBlockDrop(Material.GRAVEL));
		gravel.add(new RBlockDrop(Material.FLINT, 10.0, 14.0, 25.0, 100.0));
		blockDrops_.put(Material.GRAVEL.toString(), gravel);
	}
	
	private static final HashMap<String,ArrayList<RBlockDrop>> shearDrops_ = new HashMap<>();
	static {
		ArrayList<RBlockDrop> apple = new ArrayList<>();
		apple.add(new RBlockDrop(Material.APPLE, 10.0));
		apple.add(new RBlockDrop(new ItemStack(18, 1, (short) 0), 100.0)); // Blaetter sollten immer fallen.
		shearDrops_.put(Material.LEAVES.toString() + "-" + 0, apple);
		shearDrops_.put(Material.LEAVES.toString() + "-" + 8, apple);
		shearDrops_.put(Material.LEAVES.toString() + "-" + 12, apple);
	}
	
	private static final HashMap<String,HashMap<String,ArrayList<RBlockDrop>>> dropsByTools_ = new HashMap<>();
	static {
		dropsByTools_.put(Material.SHEARS.toString(), shearDrops_);
	}
	
	private static final HashMap<Integer,Double> oreChancesByLevel_ = new HashMap<>();
	static {
		oreChancesByLevel_.put(1, 33.0);
		oreChancesByLevel_.put(2, 25.0);
		oreChancesByLevel_.put(3, 20.0);
	}
	
	private static final HashMap<Material,String> oreWithLootBonus_ = new HashMap<>();
	static {
		oreWithLootBonus_.put(Material.COAL_ORE, "263-0");
		oreWithLootBonus_.put(Material.DIAMOND_ORE, "264-0");
		oreWithLootBonus_.put(Material.LAPIS_ORE, "351-4");
		oreWithLootBonus_.put(Material.QUARTZ_ORE, "406-0");
		oreWithLootBonus_.put(Material.EMERALD_ORE, "388-0");
	}
	
	private static final HashMap<Material,Integer> itemMultiplier_ = new HashMap<>();
	static {
		
		itemMultiplier_.put(Material.CARROT, 1);
		itemMultiplier_.put(Material.REDSTONE_ORE, 1);
		itemMultiplier_.put(Material.GLOWING_REDSTONE_ORE, 1);
		itemMultiplier_.put(Material.GLOWSTONE, 1);
		itemMultiplier_.put(Material.MELON_BLOCK, 1);
		itemMultiplier_.put(Material.NETHER_WARTS, 1);
		itemMultiplier_.put(Material.GRASS, 2);
		itemMultiplier_.put(Material.CROPS, 1);
	}
	
	private static final HashMap<Material,String> itemsWithMultiplier_ = new HashMap<>();
	static {
		
		itemsWithMultiplier_.put(Material.CARROT, "391-0");
		itemsWithMultiplier_.put(Material.REDSTONE_ORE, "331-0");
		itemsWithMultiplier_.put(Material.GLOWING_REDSTONE_ORE, "331-0");
		itemsWithMultiplier_.put(Material.GLOWSTONE, "348-0");
		itemsWithMultiplier_.put(Material.MELON_BLOCK, "360-0");
		itemsWithMultiplier_.put(Material.NETHER_WARTS, "372-0");
		itemsWithMultiplier_.put(Material.GRASS, "295-0");
		itemsWithMultiplier_.put(Material.CROPS, "295-0");
	}
	
	private static final HashMap<String,Integer> maxAmountForMultipliedItems_ = new HashMap<>();
	static {
		maxAmountForMultipliedItems_.put("348-0", 4);
		maxAmountForMultipliedItems_.put("360-0", 9);
	}
	
	private static final ArrayList<Material> toolsThatCheckRawDrops_ = new ArrayList<>();
	static {
		toolsThatCheckRawDrops_.add(Material.IRON_AXE);
		toolsThatCheckRawDrops_.add(Material.IRON_HOE);
		toolsThatCheckRawDrops_.add(Material.IRON_PICKAXE);
		toolsThatCheckRawDrops_.add(Material.IRON_SPADE);
		toolsThatCheckRawDrops_.add(Material.STONE_AXE);
		toolsThatCheckRawDrops_.add(Material.STONE_HOE);
		toolsThatCheckRawDrops_.add(Material.STONE_PICKAXE);
		toolsThatCheckRawDrops_.add(Material.STONE_SPADE);
		toolsThatCheckRawDrops_.add(Material.WOOD_AXE);
		toolsThatCheckRawDrops_.add(Material.WOOD_HOE);
		toolsThatCheckRawDrops_.add(Material.WOOD_PICKAXE);
		toolsThatCheckRawDrops_.add(Material.WOOD_SPADE);
		toolsThatCheckRawDrops_.add(Material.GOLD_AXE);
		toolsThatCheckRawDrops_.add(Material.GOLD_HOE);
		toolsThatCheckRawDrops_.add(Material.GOLD_PICKAXE);
		toolsThatCheckRawDrops_.add(Material.GOLD_SPADE);
		toolsThatCheckRawDrops_.add(Material.DIAMOND_AXE);
		toolsThatCheckRawDrops_.add(Material.DIAMOND_HOE);
		toolsThatCheckRawDrops_.add(Material.DIAMOND_PICKAXE);
		toolsThatCheckRawDrops_.add(Material.DIAMOND_SPADE);
	}
	
	
	private static final Set<Material> silkTouchBlacklist_ = EnumSet.of(Material.AIR, Material.WATER, Material.LAVA, Material.MOB_SPAWNER);
	
	/**
	 * @param block
	 * @param tool
	 * @return
	 */
	public static Collection<ItemStack> getDrops(Block block, ItemStack tool) {
		
		if(hasEnchantment(tool, Enchantment.SILK_TOUCH) && !silkTouchBlacklist_.contains(block.getType())) {
			
			ArrayList<ItemStack> calculatedDrops = new ArrayList<>();
			calculatedDrops.add(new ItemStack(block.getType(), 1, (short) block.getData()));
			return calculatedDrops;
		}
		
		ArrayList<RBlockDrop> blockDropList = null;
		
		// TOOL RELATED DROPS
		if(tool != null) {
		
			HashMap<String,ArrayList<RBlockDrop>> dropsByTool = dropsByTools_.get(tool.getType().toString());
			
			if(dropsByTool != null) blockDropList = getRBlockDropsFromList(dropsByTool, block);
		}
		
		// TOOL UNRELATED DROPS
		if(blockDropList == null) blockDropList = getRBlockDropsFromList(blockDrops_, block);
		
		// Nachdem Tools gecheckt wurden, jetzt noch RawDrops checken. Wenn es beides nicht gibt, dann Ende.
		Collection<ItemStack> rawDrops = block.getDrops(tool);
		
		boolean noRawDrops = rawDrops == null || rawDrops.isEmpty();
		boolean noBlockDropList = blockDropList == null || blockDropList.isEmpty();
		boolean isToolAndHasNoRawDrops = toolsThatCheckRawDrops_.contains(tool.getType()) && noRawDrops;
		
		if(isToolAndHasNoRawDrops || (noRawDrops && noBlockDropList)) return (Collection<ItemStack>) new ArrayList<ItemStack>();
		
		// Drops ausrechnen, wenn custom drops da sind andernfalls jetzt schonmal
		// die urspruenglichen Drops zurueck geben.
		if(blockDropList == null || blockDropList.isEmpty()) return calculateLuck(block, rawDrops, tool);
		
		ArrayList<ItemStack> calculatedDrops = new ArrayList<>();
		ItemStack drop;
		
		for(RBlockDrop blockDrop : blockDropList) {
			
			if((drop = blockDrop.getDrop(tool)) == null) continue;
			
			calculatedDrops.add(drop);
		}
		
		if(calculatedDrops.isEmpty()) return (Collection<ItemStack>) new ArrayList<ItemStack>();
		
		return calculateLuck(block, (Collection<ItemStack>) calculatedDrops, tool);
	}
	
	public static boolean hasEnchantment(ItemStack tool, Enchantment enchantment) {
		
		if(tool == null) return false; 
		
		Map<Enchantment, Integer> enchantments = tool.getEnchantments();
		if(enchantments == null || enchantments.isEmpty()) return false;
		
		return enchantments.containsKey(enchantment);
	}
	
	public static Integer getEnchantmentLevel(ItemStack tool, Enchantment enchantment) {
		
		if(!hasEnchantment(tool, enchantment)) return null;
		
		return tool.getEnchantmentLevel(enchantment);
	}
	
	public static Collection<ItemStack> calculateLuck(Block block, Collection<ItemStack> rawDrops, ItemStack tool) {
		
		if(rawDrops == null || rawDrops.isEmpty() || !hasEnchantment(tool, Enchantment.LOOT_BONUS_BLOCKS)) return rawDrops;
		
		boolean hasOreWithLootBonus = oreWithLootBonus_.containsKey(block.getType());
		boolean hasMultiplierBonus = itemMultiplier_.containsKey(block.getType());
		
		if(!hasOreWithLootBonus && !hasMultiplierBonus) return rawDrops;
		
		Integer lvl = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		if(lvl == null || lvl == 0) return rawDrops;
		
		// Glueck geht nich hoeher als drei.
		if(lvl > 3) lvl = 3;
		
		if(hasOreWithLootBonus) {
			
			Double chance = oreChancesByLevel_.get(lvl) / 100;
			String whitelist = oreWithLootBonus_.get(block.getType());
			boolean dropIsWhitelisted;
			int stackAmount;
			int resultAmount;
			
			for(ItemStack rawDrop : rawDrops) {
			
				dropIsWhitelisted = whitelist == null || whitelist.equals(rawDrop.getTypeId() + "-" + rawDrop.getDurability());
				
				if(!dropIsWhitelisted) continue;
				
				stackAmount = resultAmount = rawDrop.getAmount();
				
				for(int i = 1; i <= stackAmount; ++i) {
					
					for(int j = 1; j <= lvl; ++j) {
						
						if(Math.random() <= chance) ++resultAmount;
					}
				}
				
				rawDrop.setAmount(resultAmount);
			}
			
		} else if(hasMultiplierBonus) {
			
			int toAdd = itemMultiplier_.get(block.getType()) * lvl;
			String whitelist = itemsWithMultiplier_.get(block.getType());
			Integer maxAmount = maxAmountForMultipliedItems_.get(whitelist);
			boolean dropIsWhitelisted;
			int resultAmount;
			String itemId;
			
			for(ItemStack rawDrop : rawDrops) {
				
				itemId = rawDrop.getTypeId() + "-" + rawDrop.getDurability();
				dropIsWhitelisted = whitelist == null || whitelist.equals(itemId);
				if(!dropIsWhitelisted) continue;
				
				resultAmount = rawDrop.getAmount() + toAdd;
				if(maxAmount != null && maxAmount < resultAmount) resultAmount = maxAmount;
				
				rawDrop.setAmount(resultAmount);
			}
		}
		
		return rawDrops;
	}
	
	public static ArrayList<RBlockDrop> getRBlockDropsFromList(HashMap<String,ArrayList<RBlockDrop>> list, Block block) {
		
		// Erst ID pruefen.
		String materialId = block.getType().toString();
		ArrayList<RBlockDrop> blockDropList = list.get(materialId);
		if(blockDropList != null) return blockDropList;
		
		// Dann SUBID checken.
		return list.get(materialId + "-" + (int) block.getData());
	}
}
