package dev.cookingchaos

import java.util.UUID

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

/** Sends the per-tick action bar (hearts, score, combo, timer, order tracker) to players in the round. */
object Hud:
  def update(server: MinecraftServer, uuids: Iterable[UUID]): Unit =
    val bar = actionBarText()
    uuids.foreach { uuid =>
      Option(server.getPlayerList.getPlayer(uuid)).foreach(_.displayClientMessage(bar, true))
    }

  private def actionBarText(): Component =
    val lives = Game.currentLives
    val hearts = ("❤" * lives) + ("🖤" * (Game.MaxLives - lives))
    val combo = if Game.currentCombo > 1 then s"  Combo x${Game.currentCombo}" else ""
    val left = Game.secondsLeft
    val time = f"${left / 60}:${left % 60}%02d"
    val orders = Game.activeOrders
      .map(o => s"${o.dish.name} ${o.fulfilled}/${o.servings} (${Game.secondsUntil(o.deadlineTick)}s)")
      .mkString("  |  ")
    val head = Component.literal(s"$hearts  Score ${Game.currentScore}$combo  Time $time").withStyle(ChatFormatting.GOLD)
    if orders.nonEmpty then head.append(Component.literal(s"   $orders").withStyle(ChatFormatting.AQUA))
    else head.append(Component.literal("   Waiting for customers...").withStyle(ChatFormatting.GRAY))
