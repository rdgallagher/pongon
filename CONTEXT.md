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

**Lava Blob**:
An invincible, springy lava mob that hops around the surface during the hot day. Friendly to players (harmless to bump; attacking one inflicts Potion of Blobiness) but a protective ally, not passive — it breathes a Rock Vapor Beam at hostile mobs that threaten it (or its owner, when tamed). Tameable, but only with food from the Canyons biome. See `docs/specs/lava-blob.md`.
_Avoid_: lava slime, magma cube, lava cube

**Rock Vapor Beam**:
A Lava Blob's only attack: a continuous stream of Rock Vapor breathed at a hostile target, extending at 16 blocks/s, piercing every entity in its line (30/20/10 HP/s by difficulty — players and pets included) and melting the block it ends on into lava (except unbreakable blocks). Distinct from the Rock Vapor block itself.
_Avoid_: vapor breath, lava breath, flamethrower

**Potion of Blobiness**:
The 2-minute status effect a Lava Blob inflicts on any player who attacks it: Slowness + Mining Fatigue ("tired"), plus the player deals zero damage to passive (PassiveEntity) mobs for the duration. Not a brewed potion — only applied by Lava Blobs.
_Avoid_: blob potion, tiredness, sliminess

**Hot / hot day (300 °C)**:
Pongon's daytime, when the surface is lava and Lava Blobs spawn. The "300 °C" threshold in design notes is the same binary day flag the day/night cycle already uses; cold/night is its complement.
_Avoid_: warm, temperature level, summer
