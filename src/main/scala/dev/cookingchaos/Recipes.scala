package dev.cookingchaos

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/** A dish customers can order. Players must deliver the `output` item. */
final case class Dish(
    name: String,
    output: Item,
    points: Int,
    timeLimitSeconds: Int,
    ingredients: List[Item]
)

object Recipes:
  val dishes: List[Dish] = List(
    Dish("Baked Potato", Items.BAKED_POTATO, 60, 70, List(Items.POTATO)),
    Dish("Grilled Steak", Items.COOKED_BEEF, 100, 90, List(Items.BEEF)),
    Dish("Roast Chicken", Items.COOKED_CHICKEN, 90, 80, List(Items.CHICKEN)),
    Dish("Grilled Fish", Items.COOKED_COD, 90, 80, List(Items.COD)),
    Dish("Fresh Bread", Items.BREAD, 120, 90, List(Items.WHEAT)),
    Dish("Forest Stew", Items.MUSHROOM_STEW, 150, 110, List(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.BOWL)),
    Dish("Birthday Cake", Items.CAKE, 250, 150, List(Items.WHEAT, Items.SUGAR, Items.EGG, Items.MILK_BUCKET))
  )

  /** raw item -> (cooked item, cook time in ticks) at a stove. */
  val cookable: Map[Item, (Item, Int)] = Map(
    Items.BEEF -> (Items.COOKED_BEEF, 160),
    Items.POTATO -> (Items.BAKED_POTATO, 140),
    Items.CHICKEN -> (Items.COOKED_CHICKEN, 160),
    Items.COD -> (Items.COOKED_COD, 160)
  )
