# Spec — Lava Blob (mob)

Status: **design agreed; ready to implement (taming deferred to Canyons)**

The Lava Blob is Pongon's first mob: an invincible, springy creature of living lava
that bounces around the surface during the hot day. To players it is friendly and
harmless (bumping it is a springy push; attacking it inflicts **Potion of
Blobiness**), but it is **not** merely passive — it is a **protective ally**, more
like an Iron Golem. It guards itself (and, when tamed, its owner) by breathing a
**continuous Rock Vapor beam** at hostile mobs (see [Combat](#5-combat--rock-vapor-beam)).
It is **tameable** (taming is gated on the Canyons biome — see [Taming](#10-taming)).

## 1. Technical base

- `LavaBlobEntity extends SlimeEntity` — inherits the hop movement, squish/stretch
  animation, and size system; **no** Magma Cube melee attack. Its only attack is the
  Rock Vapor beam ([§5](#5-combat--rock-vapor-beam)), driven by a custom goal.
- **Friendly to players but a protector, not passive** — registered in the `CREATURE`
  spawn group, **single fixed size** (player-ish — see tunables), custom model +
  texture.
- The class hierarchy means a Lava Blob is **not** a `PassiveEntity` — relevant to
  the Blobiness rule below (a Blobied player can still whack a blob), and it targets
  `HostileEntity`s for its beam.

## 2. Invincibility & getting hit

- **Immune to all damage** (lava, fall, players, void, etc.) — never takes damage,
  never dies. "Infinite HP" is flavour; mechanically `damage()` always cancels.
- **Any player attack inflicts Blobiness** on that player:
  - **Melee** → apply Blobiness to the attacker, blob does a **springy recoil**
    (bounces/hops away) + squelch sound. No HP lost.
  - **Projectile** (arrow, etc.) → apply Blobiness to the projectile's player owner,
    blob just **wobbles**. No HP lost.
- Each new hit **refreshes** Blobiness to the full 2:00.
- Implementation: override `LavaBlobEntity.damage(...)` — both melee and projectile
  damage arrive here with the player as attacker/owner; apply the effect, trigger
  recoil, return without applying damage.

## 3. Potion of Blobiness (custom status effect)

Duration **2:00**, single effect / single HUD icon. Two halves:

1. **Tired:** Slowness II (slower walk) + Mining Fatigue I (slower mining + slower
   attack speed). No screen distortion.
2. **Can't hit passive mobs:** while active, the player deals **0 damage and no
   knockback** to `PassiveEntity` mobs (the swing still animates). Hostile mobs and
   **neutral** mobs (e.g. Fishongons) are unaffected — self-defence and Fishongon
   fights still work.

Implementation notes:
- One custom `StatusEffect` (so the player sees a single "Blobiness" icon). Reproduce
  Slowness via a movement-speed attribute modifier on the effect; the mining/attack
  slowdown is most cleanly done with a small mixin on block-break speed (or by
  applying hidden vanilla effects if a single icon isn't strictly required).
- "Can't hit passives" via Fabric `AttackEntityCallback`: cancel the attack when the
  player has Blobiness and the target is a `PassiveEntity`.

## 4. Wild behaviour (untamed)

- **Curious / friendly:** idle slime-style wander, and **occasionally hops toward a
  nearby player** (it likes you — also foreshadows tameability). Never flees, never
  attacks.
- **Lava is walkable:** no lava damage; pathfinding treats lava as ground, so it can
  wander into the **Lava Ocean**, though it mostly roams the land.
- **Contact:** bumping into a blob gives a **springy push** (bouncy knockback) and
  **no damage** — safe to approach, pen, and (later) tame. The only player downside
  is Blobiness from attacking it.

## 5. Combat — Rock Vapor beam

The blob defends with a **continuous Rock Vapor beam** breathed from its mouth at a
target. This is its only attack.

- **Targets:** hostile mobs (`HostileEntity`) **on sight** within **16 blocks** (the
  usual hostile aggro range), no provocation needed.
  - **Wild:** guards itself — targets hostiles near *itself*.
  - **Tamed:** also guards its **owner** — targets hostiles within 16 blocks of the
    owner. (Tamed targeting is part of the deferred [Taming](#10-taming) work; build
    the wild self-guard now with a clean seam for owner-guard.)
- **The beam:** a sustained stream (not a projectile) that **extends outward at
  16 blocks/s** from the blob toward the target (the tip advances ~0.8 block/tick
  until it reaches its end point).
  - It **pierces entities** — it burns *through* every mob/player in its line and
    keeps going; it does **not** stop on an entity.
  - It **stops at the first block** it reaches; that becomes its end point.
- **Damage:** **30 / 20 / 10 HP per second** (Hard / Normal / Easy) to **every entity
  the beam currently touches** — the same rate as standing in the Rock Vapor block,
  scaled by difficulty like the Pongol Dragon.
  - **It is hot and indiscriminate:** **players and tamed pets caught in the line of
    fire take this damage too.** Mind your position when a blob is defending you.
- **Terrain — melts blocks to lava:** the block the beam terminates on is **converted
  to lava**, **always** (independent of the `mobGriefing` gamerule), **except** the
  unbreakable blocks (bedrock, end portal frame) and Rock Vapor itself, which stop the
  beam without changing. Because melting the end block opens the path, sustained fire
  **eats forward block by block**, carving a lava channel until it reaches an
  unbreakable block or max reach.
  - ⚠️ **Consequence (accepted):** a tamed blob defending you near a build *will* melt
    your blocks into lava and bore a lava channel along the beam's path. This is
    intentional; there is no opt-out beyond the unbreakable set. Flag prominently in
    player-facing docs.
- The blob itself is immune to its own and others' Rock Vapor / lava (invincible).

Implementation notes:
- Custom `Goal` on `LavaBlobEntity` that acquires a `HostileEntity` target (and, when
  tamed, defends the owner) and sustains the beam while the target is in range + line
  of sight.
- The beam is **server-driven**. Each tick: ray-march from the blob's mouth toward the
  target, advancing the tip by `16/20 = 0.8` block (16 blocks/s) up to the current
  reach. Find the **end point** = the first non-air, non-lava block; if it is
  unbreakable or Rock Vapor, stop there unchanged, else `setBlockState(..., LAVA)`.
  Then apply the per-tick share of the HP/s rate (`rate/20` per tick) to **every**
  entity whose hitbox the blob→endpoint segment intersects (piercing — players and
  pets included).
- Client renders the beam itself (a Rock-Vapor-coloured stream / particle column from
  mouth to end point); gameplay is fully server-authoritative.

## 6. Spawning

- **Daytime only** — gated on the 300 °C threshold, which maps to Pongon's day (see
  [§9](#9-300c--the-daynight-cycle)).
- **Any biome.**
- On **solid land OR the lava-ocean surface** (it's a lava creature).
- **Groups of 1–3**, moderate spawn weight, `CREATURE` spawn group.

## 7. Despawn & keep-alive

- A wild blob is **safe** (no despawn roll) when **it is daytime** (hot again) **or**
  it is standing on a **heat source** at night: fire, lava, magma block, or Crushed
  Magma. This is the "box it in above fire" keep-alive trick, generalised.
- Otherwise, at night it rolls a **50% despawn chance every 5 minutes**.
- Standard distance-based despawn still applies (prevents accumulation).
- **Tamed blobs never despawn** (persistent) — see Taming.

## 8. Model & rendering

- Reuse the **vanilla slime model flattened into a dome** (flat bottom, sagging
  rounded top), keeping **slime-style eyes/mouth** (a face).
- **Animated molten-lava texture**, rendered **emissive / full-bright** so it glows
  in the dark. (Emissive texture only — it does not emit block light.)
- Native slime **squish-on-bounce** animation.

## 9. 300 °C ↔ the day/night cycle

The 300 °C spawn/despawn threshold = **Pongon daytime**. The day flag is currently
computed locally inside `DayNightCycle` (`world.getTimeOfDay() % 24000 < 12000`).
**Extract a shared helper** (e.g. `PongonTime.isHot(ServerWorld)` / `isDay`) and have
both `DayNightCycle` and the Lava Blob spawn/despawn logic call it, rather than
duplicating the expression.

## 10. Taming

Lava Blobs **are tameable**, but the mechanic is **gated on the Canyons biome**: the
taming food is a **dome-shaped mushroom built from glowing red-mushroom blocks** that
grows in the Canyons. Deferred to the Canyons biome work:

- the taming food item/block and how feeding works (chance-to-tame, etc.),
- what a **tamed** blob does (follow owner, persistence, any utility),
- ownership / limits.

Forward dependency: **Canyons biome spec** (TBD). Until then, implement only the
**wild** blob; leave a clean seam for a "tamed" state to layer on.

## 11. Drops, sounds, misc

- **Drops: none** — it is invincible and never dies. (Its only "output" is Blobiness,
  applied on hit, not dropped.)
- **Sounds:** slime squish set, lava-tinted (tunable).
- **No breeding / no farming** (per original notes).

## 12. Open tunables (pick during implementation)

- Fixed size / hitbox dimensions ("player-sized" dome).
- Hop height & frequency; how often it hops toward a player.
- Spawn weight, per-chunk cap, group size distribution.
- Blobiness amplifiers (Slowness II / Mining Fatigue I as written) and refresh rule.
- Despawn interval (5 min) and chance (50%).
- Beam: aggro range (16 blocks), extend speed (16 blocks/s), damage 30/20/10 HP/s,
  beam thickness for the entity-intersection check, and the beam's visual width/look.

## 13. New code/assets (first-entity scaffolding)

- `entity/LavaBlobEntity.java`, `entity/ModEntities.java` (EntityType + attributes +
  `SpawnRestriction`), spawn wiring (Fabric `BiomeModifications` for the Pongon
  biomes).
- `effect/ModEffects.java` + the Blobiness `StatusEffect`; `AttackEntityCallback`
  registration; block-break-speed mixin if used.
- Rock Vapor beam: a custom targeting/attack `Goal`, the server-side ray-march +
  block→lava + piercing-damage logic, and a client beam renderer (no new entity type
  needed — the beam is a property of the blob while it has a target).
- Client: entity renderer + (flattened slime) model in `PongonClient` /
  `ModEntityRenderers`.
- Texture (emissive lava), `en_us.json` entries (entity + effect), and a
  `lava_blob_spawn_egg` if desired for creative.
