# Recipes

Please visit the [Wiki](https://github.com/systemNEO/Recipes/wiki) for more information.

# CraftBukkit Compatibility

- Latest version successfully testet with craftbukkit-1.7.2-R0.3.

# Changelog / Downloads

Download [Version 1.8.18](http://www.systemneo.de/_bukkit/Recipes_v1.8.18.zip) (Released 08.03.2014)

- Successfully testet with craftbukkit-1.7.2-R0.3.
- Updated Plugin to Minecraft 1.7.5
- Removed "&r" color reset for recipes.

Download [Version 1.8.15](http://www.systemneo.de/_bukkit/Recipes_v1.8.15.zip) (Released 09.07.2013)

- Successfully testet with craftbukkit-1.6.2-R0.1-20130709.000904-1.
- Updated Plugin to Minecraft 1.6.2

Download [Version 1.8.14](http://www.systemneo.de/_bukkit/Recipes_v1.8.14.zip) (Released 03.07.2013)

- Successfully testet with craftbukkit-1.6.1-R0.1-20130703.085909-10.
- Updated Plugin to Minecraft 1.6.1

Download [Version 1.8.12](http://www.systemneo.de/_bukkit/Recipes_v1.8.12.zip) (Released 27.06.2013)

- Successfully testet with craftbukkit-1.5.2-R0.2-20130610.181759-14.
- Fixed issue: Due to clients mods occouring wrong recipe results would be now prevented (again).

Download [Version 1.8.11](http://www.systemneo.de/_bukkit/Recipes_v1.8.11.zip) (Released 26.06.2013)

- Successfully testet with craftbukkit-1.5.2-R0.2-20130610.181759-14.
- Fixed [issue #9]: On canceled crafting actions the inventory now will be updated so no ghost items will be shown in inventory.
- Fixed [issue #11]: Durability enchantment is now considered correctly on block breaks.
- Fixed issue: Users with client-mods Optifine+Modloader+Reis minmap+MorePlayerModels have the issue that if they shift right click on crafting result then the client fires each item within the crafting grid a craft event. So the plugin was triggered many times in a second and the recipes was executeable more times than provided because a calculation bug occurs from the second craft event. The calculation works now correctly.

Download [Version 1.8.10](http://www.systemneo.de/_bukkit/Recipes_v1.8.10.zip) (Released 20.06.2013)

- Successfully testet with craftbukkit-1.5.2-R0.2-20130610.181759-14.
- Fixed issue: Lag protection works now correctly. It is no more possible to get results of removed recipes.
- Fixed issue: Recipes with result "air" (e.g. type "remove") no longer consumes ingredients on shift-click.
- Fixed issue: SubIds will be determined now by a better way (byte instead of durability).
- Fixed issue: On canceled crafting actions the inventory now will be updated so no ghost items will be shown in inventory.
- Internal refactoring for better performance.

Download [Version 1.8.9](http://www.systemneo.de/_bukkit/Recipes_v1.8.9.zip) (Released 12.06.2013)

- Successfully testet with craftbukkit-1.5.2-R0.2-20130610.181759-14.
- Fixed issue: Falling blocks like sand and gravel now considered correctly for drop recipes.
- Fixed issue: Order of player groups is now correct so recipes with wildcards are cloned correctly.
- Added support for inventory drag events (new feature of Minecraft 1.5.x).

Download [Version 1.8.8](http://www.systemneo.de/_bukkit/Recipes_v1.8.8.zip) (Released 11.06.2013)

- From version 1.8.8 only compatible with craftbukkit-1.5.2-R0.2-20130610.181759-14 or higher. See changelog notes for more information about compatibility.
- Successfully testet with craftbukkit-1.5.2-R0.2-20130610.181759-14.
- Updated: Used new CraftItemEvent of CraftBukkit (craftbukkit-1.5.2-R0.2-20130610.181759-14). Note: Other CraftBukkit Versions wont work!
- Fixed issue: Recipes checks Shift-click conditions on crafting now correctly.
- Fixed [issue #8](https://github.com/systemNEO/Recipes/issues/7): Recipes stacks items on Right-click crafting now correctly.
- Fixed issue: Items drops of drop recipes now dropping naturally.
- Fixed issue: Tools that would be used to break blocks of drop recipes getting now the correct damage.
- Fixed issue: Drop recipes of type block will now considering tool types correctly.
- Fixed issue: No more nullpointerexception on recipe cloning. Internal recipe cloning is now correct.
- Fixed issue: Shearing leaves drops leaves of type oak correctly.
- Added nodrop support for plugin "KingdomSide".

Download [Version 1.8.5](http://www.systemneo.de/_bukkit/Recipes_v1.8.5.zip) (Released 30.05.2013)

- Successfully tested with craftbukkit-1.5.2-R0.2.
- Fixed issue: Drop recipes of type "chance" for entites calculating the drops now correctly.

Download [Version 1.8.4](http://www.systemneo.de/_bukkit/Recipes_v1.8.4.zip) (Released 20.05.2013)

- Fixed issue: If a block including meta data was placed then the meta data will be now restored correctly on block break.
- Fixed [issue #7](https://github.com/systemNEO/Recipes/issues/7): Removed chat color reset tags from custom lore.

Download [Version 1.8.3](http://www.systemneo.de/_bukkit/Recipes_v1.8.3.zip) (Released 09.05.2013)

- Added verion info on ingame command /recipes
- Added "rebreak" time of 10 minutes for drop recipes so player placed blocks can be broken without considering the drop chance of the drop recipe.
- Added custom getDrops-Methods for drop recipes to simulate drops by an used item correctly , e.g. enchantments and random multiple drops (bukkit hasn't implemented this correctly).
- Fixed issue: On crafting non-plugin recipes the crafting inventory and its result will be updated now correctly.
- Fixed issue: If a sheep was sheared by a player then the shear now losts its durability correctly.
- Fixed issue: If a block breaks and drops xp the drop recipes considering xp now.

Download [Version 1.8.1](http://www.systemneo.de/_bukkit/Recipes_v1.8.1.zip) (Released 15.04.2013)

- Added wildcard support for drop recipes of type block.
- Added sheep shearing support for drop recipes of type entity "sheep".
- Fixed issue: It is now possible to create drop recipes with 100.0 chance.

Download [Version 1.8](http://www.systemneo.de/_bukkit/Recipes_v1.8.zip) (Released 14.04.2013)

- Added new parameter "chance" for drop recipes, see [drops](https://github.com/systemNEO/Recipes/wiki#drops) for more information.
- Recipes of type drop now supporting region (kingdom) based algorythm.
- Recipes now depends to WorldGuard, too.
- The base command /recipes shows now the plugin version (inGame).
- If no drop recipes of type entity or block configured then algorythms are skipped to safe performance.

Download [Version 1.7.4](http://www.systemneo.de/_bukkit/Recipes_v1.7.4.zip) (Released 01.04.2013)

- Fixed issue: Leaving chances are now correct.

Download [Version 1.7.3](http://www.systemneo.de/_bukkit/Recipes_v1.7.3.zip) (Released 01.04.2013)

- Fixed [issue #4](https://github.com/systemNEO/Recipes/issues/4): If a player uses Shift-Click to craft more than one stack at the same time and the inventory contains a minimum of an uncomplete stack of the same type then the result is now correct.

Download [Version 1.7.2](http://www.systemneo.de/_bukkit/Recipes_v1.7.2.zip) (Released 28.03.2013)

- Fixed issue: onBlockBreak and onBlockPlace respects if the events was canceled before (e.g. via WorldGuard or ModifyWorld).

Download [Version 1.7.1](http://www.systemneo.de/_bukkit/Recipes_v1.7.1.zip) (Released 28.03.2013)

- Fixed [issue #3](https://github.com/systemNEO/Recipes/issues/3): If a player crafts an item via player inventory crafting grid then the resources would be removed now correctly from slot 3 and 4.

Download [Version 1.7](http://www.systemneo.de/_bukkit/Recipes_v1.7.zip) (Released 26.03.2013)

- Added new feature "drop recipes" (see Wiki for more information).
- Fixed issue: If a player in creative mode breaks a block then possibly meta data will be removed now, too.

Download [Version 1.6.4](http://www.systemneo.de/_bukkit/Recipes_v1.6.4.zip) (Released 26.03.2013)

- Added support for plugin KingdomSide: Access rights for recipes can be restricted via kingdom names.
- Added new shift+click behavior: All items will be crafted instead of the maximum stack size of the item hand. Items that could'nt stored in the inventory will drop in front of the player.

Download [Version 1.6.3](http://www.systemneo.de/_bukkit/Recipes_v1.6.3.zip) (Released 24.03.2013)

- Fixed issue: Crafting grid is now converted correctly to workbench grid.
- Fixed issue: MetaData of placed blocks are now saved persistent.
- Fixed issue: Only drops of the same type as the broken block inherits the meta data.

Download [Version 1.6.2](http://www.systemneo.de/_bukkit/Recipes_v1.6.2.zip) (Released 02.03.2013)

- Fixed issue: If a placed block with metadata (displayname or lore) is broken and later another block is placed on the same position then the new placed block do not inherit the metadata anymore.

Download [Version 1.6.1](http://www.systemneo.de/_bukkit/Recipes_v1.6.1.zip) (Released 23.02.2013)

- Fixed issue: Configured PermissionsEX groups of the same rank no more throws an out of bound exception.
- Fixed issue: Recipes that using ingredients with wild cards no more throws null pointer exception on recipe type check.

Download [Version 1.6](http://www.systemneo.de/_bukkit/Recipes_v1.6.zip) (Released 06.02.2013)

- Recipe results now supporting [enchantments](https://github.com/systemNEO/Recipes/wiki#wiki-enchants).

Download [Version 1.5](http://www.systemneo.de/_bukkit/Recipes_v1.5.zip) (Released 06.02.2013)

- Added feature thats restore a placed blocks item meta data to the spawning item after breaking a block. Supported are display names and lore.

Download [Version 1.4](http://www.systemneo.de/_bukkit/Recipes_v1.4.zip) (Released 29.01.2013)

- Added item [subId wildcards (*)](https://github.com/systemNEO/Recipes/wiki#wiki-wildcards) to ingredients.

Download [Version 1.3.1](http://www.systemneo.de/_bukkit/Recipes_v1.3.1.zip) (Released 28.01.2013)

- Added "lag protection" to fix an issue where players can get the results of overwritten original recipes during a lag.

Download [Version 1.3](http://www.systemneo.de/_bukkit/Recipes_v1.3.zip) (Released 21.01.2013)

- Added optional drop chance for result and leaving items.
- Fixed issue: The amount of leaving items are now calculated correctly on SHIFT+click.

Download [Version 1.2.3](http://www.systemneo.de/_bukkit/Recipes_v1.2.3.zip) (Released 20.01.2013)

- Fixed issue: The plugin checks now if an item id really exists in minecraft to prevent null pointer exceptions.
- Fixed issue: Recipes of type remove removes now all related recipes of the result instead of only the first one.
- Plugin successfully tested for CraftBukkit 1.4.7.

Download [Version 1.2.2](http://www.systemneo.de/_bukkit/Recipes_v1.2.2.zip) (Released 13.01.2013)

- Added recipe leavings (see Wiki for more information).

Download [Version 1.2.1](http://www.systemneo.de/_bukkit/Recipes_v1.2.1.zip) (Released 11.01.2013)

- Fixed issue: Recipes of type free will be skipped on duplicate items,
because not allowed.
- Fixed issue: Result items of type AIR can not have a display name or
lore. This is checked now to prevent a null pointer exception.

Download [Version 1.2](http://www.systemneo.de/_bukkit/Recipes_v1.2.zip) (Released 10.01.2013)

- Prepared for CraftBukkit 1.4.6
- Added "remove" recipes functionality.
- Cleaned up the code a little bit.
