# Kitchen Chaos

An **Overcooked-style multiplayer cooking challenge** for Minecraft servers, written in **Scala 3** as a
**Fabric server-side mod** (vanilla clients can join — no client mod required).

Gather ingredients from the pantry, cook them at the stove, and serve orders to the service bell
before the customers run out of patience.

## How to play

> **New here?** Run `/cook tutorial` — it walks you through the whole loop step by step
> (get an ingredient from the barrel → cook it → collect it → serve at the bell), with live
> hints in your action bar. No timer, no hearts, no pressure.

1. **Get a kitchen** — either run `/cook kitchen` (op) to auto-build a starter kitchen in front of you,
   or build one yourself out of vanilla blocks:
   - 🛢️ **Barrel** = pantry (right-click to receive an ingredient that is currently needed) —
     **you never need to forage**: the pantry hands out everything the orders require, like
     Overcooked's infinite ingredient bins. Auto-built kitchens have instruction signs at the front.
   - 🔥 **Campfire or smoker** = stove (right-click with a raw ingredient to cook it, right-click again to collect; leave it too long and it burns!)
   - 🔔 **Bell** = service window (right-click while holding a requested dish to serve it)
2. Players join: `/cook join`
3. An operator starts the round: `/cook start` (300-second round)
4. Orders appear in chat and in your action bar. Serve them before their timer expires.

### Kitchen setup

The mod does **not** generate anything by itself — it repurposes ordinary vanilla blocks while a
round is running. Two ways to get a kitchen:

- **`/cook kitchen` (op)** — auto-builds a starter kitchen on a 9×7 platform a few blocks in front
  of you: 3 barrels (pantry) along the back wall, 3 campfires (stoves) in the middle, 2 crafting
  tables (prep) on the sides, and a bell (service window) on a fence post at the front.
- **Build your own** — any barrels, campfires/smokers and bells anywhere in the world act as
  pantry/stoves/service windows. Get creative: separate stations across rooms or floors for harder
  maps, like Overcooked's split kitchens.
- **Remove them** — `/cook clear` removes the most recently auto-built kitchen, `/cook clear all`
  removes every kitchen built since the server started. (Hand-built blocks are never touched;
  anything players built *inside* an auto-built kitchen's area is removed along with it.)

### Rules

- Each expired order costs a **heart** (3 hearts per round). Lose them all and the restaurant closes.
- Serving dishes back-to-back builds a **combo multiplier** on the score; serving a wrong dish resets it.
- The pantry is smart: it hands out ingredients that active orders actually need (like Overcooked).
- Final score and best combo are announced at the end of the round.

### Commands

| Command | Who | Description |
|---|---|---|
| `/cook tutorial` | everyone | Guided 4-step tutorial: pantry → stove → serve |
| `/cook join` | everyone | Join the lobby / rejoin a running round |
| `/cook leave` | everyone | Leave the kitchen |
| `/cook start [theme]` | op (level 2) | Start a round (optionally in a theme) |
| `/cook kitchen [theme]` | op (level 2) | Auto-build a themed kitchen in front of you |
| `/cook clear` | op (level 2) | Remove the most recently built kitchen |
| `/cook clear all` | op (level 2) | Remove all kitchens built this server session |
| `/cook themes` | everyone | List all kitchen themes |
| `/cook stop` | op (level 2) | End the round early |
| `/cook menu` | everyone | Show today's menu (dishes, points, ingredients) |
| `/cook help` | everyone | Quick usage help |

### Kitchen themes

Pick a theme with `/cook kitchen <theme>` (builds the matching kitchen) and start a themed round
with `/cook start <theme>`. Each theme has its own block palette, menu, and rules twist.

| Theme | Kitchen | Twist |
|---|---|---|
| `classic` | Classic Diner | The baseline rules |
| `halloween` | The Witch's Kitchen — spruce, carved pumpkins, soul campfires | Witch's Brew, Pumpkin Soup, Cobweb Cake |
| `pirate` | The Salty Galley — dark oak, prismarine, sea lanterns | Fast orders ("the tide waits for no chef") |
| `nether` | Hell's Kitchen — blackstone, basalt, soul fire | Faster orders, dishes burn twice as fast |
| `winter` | North Pole Diner — blue ice, white concrete | Generous timers, big-ticket roasts and cakes |
| `sushi` | Sakura Counter — cherry planks, bamboo | Quick prep; risky Pufferfish Sashimi (300 pts) |
| `medieval` | The Prancing Pony Tavern — stone bricks, deepslate | Feast orders: long timers, multi-serving demands |
| `space` | Canteen Zero-G — quartz, copper, end rods | Everyone gets Slow Falling |
| `candy` | The Sugar Factory — pink/red concrete, shroomlight | Desserts only, rapid orders, Speed boost |
| `harvest` | Farmhouse BBQ — birch, hay bales | Pantry hands out double portions |
| `jungle` | Treetop Restaurant — jungle planks, moss | Everyone gets Jump Boost |

Add or edit themes in [`Themes.scala`](src/main/scala/dev/cookingchaos/Themes.scala) — a theme is
just a block palette + dish list + timing modifiers.

### The menu (classic)

| Dish | Points | How to make it |
|---|---|---|
| Baked Potato | 60 | Potato → stove |
| Grilled Steak | 100 | Beef → stove |
| Roast Chicken | 90 | Chicken → stove |
| Grilled Fish | 90 | Cod → stove |
| Fresh Bread | 120 | Wheat → crafting table (vanilla recipe) |
| Forest Stew | 150 | Bowl + 2 mushrooms → crafting table (vanilla mushroom stew) |
| Birthday Cake | 250 | Wheat + sugar + egg + milk bucket → crafting table (vanilla cake) |

## Building

Requires a **JDK 25** for Gradle (Fabric Loom 1.19 requires it; the mod itself still compiles to
Java 21 bytecode for Minecraft 1.21). Point Gradle at it via `org.gradle.java.home` in
`gradle.properties` or a Java 25 `JAVA_HOME`. The Gradle wrapper downloads everything else
(Fabric Loom, Minecraft 1.21 with official Mojang mappings, Fabric API, Scala 3.3 stdlib).

```bash
./gradlew build
```

The mod jar is produced at `build/libs/kitchen-chaos-1.0.0.jar` (already remapped to intermediary
names, with the Scala stdlib bundled as a jar-in-jar — no Scala plugin needed on the server).

### Run a test server

```bash
./gradlew runServer
```

Accept the EULA in `run/eula.txt`, join with a vanilla 1.21/1.21.1 client, then in the server
console run `op your_username` and in game:

```
/cook kitchen   # build a starter kitchen in front of you
/cook join
/cook start
```

## Installing on a real server

1. Install the [Fabric server](https://fabricmc.net/use/) for Minecraft 1.21.
2. Drop `kitchen-chaos-1.0.0.jar` and
   [Fabric API](https://modrinth.com/mod/fabric-api) (`0.102.0+1.21`) into the `mods/` folder.
3. Players connect with an unmodified (vanilla) client — the mod is server-authoritative.

## Tuning

Gameplay knobs live at the top of [`Game.scala`](src/main/scala/dev/cookingchaos/Game.scala)
(round length, lives, order rate, burn grace, pantry cooldown) and dishes/recipes in
[`Recipes.scala`](src/main/scala/dev/cookingchaos/Recipes.scala).

## Project layout

```
src/main/scala/dev/cookingchaos/
  CookingChaosMod.scala  Fabric entrypoint, event wiring
  Game.scala             Round state machine: orders, stations, scoring, hearts
  Recipes.scala          Dish definitions + raw→cooked stove map
  Interactions.scala     Block right-click hooks (barrel/stove/bell)
  KitchenBuilder.scala   /cook kitchen: auto-builds a starter kitchen
  CookCommand.scala      /cook command tree (Brigadier)
  Hud.scala              Action-bar HUD: hearts, score, combo, timer, order tracker
```

## Hosting (optional)

If you want infrastructure-as-code hosting, [dirien/pulumi-scala-minecraft](https://github.com/dirien/pulumi-scala-minecraft)
shows how to provision a DigitalOcean droplet running a Minecraft server with Pulumi + Scala. It is a
deployment example only — it has nothing to do with modding — but you could adapt it to install the
Fabric loader plus this mod instead of a vanilla server.

## License

MIT
