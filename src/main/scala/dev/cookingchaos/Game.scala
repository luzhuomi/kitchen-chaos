package dev.cookingchaos

import java.util.UUID
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Random

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.ItemLore

/**
 * Holds the state of one cooking-challenge round.
 *
 * Gameplay loop (Overcooked style):
 *  - Players join a lobby with /cook join, an admin starts the round with /cook start.
 *  - Orders appear in chat; each order wants N servings of a dish before its timer runs out.
 *  - Right-click a barrel (pantry) to receive an ingredient that is currently needed.
 *  - Right-click a campfire/smoker (stove) with a raw ingredient to cook it; right-click again when ready.
 *  - Right-click a bell (service window) while holding a requested dish to serve it.
 *  - Expired orders cost a heart; running out of hearts or time ends the round.
 */
object Game:
  import Recipes.*

  // Tuning knobs
  val DefaultRoundSeconds = 300
  val MaxLives = 3
  val MaxConcurrentOrders = 4
  val OrderGapSeconds = 12
  val BurnGraceTicks = 240L
  val PantryCooldownTicks = 20L

  enum Phase:
    case Idle, Lobby, Running

  final class Order(val dish: Dish, val servings: Int):
    var fulfilled = 0
    var deadlineTick = 0L

  final class Station:
    var output: Item = null
    var doneAtTick = -1L
    var readyAtTick = -1L
    var state = 0 // 0 empty, 1 cooking, 2 ready

  private var phase = Phase.Idle
  private var theme: Theme = Themes.classic
  private val roundSeconds = 300
  private var tutorial = false
  private val tutorialSteps = mutable.Map[UUID, Int]()
  // difficulty scaling from cook levels, recomputed at round start
  private var diffLevel = 1
  private var effTimeScale = 1.0
  private var effOrderGap = 12
  private var effBurnGrace = 240L
  private var completedOrders = 0
  private val participants = mutable.LinkedHashSet[UUID]()
  private var score = 0
  private var combo = 0
  private var bestCombo = 0
  private var lives = MaxLives
  private var nextOrderTick = 0L
  private var elapsedTicks = 0L
  private val orders = mutable.ListBuffer[Order]()
  private val stations = mutable.Map[Long, Station]()
  private val pantryCooldown = mutable.Map[UUID, Long]()
  private val rng = Random()

  def isRunning: Boolean = phase == Phase.Running
  def isParticipant(player: ServerPlayer): Boolean = participants.contains(player.getUUID)
  def currentTheme: Theme = theme

  def setTheme(id: String): Option[Theme] =
    val found = Themes.byId(id)
    found.foreach(theme = _)
    found

  // --- tutorial mode: one player, one slow order, step-by-step hints ---

  def startTutorial(player: ServerPlayer): String =
    if phase == Phase.Running then return "A round is already running. Use /cook stop first."
    phase = Phase.Running
    tutorial = true
    participants.clear()
    participants += player.getUUID
    score = 0
    combo = 0
    bestCombo = 0
    lives = MaxLives
    nextOrderTick = Long.MaxValue / 4
    elapsedTicks = 0L
    orders.clear()
    stations.clear()
    pantryCooldown.clear()
    // pick a prep-free stove dish so the tutorial stays a simple 4-step loop
    val dish = theme.dishes
      .find(d => d.prep.isEmpty && d.ingredients.exists(i => cookable.get(i).exists(_._3.isEmpty)))
      .orElse(theme.dishes.find(d => d.ingredients.exists(cookable.contains)))
      .getOrElse(theme.dishes.head)
    val order = Order(dish, 1)
    order.deadlineTick = Long.MaxValue / 2
    orders += order
    tutorialSteps(player.getUUID) = 1
    message(player, s"Tutorial: make a ${dish.name}. Follow the hints in your action bar.", ChatFormatting.GOLD)
    "Tutorial started - follow the action-bar hints."

  private def advanceTutorial(player: ServerPlayer): Unit =
    if !tutorial then return
    val uuid = player.getUUID
    val step = tutorialSteps.getOrElse(uuid, 0)
    if step >= 1 && step <= 3 then
      tutorialSteps(uuid) = step + 1
      message(player, Hud.stepText(step + 1), ChatFormatting.AQUA)

  private def finishTutorial(player: ServerPlayer): Unit =
    tutorial = false
    phase = Phase.Idle
    participants.clear()
    orders.clear()
    stations.clear()
    tutorialSteps.clear()
    message(player, "Tutorial complete! Start a real game: /cook join, then /cook start.", ChatFormatting.GREEN)

  def join(player: ServerPlayer): String =
    if participants.contains(player.getUUID) then "You are already in the kitchen."
    else
      participants += player.getUUID
      if phase == Phase.Idle then phase = Phase.Lobby
      s"Joined the kitchen (${participants.size} cook${if participants.size > 1 then "s" else ""}). Start with /cook start."

  def leave(player: ServerPlayer): String =
    if !participants.remove(player.getUUID) then "You are not in the kitchen."
    else
      if participants.isEmpty && phase == Phase.Lobby then phase = Phase.Idle
      "Left the kitchen."

  def onDisconnect(uuid: UUID): Unit =
    participants -= uuid
    pantryCooldown -= uuid

  def start(server: MinecraftServer, themeId: String): String =
    if phase == Phase.Running then return "A round is already running."
    if participants.isEmpty then return "Nobody in the lobby. Use /cook join first."
    if themeId.nonEmpty && setTheme(themeId).isEmpty then
      return s"Unknown theme '$themeId'. Use /cook themes to list them."
    diffLevel = Leaderboard.maxLevelOf(server, participants)
    if theme.unlockLevel > diffLevel then
      return s"${theme.name} is locked until cook level ${theme.unlockLevel} (best level here: $diffLevel). Keep cooking to unlock it!"
    // higher cook levels tighten every timer: -3% order time, -0.5s order gap, -5% burn grace per level
    val d = (diffLevel - 1).toDouble
    effTimeScale = theme.timeScale * math.max(0.7, 1.0 - 0.03 * d)
    effOrderGap = math.max(5, (theme.orderGapSeconds - (0.5 * d)).toInt)
    effBurnGrace = (theme.burnGraceTicks * math.max(0.5, 1.0 - 0.05 * d)).toLong
    completedOrders = 0
    phase = Phase.Running
    score = 0
    combo = 0
    bestCombo = 0
    lives = MaxLives
    nextOrderTick = 0L
    elapsedTicks = 0L
    orders.clear()
    stations.clear()
    pantryCooldown.clear()
    theme.speedEffect.foreach { effect =>
      participants.foreach { uuid =>
        Option(server.getPlayerList.getPlayer(uuid)).foreach(_.addEffect(MobEffectInstance(effect, roundSeconds * 20, 0, false, true)))
      }
    }
    broadcast(
      server,
      Component
        .literal(s"The ${theme.name} kitchen is OPEN! Serve orders at the bell before time runs out. Difficulty level: $diffLevel.")
        .withStyle(ChatFormatting.GOLD)
    )
    s"Round started in ${theme.name} with ${participants.size} cook(s)."

  def stop(server: MinecraftServer): String =
    if phase != Phase.Running then return "No round is running."
    endRound(server, aborted = true)
    "Round stopped."

  def tick(server: MinecraftServer): Unit =
    if phase != Phase.Running then return
    elapsedTicks += 1
    tickStations(server)
    if phase == Phase.Running then
      if !tutorial then
        tickOrders(server)
        if phase == Phase.Running then
          spawnOrders(server)
          if elapsedTicks >= roundSeconds * 20L then endRound(server, aborted = false)
      if phase == Phase.Running then Hud.update(server, participants)

  private def tickStations(server: MinecraftServer): Unit =
    stations.foreach { (_, st) =>
      if st.state == 1 && elapsedTicks >= st.doneAtTick then
        st.state = 2
        st.readyAtTick = elapsedTicks
        broadcast(
          server,
          Component
            .literal(s"${itemName(st.output)} is ready on the stove - grab it before it burns!")
            .withStyle(ChatFormatting.YELLOW)
        )
      else if st.state == 2 && elapsedTicks - st.readyAtTick > effBurnGrace then
        st.state = 0
        st.output = null
        broadcast(server, Component.literal("Something burned on the stove!").withStyle(ChatFormatting.RED))
    }

  private def tickOrders(server: MinecraftServer): Unit =
    val expired = orders.filter(o => elapsedTicks > o.deadlineTick).toList
    if expired.nonEmpty then
      orders --= expired
      lives -= expired.size
      combo = 0
      expired.foreach(o =>
        broadcast(
          server,
          Component.literal(s"A customer left without their ${o.dish.name}!").withStyle(ChatFormatting.RED)
        )
      )
      if lives <= 0 then endRound(server, aborted = false)

  private def spawnOrders(server: MinecraftServer): Unit =
    val maxOrders = math.min(MaxConcurrentOrders, participants.size + 1)
    if elapsedTicks >= nextOrderTick && orders.size < maxOrders then
      val dish = theme.dishes(rng.nextInt(theme.dishes.size))
      val servings = if rng.nextInt(100) < 30 then 2 else 1
      val order = Order(dish, servings)
      order.deadlineTick = elapsedTicks + (dish.timeLimitSeconds * effTimeScale).toLong * 20L
      orders += order
      nextOrderTick = elapsedTicks + effOrderGap.toInt * 20L
      broadcast(
        server,
        Component
          .literal(s"New order: ${dish.name} x$servings within ${dish.timeLimitSeconds}s!")
          .withStyle(ChatFormatting.AQUA)
      )

  /** Right-click on the bell: try to serve the held item to a matching order. */
  def serve(player: ServerPlayer): Unit =
    val hands = List(player.getItemInHand(InteractionHand.MAIN_HAND), player.getItemInHand(InteractionHand.OFF_HAND))
    val matching = orders.find(o =>
      o.fulfilled < o.servings && hands.exists(s => !s.isEmpty && s.getItem == o.dish.output)
    )
    matching match
      case Some(order) =>
        val stack = hands.find(s => !s.isEmpty && s.getItem == order.dish.output).get
        if order.dish.prep.exists(p => !isTagged(stack, p)) then
          message(player, s"The ${order.dish.name} must be ${order.dish.prep.get.label.toLowerCase} first!", ChatFormatting.RED)
          return
        stack.shrink(1)
        order.fulfilled += 1
        if order.fulfilled >= order.servings then
          if tutorial then
            finishTutorial(player)
            return
          orders -= order
          completedOrders += 1
          combo += 1
          bestCombo = math.max(bestCombo, combo)
          val points = order.dish.points * math.max(1, combo)
          score += points
          val comboText = if combo > 1 then s" Combo x$combo!" else ""
          broadcast(
            player.server,
            Component.literal(s"+$points for the ${order.dish.name}!$comboText").withStyle(ChatFormatting.GREEN)
          )
          player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f)
        else
          message(player, s"${order.dish.name}: ${order.fulfilled}/${order.servings} served.", ChatFormatting.GREEN)
      case None =>
        combo = 0
        message(player, "That is not what the customers ordered!", ChatFormatting.RED)
        player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f)

  /** Right-click on a barrel: receive a random ingredient needed by an active order. */
  def pantry(player: ServerPlayer): Unit =
    val uuid = player.getUUID
    if elapsedTicks - pantryCooldown.getOrElse(uuid, -1000L) < PantryCooldownTicks then return
    pantryCooldown(uuid) = elapsedTicks
    val pool =
      if orders.nonEmpty then orders.flatMap(o => o.dish.ingredients).distinct
      else theme.dishes.flatMap(_.ingredients).distinct
    val give = if theme.doublePantry then 2 else 1
    (1 to give).foreach { _ =>
      val stack = ItemStack(pool(rng.nextInt(pool.size)))
      // capture the name first: Inventory.add mutates the stack (count -> 0 on success)
      val name = stack.getHoverName.getString
      if !player.getInventory.add(stack) then player.drop(stack, false)
      message(player, s"Pantry: $name", ChatFormatting.GRAY)
    }
    advanceTutorial(player)

  /** Right-click on a campfire/smoker: start cooking, check progress, or collect the result. */
  def useStove(player: ServerPlayer, pos: BlockPos): Unit =
    val st = stations.getOrElseUpdate(pos.asLong, Station())
    st.state match
      case 0 =>
        val held = player.getItemInHand(InteractionHand.MAIN_HAND)
        cookable.get(held.getItem) match
          case Some((out, ticks, prep)) if !held.isEmpty =>
            if prep.exists(p => !isTagged(held, p)) then
              message(player, s"That must be ${prep.get.label.toLowerCase} first - use the prep station!", ChatFormatting.RED)
              return
            held.shrink(1)
            st.output = out
            st.doneAtTick = elapsedTicks + ticks
            st.state = 1
            message(player, "Cooking...", ChatFormatting.GRAY)
            advanceTutorial(player)
          case Some(_) =>
            message(player, "Hold the raw ingredient in your main hand.", ChatFormatting.GRAY)
          case None =>
            if held.isEmpty then message(player, "The stove is empty.", ChatFormatting.GRAY)
            else message(player, "That cannot be cooked on a stove.", ChatFormatting.GRAY)
      case 1 =>
        val secs = math.max(0, (st.doneAtTick - elapsedTicks + 19) / 20)
        message(player, s"Still cooking... ${secs}s left.", ChatFormatting.GRAY)
      case 2 =>
        val out = st.output
        st.state = 0
        st.output = null
        val stack = ItemStack(out)
        val name = stack.getHoverName.getString
        if !player.getInventory.add(stack) then player.drop(stack, false)
        message(player, s"Grabbed $name.", ChatFormatting.GREEN)
        advanceTutorial(player)

  /** Right-click on a stonecutter: chop ingredients that dishes require chopped. */
  def cut(player: ServerPlayer): Unit = prepStation(player, PrepKind.Chopped)

  /** Right-click on a water cauldron: wash ingredients that dishes require washed. */
  def wash(player: ServerPlayer): Unit = prepStation(player, PrepKind.Washed)

  private def prepStation(player: ServerPlayer, kind: PrepKind): Unit =
    val held = player.getItemInHand(InteractionHand.MAIN_HAND)
    if held.isEmpty then
      message(player, "Hold an ingredient in your main hand.", ChatFormatting.GRAY)
      return
    val relevant =
      cookable.get(held.getItem).exists(_._3.contains(kind)) ||
        theme.dishes.exists(d => d.prep.contains(kind) && d.ingredients.contains(held.getItem))
    if !relevant then message(player, "This ingredient needs no prep here.", ChatFormatting.GRAY)
    else if isTagged(held, kind) then message(player, "Already prepped.", ChatFormatting.GRAY)
    else tagPrepped(player, held, kind)

  private def prepKey(kind: PrepKind): String = "kc_" + kind.id

  private def isTagged(stack: ItemStack, kind: PrepKind): Boolean =
    Option(stack.get(DataComponents.CUSTOM_DATA)).exists(_.getUnsafe.getBoolean(prepKey(kind)))

  private def tagPrepped(player: ServerPlayer, stack: ItemStack, kind: PrepKind): Unit =
    CustomData.update(DataComponents.CUSTOM_DATA, stack, (tag: CompoundTag) => tag.putBoolean(prepKey(kind), true))
    val existing = Option(stack.get(DataComponents.LORE)).map(_.lines().asScala.toList).getOrElse(Nil)
    stack.set(
      DataComponents.LORE,
      ItemLore((existing :+ Component.literal(kind.label).withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN)).asJava)
    )
    message(player, s"${kind.label}: ${stack.getHoverName.getString}", ChatFormatting.AQUA)

  private def endRound(server: MinecraftServer, aborted: Boolean): Unit =
    phase = Phase.Idle
    tutorial = false
    tutorialSteps.clear()
    orders.clear()
    stations.clear()
    val reason =
      if aborted then "Round aborted."
      else if lives <= 0 then "The restaurant ran out of customers!"
      else "Time's up!"
    broadcast(
      server,
      Component
        .literal(s"$reason Final score: $score (best combo x$bestCombo)")
        .withStyle(ChatFormatting.GOLD)
    )
    val xpEarned = score / 10 + completedOrders * 5
    val (records, levelUps) = Leaderboard.record(server, participants.toList, score, bestCombo, xpEarned)
    if records.nonEmpty then
      broadcast(
        server,
        Component.literal(s"New leaderboard record${if records.size > 1 then "s" else ""}: ${records.mkString(", ")}! See /cook leaderboard").withStyle(
          ChatFormatting.LIGHT_PURPLE
        )
      )
    levelUps.foreach { (name, lvl) =>
      val unlocked = Themes.all.filter(_.unlockLevel == lvl).map(_.name)
      val unlockText = if unlocked.nonEmpty then s" Unlocked: ${unlocked.mkString(", ")}!" else ""
      broadcast(
        server,
        Component.literal(s"$name reached cook level $lvl!$unlockText").withStyle(ChatFormatting.LIGHT_PURPLE)
      )
    }
    participants.foreach { uuid =>
      Option(server.getPlayerList.getPlayer(uuid)).foreach { p =>
        p.connection.send(ClientboundSetTitlesAnimationPacket(10, 70, 20))
        p.connection.send(ClientboundSetTitleTextPacket(Component.literal(s"Score: $score").withStyle(ChatFormatting.GOLD)))
        p.connection.send(
          ClientboundSetSubtitleTextPacket(
            Component.literal(if lives <= 0 then "Out of customers!" else "Time's up!")
          )
        )
      }
    }
    participants.clear()

  // --- helpers and read-only views for the HUD ---

  private[cookingchaos] def isTutorial: Boolean = tutorial
  private[cookingchaos] def tutorialStepFor(uuid: UUID): Int = tutorialSteps.getOrElse(uuid, 0)
  private[cookingchaos] def currentScore: Int = score
  private[cookingchaos] def currentCombo: Int = combo
  private[cookingchaos] def currentLives: Int = lives
  private[cookingchaos] def secondsLeft: Long = math.max(0L, roundSeconds - elapsedTicks / 20)
  private[cookingchaos] def activeOrders: List[Order] = orders.toList
  private[cookingchaos] def secondsUntil(deadlineTick: Long): Long =
    math.max(0L, (deadlineTick - elapsedTicks) / 20)

  private[cookingchaos] def broadcast(server: MinecraftServer, msg: Component): Unit =
    server.getPlayerList().broadcastSystemMessage(msg, false)

  private def message(player: ServerPlayer, text: String, color: ChatFormatting): Unit =
    player.displayClientMessage(Component.literal(text).withStyle(color), false)

  private def itemName(item: Item): String = ItemStack(item).getHoverName.getString
