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
    # Pongol Dirt: Coarse Dirt (brown ~25-40°, no soul-sand faces) recolored to a vivid
    # warm range — shadows red, midtones orange, highlights yellow — with boosted
    # saturation so it reads hot rather than dull brown.
    ("assets/minecraft/textures/block/coarse_dirt.png", 15/360, 55/360, 14/360, "block/pongol_dirt.png",
     {"sat_mult": 2.2, "sat_floor": 0.65, "warm_range": (6/360, 48/360)}),
    # Pongol Log: Oak log recolored to orange 26° with boosted saturation (a plain
    # hue-shift on the desaturated bark just read as brown).
    ("assets/minecraft/textures/block/oak_log.png",     15/360, 55/360, 26/360, "block/pongol_log.png",
     {"sat_mult": 1.8, "sat_floor": 0.9, "val_mult": 1.45, "val_floor": 0.62}),
    ("assets/minecraft/textures/block/oak_log_top.png", 15/360, 55/360, 26/360, "block/pongol_log_top.png",
     {"sat_mult": 1.8, "sat_floor": 0.9, "val_mult": 1.45, "val_floor": 0.62}),
    # Pongol Planks: Oak planks recolored to match the orange logs.
    ("assets/minecraft/textures/block/oak_planks.png",  15/360, 55/360, 26/360, "block/pongol_planks.png",
     {"sat_mult": 1.8, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.55}),
    # Pongol Door / Trapdoor: Oak equivalents recolored to match the planks.
    ("assets/minecraft/textures/block/oak_door_top.png",    15/360, 55/360, 26/360, "block/pongol_door_top.png",
     {"sat_mult": 1.8, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.55}),
    ("assets/minecraft/textures/block/oak_door_bottom.png", 15/360, 55/360, 26/360, "block/pongol_door_bottom.png",
     {"sat_mult": 1.8, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.55}),
    ("assets/minecraft/textures/block/oak_trapdoor.png",    15/360, 55/360, 26/360, "block/pongol_trapdoor.png",
     {"sat_mult": 1.8, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.55}),
    ("assets/minecraft/textures/item/oak_door.png",         15/360, 55/360, 26/360, "item/pongol_door.png",
     {"sat_mult": 1.8, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.55}),
    # Pongol Leaves: Cherry leaves recolored across the whole texture into an orange→
    # yellow range (shadows orange, highlights yellow). Recoloring every pixel (not
    # just the pink band) is what kills the leftover yellow-green pixels that were
    # reading as green in-game (the block has no tint provider, so the texture shows
    # as-is).
    ("assets/minecraft/textures/block/cherry_leaves.png", 290/360, 360/360, 50/360, "block/pongol_leaves.png",
     {"sat_mult": 2.0, "sat_floor": 0.85, "val_mult": 1.35, "val_floor": 0.7, "warm_range": (20/360, 38/360)}),

    # Rock Vapor: Bedrock (greyscale) recolored to a bright glowing yellow gas. The
    # tight warm_range colorises the otherwise-grey source (the saturation floor does
    # the work) and lifts brightness so it reads as hot, glowing vapor.
    ("assets/minecraft/textures/block/bedrock.png", 0/360, 360/360, 52/360, "block/rock_vapor.png",
     {"sat_mult": 3.0, "sat_floor": 0.85, "val_mult": 1.4, "val_floor": 0.7, "alpha": 140,
      "warm_range": (40/360, 50/360)}),
]


def recolor(img: Image.Image, src_hue_min: float, src_hue_max: float, target_hue: float,
            sat_mult: float = 1.0, sat_floor: float = 0.0,
            val_mult: float = 1.0, val_floor: float = 0.0,
            alpha: int | None = None,
            warm_range: tuple | None = None) -> Image.Image:
    """Hue-shift the matching pixels of `img`.

    By default every matching pixel is mapped to `target_hue`, keeping its
    saturation and value. Optional tweaks:
      - sat_mult / sat_floor: scale (then floor) saturation, to make a washed-out
        source read vividly.
      - val_mult / val_floor: scale (then floor) brightness, to lift shadows so the
        result looks luminous / self-lit ("glowing") rather than flat.
      - alpha: if set, override the output alpha of every recolored pixel (0-255),
        for a translucent result. Fully transparent source pixels stay transparent.
      - warm_range = (hue_dark, hue_light): instead of a single hue, map hue across
        the brightness of the matching pixels — darkest → hue_dark, lightest →
        hue_light — so one texture spans a range (e.g. red → orange → yellow).
    """
    out = img.copy().convert("RGBA")
    pixels = out.load()

    # warm_range is a deliberate full-texture restyle, so it recolors every opaque
    # pixel (including desaturated grey specks); the plain hue-shift only touches
    # pixels already within the source hue band.
    def matches(h, s):
        if warm_range is not None:
            return True
        return src_hue_min <= h <= src_hue_max and s > 0.1

    # For warm_range we stretch the hue across the actual brightness span of the
    # matching pixels, so the full red→yellow range is used regardless of source.
    v_lo, v_hi = 0.0, 1.0
    if warm_range is not None:
        vs = []
        for y in range(out.height):
            for x in range(out.width):
                r, g, b, a = pixels[x, y]
                if a == 0:
                    continue
                h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                if matches(h, s):
                    vs.append(v)
        if vs:
            v_lo, v_hi = min(vs), max(vs)

    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if not matches(h, s):
                continue
            if warm_range is not None:
                hue_dark, hue_light = warm_range
                t = (v - v_lo) / (v_hi - v_lo) if v_hi > v_lo else 0.5
                new_h = hue_dark + (hue_light - hue_dark) * t
            else:
                new_h = target_hue
            new_s = min(1.0, max(sat_floor, s * sat_mult))
            new_v = min(1.0, max(val_floor, v * val_mult))
            nr, ng, nb = colorsys.hsv_to_rgb(new_h, new_s, new_v)
            new_a = alpha if alpha is not None else a
            pixels[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), new_a)
    return out


def main():
    with zipfile.ZipFile(JAR) as jar:
        for entry in ITEMS:
            src, src_min, src_max, target_hue, dst = entry[:5]
            opts = entry[5] if len(entry) > 5 else {}
            if isinstance(src, Path):
                img = Image.open(src).convert("RGBA")
                src_label = src.name
            else:
                data = jar.read(src)
                img = Image.open(io.BytesIO(data)).convert("RGBA")
                src_label = src.split("/")[-1]

            result = recolor(img, src_min, src_max, target_hue, **opts)
            out_path = OUT / dst
            out_path.parent.mkdir(parents=True, exist_ok=True)
            result.save(out_path, "PNG")
            print(f"  {src_label:40s} → {dst}")


if __name__ == "__main__":
    print(f"Extracting from {JAR.name}...\n")
    main()
    print("\nDone.")
