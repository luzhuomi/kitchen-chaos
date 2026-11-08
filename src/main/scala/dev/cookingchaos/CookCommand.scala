package dev.cookingchaos

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.arguments.StringArgumentType
import scala.jdk.CollectionConverters.*

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
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
              .executes(ctx => withServer(ctx, s => Game.start(s, "")))
              .`then`(themeArgument("theme", ctx => withServer(ctx, s => Game.start(s, StringArgumentType.getString(ctx, "theme")))))
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
              .`then`(themeArgument("theme", ctx => withPlayer(ctx, p => setThemeThenBuild(ctx, p, split = false))))
              .`then`(
                Commands.literal("split")
                  .executes(ctx => withPlayer(ctx, KitchenBuilder.buildSplit))
                  .`then`(themeArgument("theme", ctx => withPlayer(ctx, p => setThemeThenBuild(ctx, p, split = true))))
              )
          )
          .`then`(
            Commands.literal("clear")
              .requires(src => src.hasPermission(2))
              .executes(ctx => withPlayer(ctx, p => KitchenBuilder.clear(p, all = false)))
              .`then`(
                Commands.literal("all")
                  .executes(ctx => withPlayer(ctx, p => KitchenBuilder.clear(p, all = true)))
              )
          )
          .`then`(Commands.literal("tutorial").executes(ctx => withPlayer(ctx, Game.startTutorial)))
          .`then`(Commands.literal("themes").executes(ctx => themes(ctx)))
          .`then`(Commands.literal("menu").executes(ctx => menu(ctx)))
      )
    )

  private def themeArgument(name: String, body: CommandContext[CommandSourceStack] => Int) =
    Commands.argument(name, StringArgumentType.word())
      .suggests((_, builder) => SharedSuggestionProvider.suggest(Themes.all.map(_.id).asJava, builder))
      .executes(ctx => body(ctx))

  private def setThemeThenBuild(ctx: CommandContext[CommandSourceStack], p: ServerPlayer, split: Boolean): String =
    val id = StringArgumentType.getString(ctx, "theme")
    Game.setTheme(id) match
      case Some(t) =>
        s"Theme set to ${t.name}. " + (if split then KitchenBuilder.buildSplit(p) else KitchenBuilder.build(p))
      case None => s"Unknown theme '$id'. Use /cook themes to list them."

  private def themes(ctx: CommandContext[CommandSourceStack]): Int =
    ctx.getSource.sendSuccess(() => Component.literal("Kitchen themes:").withStyle(ChatFormatting.GOLD), false)
    Themes.all.foreach { t =>
      ctx.getSource.sendSuccess(
        () => Component.literal(s"/cook kitchen ${t.id} - ${t.name} (${t.dishes.size} dishes)").withStyle(ChatFormatting.AQUA),
        false
      )
    }
    1

  private def help(ctx: CommandContext[CommandSourceStack]): Int =
    ctx.getSource.sendSuccess(
      () =>
        Component
          .literal(
            "Kitchen Chaos: /cook tutorial (learn by playing!), /cook themes, /cook kitchen [theme] (op), " +
              "/cook kitchen split [theme] (op, two-room map), /cook clear [all] (op), /cook join, " +
              "/cook start [theme] (op), /cook stop (op), /cook leave, /cook menu. " +
              "Barrel = pantry, campfire/smoker = stove, bell = service window."
          )
          .withStyle(ChatFormatting.YELLOW),
      false
    )
    1

  private def menu(ctx: CommandContext[CommandSourceStack]): Int =
    ctx.getSource.sendSuccess(
      () => Component.literal(s"Today's menu (${Game.currentTheme.name}):").withStyle(ChatFormatting.GOLD),
      false
    )
    Game.currentTheme.dishes.foreach { d =>
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
