# Biome placement derived from terrain noise

## Status

accepted

## Context

Pongon is moving from a single fixed biome to multiple biomes. The Base
(terrain shape, temperature cycle, star, vertical ore structure) stays constant
everywhere; biomes only layer surface skin, sky/fog, vegetation, structures, and
mob spawns on top (scope B). The Base terrain is a lava/magma ocean dotted with
volcanic shield islands.

The goal was for whole islands to read as a single biome (a biome may span
several nearby islands) while the lava between them stays one uniform base biome.
Vanilla datapack worldgen offers no way to paint an explicit, irregular biome
boundary — biome placement is entirely noise-driven.

## Decision

Use a `minecraft:multi_noise` biome source and derive its climate axes from the
terrain, rather than from independent climate noise:

- **`continentalness` is derived from the island terrain noise.** This makes
  "ocean vs. island" a climate axis, so the ocean→island biome boundary hugs the
  coastline automatically (it is computed from the same noise that draws the
  coast).
- **The Lava Ocean is the single base biome**, selected for low continentalness
  (open lava between islands).
- **Island type is chosen by a slow, large-scale regional noise** (e.g.
  temperature). Because it varies slowly relative to an island's footprint, a
  whole island reads as one type and neighbouring islands tend to share it.
- **`depth` (altitude) is reserved** for a future surface-vs-underground split
  (cave biomes), not for the island/ocean distinction — altitude cannot separate
  them because the ocean's air column and an island's rock share the same Y
  levels.

## Considered alternatives

- **`checkerboard` biome source** — equal-size square cells, no climate noise
  needed. Rejected: artificial straight boundaries, every biome equally common,
  no way to make a biome rare or dominant.
- **Naïve large-scale climate noise** (independent of terrain) — simpler, but
  biome boundaries fall at arbitrary points in the ocean and can clip island
  edges; the ocean/coast split would not track the coastline.

## Consequences

- Biome placement is coupled to the terrain density functions; changing the
  island noise also shifts biome placement.
- Whole-island atomicity is a strong tendency, not a guarantee — a sufficiently
  large island spanning a regional-noise transition could still carry two island
  types. Mitigated by keeping the regional type-noise scale much larger than the
  biggest island.
- Adding the second island biome is what first requires the regional type noise;
  a single island biome (Lava Ocean + Pongol Forest) only needs the
  continentalness split.
