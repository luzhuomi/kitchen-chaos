package dev.cookingchaos

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

/**
 * Builds a starter kitchen on a 9x7 platform a few blocks in front of the player:
 * barrels (pantry) at the back, campfires (stoves) in the middle,
 * crafting tables (prep) on the sides, and a bell (service window) at the front.
 */
object KitchenBuilder:
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

    var placed = 0
    def set(pos: BlockPos, block: Block): Unit =
      if level.setBlock(pos, block.defaultBlockState, 3) then placed += 1

    // floor platform + clear headroom
    for f <- 0 to 6; l <- -4 to 4 do
      set(at(f, l, 0), Blocks.OAK_PLANKS)
      set(at(f, l, 1), Blocks.AIR)
      set(at(f, l, 2), Blocks.AIR)

    // pantry: barrels along the back wall
    for l <- List(-2, 0, 2) do set(at(6, l, 1), Blocks.BARREL)

    // stoves: campfires in the middle row
    for l <- List(-2, 0, 2) do set(at(4, l, 1), Blocks.CAMPFIRE)

    // prep tables for stew/bread/cake orders
    set(at(5, -3, 1), Blocks.CRAFTING_TABLE)
    set(at(5, 3, 1), Blocks.CRAFTING_TABLE)

    // service window: bell on a fence post at the front
    set(at(1, 0, 1), Blocks.OAK_FENCE)
    set(at(1, 0, 2), Blocks.BELL)

    s"Kitchen built in front of you ($placed blocks). Barrel = pantry, campfire = stove, bell = serve. Start with /cook join then /cook start."
