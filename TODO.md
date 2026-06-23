# Pongon Design TODOs

## Needs a design decision before implementation

1. **Portal/Access** — how do you get to Pongon?
2. **Dingolin tool/armour set** — which tools? same set as Pongonite?
3. **Armour special properties** — does Pongonite/Dingolin armour give heat resistance or other effects?
4. **X-Ray Vision Potion** — duration, brewing recipe specifics
5. **Swim Boost Potion** — duration
6. **Music/ambience** — any custom sounds?
7. **Skybox** — what does the sky look like? Star colour?
8. **Advancements** — any custom ones?
9. **Lava Gun** — range, fire rate, does it place lava or just do damage?

## Biomes

Architecture decided — `multi_noise` with continentalness derived from the island
noise; Lava Ocean base + per-island biomes. See `docs/adr/0001-biome-placement-from-terrain-noise.md`
and `CONTEXT.md`.

- [ ] Pongol Forest follow-ups — saplings + sapling growth, Pongol Planks + crafting recipes, ground tuft vegetation, distinct sky/fog tint
- [ ] Ashen Barrens biome — bare ash/crushed-magma islands, harsher feel
- [ ] Dingolin Spires biome — rocky islands studded with crystal/Dingolin formations
- [ ] Canyons biome — **scope-C** (varies terrain shape): canyons, lava lake + lava rivers; needs a scope revisit before building

## Known bugs

- [ ] **Eating/drinking is interrupted (very slow) in the Pongon dimension** —
  consumables take far longer than the Overworld and the use animation restarts
  partway through, but eventually completes. Confirmed in Survival. The action is
  interrupted/restarted (or game ticks are stretched by low TPS), not blocked.
  Structured report + diagnostic plan: `docs/bugs/0001-consumables-interrupted-in-pongon.md`.
  Next step: run the discriminating experiments in that report (measure MSPT/TPS,
  day vs night, disable the day-night cycle).

## Ready to implement (design settled)

- [x] Dingolin ore — block, world gen, drops (fully specced in design notes)
- [ ] Crushed Magma world gen — underground stone layer (fully specced)
- [x] Rock Vapor kill mechanic — glowing yellow pass-through gas, 20 HP/sec contact damage
- [ ] Custom terrain generation — replace flat generator with noise-based terrain

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
