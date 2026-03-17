# Pongon

A Minecraft Fabric mod adding a new dimension called Pongon — a hot exoplanet with lava, magma, a day/night temperature cycle, custom blocks, items, and mobs.

## The Dimension

A hot exoplanet. ~800C during the day, ~140C at night. No liquid water. Lots of lava and magma. Surface at Y=135.

### Temperature Cycle

- **Day:** Surface is lava, the star is blinding, Lava Blobs spawn
- **Night:** Surface lava gradually cools to magma (walkable), star is gone, Lava Blobs slowly despawn
- Transition is gradual at dawn/dusk

### The Star (Sun)

- Looking directly at it deals 2HP/sec + **White Out** effect (screen goes white)
- Fades 3 seconds after looking away
- Being under a solid block protects you

### Vertical Structure

| Y Level | What's There |
|---------|-------------|
| 135 | Surface |
| 50+ | Lava cools to magma at night |
| Below 50 | Permanent lava (never cools) |
| +18 to +145 | Dingolin ore (peak at +45) |
| -17 to -64 | Pongonite ore (peak at -48) |
| -75 to -80 | Rock Vapour (world floor, instant death) |

Underground lava pools increase in density the deeper you go.

## Blocks

### Pongol Variants

- **Pongol Dirt** — functions like dirt, looks like Soul Sand but red/brown
- **Pongol Trees** — orange/yellow bark and leaves, craft into Pongol Planks (same colours). Have saplings. Slightly less hard and blast resistant than Overworld trees.

### Pongonite (Ore)

- Pink and purple
- Same hardness/blast resistance as Ancient Debris
- Requires Diamond Pickaxe
- ~30 per chunk, Y=-64 to -17 (peak -48)
- Drops **Pongonite Lumps** → craft 9 into **Pongonite Block**
- Lumps brew **Potions of X-Ray Vision** (see ores, mobs, lava, water)

### Dingolin (Ore)

- Yellow-green
- Hardness 150, blast resistance 3600 (3x Obsidian)
- Requires **Pongonite Pickaxe** (Diamond/Netherite won't work)
- ~7 per chunk, Y=+18 to +145 (peak +45), all biomes
- Drops **Dingolin Crystals** → smelt into **Dingolin Balls** → craft 9 into **Dingolin Block**
- All Dingolin forms work as fuel (16 items per Ball)

### Rock Vapour

- Glowing yellow, Y=-80 to -75
- Unmineable, kills in 1 second of contact
- Replaces bedrock

### Tool Progression

Diamond → Pongonite → Dingolin

## Mobs

### Lava Blobs (Passive)

- Springy lava blobs, player-sized, flat bottom/semicircular top
- Bounce across lava lake surfaces
- **Invincible** — infinite HP, can't die
- Hitting one gives you **Potion of Blobiness** (2 min): tired, can't hit passive mobs
- Daytime only (need 300C+), 50% despawn chance every 5 min when it cools
- Can be kept alive above fire in an enclosure
- Can't be bred or farmed

### Pongol Dragon (Boss)

- One per world, roams surface and sky, flies
- 20 blocks long, made of magma/lava, 200HP
- Shoots lava: 18HP (Hard) / 12HP (Normal) / 8HP (Easy)
- No healing mechanic
- Drops **Lava Gun**: same damage as dragon's attack, unlimited ammo, 4x diamond pickaxe durability

### Fishongons (Neutral)

- Lava fish, almost player-height in length, orange front/red back, spiky
- 30HP, swim 15 blocks/sec
- Spawn in all lava in Pongon
- Thorns if attacked: 11HP (Hard) / 8HP (Normal) / 5HP (Easy)
- **Rideable** with saddle — gives lava damage immunity while riding
- Bred with lava buckets
- Drop **Potions of Swim Boost** when defeated: faster swimming + lava immunity

## Key Items

- **Pongonite Lumps** — ore drop, crafting/brewing ingredient
- **Pongonite Block** — 9 lumps
- **Pongonite Pickaxe** — needed for Dingolin
- **Dingolin Crystals** — ore drop
- **Dingolin Balls** — smelted crystals, fuel (16 items)
- **Dingolin Block** — 9 balls
- **Lava Gun** — boss drop, endgame weapon
- **Potion of X-Ray Vision** — brewed from Pongonite Lumps
- **Potion of Blobiness** — inflicted by hitting Lava Blobs
- **Potion of Swim Boost** — dropped by defeated Fishongons
