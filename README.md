# Recipes

Please visit the [Wiki](https://github.com/systemNEO/Recipes/wiki) for more information.

# CraftBukkit Compatibility

- Latest version of Recipes successfully tested with craftbukkit-1.5.1-R1.0

# Changelog / Downloads

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
