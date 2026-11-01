package dev.cookingchaos

import com.mojang.brigadier.context.CommandContext

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/** /cook help | join | leave | start | stop | menu */
object CookCommand:
  def register(): Unit =
    CommandRegistrationCallback.EVENT.register((dispatcher, _, _) =>
      dispatcher.register(
        Commands.literal("cook")
          .`then`(Commands.literal("help").executes(ctx => help(ctx)))
          .`then`(Commands.literal("join").executes(ctx => withPlayer(ctx, Game.join)))
          .`then`(Commands.literal("leave").executes(ctx => withPlayer(ctx, Game.leave)))
          .`then`(
            Commands.literal("start")
              .requires(src => src.hasPermission(2))
              .executes(ctx => withServer(ctx, Game.start))
          )
          .`then`(
            Commands.literal("stop")
              .requires(src => src.hasPermission(2))
              .executes(ctx => withServer(ctx, Game.stop))
          )
          .`then`(
            Commands.literal("kitchen")
              .requires(src => src.hasPermission(2))
              .executes(ctx => withPlayer(ctx, KitchenBuilder.build))
          )
          .`then`(Commands.literal("menu").executes(ctx => menu(ctx)))
      )
    )

  private def help(ctx: CommandContext[CommandSourceStack]): Int =
    ctx.getSource.sendSuccess(
      () =>
        Component
          .literal(
            "Kitchen Chaos: /cook kitchen (op, builds a starter kitchen in front of you), /cook join, " +
              "/cook start (op), /cook stop (op), /cook leave, /cook menu. " +
              "Barrel = pantry, campfire/smoker = stove, bell = service window."
          )
          .withStyle(ChatFormatting.YELLOW),
      false
    )
    1

  private def menu(ctx: CommandContext[CommandSourceStack]): Int =
    ctx.getSource.sendSuccess(
      () => Component.literal("Today's menu:").withStyle(ChatFormatting.GOLD),
      false
    )
    Recipes.dishes.foreach { d =>
      val ingredients = d.ingredients.map(i => ItemStack(i).getHoverName.getString).mkString(", ")
      ctx.getSource.sendSuccess(
        () =>
          Component
            .literal(s"${d.name} (${d.points} pts): serve ${ItemStack(d.output).getHoverName.getString} | needs: $ingredients")
            .withStyle(ChatFormatting.AQUA),
        false
      )
    }
    1

  private def withPlayer(ctx: CommandContext[CommandSourceStack], f: ServerPlayer => String): Int =
    ctx.getSource.getEntity match
      case p: ServerPlayer =>
        val msg = f(p)
        ctx.getSource.sendSuccess(() => Component.literal(msg), false)
        1
      case _ =>
        ctx.getSource.sendFailure(Component.literal("This command needs a player."))
        0

  private def withServer(ctx: CommandContext[CommandSourceStack], f: MinecraftServer => String): Int =
    val msg = f(ctx.getSource.getServer)
    ctx.getSource.sendSuccess(() => Component.literal(msg), false)
    1
