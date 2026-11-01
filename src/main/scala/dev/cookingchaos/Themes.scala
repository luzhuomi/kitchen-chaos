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
    doublePantry: Boolean
)

object Themes:
  private def dish(
      name: String,
      output: net.minecraft.world.item.Item,
      points: Int,
      time: Int,
      ingredients: List[net.minecraft.world.item.Item]
  ): Dish = Dish(name, output, points, time, ingredients)

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
      dish("Birthday Cake", Items.CAKE, 250, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
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
      dish("Cobweb Cake", Items.CAKE, 260, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
    ),
    floor = Blocks.SPRUCE_PLANKS, wall = Blocks.CARVED_PUMPKIN, light = Blocks.SOUL_LANTERN, stove = Blocks.SOUL_CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = None, doublePantry = false
  )

  val pirate: Theme = Theme(
    id = "pirate",
    name = "The Salty Galley",
    dishes = List(
      dish("Grilled Fish", Items.COOKED_COD, 100, 70, List(Items.COD)),
      dish("Kraken Calamari", Items.COOKED_SALMON, 130, 70, List(Items.SALMON)),
      dish("Sea Greens", Items.DRIED_KELP, 60, 50, List(Items.KELP)),
      dish("Sea Sorbet", Items.GLOW_BERRIES, 90, 60, List(Items.GLOW_BERRIES))
    ),
    floor = Blocks.DARK_OAK_PLANKS, wall = Blocks.PRISMARINE, light = Blocks.SEA_LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 9, timeScale = 0.8, burnGraceTicks = 240L, speedEffect = None, doublePantry = false
  )

  val nether: Theme = Theme(
    id = "nether",
    name = "Hell's Kitchen",
    dishes = List(
      dish("Blaze Curry", Items.COOKED_CHICKEN, 120, 70, List(Items.CHICKEN)),
      dish("Magma Burger", Items.COOKED_PORKCHOP, 130, 70, List(Items.PORKCHOP)),
      dish("Crimson Stew", Items.MUSHROOM_STEW, 170, 90, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM))
    ),
    floor = Blocks.BLACKSTONE, wall = Blocks.BASALT, light = Blocks.SOUL_LANTERN, stove = Blocks.SOUL_CAMPFIRE,
    orderGapSeconds = 8, timeScale = 0.8, burnGraceTicks = 120L, speedEffect = None, doublePantry = false
  )

  val winter: Theme = Theme(
    id = "winter",
    name = "North Pole Diner",
    dishes = List(
      dish("Festive Roast", Items.COOKED_MUTTON, 140, 100, List(Items.MUTTON)),
      dish("Yule Log Cake", Items.CAKE, 280, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Hot Cocoa", Items.COOKIE, 80, 70, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Reindeer Carrot", Items.CARROT, 50, 50, List(Items.CARROT))
    ),
    floor = Blocks.BLUE_ICE, wall = Blocks.WHITE_CONCRETE, light = Blocks.SEA_LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.1, burnGraceTicks = 240L, speedEffect = None, doublePantry = false
  )

  val sushi: Theme = Theme(
    id = "sushi",
    name = "Sakura Counter",
    dishes = List(
      dish("Tuna Nigiri", Items.COOKED_SALMON, 130, 70, List(Items.SALMON)),
      dish("Kelp Roll", Items.DRIED_KELP, 60, 50, List(Items.KELP)),
      dish("Pufferfish Sashimi", Items.PUFFERFISH, 300, 60, List(Items.PUFFERFISH)),
      dish("Rice Ball", Items.BREAD, 110, 70, List(Items.WHEAT))
    ),
    floor = Blocks.CHERRY_PLANKS, wall = Blocks.BAMBOO_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 10, timeScale = 0.9, burnGraceTicks = 240L, speedEffect = None, doublePantry = false
  )

  val medieval: Theme = Theme(
    id = "medieval",
    name = "The Prancing Pony Tavern",
    dishes = List(
      dish("Dragon Steak", Items.COOKED_BEEF, 150, 110, List(Items.BEEF)),
      dish("Troll Stew", Items.MUSHROOM_STEW, 180, 120, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM)),
      dish("Elven Bread", Items.BREAD, 130, 100, List(Items.WHEAT)),
      dish("Mead", Items.HONEY_BOTTLE, 90, 70, List(Items.HONEY_BOTTLE))
    ),
    floor = Blocks.STONE_BRICKS, wall = Blocks.DEEPSLATE_BRICKS, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 15, timeScale = 1.3, burnGraceTicks = 300L, speedEffect = None, doublePantry = false
  )

  val space: Theme = Theme(
    id = "space",
    name = "Canteen Zero-G",
    dishes = List(
      dish("Meteor Steak", Items.COOKED_BEEF, 140, 90, List(Items.BEEF)),
      dish("Glow Soup", Items.SUSPICIOUS_STEW, 170, 110, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.DANDELION)),
      dish("Comet Cookies", Items.COOKIE, 90, 80, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Moon Cake", Items.CAKE, 270, 140, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
    ),
    floor = Blocks.QUARTZ_BLOCK, wall = Blocks.COPPER_BLOCK, light = Blocks.END_ROD, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = Some(MobEffects.SLOW_FALLING), doublePantry = false
  )

  val candy: Theme = Theme(
    id = "candy",
    name = "The Sugar Factory",
    dishes = List(
      dish("Birthday Cake", Items.CAKE, 260, 130, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Cookie Tower", Items.COOKIE, 100, 70, List(Items.COCOA_BEANS, Items.WHEAT)),
      dish("Candy Apple", Items.APPLE, 70, 50, List(Items.APPLE, Items.SUGAR)),
      dish("Fairy Pie", Items.PUMPKIN_PIE, 150, 90, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
    ),
    floor = Blocks.PINK_CONCRETE, wall = Blocks.RED_CONCRETE, light = Blocks.SHROOMLIGHT, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 8, timeScale = 0.9, burnGraceTicks = 240L, speedEffect = Some(MobEffects.MOVEMENT_SPEED), doublePantry = false
  )

  val harvest: Theme = Theme(
    id = "harvest",
    name = "Farmhouse BBQ",
    dishes = List(
      dish("BBQ Ribs", Items.COOKED_PORKCHOP, 130, 90, List(Items.PORKCHOP)),
      dish("Harvest Pie", Items.PUMPKIN_PIE, 150, 100, List(Items.PUMPKIN, Items.SUGAR, Items.EGG, Items.MILK_BUCKET)),
      dish("Corn Chowder", Items.MUSHROOM_STEW, 140, 100, List(Items.BOWL, Items.CARROT, Items.BROWN_MUSHROOM)),
      dish("Hay Bread", Items.BREAD, 120, 90, List(Items.WHEAT))
    ),
    floor = Blocks.BIRCH_PLANKS, wall = Blocks.HAY_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 12, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = None, doublePantry = true
  )

  val jungle: Theme = Theme(
    id = "jungle",
    name = "Treetop Restaurant",
    dishes = List(
      dish("Jungle Stew", Items.SUSPICIOUS_STEW, 170, 110, List(Items.BOWL, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.DANDELION)),
      dish("Cacao Cake", Items.CAKE, 260, 140, List(Items.WHEAT, Items.COCOA_BEANS, Items.EGG, Items.MILK_BUCKET)),
      dish("Melon Sorbet", Items.MELON_SLICE, 60, 50, List(Items.MELON_SLICE)),
      dish("Bug Stir-Fry", Items.COOKED_CHICKEN, 110, 80, List(Items.CHICKEN))
    ),
    floor = Blocks.JUNGLE_PLANKS, wall = Blocks.MOSS_BLOCK, light = Blocks.LANTERN, stove = Blocks.CAMPFIRE,
    orderGapSeconds = 11, timeScale = 1.0, burnGraceTicks = 240L, speedEffect = Some(MobEffects.JUMP), doublePantry = false
  )

  val all: List[Theme] = List(
    classic, halloween, pirate, nether, winter, sushi, medieval, space, candy, harvest, jungle
  )

  def byId(id: String): Option[Theme] = all.find(_.id.equalsIgnoreCase(id))
end Themes
