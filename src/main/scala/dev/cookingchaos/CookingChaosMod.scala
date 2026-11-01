package dev.cookingchaos

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

class CookingChaosMod extends ModInitializer:
  override def onInitialize(): Unit =
    CookCommand.register()
    Interactions.register()
    ServerTickEvents.END_SERVER_TICK.register(server => Game.tick(server))
    ServerPlayConnectionEvents.DISCONNECT.register((handler, _) => Game.onDisconnect(handler.getPlayer.getUUID))
