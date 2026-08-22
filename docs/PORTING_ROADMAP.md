# Touhou Little Maid: Forge 1.7.10 port map

This file is the source of truth for the port. The untouched Minecraft 1.20
implementation in `src/main/java` defines expected behaviour. Executable
Minecraft 1.7.10 code lives in `src/legacy/java`.

Status legend:

- `[x]` implemented and accepted by the 1.7.10 compiler/build;
- `[~]` partially implemented or using a temporary compatibility version;
- `[ ]` not ported yet.

## 0. Build and runtime foundation

- [x] ForgeGradle/Forge `1.7.10-10.13.4.1614` build.
- [x] Java 8 source target and reobfuscated production JAR.
- [x] Separate `src/legacy/java` source tree; 1.20 sources remain the reference.
- [x] `@Mod` entry point, lifecycle events and `mcmod.info`.
- [x] common/client proxies and GUI-handler hook.
- [x] SimpleNetworkWrapper channel foundation.
- [x] dedicated-server discovery and pre/post-init smoke test.
- [x] client launch smoke test reaches an integrated world with all renderers loaded;
  repeatable in-world game-test world is pending.
- [x] modern sound categories/rabbit references are converted and nested modern
  pack metadata is filtered from the production JAR.

## 1. EntityMaid core

- [x] 1.7.10 `EntityTameable` implementation and entity registration.
- [x] base size, health, movement speed and vanilla navigation.
- [x] cake taming, owner assignment and FakePlayer rejection.
- [x] owner interaction, Shift-click sitting and feeding/healing.
- [x] synchronized hunger, favorability, XP, schedule, activity and flags.
- [x] 36-slot maid inventory with stacking and overflow handling.
- [x] item and XP-orb pickup.
- [x] NBT persistence using stable modern key names.
- [x] hunger drain, starvation and passive healing.
- [x] damage/invulnerability, ten bauble protections, extra life and mute effects.
- [x] armor, main/off hand and hidden/task inventory separation, persistent NBT
  and dedicated GUI tabs; profession lookup prioritizes task slots.
- [x] favorability levels/cooldowns use modern thresholds and NBT history, with
  health/attack scaling and meal/death/joy/game events.
- [x] death/tombstone and automatic rolling NBT backups work: tombstones protect
  the 36-slot inventory and Maid Film, `/tlmmaid backup list|restore <UUID>`
  restores the latest healthy snapshot, and shrine/altar resurrection works;
  the latest healthy snapshot, with cross-dimension UUID duplicate protection.
- [x] cross-dimension following and collision/liquid-safe teleport fallback.
- [x] riding, vanilla leash, maid-bed sleep, chair and invisible sit mounts work;
  broom uses the native single-passenger 1.7 vehicle contract because this engine
  has no passenger list capable of representing the modern two-seat layout.
- [x] configurable per-owner maid count limit across loaded dimensions.

## 2. Schedule, home area and AI framework

- [x] `DAY`, `NIGHT`, `ALL` schedules.
- [x] `WORK`, `IDLE`, `REST` activity switching.
- [x] separate work/idle/sleep positions and radii.
- [x] NBT-compatible `MaidSchedulePos` storage.
- [x] home-area navigation and target restriction.
- [x] conditional owner-follow, melee and ranged AI tasks.
- [x] extensible string-ID task registry.
- [x] safe teleport after excessive owner-follow navigation distance.
- [x] panic, swimming, drowning protection, wooden-door handling, collision
  climbing and self-opened fence-gate closing work.
- [x] joy seating, begging, home meals and rest/maid-bed behaviours.
- [x] task-specific per-maid NBT data map with stable `MaidTaskData` key.
- [x] public `IMaidTask`/`TaskManager.register` extension API works; KubeJS has
  no 1.7 runtime, so Java registration plus MineTweaker/OreDictionary form the
  native 1.7 extension surface.

## 3. Maid professions

- [x] `idle` including cold-biome/snow-block snowball play with the owner or
  another maid belonging to the same owner.
- [x] `attack` (melee hostile targeting and owner defence).
- [x] `ranged_attack` (bow, arrows, enchantments and durability).
- [x] `farm` (wheat, carrots, potatoes and nether wart).
- [x] `sugar_cane`.
- [x] `melon` (melon and pumpkin).
- [x] `cocoa`.
- [x] `grass`.
- [x] `snow`.
- [x] `crossbow_attack` via a native 1.7 compatibility crossbow and arrows.
- [x] `danmaku_attack` with gohei selection, aimed projectiles, durability,
  colors/types, gravity, owner safety and enchantment-driven fan/effects.
- [x] `trident_attack` via a durable 1.7 compatibility trident projectile.
- [x] `honey`: tends nearby flowers, produces the native honey fallback and
  selects Forestry/Gendustry OreDictionary/registry honey when those mods exist.
- [x] `feed` owner (food and milk; complex modern food effects remain limited by 1.7.10 data).
- [x] `feed_animal`.
- [x] `shears`.
- [x] `milk`.
- [x] `torch`.
- [x] `fishing` (water search, synchronized custom bobber, bite timing, rendered
  line, rod enchantments/durability and Forge `FishingHooks` loot integration,
  including fish/junk/treasure registered by other 1.7 mods; the rod is equipped
  in the visible main hand and remains the authoritative durability source).
- [x] `extinguishing` (durable extinguisher and short-lived cloud agent remove
  entity/block fire, render particles and damage fire-immune monsters; task use
  equips it in the main hand, melee extra damage uses the offhand as in source).
- [x] `board_games`: persistent gomoku with victory/draw checks and opponent AI,
  plus chess and xiangqi using original rules/search engines; owned maid seating,
  win records/favorability and Board State copy/restore work; dedicated renderers
  and TESR state rendering work; the modern multi-block table is represented by
  a rotatable one-block multi-cuboid ISBRH form, with configurable owner access.
- [~] EXTRAS `miner`: registered as the 22nd selectable profession. Its bounded
  loaded-chunk scanner recognizes vanilla and metadata-aware OreDictionary ores,
  respects home bounds and Forge break/harvest cancellation, equips compatible
  pickaxes, preserves Fortune/Silk Touch/drops/NBT and provides a guarded ore-only
  3x3 hammer plane. IC2/GC/GT5/GT6/TConstruct packaged adapters and runtime
  acceptance remain in the EXTRAS section.

## 4. Registries and gameplay content

- [x] maid entity is registered with stable tracking/update settings.
- [x] entity registry includes maid, Power Point, danmaku, hostile fairy,
  fishing hook, extinguishing agent, chair, sit mount, broom and cake box;
  tombstone; all modern entity types now have a registered 1.7 counterpart,
  while final Bedrock rendering/advanced behaviour is tracked separately below.
- [x] gameplay item registry includes combat/utility items, Power/gohei,
  vehicles, Film/photo/Smart Slab, 10 baubles, seven backpacks, Chisel,
  Kappa Compass, Wireless IO, fox scrolls, spawn eggs, compatibility weapons,
  Board State and maintenance tools; internal advancement-only icons are omitted.
- [x] all mod items and blocks are grouped in a dedicated 1.7 creative tab.
- [x] all 16 gameplay block IDs are registered with item forms and translations;
  all 15 modern tile-entity types have persistent 1.7 counterparts, plus a tiny
  loaded-world TileEntity index replacing the modern scarecrow POI. Scarecrow,
  furniture/sit blocks, shrine, picnic mat, snack cabinet, maid bed, statue,
  garage kit, beacon, model switcher, three boards and altar are functional.
- [x] configurable-ID enchantments, attributes, sounds and the 32×48 Wine Fox
  hanging painting entity/item/renderer work.
- [x] altar has six persistent offering slots, absorbs Power, covers all 43
  modern altar definitions (item/block outputs, boxed maid, lightning and Film
  resurrection); the one-block 1.7 geometry replaces the template shell.
- [x] core shaped/shapeless and altar recipes use 1.7 GameRegistry ingredients;
  unavailable bamboo/barrel/shield inputs map to reeds/chest/iron equivalents.
- [x] fairy spawning, scarecrow exclusion, Power Point death reward and chest
  loot injection work. The modern source contains no world generator; its NBT
  templates belong to altar formation/game-test and are handled by 1.7 adapters.
- [x] core taming, board-win, devotion and resurrection advancements are
  converted to a native 1.7.10 achievement page.
- [x] core spawn/maid/gameplay/AI/TTS settings use Forge 1.7 `Configuration`.

## 5. Inventory, GUI and networking

- [x] SimpleNetworkWrapper channel exists.
- [x] stable packet discriminator table covers task/config/GUI C2S and chat/TTS
  S2C; all handlers validate bounds/ownership and rendezvous with the proper
  client or server tick thread before touching game state.
- [x] maid inventory container and initial screen.
- [~] task/schedule/config controls and main/bauble/equipment/task tabs work;
  the 256x256 source layout, non-uniform backpack rows, hand/armor slots and
  source-textured Baubles open/close control are ported; remaining advanced
  configuration pages are pending.
- [x] dynamic-capacity backpack, 30-slot bauble, equipment and hidden/task
  inventory containers with server-validated tab switching.
- [~] beacon and model switcher have server-authoritative interaction/status;
  board games are playable on-block; Wireless IO binds accessible inventories
  and performs unloaded-chunk-safe bidirectional transfer with a nine-entry
  item/NBT filter captured by sneak-use. Both dedicated editors are ported;
  their remaining multiplayer and effect acceptance passes are tracked below.
- [x] every implemented C2S packet validates bounds, ownership and distance;
  all S2C handlers rendezvous with the client thread before changing client state.
- [x] model, sound-pack and task synchronization via DataWatcher; server-validated
  request/response packets synchronize configured work/idle/sleep home points.
- [x] AI chat/TTS packets, bounded history, bubbles and bounded WAV playback.

## 6. Client rendering and resources

- [x] default maid and fairy use Bedrock geometry instead of `ModelBiped`.
- [x] Bedrock 1.10/1.12 geometry parser adapted to 1.7 `ModelRenderer`:
  hierarchy, source-equivalent Z-Y-X pivot/rotation signs, cube pivot/rotation,
  UV, mirror and inflate work. Reimu was verified both in the GUI preview and
  standing in the integrated world after correcting inverted X/Y bone angles.
- [x] both classic `[u,v]` and newer per-face Bedrock UV object schemas are
  rendered; the dedicated 1.7 quad renderer now matches the source vertex order,
  face-axis swaps, negative UV sizes and 0/90/180/270 `uv_rotation`, preventing
  furniture/bed/model faces from being mirrored or silently discarded.
- [~] dependency-free animation controller maps common Bedrock/Gecko bone names
  for walk, look, sit, sleep, blink, beg, swing, float, wing, hair, skirt and
  tail states using the source amplitudes/phases; arbitrary modern
  JavaScript/Molang expressions are intentionally not evaluated.
- [x] built-in and resource-pack maid models are discovered from `maid_model.json`;
  model/texture selection, extra-texture variant IDs, entity/item scale metadata,
  `show_backpack`/`show_custom_head`, reload invalidation and geometry cache work.
- [~] maid layers render main/offhand, all four armor slots and backpack;
  baubles remain logical effects as in the source. Authored arm and backpack
  positioning bones are honored; models without them use the source fallback.
  Generic armor item icons are deliberately not rendered as detached cuboids.
- [x] other renderers include billboards/fishing line/no-op helpers, Bedrock
  fairy/broom/box/tombstone and dynamic bundled chair geometry with fallback;
  boards/altar use state TESRs.
- [x] source-default vanilla visuals are restored behind legacy config switches:
  slime/magma cube use the bundled Reimu/Marisa Yukkuri Bedrock models and XP
  orbs use the original `point_item` atlas without changing vanilla entity logic.
- [x] bundled Peco sound namespace, positional maid voices and Mute bauble support.
- [x] timed AI text/error chat bubbles, activity particles, server-synchronized
  coloured home-area wireframes and F3 maid/task diagnostics render client-side.
- [x] modern non-empty locale JSON files are converted to UTF-8 1.7 `.lang`;
  full English/Russian sets are merged with legacy-only item/achievement keys.
- [x] modern blockstate/model forms are replaced by a multi-cuboid
  `ISimpleBlockRenderingHandler` for inventory/fallback geometry; altar, game
  boards, maid bed, keyboard, bookshelf, computer, shrine, picnic mat and snack
  cabinet use their original Bedrock geometry/textures through reload-safe TESRs.
- [x] statue and Garage Kit now have registered TESRs: both render the original
  `statue_base` Bedrock geometry and a static maid preview reconstructed from
  synchronized photo NBT instead of the previous placeholder cuboids.
- [x] mutable tile state uses `S35PacketUpdateTileEntity`; altar inventory/power,
  board pieces, bed colour, beacon, statue, garage and model-switcher changes
  invalidate the block and reach the client renderer immediately.
- [x] maid spawn regression fixed: `entityInit()` now registers non-null constant
  model/sound defaults because the 1.7 base `Entity` constructor invokes it before
  subclass fields are initialized.
- [x] Model Switcher geometry regression fixed: the dynamic model assignment now
  overrides the actual 1.7 `RenderLiving.doRender(EntityLiving, ...)` dispatch
  point. Previously the `EntityLivingBase` overload was bypassed, so only the
  texture changed while every maid retained Reimu geometry.

## 7. Data-driven systems

- [x] core modern tags are replaced by OreDictionary/configurable registries.
- [x] datapack reload listeners are replaced by Forge config, model/sound,
  prompt and kaomoji resource loaders.
- [x] original chess/xiangqi engines are reused; board NBT and Board State
  copy/restore items replace the modern JSON-state workflow.
- [x] reloadable UTF-8 kaomoji loader with begging bubble integration.
- [x] classpath/resource-pack LLM system prompt resource and safe action schema.
- [x] Power Point global chest loot is replaced by Forge `ChestGenHooks`.
- [x] recipes use GameRegistry/altar registries; modern structure NBT is limited
  to the adapted altar shell and game-test fixture, not world generation.

## 8. AI chat, LLM and TTS

- [x] server-only endpoint/model/API-key configuration.
- [x] bounded asynchronous Java 8 `HttpURLConnection` client.
- [x] bounded per-maid conversation memory persisted in NBT.
- [x] allow-listed `[sit]`, `[follow]` and `[task:*]` maid-action bridge.
- [x] bounded WAV TTS download, client cache, decoder registration and positional playback.
- [x] owner/distance checks, rate limits, timeouts, size limits and voice fallback.
- [x] HTTP/TTS workers operate on immutable maid-state snapshots and marshal
  results back to the server tick thread, rejecting stale player/entity targets.

## 9. Required platform adaptations

- [x] Curios→maid bauble inventory, modern backpacks/guns/models→native legacy equivalents.
- [x] the 1.20 source contains no Thaumcraft or GregTech-specific behavior to
  port; on 1.7.10 both are supported through dependency-free OreDictionary and
  `IInventory` item transport.
- [x] OreDictionary names expose core items to legacy recipes and automation.
- [x] modern-only integrations degrade to native inventories/tasks without crashes.

## EXTRAS. Optional compatibility with other mods

`EXTRAS` is the dedicated backlog for third-party integrations. These entries
do not block the base 1.7.10 port unless an enabled integration crashes or
corrupts the game. Every entry must remain optional and avoid hard class links.

- [x] InfernalMobs / Compact InfernalMobs: external disarm mutations use the
  vanilla equipment bridge and no longer leave a stale maid hand stack; the
  implementation and remaining packaged stress test are documented below.
- [x] Forestry/Gendustry: honey production has a dependency-free optional output bridge.
- [x] Optional integrations are detected through `Loader` without hard class references.
- [~] NEI displays standard GameRegistry recipes automatically; a custom altar
  recipe handler remains an optional enhancement.
- [~] MineTweaker/CraftTweaker can address exposed OreDictionary entries; custom
  scripting actions for altar recipes remain an optional enhancement.

### EXTRAS compatibility contract

- [ ] Add a version-aware adapter registry keyed by mod ID. Loading the maid mod
  without any supported mod installed must never resolve its API classes.
- [ ] Run item actions on the logical server with the Maid's position, look vector
  and owner attribution. Respect Forge cancellation/protection events and never
  bypass claims merely by using a fake-player execution context.
- [ ] Keep the authoritative equipped `ItemStack` through every callback and copy
  back all metadata, durability, charge, modifiers, tool XP and custom NBT. No
  action may duplicate ammunition, drops, energy or replacement/container items.
- [ ] Recognize items by optional API/class/registry identity and capability, not
  localized display names. Each adapter gets per-feature allow/deny configuration.
- [ ] Acceptance matrix for every adapter: mod absent, supported versions present,
  empty charge/ammunition, save/reload, death/resurrection, disarm, task switch,
  combat end, chunk unload and dedicated multiplayer server.

### EXTRAS profession: `miner`

The modern source has no miner among its 21 base professions. This is a new
optional 1.7.10 profession whose ore discovery must be mod-aware from the start.

- [~] Added a central ore classifier for the exact block plus metadata. Resolution
  order: explicit config deny list, explicit config allow list, installed-mod
  adapter, OreDictionary name, then the vanilla ore table; installed-mod adapter
  dispatch remains to be added.
- [x] Treat registered OreDictionary ore variants as targets, including normal,
  small, poor, dense, Nether, End and host-rock variants where exposed by the
  installed mod. Prefix/name rules must be configurable rather than accepting
  every block whose display name merely contains "ore".
- [ ] Add tested classifiers for IC2/IC2 Experimental, Galacticraft (`GC`), GT5
  and GT6 meta-ores. GT5 and GT6 must decode their own block metadata/material
  identity through separate optional adapters rather than sharing assumptions.
- [x] Other mods work automatically when they register block+metadata stacks in
  OreDictionary; packs can extend or correct detection through registry-ID plus
  metadata allow/deny configuration without recompiling the mod.
- [x] Never classify machines, cables, storage blocks, decorative blocks or an
  arbitrary TileEntity as ore. A TileEntity block is denied by default unless a
  loaded adapter or explicit configuration marks that exact state harvestable.
- [~] Ore searches now stay inside loaded chunks, Maid home/work radius and a
  configurable bounded per-tick scan budget with a persistent rotating cursor;
  classification caching and adapter-generation invalidation remain.
- [~] Before selecting or breaking a target, require the active tool's native
  harvest class/level and energy/durability check. Fire Forge break/harvest/drop
  events with Maid/owner attribution and preserve Silk Touch, Fortune, native
  mod drops, tool NBT and the normal `onBlockDestroyed` callback. Baseline Forge
  tools work; explicit electric/replacement-item adapters remain.
- [~] Hammer 3x3 selection uses the struck face and the tool adapter's native
  area rules. Every secondary block is independently checked for permissions,
  loaded state, ore classification and harvestability. The safe generic plane is
  implemented; native TConstruct/GT orientation and energy rules remain.
- [ ] Acceptance worlds must cover vanilla, IC2, Galacticraft, GT5, GT6 and at
  least one OreDictionary-only third-party ore across Overworld/Nether/End or
  mod dimensions, including mixed metadata blocks and protected claims.

### Tinkers' Construct (`TConstruct`)

- [ ] Allow appropriate TConstruct tools/weapons to select existing professions
  and remain the active visible item during combat and normal work.
- [ ] Rapier melee must execute the mod's native hit/modifier callbacks so armor
  penetration and other effects work, while tool XP, modifiers, durability and
  NBT advance exactly as for a player-attributed hit.
- [~] Added the `miner` work mode, generic pickaxe recognition and protected
  OreDictionary mining. Hammer work must
  use the native 3x3 orientation/harvest rules, permissions, drops and tool XP;
  the current source has no miner among its 21 tasks, so `miner` is an EXTRAS
  profession extension rather than a missing base-port profession.
- [ ] TConstruct bow, longbow and crossbow must qualify for ranged professions,
  find and consume their correct arrows/bolts, and inherit native draw time,
  aiming, projectile, damage, modifiers and weapon/ammunition NBT updates.

### IndustrialCraft 2 / IC2 Experimental and GraviSuite (`IC2`, `GraviSuite`)

- [ ] Permit supported electric tools as profession/active items and debit energy
  through the installed IC2 electric-item API; a discharged tool must stop acting
  rather than falling back to free vanilla durability behavior.
- [ ] Apply supported IC2 and GraviSuite armor effects, energy use and damage
  mitigation to Maid without assuming the wearer is an `EntityPlayer`.
- [ ] Nano Saber automatically activates immediately before a valid melee attack,
  applies its native powered damage/effects, and deactivates when combat ends,
  the target is lost, Maid is sitting/low-health, the task changes, the chunk
  unloads, or charge is exhausted. Its actual stack NBT must retain the state.

### GregTech 5 / GregTech 6 (`gregtech`, version-selected adapters)

- [ ] Implement separate GT5 and GT6 MetaTool adapters; do not assume their
  similarly named classes, metadata layouts or electric APIs are compatible.
- [~] Dependency-free GT MetaTool introspection plus standard
  `craftingToolPickaxe/MiningDrill/JackHammer/HardHammer` OreDictionary support
  recognizes 9 of 10 mining-tool stacks exposed by the installed GT6 Unofficial
  6.15.07 runtime. GT5 and the remaining GT6 stack still require packaged tests.
- [~] Recognize MetaTool pickaxes, hammers, swords and other relevant tools for
  profession assignment and active use. Preserve native material stats, attack,
  harvest/AoE behavior, electric charge, durability and all MetaTool NBT. Miner
  pickaxe/drill/hammer assignment and native block-destroy/harvest callbacks are
  active; combat tools and explicit energy verification remain.
- [ ] Apply multi-block mining only when the installed MetaTool itself authorizes
  it and the active work mode permits it; retain Forge harvest/drop events and
  protection checks for every affected block.

### Draconic Evolution (`DraconicEvolution`)

- [ ] Permit Draconic weapons/tools as active profession items and dispatch their
  native attack/use/energy behavior with the real Maid-held stack.
- [ ] Inherit supported Draconic armor protection, shield and energy effects.
  A compatible capacitor anywhere in Maid's accessible inventory must recharge
  equipped armor and active weapons using native transfer limits and priorities.
- [ ] Explicitly define and test player-only abilities such as flight/area mining;
  unsupported abilities remain disabled instead of being approximated unsafely.

### Avaritia (`Avaritia`)

- [ ] Permit Infinity armor and inherit supported protection/effects without
  granting abilities that require an actual player implementation.
- [ ] Hard-block World Breaker mining mode and Avaritia axe activation for Maid,
  including indirect task selection and right-click use; keep items intact and
  report the rejection instead of deleting or damaging them.
- [ ] Permit the Avaritia bow for the ranged profession and use its native draw,
  ammunition/projectile, damage and NBT logic without synthesizing free shots.

### Ender IO (`EnderIO`)

- [ ] Permit The Ender and other explicitly supported weapons as active melee
  items, preserving native hit effects, energy, durability, upgrades and NBT.
- [ ] Inherit supported Dark Steel armor upgrades/effects and their energy or
  durability costs; player-only movement abilities require an explicit safe hook.

### Extra Utilities (`ExtraUtilities`)

- [ ] Blacklist Angel Ring from Maid equipment, bauble activation and passive
  inventory use to prevent flight/state bugs. Reject it without consuming it.
- [ ] Permit Healing Axe melee/use and inherit its native healing, hunger/effect,
  cooldown and durability behavior using the authoritative held stack.

### EXTRAS implementation order

1. [~] Common mining item-action/NBT bridge is implemented; armor-tick, ranged
   weapon and explicit energy bridges remain.
2. [ ] TConstruct melee/ranged support, then the separate `miner` extension.
3. [ ] IC2/IC2 Experimental/GraviSuite and GT5/GT6 energy/MetaTool adapters.
4. [ ] Draconic Evolution and Avaritia high-impact item/armor safety adapters.
5. [ ] Ender IO and Extra Utilities adapters.
6. [ ] Packaged-mod dedicated-server regression matrix for every supported version.

## 10. Verification and release

- [x] `gradlew build` produces a reobfuscated JAR.
- [x] dedicated server reaches mod loading without registry crashes.
- [ ] accept EULA manually and run a persistent dedicated test world.
- [x] integrated client/server completed the Forge mod handshake and joined a world.
- [ ] save/reload and chunk unload/reload tests for every entity state.
- [ ] one scenario test per profession.
- [~] packet handlers, tombstone/backups, Smart Slab, Model Switcher and broom
  enforce owner UUID (plus distance where applicable); a two-client adversarial
  runtime pass remains.
- [~] `/tlmmaid profile|profile reset` reports loaded maid count and server-side
  maid-tick sample/average/max microseconds; a representative multi-maid world
  still needs to be run on the target server hardware.
- [x] structural migration fixture covers modern entity/owner UUID arrays,
  ItemStackHandler compounds, string registry IDs, namespaced backpack IDs and
  deprecated backpack levels.
- [x] `/tlmmaid verify` exercises the real world-bound maid constructor, all 21
  source professions plus the EXTRAS `miner` task, and a separate NBT
  write/read/re-tick round-trip without leaving a test entity in the world.
- [~] production JAR contents and credits are audited in `RELEASE_AUDIT.md`;
  redistribution waits for the project owner to supply/confirm missing licenses.

## Full-port gate (audit updated 2026-08-13)

Current verdict: **feature-complete at the static/core level, not yet a fully
accepted port**. `EXTRAS` enhancements are excluded from this verdict.

- [x] Registry parity: every modern gameplay entity has a 1.7 counterpart;
  all 16 gameplay blocks and all 15 modern TileEntity roles are represented.
- [x] Item parity was reconciled by function rather than raw registry count:
  modern block-item duplicates and advancement-only icons are intentionally not
  copied as standalone 1.7 items; achievements replace the icons, while native
  crossbow, trident, honey, cake-box and Wine Fox painting items preserve
  mechanics unavailable in vanilla 1.7.10.
- [x] Profession parity: all 21 source tasks plus EXTRAS `miner` are registered
  and pass constructor, switch, tick and NBT round-trip verification.
- [x] Resource integrity: 234 selectable model entries, 231 referenced geometry
  files, translations, sounds and required board-piece bones pass startup checks.
- [x] Build/runtime smoke gate: Java 8 compilation, reobfuscated JAR, Forge
  initialization self-test, integrated handshake and world join pass.
- [ ] Persistent-world gate: dedicated-world EULA run plus save/reload and chunk
  unload/reload for every entity and stateful block.
- [ ] Behaviour/UI gate: one real-world scenario for each of the 21 professions,
  the remaining advanced maid configuration pages, and the five Shrine Lamp
  effects/transfer/autocollect acceptance pass.
- [ ] Rendering gate: capture the held-item/state matrix across representative
  custom maid models and all seven backpack types; prove bundled animation
  expressions fit the legacy controller or port the remaining required subset.
- [ ] Multiplayer/security gate: two-client ownership, distance and malformed
  packet tests, including Model Switcher/redstone and third-party disarm stress.
- [ ] Release gate: profile a representative multi-maid world and resolve the
  missing redistribution licences/notices listed in `RELEASE_AUDIT.md`.

## Current execution order

1. [x] Forge foundation and build/runtime smoke tests.
2. [x] EntityMaid core, inventory, persistence, basic GUI and packets.
3. [x] Vanilla-compatible professions and entity registry coverage.
4. [x] Bedrock parser and built-in entity rendering pipeline.
5. [x] All block IDs and all 15 tile-entity persistence/function foundations.
6. [x] Advanced gameplay: maid board games, tombstone/backups/resurrection,
   equipment, backpacks, baubles, altar recipes and data loaders.
7. [x] OpenAI-compatible Java 8 LLM/TTS, command, memory, cooldowns,
   safe maid-action bridge, chat bubbles and bounded positional audio.
8. [x] NBT migration, packet-thread safety and lifecycle self-tests.
9. [x] Dependency-free compatibility surface, legacy block/TESR rendering,
   home/config networking, Wireless IO filtering and client diagnostics.
10. [~] Remaining base-port gate: exhaustive persistent-world, profession,
    rendering and multiplayer tests plus redistribution licence confirmation
    listed in section 10 and `Full-port gate`; `EXTRAS` is tracked separately.

## Active verification pass (updated 2026-08-13)

- [x] Main maid container visually checked in the Java 8 integrated client:
  equipment, backpack and player slots align with the source textures.
- [x] Removed overlapping vanilla Baubles buttons/text and restored the original
  54x63 two-state texture control.
- [x] Corrected legacy armor inventory mapping (helmet/chest/legs/boots) in both
  the main and equipment containers.
- [x] Corrected Bedrock bone and rotated-cube X/Y signs against the original
  `AbstractBedrockEntityModel`; standing Reimu geometry no longer separates.
- [x] Audited the state matrix against `MaidBaseAnimation`: corrected limb
  phases/amplitudes, sitting arms/legs/skirt, blink/sleep visibility, attack,
  hair/wing/tail motion and sleeping backpack suppression.
- [x] Removed the incorrect generic armor-as-item layer and added authored
  hand/backpack positioning-bone support plus source fallback transforms.
- [ ] Complete visual runtime captures for every matrix combination with held
  items and all seven backpacks across representative custom models.
- [ ] Run save/reload and one real-world scenario for each of the 21 professions.
- [x] First profession audit fixed concrete scenario defects: double-plant grass
  harvesting no longer leaves/duplicates halves; honey respects home bounds and
  negative coordinates; idle snowball probability is 1/32 rather than 31/32;
  extinguishing now respects sitting/home state and actively locates block fire.
- [x] Source animation audit found no dedicated extinguisher spray/arm animation:
  source uses the normal main-hand swing and only toggles authored
  `extinguishingHidden`/`extinguishingShow` bones. Both behaviours are now ported.
- [x] Restored source-style automatic visible tool handling for extinguisher,
  fishing rod and shears; selecting idle returns the main-hand work tool to the
  maid inventory. Fishing and shearing durability now follows the equipped item.
- [x] Fishing visual/lifecycle regression fixed from runtime capture: the maid
  now sits facing the water, holds the source `hold_mainhand:fishing` pose for
  the complete hook lifetime, repeatedly casts after catches, uses the correct
  vanilla bobber-atlas UV and source-aligned hand-to-bobber line anchor.
- [x] Restored source task-equipment contracts for melee, bow, danmaku,
  crossbow and trident modes. Attacks now require the visible main-hand weapon;
  task switches pull it from the task/backpack inventory without deleting the
  previous hand item.
- [x] Combat parity pass restores weapon-aware melee targeting, visibility/home/
  team/tamed filters, bow damage scaling and range accuracy, source-sized
  danmaku fans (1/3/8/32), friendly-fire rejection and repeated ranged swings.
- [x] Household parity pass restores Silk Touch melon harvesting, shovel-aware
  snow drops, harmful-effect-only milk use, milk output-space checks, population
  culling in animal feeding and the missing 21st `board_games` profession with
  autonomous board discovery, navigation and seating.
- [x] Replaced integer-rounded vanilla `ModelBox` conversion with float-sized
  Bedrock cubes and source-compatible unfolded UV order. Fractional/flat ribbons,
  sleeves, hair, wings and rotated decorative parts retain authored dimensions;
  the same fix applies to maid/fairy/entity and TESR Bedrock models. Rendering
  now also matches source `entityCutoutNoCull`, so zero-depth decorations remain
  visible from both sides.
- [x] Added Gecko-style `Head`/`LeftArm`/`RightArm`/leg aliases to base, sitting,
  swing and fishing state handling, plus `LeftHandLocator`/`RightHandLocator`
  attachment support so modern custom models no longer freeze or place held
  items at the shoulder.
- [x] Audited all 234 manifest entries and 231 referenced geometry files: no
  missing model/texture references or orphaned parents were found. Exact float
  cubes cover 13,801 rotated and 17,435 fractional-size cubes; all 22,171
  per-face definitions now use source-compatible UV construction.
- [x] Duplicate bone names are preserved as distinct hierarchy nodes rather than
  overwritten (`kurokoma_saki` hair and `kisume` roots); parent resolution follows
  the most recent preceding definition while animation lookup remains deterministic.
- [x] Restored the model-controlled head-block layer and suppression switches for
  the two bundled models which explicitly forbid backpack/head accessories.
- [x] Power Point right-click now launches a distinct gravity projectile; impact
  emits the potion effect and splits 30-88 Power into source denominations rather
  than spawning one immediately collectible 100-value point. Sprite thresholds
  now match `EntityPowerPoint.getPowerValue` from the 1.20 source.
- [x] Bauble capacity again follows source favorability gates (10/20/30 slots):
  locked rows are absent server-side, excluded from shift-click and rendered with
  the original dark overlay/lock icon instead of exposing all slots immediately.
- [x] Replaced temporary previous/next profession buttons with the original-style
  12-row paged task panel and a bounded, owner/distance-validated direct-select packet.
- [~] Model Switcher item binding and item-to-tile `StorageData` round-trip are restored;
  breaking/replacing retains bound maid UUID, owner, index and model/name/yaw list.
  The six-row editor supports model selection, add/delete/apply, name editing,
  cardinal rotation and capture; server packets validate block, owner, distance,
  indices and payload bounds. Left/right-only redstone connections cycle in
  opposite directions on the rising edge. Both native 1.7 flat NBT and the 1.20
  `ForgeData` + `int[4]` UUID + direction format migrate safely, with malformed
  lists capped at 128 entries. The block now uses the source full-cube geometry
  and its three face textures. Clean reobfuscated build and static migration/edit
  checks pass. Integrated-world testing confirms item binding, GUI opening,
  adding 24 entries, selecting multiple indices, client/server synchronization,
  clean save/exit and owner persistence after reload. Explicit left/right
  redstone, break/place NBT and two-client adversarial passes remain.
- [ ] Run the two-client ownership/distance/packet adversarial pass.
- [x] Sequential TileEntity audit: Altar now clears client ghost stacks on empty
  S35 updates, clamps malformed Power NBT and never deletes points at capacity.
- [x] Sequential TileEntity audit: Statue restores the complete source
  1x1x1/1x2x1/2x4x2/3x6x3 clay-volume selection, core/non-core storage,
  bounded NBT migration and whole-structure clay restoration on break. Garage
  Kit placement orientation and modern string-facing migration are corrected.
- [x] Sequential TileEntity audit: Maid Beacon uses the source-configurable
  range/storage/cost (including the original `/900` tick scaling), normalizes
  invalid NBT and preserves its state in the dropped/replaced ItemBlock.
  Model Switcher storage, binding, S35 sync and model/name/yaw application were
  rechecked after the dynamic RenderMaid dispatch repair.
- [x] Sequential TileEntity audit: Gomoku/CChess/WChess now migrate and sanitize
  board state safely, update draw/checkmate status after both player and maid
  moves, preserve chess irreversible-move counters and migrate modern Joy
  `SitId`. The legacy TESR now indexes both chess engines through their mailbox
  coordinates, so all pieces render on their actual squares.
- [x] Sequential TileEntity audit: Keyboard/Bookshelf/Computer share safe Joy
  UUID migration and now use the source seat heights, yaw offsets, favorability
  type names and collision heights for both players and autonomous maids.
  Shrine accepts the modern `ForgeData/StorageItem` ItemStackHandler format in
  addition to native 1.7 inventory NBT.
- [x] Sequential TileEntity audit: Picnic Mat restores `CenterPos`, four
  persistent seats, source seat transforms, `OnHomeMeal`, nested ItemStackHandler
  migration and seat cleanup while retaining the practical one-block 1.7 GUI
  adaptation. Maid Bed now converts reversed 1.7 dye damage to stable modern
  DyeColor IDs, restricts dyes to the seven source colours, preserves the colour
  in item NBT and removes its sleeping seat on break. Snack Cabinet inventory,
  comparator, drops and string-ID ItemStack migration are verified.
- [x] Full 15-TileEntity pass finishes with a clean reobfuscated build and a
  Java 8 dev-client startup; the expanded registry/resource/NBT self-test passes
  during Forge initialization.
- [x] Cake Box spawn parity restored: the legacy item used to construct every
  passenger with the hard-coded default Reimu model. `EntityMaid.onSpawnWithEgg`
  now performs the source `finalizeSpawn`-style random selection from all bundled
  manifest models (including decorated extra textures), and Cake Box creation
  invokes that hook before spawning/mounting the maid. Box texture remains an
  independent random cosmetic, matching the source entity.
- [x] Maid model integrity follow-up: all 234 selectable manifest entries were
  revalidated against their geometry and PNG resources (no missing files or
  orphaned parents). The renderer now retains each entry's explicit animation
  list and restores the source's mutually-exclusive visibility layers for
  blink/beg/sleep/sitting skirts, equipment and reverse-equipment parts, task
  variants, low-health parts, backpack state and Hecatia's dimension variants.
  Previously every bone reset to visible each frame, causing overlapping parts
  and apparent geometry artifacts on models such as Cyra and Hecatia.
- [x] Mini-game rendering parity follow-up: Gomoku, western chess and xiangqi
  now use their original Bedrock piece geometry, textures and selection bones.
  Their source 2x2/3x3/4x4 board geometry and piece spacing are normalized to
  the port's single interactive block. Startup validation checks every required
  piece bone, while engine smoke tests cover legal player moves, AI replies,
  turn changes, Gomoku victory, terminal rejection and reset.
- [x] Maid profession UI follow-up: task rows and pager controls were previously
  swallowed by the generic tab-button ID branch. Only IDs 100-102 are now
  delegated to tab handling, so all 21 registered professions can be selected;
  the direct-select packet retains owner, distance, ID and server-thread checks.
- [x] Board opponent placement follow-up: the one-block adaptation no longer
  creates its invisible maid seat in the centre/on top of the board. Navigation,
  manual game start and already-mounted NPCs now use a rotated ground-level
  position one block outside the board and face back toward the playing field.
- [x] Board 3x3 interaction restoration: newly placed and unobstructed legacy
  boards expand into eight stateless invisible hit sections around their single
  state-owning TileEntity. Board geometry, pieces, render bounds, NPC clearance
  and ray-hit coordinates scale together to 3x3; clicks are normalized across
  all nine blocks. Old one-block boards expand automatically when space permits
  and retain the compact fallback when obstructed. Breaking any section removes
  the complete structure while dropping only the actual board item.
- [x] Board 3x3 render/hit correction: enlarged pieces now inherit the board's
  reflected-X-before-rotation transform and a footprint-scaled surface offset,
  preventing chess pieces from being mirrored below the tabletop. The click
  transform is its exact inverse for all four facings, and per-game grid tests
  cover every western-chess square and every gomoku/xiangqi intersection.
- [x] InfernalMobs/Compact InfernalMobs disarm compatibility: the maid now
  exposes its authoritative main-hand stack through the vanilla equipment API
  and mirrors external `setCurrentItemOrArmor` mutations back into the maid
  inventory, avoiding a stale-stack server crash. The verification command
  covers both directions of this bridge; a packaged third-party multiplayer
  stress pass remains part of the compatibility gate.
- [x] Scarecrow parity pass: placement creates synchronized two-block halves,
  preserves all four horizontal facings, removes the pair without double drops,
  uses the source compound collision shapes and renders all 4 lower plus 22
  upper JSON elements with their original textures, UVs and rotated cubes. The
  source radius-48 square exclusion is implemented through a loaded TileEntity
  index instead of an 84k-block scan per fairy spawn attempt, and the item shows
  the configured range. Java 8 compilation, clean reobfuscated build, resource/
  registry self-test pass. A single in-world capture confirms all four facings;
  survival removal drops exactly one item and creative removal correctly drops
  none while both halves are removed.
- [~] Shrine Lamp parity pass: the previous approximate one-block cuboids are
  replaced by the complete source `maid_beacon_down` (9 elements) and
  `maid_beacon_up` (15 elements) JSON geometry, including the N/S and W/E roof
  orientations, two-block lifecycle, source collision/light and legacy
  metadata-0 world compatibility. State lives in the upper half and survives
  breaking/placing in both flat 1.7 and nested modern `ForgeData` formats. The
  restored screen controls all five effects, one-Power deposit/withdrawal and
  overflow policy; player Power (0-5), excess-to-XP pickup, radius-6 automatic
  collection, 100-Power capacity, 80-tick effect cycle, `/900` cost and the +3
  weapon modifier follow source behaviour. Both model orientations and the
  corrected non-overlapping localized GUI were captured and accepted in-world.
  Survival drop and re-placement now have in-world confirmation that stored
  Power is preserved. Effect execution is shared by the normal server tick and
  `/tlmmaid beaconverify`, which reports the actual Potion ID, affected Maid
  count, duration/amplifier and measured Power debit; the five effects plus
  transfer/autocollect remain for the final manual acceptance pass. Destruction
  lifecycle review found no Maid-to-beacon navigation reference in either the
  source or port: an already applied effect expires after 100 ticks. Diagnostic
  status now exposes both the Maid's live path endpoint and remaining durations
  of all five beacon-compatible effects so a reported post-removal path can be
  separated from owner-follow, home/schedule and profession navigation.
- [x] Maid owner binding/GameType audit: ownership is persisted exclusively as
  the vanilla tameable `OwnerUUID`; Creative/Survival is not part of bind,
  resolve or interaction checks, and `/gamemode` retains the same server-player
  identity. `/tlmmaid ownerverify` independently locates a Maid by stored UUID
  and reports game mode, tame state, resolved owner UUID and object identity,
  allowing before/after mode-switch verification without relying on GUI access.
- [x] Melee attack critical fix: the port delegated attacks to
  `EntityLivingBase.attackEntityAsMob` through the tameable inheritance chain;
  in Minecraft 1.7.10 that method only records the attacker and always returns
  false. Maid now performs the complete vanilla `EntityMob` damage transaction:
  attack attribute and held-item modifiers, living-target enchantment bonus,
  knockback, Fire Aspect and enchantment callbacks. Runtime status exposes the
  successful-hit counter, measured last health delta and ticks since the hit.
- [x] Owner-follow interruption audit: the apparent link to a removed Shrine
  Lamp was the independent Home Mode point recorded by the Maid GUI's `H`
  control while the Maid stood beside the lamp. Disabling Home now immediately
  clears the obsolete navigator path, reports the old home coordinates and
  resumes owner following. Follow is no longer incorrectly disabled by the
  DAY/NIGHT schedule's REST period when Home Mode itself is off, matching the
  source CORE follow behaviour. Status exposes the active schedule home target.
- [x] Low-health combat safety: every combat selector and both 1.7 melee/ranged
  AI goals reject combat below 25% maximum health. Existing targets and their
  navigator path are cleared before the AI tick, while direct melee and ranged
  entry points provide a final guard. Exactly 25% permits combat again; status
  exposes `combatAllowed` for deterministic testing.
- [x] Sound-log audit: all twenty source placeholder events that pointed at the
  decoder-hostile silent `maid/empty.ogg` now resolve to valid bundled Peco OGG
  variants, and profession selection dispatches its actual mode event instead
  of collapsing every non-combat profession to idle. The remaining
  `minecraft:mob.witch.*` warnings originate from vanilla witches/assets rather
  than EntityMaid or EntityFairy and are tracked separately from mod sounds.
- [x] Miner GT6 recognition hotfix: GT6 `PrefixBlock` ores store their material
  in `PrefixBlockTileEntity` extended metadata, so the generic machine-safety
  guard previously rejected them. The classifier now permits only the exact
  optional GT prefix-block contract, obtains its OreDictionary stack through
  `getItemStackFromBlock`, and still denies unrelated TileEntities. Harvest
  validation now calls native `MultiItemTool.canHarvestBlock` before the generic
  Forge harvest-level fallback. The fix compiles and reobfuscates against the
  installed GT6 Unofficial 6.15.07 environment.
- [x] Combat-assist trigger fix: the port only polled `owner.getAITarget()`
  (the entity which hurt the owner) and omitted Minecraft 1.7.10's separate
  `owner.getLastAttacker()` path (the entity attacked by the owner). Restricted
  `OwnerHurtTarget` and `OwnerHurtByTarget` goals are now registered for melee,
  bow, danmaku, crossbow and trident professions. They respect work activity,
  sitting, the 25% health gate, task range, home bounds, teams and friendly
  tameable/villager exclusions. Periodic targeting also prioritizes owner-hit,
  owner-attacked and maid-attacked targets before autonomous hostile scanning;
  `/tlmmaid verify` exercises owner-hit assignment and `status` exposes both
  the Maid attack target and the owner's last attacked entity.
