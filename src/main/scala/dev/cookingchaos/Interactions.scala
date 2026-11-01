package dev.cookingchaos

import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.block.Blocks

/**
 * Hooks block right-clicks while a round is running:
 *  - barrel  -> pantry (gives a needed ingredient)
 *  - campfire / smoker -> stove (cook / collect)
 *  - bell -> service window (serve a dish)
 */
object Interactions:
  def register(): Unit =
    UseBlockCallback.EVENT.register((player, world, _, hitResult) =>
      (player, Game.isRunning) match
        case (p: ServerPlayer, true) if Game.isParticipant(p) =>
          val state = world.getBlockState(hitResult.getBlockPos)
          if state.is(Blocks.BARREL) then
            Game.pantry(p)
            InteractionResult.SUCCESS
          else if state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.SMOKER) then
            Game.useStove(p, hitResult.getBlockPos)
            InteractionResult.SUCCESS
          else if state.is(Blocks.BELL) then
            Game.serve(p)
            InteractionResult.SUCCESS
          else InteractionResult.PASS
        case _ => InteractionResult.PASS
    )
