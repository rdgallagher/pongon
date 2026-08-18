# Volcanoes as a placed feature, not terrain noise

## Status

accepted

## Context

The Ashen Barrens' headline feature is Volcanoes: cones that rise from the
island, spew ash, and run lava down their flanks. A volcano is terrain shape,
which sits awkwardly against the scope-B contract in `CONTEXT.md` and ADR 0001 —
a Biome "layers surface skin, sky/fog, vegetation, structures, and mob spawns on
top" of the Base and "varies decoration and surface, never terrain shape".

The natural reading of "make volcanic terrain" would be to shape it in the noise
router, the way the islands themselves are shaped. That turns out not to be
reachable: density functions in the noise router are sampled before biomes are
resolved and have no access to the biome at a position. There is no vanilla
datapack mechanism for biome-conditional terrain density. Only the surface rule
is biome-aware, and it can swap blocks but not add height.

## Decision

Build volcanoes as a **placed feature** — a custom `pongon:volcano` `Feature`
implemented in Java, placed by a JSON placed-feature in the Ashen Barrens' own
feature list.

Features are decoration by the datapack's own division of labour: they run per
chunk after terrain and surface rules, and they are declared per biome. So a
volcano is a structure the biome decorates its islands with, not a change to the
Base terrain — the scope-B contract holds as written, and no other biome is
affected.

Because features run *after* surface rules, the cone must lay its own Ash skin;
it will not inherit the biome's surface rule.

## Considered alternatives

- **Biome-conditional density function** — shape the cones in the noise router.
  Rejected: not expressible. Density functions cannot read the biome, and the
  biome source reads *from* the router, so the dependency runs the wrong way.
- **A `minecraft:jigsaw` structure** — real structure placement, spacing, and
  structure-start bookkeeping for free. Rejected as far too heavy for a shape
  that is a cone of two block types, and structure pieces are template-based,
  which fights the per-volcano size variation we want.
- **Widen the scope-B contract to let biomes vary terrain** — honest about what a
  volcano is, but it would make the Base meaningless and re-open ADR 0001's
  coupling of biome placement to terrain noise. Rejected as a much larger change
  than this feature justifies. The Canyons biome (already flagged scope-C in
  `TODO.md`) is where that question genuinely has to be answered.

## Consequences

- The volcano's cone is built by imperative Java, so its shape is tunable only by
  recompiling, not by editing a datapack. Accepted for now; the constants live
  together at the top of `VolcanoFeature` so a config record can be extracted if
  tuning proves painful.
- Volcanoes cannot influence the heightmap that other features place against
  within the same chunk generation step, so anything that must sit on a volcano
  has to be placed by the volcano feature itself.
- A volcano near a chunk edge is still written across chunk borders by the
  feature's own block writes, which is normal for large features but means the
  cone can be clipped if a neighbouring chunk generated first. Mitigated by
  keeping the radius modest relative to the feature's placement spacing.
- This is the first custom `Feature` in the mod, so it introduces `ModFeatures`
  and the `Registries.FEATURE` registration pattern.
