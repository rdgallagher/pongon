#!/usr/bin/env python3
"""
Extract vanilla Minecraft item textures from the client jar and recolor them
to mod material palettes by hue-shifting. Stick/handle browns and black
outlines are preserved.

Usage:
  python3 scripts/recolor_from_vanilla.py
"""

import colorsys
import io
import zipfile
from pathlib import Path
from PIL import Image

JAR = Path.home() / ".gradle/caches/fabric-loom/1.21/minecraft-client.jar"
OUT = Path("src/main/resources/assets/pongon/textures")

# Diamond teal source range (used for most tools/armor)
DIAMOND_HUE_MIN = 140 / 360
DIAMOND_HUE_MAX = 200 / 360

# Each entry: (source, src_hue_min, src_hue_max, target_hue, output)
# source is either a path in the vanilla jar, or a Path for a local file.
ITEMS = [
    # --- Pongonite (diamond teal → pink-magenta 335°) ---
    ("assets/minecraft/textures/item/diamond_pickaxe.png",    DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_pickaxe.png"),
    ("assets/minecraft/textures/item/diamond_axe.png",        DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_axe.png"),
    ("assets/minecraft/textures/item/diamond_shovel.png",     DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_shovel.png"),
    ("assets/minecraft/textures/item/diamond_hoe.png",        DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_hoe.png"),
    ("assets/minecraft/textures/item/diamond_sword.png",      DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_sword.png"),
    ("assets/minecraft/textures/item/diamond_helmet.png",     DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_helmet.png"),
    ("assets/minecraft/textures/item/diamond_chestplate.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_chestplate.png"),
    ("assets/minecraft/textures/item/diamond_leggings.png",   DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_leggings.png"),
    ("assets/minecraft/textures/item/diamond_boots.png",      DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "item/pongonite_boots.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_1.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "models/armor/pongonite_layer_1.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_2.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 335/360, "models/armor/pongonite_layer_2.png"),

    # --- Dingolin tools/armor (diamond teal → yellow-green 80°) ---
    ("assets/minecraft/textures/item/diamond_pickaxe.png",    DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_pickaxe.png"),
    ("assets/minecraft/textures/item/diamond_axe.png",        DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_axe.png"),
    ("assets/minecraft/textures/item/diamond_shovel.png",     DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_shovel.png"),
    ("assets/minecraft/textures/item/diamond_hoe.png",        DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_hoe.png"),
    ("assets/minecraft/textures/item/diamond_sword.png",      DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_sword.png"),
    ("assets/minecraft/textures/item/diamond_helmet.png",     DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_helmet.png"),
    ("assets/minecraft/textures/item/diamond_chestplate.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_chestplate.png"),
    ("assets/minecraft/textures/item/diamond_leggings.png",   DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_leggings.png"),
    ("assets/minecraft/textures/item/diamond_boots.png",      DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "item/dingolin_boots.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_1.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "models/armor/dingolin_layer_1.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_2.png", DIAMOND_HUE_MIN, DIAMOND_HUE_MAX, 80/360, "models/armor/dingolin_layer_2.png"),

    # --- Dingolin blocks: Pongonite block textures recolored (→ yellow-green 80°) ---
    # pongonite_ore uses blue-purple (258–308°); pongonite_block uses pink (323–329°)
    (OUT / "block/pongonite_ore.png",   248/360, 315/360, 80/360, "block/dingolin_ore.png"),
    (OUT / "block/pongonite_block.png", 310/360, 360/360, 80/360, "block/dingolin_block.png"),

    # --- Dingolin Ball: Pongonite Lump recolored (pink 335° → yellow-green 80°) ---
    (OUT / "item/pongonite_lump.png", 310/360, 360/360, 80/360, "item/dingolin_ball.png"),

    # --- Dingolin Crystal: Amethyst Cluster recolored (purple ~260-310° → yellow-green 80°) ---
    ("assets/minecraft/textures/block/amethyst_cluster.png", 255/360, 315/360, 80/360, "item/dingolin_crystal.png"),

    # --- Pongol Forest blocks ---
    # Pongol Dirt: Soul Sand (brown ~30-40°) recolored to red-brown 16°
    ("assets/minecraft/textures/block/soul_sand.png",   18/360, 55/360, 16/360, "block/pongol_dirt.png"),
    # Pongol Log: Oak log recolored brown → orange 26°
    ("assets/minecraft/textures/block/oak_log.png",     15/360, 55/360, 26/360, "block/pongol_log.png"),
    ("assets/minecraft/textures/block/oak_log_top.png", 15/360, 55/360, 26/360, "block/pongol_log_top.png"),
    # Pongol Leaves: Cherry leaves (untinted pink ~340°) recolored to yellow 50°
    ("assets/minecraft/textures/block/cherry_leaves.png", 290/360, 360/360, 50/360, "block/pongol_leaves.png"),
]


def recolor(img: Image.Image, src_hue_min: float, src_hue_max: float, target_hue: float) -> Image.Image:
    out = img.copy().convert("RGBA")
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if src_hue_min <= h <= src_hue_max and s > 0.1:
                nr, ng, nb = colorsys.hsv_to_rgb(target_hue, s, v)
                pixels[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), a)
    return out


def main():
    with zipfile.ZipFile(JAR) as jar:
        for src, src_min, src_max, target_hue, dst in ITEMS:
            if isinstance(src, Path):
                img = Image.open(src).convert("RGBA")
                src_label = src.name
            else:
                data = jar.read(src)
                img = Image.open(io.BytesIO(data)).convert("RGBA")
                src_label = src.split("/")[-1]

            result = recolor(img, src_min, src_max, target_hue)
            out_path = OUT / dst
            out_path.parent.mkdir(parents=True, exist_ok=True)
            result.save(out_path, "PNG")
            print(f"  {src_label:40s} → {dst}")


if __name__ == "__main__":
    print(f"Extracting from {JAR.name}...\n")
    main()
    print("\nDone.")
