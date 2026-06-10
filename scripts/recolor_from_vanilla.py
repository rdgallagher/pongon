#!/usr/bin/env python3
"""
Extract vanilla Minecraft item textures from the client jar and recolor them
to the Pongonite palette by hue-shifting the diamond teal (169°) to
pongonite pink (335°). Stick/handle browns and black outlines are preserved.

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

# Source hue range to replace (diamond teal)
SRC_HUE_MIN = 140 / 360
SRC_HUE_MAX = 200 / 360

# Target hue (pongonite pink-magenta, sampled from pongonite_lump)
TARGET_HUE = 335 / 360

ITEMS = [
    # (vanilla_path_in_jar, output_path_under textures/)
    ("assets/minecraft/textures/item/diamond_pickaxe.png",  "item/pongonite_pickaxe.png"),
    ("assets/minecraft/textures/item/diamond_axe.png",      "item/pongonite_axe.png"),
    ("assets/minecraft/textures/item/diamond_shovel.png",   "item/pongonite_shovel.png"),
    ("assets/minecraft/textures/item/diamond_hoe.png",      "item/pongonite_hoe.png"),
    ("assets/minecraft/textures/item/diamond_sword.png",    "item/pongonite_sword.png"),
    ("assets/minecraft/textures/item/diamond_helmet.png",   "item/pongonite_helmet.png"),
    ("assets/minecraft/textures/item/diamond_chestplate.png", "item/pongonite_chestplate.png"),
    ("assets/minecraft/textures/item/diamond_leggings.png", "item/pongonite_leggings.png"),
    ("assets/minecraft/textures/item/diamond_boots.png",    "item/pongonite_boots.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_1.png", "models/armor/pongonite_layer_1.png"),
    ("assets/minecraft/textures/models/armor/diamond_layer_2.png", "models/armor/pongonite_layer_2.png"),
]


def recolor(img: Image.Image) -> Image.Image:
    out = img.copy().convert("RGBA")
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if SRC_HUE_MIN <= h <= SRC_HUE_MAX and s > 0.1:
                nr, ng, nb = colorsys.hsv_to_rgb(TARGET_HUE, s, v)
                pixels[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), a)
    return out


def main():
    with zipfile.ZipFile(JAR) as jar:
        for src, dst in ITEMS:
            data = jar.read(src)
            img = Image.open(io.BytesIO(data)).convert("RGBA")
            result = recolor(img)
            out_path = OUT / dst
            out_path.parent.mkdir(parents=True, exist_ok=True)
            result.save(out_path, "PNG")
            print(f"  {src.split('/')[-1]:35s} → {dst}")


if __name__ == "__main__":
    print(f"Extracting from {JAR.name} and recoloring to pongonite pink ({TARGET_HUE*360:.0f}°)...\n")
    main()
    print("\nDone.")
