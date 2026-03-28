# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build the mod
./gradlew build

# Run the Minecraft client with mod loaded (for manual testing)
./gradlew runClient

# Run a dedicated server
./gradlew runServer

# Decompile Minecraft source (useful for understanding vanilla code to extend)
./gradlew genSources

# Clean build artifacts
./gradlew clean
```

No automated test framework is configured yet. Add JUnit 5 via `testImplementation` in `build.gradle` and create `src/test/java` to introduce tests.

## Architecture Overview

This is a **Fabric mod for Minecraft 1.21** (Java 21) that adds a new "Pongon" dimension — a hot exoplanet with lava/magma terrain, a day/night temperature cycle, and custom blocks, items, and mobs (per README.md).

### Entry Point

`src/main/java/net/pongon/Pongon.java` — implements `ModInitializer`, called once at mod load. All registries (blocks, items, mobs, etc.) should be initialized here or delegated to registry classes called from `onInitialize()`.

### Registration Pattern

Registry keys live in dedicated classes under `net.pongon`:
- `world/ModDimensions.java` — dimension `RegistryKey` definitions

Follow this pattern for new registries: create a `ModBlocks`, `ModItems`, `ModEntities` etc. class with `static final` registry entries, and call a `register()` method from `Pongon.onInitialize()`.

### Data-Driven Dimension

The Pongon dimension is defined entirely through JSON in `src/main/resources/data/pongon/`:
- `dimension_type/pongon.json` — physics properties (ultrawarm, no ceiling, temp settings)
- `dimension/pongon.json` — world generator config (currently flat: Netherrack + Magma surface)
- `worldgen/biome/pongon.json` — biome aesthetics (red/orange sky, hot temperature)

Custom terrain generation will require replacing the `minecraft:flat` generator with a custom noise generator and registering it in Java.

### Mixins

`pongon.mixins.json` configures the mixin system (package: `net.pongon.mixin`). Currently empty — add mixin classes here when hooking into existing game systems.

### Key Versions

| Dependency | Version |
|---|---|
| Minecraft | 1.21 |
| Fabric Loader | 0.16.5 |
| Fabric API | 0.102.0+1.21 |
| Yarn mappings | 1.21+build.9 |
| Java | 21 |

### Current State

The scaffold is complete: dimension type, flat terrain, and biome are registered and load in-game. Not yet implemented: custom blocks/items, mobs, portal, crafting recipes, custom world generation, advancements. See `README.md` for the full feature spec and `TODO.md` for outstanding design decisions.
