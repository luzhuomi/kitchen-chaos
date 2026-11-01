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
  /** raw item -> (cooked item, cook time in ticks) at a stove. */
  val cookable: Map[Item, (Item, Int)] = Map(
    Items.BEEF -> (Items.COOKED_BEEF, 160),
    Items.POTATO -> (Items.BAKED_POTATO, 140),
    Items.CHICKEN -> (Items.COOKED_CHICKEN, 160),
    Items.COD -> (Items.COOKED_COD, 160)
  )
