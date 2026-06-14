# Pongon

A Minecraft Fabric mod adding the Pongon dimension — a hot exoplanet of lava and magma. This glossary defines the domain language used when discussing its design.

## Language

**Base**:
The constant foundation shared by all of Pongon, present everywhere regardless of biome: the terrain shape (lava/magma ocean with volcanic shield islands), the day/night temperature cycle, the star, and the vertical ore and Rock Vapour structure.
_Avoid_: base environment, base layer, default terrain

**Biome**:
A region of Pongon that layers its own surface skin, sky/fog tint, vegetation, structures, and mob spawns on top of the Base. Biomes vary decoration and surface, never terrain shape.
_Avoid_: zone, region, area

**Surface skin**:
The surface block(s) a biome lays over the Base terrain — e.g. one biome floored with Pongol Dirt, another with Crushed Magma. Distinct from terrain shape, which is shared.
_Avoid_: ground cover, topsoil, surface layer

**Lava Ocean**:
The single base biome that fills all the lava between islands, uniform across the whole dimension. Distinguished from islands by terrain presence (the ocean/coast split is derived from the island terrain noise).
_Avoid_: sea, the ocean biome

**Island biome**:
A Biome that occupies islands (land above the Lava Ocean), carrying its own surface skin, sky/fog, vegetation, structures, and mobs. Which Island biome a given island gets is chosen by a slow regional climate field, so nearby islands tend to share one.
_Avoid_: land biome, terrain biome

**Magma beach**:
The band of bare magma (the Base surface) at an island's waterline, between the island's surface skin above and the day/night lava cycle at the sea-level band below. Every Island biome's shoreline is a magma beach.
_Avoid_: shore, coast, waterline
