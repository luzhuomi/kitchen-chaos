package dev.cookingchaos

import net.minecraft.core.Holder
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

/**
 * A kitchen theme: block palette for /cook kitchen, a themed menu, and rule modifiers.
 * Pantry/stove/service blocks stay functional (barrel / campfire-family / bell);
 * the palette changes floor, walls, lights and decoration.
 */
final case class Theme(
    id: String,
    name: String,
    dishes: List[Dish],
    floor: Block,
    wall: Block,
    light: Block,
    stove: Block,
    orderGapSeconds: Int,
    timeScale: Double,
    burnGraceTicks: Long,
    speedEffect: Option[Holder[MobEffect]],
    doublePantry: Boolean,
    unlockLevel: Int = 1
)

object Themes:
  private def dish(
      name: String,
      output: net.minecraft.world.item.Item,
      points: Int,
      time: Int,
      ingredients: List[net.minecraft.world.item.Item],
      prep: Option[PrepKind] = None
  ): Dish = Dish(name, output, points, time, ingredients, prep)

  val classic: Theme = Theme(
    id = "classic",
    name = "Classic Diner",
    dishes = List(
      dish("Baked Potato", Items.BAKED_POTATO, 60, 70, List(Items.POTATO)),
      dish("Grilled Steak", Items.COOKED_BEEF, 100, 90, List(Items.BEEF)),
      dish("Roast Chicken", Items.COOKED_CHICKEN, 90, 80, List(Items.CHICKEN)),
      dish("Grilled Fish", Items.COOKED_COD, 90, 80, List(Items.COD)),
      dish("Fresh Bread", Items.BREAD, 120, 90, List(Items.WHEAT)),
      dish("Forest Stew", Items.MUSHROOM_STEW, 150, 110, List(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.BOWL)),
      dish("Birthday Cake", Items.CAKE, 250, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Cottage Rabbit Stew", Items.RABBIT_STEW, 180, 130, List(Items.RABBIT, Items.POTATO, Items.CARROT, Items.BROWN_MUSHROOM, Items.BOWL))
    ),
    floor = Blocks.OAK_PLANKS, wall = Blocks.OAK_FENCE, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = None, doublePantry = false
  )

  val halloween: Theme = Theme(
    id = "halloween",
    name = "The Witch's Kitchen",
    dishes = List(
      dish("Witch's Brew", Items.SUSPICIOUS_STEW, 160, 110, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.POPPY)),
      dish("Pumpkin Soup", Items.PUMPKIN_PIE, 140, 100, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Zombie Burger", Items.COOKED_BEEF, 110, 90, List(Items.BEEF)),
      dish("Bat Wings", Items.COOKED_CHICKEN, 100, 80, List(Items.CHICKEN)),
      dish("Cobweb Cake", Items.CAKE, 260, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Mummy Bites", Items.COOKED_RABBIT, 120, 80, List(Items.RABBIT))
    ),
    floor = Blocks.SPRUCE_PLANKS, wall = Blocks.CARVED_PUMPKIN, light = Blocks.SOUL_LANTERN, stove = Blocks.SOUL_CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = None, doublePantry = false,
    unlockLevel = 2
  )

  val pirate: Theme = Theme(
    id = "pirate",
    name = "The Salty Galley",
    dishes = List(
      dish("Grilled Fish", Items.COOKED_COD, 100, 70, List(Items.COD)),
      dish("Kraken Calamari", Items.COOKED_SALMON, 130, 70, List(Items.SALMON)),
      dish("Sea Greens", Items.DRIED_KELP, 60, 50, List(Items.KELP)),
      dish("Sea Sorbet", Items.GLOW_BERRIES, 90, 60, List(Items.GLOW_BERRIES)),
      dish("Ship's Hardtack", Items.COOKIE, 70, 60, List(Items.WHEAT, Items.COCOA_BEANS))
    ),
    floor = Blocks.DARK_OAK_PLANKS, wall = Blocks.PRISMARINE, light = Blocks.SEA_LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 9, timeScale = 0.8, burnGraceTicks = 240L, speedEffect = None, doublePantry = false,
    unlockLevel = 3
  )

  val nether: Theme = Theme(
    id = "nether",
    name = "Hell's Kitchen",
    dishes = List(
      dish("Blaze Curry", Items.COOKED_CHICKEN, 120, 70, List(Items.CHICKEN)),
      dish("Magma Burger", Items.COOKED_PORKCHOP, 130, 70, List(Items.PORKCHOP)),
      dish("Crimson Stew", Items.MUSHROOM_STEW, 170, 90, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM)),
      dish("Ember Skewer", Items.COOKED_RABBIT, 140, 70, List(Items.RABBIT))
    ),
    floor = Blocks.BLACKSTONE, wall = Blocks.BASALT, light = Blocks.SOUL_LANTERN, stove = Blocks.SOUL_CAMPFIRE,
    orderGapSeconds = 8, timeScale = 0.8, burnGraceTicks = 120L, speedEffect = None, doublePantry = false,
    unlockLevel = 5
  )

  val christmas: Theme = Theme(
    id = "christmas",
    name = "Christmas Feast",
    dishes = List(
      dish("Festive Roast", Items.COOKED_MUTTON, 140, 100, List(Items.MUTTON)),
      dish("Yule Log Cake", Items.CAKE, 280, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Hot Cocoa", Items.COOKIE, 80, 70, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Reindeer Carrot", Items.CARROT, 50, 50, List(Items.CARROT)),
      dish("Hearthwarmer Stew", Items.RABBIT_STEW, 190, 120, List(Items.RABBIT, Items.POTATO, Items.CARROT, Items.BROWN_MUSHROOM, Items.BOWL))
    ),
    floor = Blocks.SPRUCE_PLANKS, wall = Blocks.RED_CONCRETE, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.1, burnGraceTicks = 240L, speedEffect = None, doublePantry = false,
    unlockLevel = 2
  )

  val sushi: Theme = Theme(
    id = "sushi",
    name = "Sakura Counter",
    dishes = List(
      dish("Tuna Nigiri", Items.COOKED_SALMON, 130, 70, List(Items.SALMON)),
      dish("Kelp Roll", Items.DRIED_KELP, 60, 50, List(Items.KELP)),
      dish("Pufferfish Sashimi", Items.PUFFERFISH, 300, 60, List(Items.PUFFERFISH), Some(PrepKind.Washed)),
      dish("Rice Ball", Items.BREAD, 110, 70, List(Items.WHEAT)),
      dish("Seaweed Miso", Items.MUSHROOM_STEW, 140, 90, List(Items.BOWL, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM))
    ),
    floor = Blocks.CHERRY_PLANKS, wall = Blocks.BAMBOO_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 10, timeScale = 0.9, burnGraceTicks = 240L, speedEffect = None, doublePantry = false,
    unlockLevel = 3
  )

  val medieval: Theme = Theme(
    id = "medieval",
    name = "The Prancing Pony Tavern",
    dishes = List(
      dish("Dragon Steak", Items.COOKED_BEEF, 150, 110, List(Items.BEEF)),
      dish("Troll Stew", Items.MUSHROOM_STEW, 180, 120, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM)),
      dish("Elven Bread", Items.BREAD, 130, 100, List(Items.WHEAT)),
      dish("Mead", Items.HONEY_BOTTLE, 90, 70, List(Items.HONEY_BOTTLE)),
      dish("Venison Pie", Items.PUMPKIN_PIE, 160, 100, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
    ),
    floor = Blocks.STONE_BRICKS, wall = Blocks.DEEPSLATE_BRICKS, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 15, timeScale = 1.3, burnGraceTicks = 300L, speedEffect = None, doublePantry = false,
    unlockLevel = 4
  )

  val space: Theme = Theme(
    id = "space",
    name = "Canteen Zero-G",
    dishes = List(
      dish("Meteor Steak", Items.COOKED_BEEF, 140, 90, List(Items.BEEF)),
      dish("Glow Soup", Items.SUSPICIOUS_STEW, 170, 110, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.DANDELION)),
      dish("Comet Cookies", Items.COOKIE, 90, 80, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Moon Cake", Items.CAKE, 270, 140, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Star Fruit", Items.GOLDEN_CARROT, 200, 80, List(Items.CARROT, Items.GOLD_NUGGET))
    ),
    floor = Blocks.QUARTZ_BLOCK, wall = Blocks.COPPER_BLOCK, light = Blocks.END_ROD, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = Some(MobEffects.SLOW_FALLING), doublePantry = false,
    unlockLevel = 5
  )

  val candy: Theme = Theme(
    id = "candy",
    name = "The Sugar Factory",
    dishes = List(
      dish("Birthday Cake", Items.CAKE, 260, 130, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Cookie Tower", Items.COOKIE, 100, 70, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Candy Apple", Items.APPLE, 70, 50, List(Items.APPLE, Items.SUGAR)),
      dish("Fairy Pie", Items.PUMPKIN_PIE, 150, 90, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Caramel Apple", Items.GOLDEN_APPLE, 300, 120, List(Items.APPLE, Items.GOLD_INGOT))
    ),
    floor = Blocks.PINK_CONCRETE, wall = Blocks.RED_CONCRETE, light = Blocks.SHROOMLIGHT, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 8, timeScale = 0.9, burnGraceTicks = 240L, speedEffect = Some(MobEffects.MOVEMENT_SPEED), doublePantry = false,
    unlockLevel = 3
  )

  val harvest: Theme = Theme(
    id = "harvest",
    name = "Farmhouse BBQ",
    dishes = List(
      dish("BBQ Ribs", Items.COOKED_PORKCHOP, 130, 90, List(Items.PORKCHOP)),
      dish("Harvest Pie", Items.PUMPKIN_PIE, 150, 100, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Corn Chowder", Items.MUSHROOM_STEW, 140, 100, List(Items.BOWL, Items.CARROT, Items.BROWN_MUSHROOM)),
      dish("Hay Bread", Items.BREAD, 120, 90, List(Items.WHEAT)),
      dish("Harvest Haunch", Items.COOKED_RABBIT, 130, 80, List(Items.RABBIT))
    ),
    floor = Blocks.BIRCH_PLANKS, wall = Blocks.HAY_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = None, doublePantry = true,
    unlockLevel = 2
  )

  val jungle: Theme = Theme(
    id = "jungle",
    name = "Treetop Restaurant",
    dishes = List(
      dish("Jungle Stew", Items.SUSPICIOUS_STEW, 170, 110, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.DANDELION)),
      dish("Cacao Cake", Items.CAKE, 260, 140, List(Items.WHEAT, Items.COCOA_BEANS, Items.EGG, Items.MILK_BUCKET)),
      dish("Melon Sorbet", Items.MELON_SLICE, 60, 50, List(Items.MELON_SLICE), Some(PrepKind.Washed)),
      dish("Bug Stir-Fry", Items.COOKED_CHICKEN, 110, 80, List(Items.CHICKEN)),
      dish("Jungle Berry Cup", Items.GLOW_BERRIES, 80, 60, List(Items.GLOW_BERRIES))
    ),
    floor = Blocks.JUNGLE_PLANKS, wall = Blocks.MOSS_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 11, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = Some(MobEffects.JUMP), doublePantry = false,
    unlockLevel = 4
  )

  val lunar: Theme = Theme(
    id = "lunar",
    name = "Spring Festival Banquet",
    dishes = List(
      dish("Whole Fish (Abundance)", Items.COOKED_COD, 120, 80, List(Items.COD)),
      dish("Nian Cake (Year Cake)", Items.CAKE, 260, 140, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Family Dumplings", Items.MUSHROOM_STEW, 150, 100, List(Items.BOWL, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM)),
      dish("Lucky Tangerine", Items.GOLDEN_CARROT, 200, 80, List(Items.CARROT, Items.GOLD_NUGGET))
    ),
    floor = Blocks.RED_CONCRETE, wall = Blocks.GOLD_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 13, timeScale = 1.15, burnGraceTicks = 260L, speedEffect = None, doublePantry = false,
    unlockLevel = 3
  )

  val easter: Theme = Theme(
    id = "easter",
    name = "The Bunny Burrow",
    dishes = List(
      dish("Bunny Stew", Items.RABBIT_STEW, 180, 120, List(Items.RABBIT, Items.POTATO, Items.CARROT, Items.BROWN_MUSHROOM, Items.BOWL)),
      dish("Carrot Cake", Items.CAKE, 260, 140, List(Items.WHEAT, Items.CARROT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Hot Cross Buns", Items.BREAD, 120, 90, List(Items.WHEAT)),
      dish("Dyed Eggs", Items.EGG, 60, 50, List(Items.EGG))
    ),
    floor = Blocks.GRASS_BLOCK, wall = Blocks.PINK_CONCRETE, light = Blocks.SHROOMLIGHT, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 11, timeScale = 1.05, burnGraceTicks = 240L, speedEffect = None, doublePantry = false,
    unlockLevel = 2
  )

  val all: List[Theme] = List(
    classic, halloween, christmas, harvest, candy, pirate, sushi, lunar, medieval, jungle, space, nether, easter
  )

  def byId(id: String): Option[Theme] = all.find(_.id.equalsIgnoreCase(id))
end Themes
