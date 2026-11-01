package dev.cookingchaos

import java.util.UUID

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

/** Sends the per-tick action bar (hearts, score, combo, timer, order tracker) to players in the round. */
object Hud:
  def update(server: MinecraftServer, uuids: Iterable[UUID]): Unit =
    val bar = if Game.isTutorial then null else actionBarText()
    uuids.foreach { uuid =>
      Option(server.getPlayerList.getPlayer(uuid)).foreach { p =>
        if Game.isTutorial then
          p.displayClientMessage(
            Component.literal("TUTORIAL  " + stepText(math.max(1, Game.tutorialStepFor(uuid))))
              .withStyle(ChatFormatting.AQUA),
            true
          )
        else p.displayClientMessage(bar, true)
      }
    }

  def stepText(step: Int): String =
    step match
      case 1 => "Step 1/4: right-click the BARREL to get an ingredient."
      case 2 => "Step 2/4: hold it in your main hand and right-click the CAMPFIRE to cook it."
      case 3 => "Step 3/4: when it says it's ready, right-click the campfire to collect it."
      case 4 => "Step 4/4: hold the dish and right-click the BELL to serve the order!"
      case _ => "Nice! Type /cook join and ask an op for /cook start to play for real."

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
