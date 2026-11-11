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
   - 🔪 **Stonecutter** = chopping block (right-click with an ingredient that must be *Chopped*)
   - 🫧 **Water cauldron** = washing station (right-click with an ingredient that must be *Washed*)
   - 🔥 **Campfire or smoker** = stove (right-click with a prepared/raw ingredient to cook it, right-click again to collect; leave it too long and it burns!)
   - 🔔 **Bell** = service window (right-click while holding a requested dish to serve it)

### Mise en place

Some dishes require prep before cooking or serving — the pipeline is the challenge:

- **Chopped** (stonecutter): beef, chicken, rabbit — the stove refuses uncut meat
- **Washed** (water cauldron): salmon, and raw-serve dishes like *Pufferfish Sashimi* and
  *Melon Sorbet* — the bell refuses un-prepped dishes

Prepped items get a green *"Chopped"/"Washed"* lore line so you can see their state. Baking
dishes (bread, cake, stew) never need prep.
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
- **Split kitchens** — `/cook kitchen split [theme]` builds an Overcooked-2 style map: a wall
  divides the kitchen into a **pantry + prep room** (barrels, stonecutter, cauldron) and a
  **hot line + service room** (stoves, crafting tables, bell), connected by two doorways.
  Ingredients and cooks must travel between rooms — routing and teamwork become the challenge.
- **Remove them** — `/cook clear` removes the most recently auto-built kitchen, `/cook clear all`
  removes every kitchen built since the server started. (Hand-built blocks are never touched;
  anything players built *inside* an auto-built kitchen's area is removed along with it.)

### Rules

- Each expired order costs a **heart** (3 hearts per round). Lose them all and the restaurant closes.
- Serving dishes back-to-back builds a **combo multiplier** on the score; serving a wrong dish resets it.
- The pantry is smart: it hands out ingredients that active orders actually need (like Overcooked).
- Final score and best combo are announced at the end of the round.
- **Leaderboard:** every round records each cook's best score, best combo, rounds played and
  total score. `/cook leaderboard` shows the top 10; data persists in
  `<world>/kitchen-chaos/leaderboard.json` across restarts. Players who beat their best score
  get a new-record callout in chat.

### Commands

| Command | Who | Description |
|---|---|---|
| `/cook tutorial` | everyone | Guided 4-step tutorial: pantry → stove → serve |
| `/cook join` | everyone | Join the lobby / rejoin a running round |
| `/cook leave` | everyone | Leave the kitchen |
| `/cook start [theme]` | op (level 2) | Start a round (optionally in a theme) |
| `/cook kitchen [theme]` | op (level 2) | Auto-build a themed kitchen in front of you |
| `/cook kitchen split [theme]` | op (level 2) | Two-room map: pantry/prep | hot line/service, joined by doorways |
| `/cook clear` | op (level 2) | Remove the most recently built kitchen |
| `/cook clear all` | op (level 2) | Remove all kitchens built this server session |
| `/cook themes` | everyone | List all kitchen themes |
| `/cook stop` | op (level 2) | End the round early |
| `/cook menu` | everyone | Show today's menu (dishes, points, ingredients) |
| `/cook leaderboard` | everyone | Top 10 best scores (persisted across rounds and restarts) |
| `/cook help` | everyone | Quick usage help |

### Kitchen themes

Pick a theme with `/cook kitchen <theme>` (builds the matching kitchen) and start a themed round
with `/cook start <theme>`. Each theme has its own block palette, menu, and rules twist — and
most are **locked until your cook level is high enough** (see [Progression](#progression)).

| Theme | Unlock | Kitchen | Twist |
|---|---|---|---|
| `classic` | — | Classic Diner | The baseline rules |
| `halloween` | Lv 2 | The Witch's Kitchen — spruce, carved pumpkins, soul campfires | Witch's Brew, Pumpkin Soup, Cobweb Cake |
| `christmas` | Lv 2 | Christmas Feast — spruce, red concrete, lanterns | Generous timers, big-ticket roasts and cakes |
| `harvest` | Lv 2 | Farmhouse BBQ — birch, hay bales | Pantry hands out double portions |
| `easter` | Lv 2 | The Bunny Burrow — grass, pink concrete, shroomlight | Bunny Stew, Carrot Cake, Dyed Eggs |
| `pirate` | Lv 3 | The Salty Galley — dark oak, prismarine, sea lanterns | Fast orders ("the tide waits for no chef") |
| `sushi` | Lv 3 | Sakura Counter — cherry planks, bamboo | Quick prep; risky Pufferfish Sashimi (300 pts) |
| `candy` | Lv 3 | The Sugar Factory — pink/red concrete, shroomlight | Desserts only, rapid orders, Speed boost |
| `lunar` | Lv 3 | Spring Festival Banquet — red concrete, gold blocks, lanterns | Long family-feast timers, Lucky Tangerines |
| `medieval` | Lv 4 | The Prancing Pony Tavern — stone bricks, deepslate | Feast orders: long timers, multi-serving demands |
| `jungle` | Lv 4 | Treetop Restaurant — jungle planks, moss | Everyone gets Jump Boost |
| `space` | Lv 5 | Canteen Zero-G — quartz, copper, end rods | Everyone gets Slow Falling |
| `nether` | Lv 5 | Hell's Kitchen — blackstone, basalt, soul fire | Faster orders, dishes burn twice as fast |

### Progression

Playing rounds earns **cook XP** (`score/10 + 5 per completed order`) and levels (1–10).
Levels do two things:

- **Unlock themes** — the table above; `/cook themes` shows what's locked and why
- **Raise the difficulty floor** — every level above 1 tightens the game in any theme:
  order timers −3 %, order gap −0.5 s, burn grace −5 % (stacking, capped at level 10)

Your level, XP and best scores are shown on `/cook leaderboard` and persist across restarts.

Add or edit themes in [`Themes.scala`](src/main/scala/dev/cookingchaos/Themes.scala) — a theme is
just a block palette + dish list + timing modifiers.

### The menu (classic)

| Dish | Points | How to make it |
|---|---|---|
| Baked Potato | 60 | Potato → stove |
| Grilled Steak | 100 | Beef → **chop** → stove |
| Roast Chicken | 90 | Chicken → **chop** → stove |
| Grilled Fish | 90 | Cod → stove |
| Fresh Bread | 120 | Wheat → crafting table (vanilla recipe) |
| Forest Stew | 150 | Bowl + 2 mushrooms → crafting table (vanilla mushroom stew) |
| Birthday Cake | 250 | Wheat + sugar + egg + milk bucket → crafting table (vanilla cake) |
| Cottage Rabbit Stew | 180 | Rabbit → **chop** → stove, potato → stove, + carrot + mushroom + bowl → crafting table (vanilla rabbit stew) |

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


## License

MIT
