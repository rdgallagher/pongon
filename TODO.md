# Pongon Design TODOs

## Needs a design decision before implementation

1. **Portal/Access** — how do you get to Pongon?
2. **Dingolin tool/armour set** — which tools? same set as Pongonite?
3. **Armour special properties** — does Pongonite/Dingolin armour give heat resistance or other effects?
4. **X-Ray Vision Potion** — duration, brewing recipe specifics
5. **Swim Boost Potion** — duration
6. **Music/ambience** — any custom sounds?
7. **Advancements** — any custom ones?
8. **Lava Gun** — range, fire rate, does it place lava or just do damage?

## Biomes

Architecture decided — `multi_noise` with continentalness derived from the island
noise; Lava Ocean base + per-island biomes. See `docs/adr/0001-biome-placement-from-terrain-noise.md`
and `CONTEXT.md`.

- [ ] Pongol Forest follow-ups — saplings + sapling growth, ground tuft vegetation, distinct sky/fog tint
- [x] Pongol wood family — Pongol Wood + Pongol Planks blocks, Oak-style recipes (log→planks, 4 logs→wood)
- [x] Pongol plank-derived set — stairs, slab, fence, fence gate, door, trapdoor, pressure plate, button (blocks, recipes, tags). Signs + boats deferred (need block-entities / entity-types)
- [ ] Ashen Barrens biome — bare ash/crushed-magma islands, harsher feel
- [ ] Dingolin Spires biome — rocky islands studded with crystal/Dingolin formations
- [ ] Canyons biome — **scope-C** (varies terrain shape): canyons, lava lake + lava rivers; needs a scope revisit before building

## Known bugs

- [x] **Bug 0001 — server-thread freeze from the day/night lava cycle** (root cause
  found). The magma↔lava conversion thrashes the light engine and leaves
  never-cleaned flowing lava, freezing the server for 22–38 s at a time → resets
  block-breaking + eating in every dimension. **DayNightCycle disabled** to confirm +
  unblock. See `docs/bugs/0001-consumables-interrupted-in-pongon.md`.
- [x] **Redesign the day/night lava cycle** (re-enabled without the freeze) —
  surface-only + landlocked source lava + freeze-the-whole-band cleanup + quiet
  setBlockState flags. Pending in-game verification (no "Can't keep up" stalls).

## Ready to implement (design settled)

- [x] Dingolin ore — block, world gen, drops (fully specced in design notes)
- [ ] Crushed Magma world gen — underground stone layer (fully specced)
- [x] Rock Vapor kill mechanic — glowing yellow pass-through gas, 20 HP/sec contact damage
- [ ] Custom terrain generation — replace flat generator with noise-based terrain
- [x] Pongon sky — bright custom star (no moon), via client mixin on renderSky. Pending
      in-game verification. Follow-up: the "blinding star" look-at-it mechanic (damage +
      White Out), and optionally a bigger/tinted star or starless night.
- [ ] Lava Blob (mob) — wild blob, spec in `docs/specs/lava-blob.md`. Taming deferred to Canyons.
      - [x] Entity + renderer + spawn egg (bounces, no aggression) — first entity, scaffolds ModEntities
      - [x] Invincibility + Potion of Blobiness (ModEffects; /kill still works)
      - [x] Day-gated spawning — vanilla creature spawn was far too sparse, so a custom
            capped near-player daytime spawner (LavaBlobSpawner) does the real work; the
            biome creature-pool entry stays as a low-rate extra. PongonTime helper shared.
      - [x] Despawn + keep-alive (night 50%/5min unless daytime or on fire/lava/magma/crushed magma) — pending in-game verification
      - [ ] Rock Vapor beam (16 blk/s, pierces, melts blocks to lava, 30/20/10 HP/s)
      - [ ] Polish: flattened-dome model, single-icon Blobiness, sounds, contact springy push

## Done

- [x] Pongonite ore — block, world gen, drops
- [x] Pongonite tool set — pickaxe, axe, shovel, hoe, sword
- [x] Pongonite armour set — helmet, chestplate, leggings, boots
- [x] Crafting recipes — all Pongonite tools, armour, and block
- [x] Crushed Magma block
- [x] Rock Vapor block
- [x] Creative inventory tab
- [x] Dingolin full set — ore, block, crystal/ball items, tools, armour, world gen, recipes, textures
- [x] Day-night lava/magma cycle — ocean floor melts to lava at dawn, freezes back to magma at dusk
- [x] Pongol Forest biome (initial pass) — Pongol Dirt/Log/Leaves blocks, worldgen-only trees, multi_noise ocean/island split, Pongol Dirt surface skin with magma beach
