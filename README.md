# Recipes

Please visit the [Wiki](https://github.com/systemNEO/Recipes/wiki) for more information.

# CraftBukkit Compatibility

- Latest version of Recipes successfully tested with craftbukkit-1.4.7-R1.0

# Changelog / Downloads

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
