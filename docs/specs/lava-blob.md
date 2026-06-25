# Spec — Lava Blob (mob)

Status: **design agreed; ready to implement (taming deferred to Canyons)**

The Lava Blob is Pongon's first mob: a passive, invincible, springy creature of
living lava that bounces around the surface during the hot day. It is harmless
but inflicts **Potion of Blobiness** on anyone who attacks it, and it is
**tameable** (taming is gated on the Canyons biome — see [Taming](#taming)).

## 1. Technical base

- `LavaBlobEntity extends SlimeEntity` — inherits the hop movement, squish/stretch
  animation, and size system; **no** Magma Cube attack behaviour.
- **Passive** (registered in the `CREATURE` spawn group), **single fixed size**
  (player-ish — see tunables), custom model + texture.
- The class hierarchy means a Lava Blob is **not** a `PassiveEntity` — relevant to
  the Blobiness rule below (a Blobied player can still whack a blob).

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

## 5. Spawning

- **Daytime only** — gated on the 300 °C threshold, which maps to Pongon's day (see
  [§8](#8-300c--the-daynight-cycle)).
- **Any biome.**
- On **solid land OR the lava-ocean surface** (it's a lava creature).
- **Groups of 1–3**, moderate spawn weight, `CREATURE` spawn group.

## 6. Despawn & keep-alive

- A wild blob is **safe** (no despawn roll) when **it is daytime** (hot again) **or**
  it is standing on a **heat source** at night: fire, lava, magma block, or Crushed
  Magma. This is the "box it in above fire" keep-alive trick, generalised.
- Otherwise, at night it rolls a **50% despawn chance every 5 minutes**.
- Standard distance-based despawn still applies (prevents accumulation).
- **Tamed blobs never despawn** (persistent) — see Taming.

## 7. Model & rendering

- Reuse the **vanilla slime model flattened into a dome** (flat bottom, sagging
  rounded top), keeping **slime-style eyes/mouth** (a face).
- **Animated molten-lava texture**, rendered **emissive / full-bright** so it glows
  in the dark. (Emissive texture only — it does not emit block light.)
- Native slime **squish-on-bounce** animation.

## 8. 300 °C ↔ the day/night cycle

The 300 °C spawn/despawn threshold = **Pongon daytime**. The day flag is currently
computed locally inside `DayNightCycle` (`world.getTimeOfDay() % 24000 < 12000`).
**Extract a shared helper** (e.g. `PongonTime.isHot(ServerWorld)` / `isDay`) and have
both `DayNightCycle` and the Lava Blob spawn/despawn logic call it, rather than
duplicating the expression.

## 9. Taming

Lava Blobs **are tameable**, but the mechanic is **gated on the Canyons biome**: the
taming food is a **dome-shaped mushroom built from glowing red-mushroom blocks** that
grows in the Canyons. Deferred to the Canyons biome work:

- the taming food item/block and how feeding works (chance-to-tame, etc.),
- what a **tamed** blob does (follow owner, persistence, any utility),
- ownership / limits.

Forward dependency: **Canyons biome spec** (TBD). Until then, implement only the
**wild** blob; leave a clean seam for a "tamed" state to layer on.

## 10. Drops, sounds, misc

- **Drops: none** — it is invincible and never dies. (Its only "output" is Blobiness,
  applied on hit, not dropped.)
- **Sounds:** slime squish set, lava-tinted (tunable).
- **No breeding / no farming** (per original notes).

## 11. Open tunables (pick during implementation)

- Fixed size / hitbox dimensions ("player-sized" dome).
- Hop height & frequency; how often it hops toward a player.
- Spawn weight, per-chunk cap, group size distribution.
- Blobiness amplifiers (Slowness II / Mining Fatigue I as written) and refresh rule.
- Despawn interval (5 min) and chance (50%).

## 12. New code/assets (first-entity scaffolding)

- `entity/LavaBlobEntity.java`, `entity/ModEntities.java` (EntityType + attributes +
  `SpawnRestriction`), spawn wiring (Fabric `BiomeModifications` for the Pongon
  biomes).
- `effect/ModEffects.java` + the Blobiness `StatusEffect`; `AttackEntityCallback`
  registration; block-break-speed mixin if used.
- Client: entity renderer + (flattened slime) model in `PongonClient` /
  `ModEntityRenderers`.
- Texture (emissive lava), `en_us.json` entries (entity + effect), and a
  `lava_blob_spawn_egg` if desired for creative.
