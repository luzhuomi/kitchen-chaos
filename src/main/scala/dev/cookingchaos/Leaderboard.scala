package dev.cookingchaos

import java.nio.file.{Files, Path}
import java.util.UUID
import scala.collection.mutable

import com.google.gson.{Gson, JsonObject, JsonParser}

import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource

/**
 * Persistent per-player stats (xp/level, best score, best combo, rounds played, total score),
 * stored as JSON in <world>/kitchen-chaos/leaderboard.json.
 */
final case class PlayerStats(
    var name: String = "unknown",
    var bestScore: Int = 0,
    var bestCombo: Int = 0,
    var rounds: Int = 0,
    var totalScore: Int = 0,
    var xp: Int = 0
):
  def level: Int = Leaderboard.levelFor(xp)

object Leaderboard:
  val MaxLevel = 10
  // cumulative xp needed to reach level index+1 (level 1 is free)
  private val levelThresholds = Array(0, 100, 250, 450, 700, 1000, 1400, 1900, 2500, 3200)

  def levelFor(xp: Int): Int =
    var lvl = 1
    while lvl < MaxLevel && xp >= levelThresholds(lvl) do lvl += 1
    lvl

  def xpForLevel(level: Int): Int = levelThresholds(math.max(0, math.min(MaxLevel - 1, level - 1)))

  private val stats = mutable.LinkedHashMap[UUID, PlayerStats]()
  private val gson = Gson()
  private var initialized = false

  private def file(server: MinecraftServer): Path =
    server.getWorldPath(LevelResource.ROOT).resolve("kitchen-chaos").resolve("leaderboard.json")

  private def ensureLoaded(server: MinecraftServer): Unit =
    if initialized then return
    initialized = true
    val f = file(server)
    if Files.exists(f) then
      try
        val obj = JsonParser.parseString(Files.readString(f)).getAsJsonObject
        obj.entrySet().forEach { e =>
          val o = e.getValue.getAsJsonObject
          stats(UUID.fromString(e.getKey)) = PlayerStats(
            name = o.get("name").getAsString,
            bestScore = o.get("bestScore").getAsInt,
            bestCombo = o.get("bestCombo").getAsInt,
            rounds = o.get("rounds").getAsInt,
            totalScore = o.get("totalScore").getAsInt,
            xp = if o.has("xp") then o.get("xp").getAsInt else 0
          )
        }
      catch case _: Exception => () // corrupt file: start fresh rather than crash the server

  /** The highest cook level among the given players (1 if none have played). */
  def maxLevelOf(server: MinecraftServer, uuids: Iterable[UUID]): Int =
    ensureLoaded(server)
    if uuids.isEmpty then 1
    else uuids.map(u => stats.get(u).map(_.level).getOrElse(1)).max

  def levelOf(server: MinecraftServer, uuid: UUID): Int =
    ensureLoaded(server)
    stats.get(uuid).map(_.level).getOrElse(1)

  /**
   * Records a finished round for every participant.
   * Returns (names that set a new best score, (name, newLevel) for each level-up).
   */
  def record(
      server: MinecraftServer,
      participants: Iterable[UUID],
      score: Int,
      bestCombo: Int,
      xpEarned: Int
  ): (List[String], List[(String, Int)]) =
    ensureLoaded(server)
    val newRecords = mutable.ListBuffer[String]()
    val levelUps = mutable.ListBuffer[(String, Int)]()
    participants.foreach { uuid =>
      val name = Option(server.getPlayerList.getPlayer(uuid)).map(_.getName.getString).getOrElse("unknown")
      val s = stats.getOrElseUpdate(uuid, PlayerStats(name = name))
      if name != "unknown" then s.name = name
      val oldLevel = s.level
      s.rounds += 1
      s.totalScore += score
      s.xp += xpEarned
      if score > s.bestScore then
        s.bestScore = score
        newRecords += s.name
      if bestCombo > s.bestCombo then s.bestCombo = bestCombo
      if s.level > oldLevel then levelUps += ((s.name, s.level))
    }
    save(server)
    (newRecords.toList, levelUps.toList)

  private def save(server: MinecraftServer): Unit =
    try
      val obj = new JsonObject
      stats.foreach { (uuid, s) =>
        val o = new JsonObject
        o.addProperty("name", s.name)
        o.addProperty("bestScore", s.bestScore)
        o.addProperty("bestCombo", s.bestCombo)
        o.addProperty("rounds", s.rounds)
        o.addProperty("totalScore", s.totalScore)
        o.addProperty("xp", s.xp)
        obj.add(uuid.toString, o)
      }
      val f = file(server)
      Files.createDirectories(f.getParent)
      Files.writeString(f, gson.toJson(obj))
    catch case _: Exception => ()

  /** Sends the top 10 by best score to the command source. */
  def show(server: MinecraftServer, src: CommandSourceStack): Int =
    ensureLoaded(server)
    val top = stats.values.toList.sortBy(s => -s.bestScore).take(10)
    if top.isEmpty then
      src.sendSuccess(() => Component.literal("No scores yet. Play a round: /cook join, then /cook start.").withStyle(
        ChatFormatting.YELLOW
      ), false)
    else
      src.sendSuccess(() => Component.literal("Kitchen Chaos leaderboard (best score):").withStyle(ChatFormatting.GOLD), false)
      top.zipWithIndex.foreach { (s, i) =>
        src.sendSuccess(
          () =>
            Component
              .literal(
                s"#${i + 1} ${s.name} [Lv ${s.level}] best ${s.bestScore}, combo x${s.bestCombo}, ${s.rounds} round(s), total ${s.totalScore}, xp ${s.xp}"
              )
              .withStyle(if i == 0 then ChatFormatting.GOLD else ChatFormatting.AQUA),
          false
        )
      }
    1
end Leaderboard
