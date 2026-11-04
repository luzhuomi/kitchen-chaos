package dev.cookingchaos

import scala.collection.mutable

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.SignBlockEntity

/**
 * Builds a starter kitchen on a 9x7 platform a few blocks in front of the player:
 * barrels (pantry) at the back, campfires (stoves) in the middle,
 * crafting tables (prep) on the sides, and a bell (service window) at the front.
 */
object KitchenBuilder:
  // bounding boxes (minX,minY,minZ,maxX,maxY,maxZ) of kitchens built this server session
  private val built = mutable.Stack[(Int, Int, Int, Int, Int, Int)]()

  def build(player: ServerPlayer): String =
    val level = player.level
    val dir = player.getDirection
    val right = dir.getClockWise
    val base = player.blockPosition.offset(dir.getStepX * 4, 0, dir.getStepZ * 4)

    def at(forward: Int, lateral: Int, dy: Int): BlockPos =
      base.offset(
        right.getStepX * lateral + dir.getStepX * forward,
        dy,
        right.getStepZ * lateral + dir.getStepZ * forward
      )

    val theme = Game.currentTheme
    var placed = 0
    def set(pos: BlockPos, block: Block): Unit =
      if level.setBlock(pos, block.defaultBlockState, 3) then placed += 1

    def sign(pos: BlockPos, lines: List[String]): Unit =
      set(pos, Blocks.OAK_SIGN)
      level.getBlockEntity(pos) match
        case s: SignBlockEntity =>
          val front = s.getFrontText
          lines.zipWithIndex.foreach { (line, i) => front.setMessage(i, Component.literal(line)) }
          s.setChanged()
        case _ => ()

    // floor platform + clear headroom
    for f <- 0 to 6; l <- -4 to 4 do
      set(at(f, l, 0), theme.floor)
      set(at(f, l, 1), Blocks.AIR)
      set(at(f, l, 2), Blocks.AIR)

    // themed back wall, then pantry barrels set into it
    for l <- -4 to 4 do set(at(6, l, 1), theme.wall)
    for l <- List(-2, 0, 2) do set(at(6, l, 1), Blocks.BARREL)

    // corner lights (the front pair hangs above the sign posts so signs don't replace them)
    for l <- List(-4, 4) do
      set(at(6, l, 2), theme.light)
      set(at(0, l, 3), theme.light)

    // stoves in the middle row
    for l <- List(-2, 0, 2) do set(at(4, l, 1), theme.stove)

    // prep tables for stew/bread/cake orders
    set(at(5, -3, 1), Blocks.CRAFTING_TABLE)
    set(at(5, 3, 1), Blocks.CRAFTING_TABLE)

    // mise en place: stonecutter (chopping) and water cauldron (washing)
    set(at(3, -3, 1), Blocks.STONECUTTER)
    set(at(3, 3, 1), Blocks.WATER_CAULDRON)

    // service window: bell on a fence post at the front
    set(at(1, 0, 1), Blocks.OAK_FENCE)
    set(at(1, 0, 2), Blocks.BELL)

    // instruction signs at the front corners
    val rules = List("KITCHEN RULES", "Barrel pantry Bell serve", "Campfire stove", "Cutter cut Cauldron wash")
    for l <- List(-4, 4) do
      set(at(0, l, 1), Blocks.OAK_FENCE)
      sign(at(0, l, 2), rules)

    // remember the bounding box so /cook clear can remove it
    val c1 = at(0, -4, 0)
    val c2 = at(6, 4, 2)
    built.push((
      math.min(c1.getX, c2.getX), math.min(c1.getY, c2.getY), math.min(c1.getZ, c2.getZ),
      math.max(c1.getX, c2.getX), math.max(c1.getY, c2.getY), math.max(c1.getZ, c2.getZ)
    ))

    s"Kitchen built in front of you ($placed blocks). Barrel = pantry, campfire = stove, bell = serve. Start with /cook join then /cook start. Remove it with /cook clear."

  /** Removes the most recently built kitchen (or all of them). */
  def clear(player: ServerPlayer, all: Boolean): String =
    if built.isEmpty then return "No kitchens to clear."
    val level = player.level
    val regions =
      if all then
        val r = built.toList
        built.clear()
        r
      else List(built.pop())
    var blocks = 0
    regions.foreach { case (x1, y1, z1, x2, y2, z2) =>
      for x <- x1 to x2; y <- y1 to y2; z <- z1 to z2 do
        val pos = BlockPos(x, y, z)
        if !level.getBlockState(pos).isAir then
          level.setBlock(pos, Blocks.AIR.defaultBlockState, 3)
          blocks += 1
    }
    s"Cleared ${regions.size} kitchen(s) ($blocks blocks). Note: anything built inside the kitchen area was removed too."
