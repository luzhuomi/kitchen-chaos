package dev.cookingchaos

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

/** Mise en place: preparation a dish requires before cooking/serving. */
enum PrepKind(val id: String, val label: String):
  case Chopped extends PrepKind("chopped", "Chopped")
  case Washed extends PrepKind("washed", "Washed")

/** A dish customers can order. Players must deliver the `output` item. */
final case class Dish(
    name: String,
    output: Item,
    points: Int,
    timeLimitSeconds: Int,
    ingredients: List[Item],
    prep: Option[PrepKind] = None
)

object Recipes:
  /** raw item -> (cooked item, cook time in ticks, required prep) at a stove. */
  val cookable: Map[Item, (Item, Int, Option[PrepKind])] = Map(
    Items.BEEF -> (Items.COOKED_BEEF, 160, Some(PrepKind.Chopped)),
    Items.CHICKEN -> (Items.COOKED_CHICKEN, 160, Some(PrepKind.Chopped)),
    Items.POTATO -> (Items.BAKED_POTATO, 140, None),
    Items.COD -> (Items.COOKED_COD, 160, None),
    Items.SALMON -> (Items.COOKED_SALMON, 160, Some(PrepKind.Washed)),
    Items.PORKCHOP -> (Items.COOKED_PORKCHOP, 160, None),
    Items.MUTTON -> (Items.COOKED_MUTTON, 160, None),
    Items.RABBIT -> (Items.COOKED_RABBIT, 160, Some(PrepKind.Chopped))
  )
