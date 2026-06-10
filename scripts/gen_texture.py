#!/usr/bin/env python3
"""Generate a Minecraft block texture via Replicate retro-diffusion/rd-tile."""

import json
import os
import sys
import time
import urllib.request
from pathlib import Path

try:
    from PIL import Image
    import io
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

MODEL_VERSION = "c4f59be396f222aa021dec778f1cb49bdc4434ea4727bc084d2baae1447ed1b7"
API_BASE = "https://api.replicate.com/v1"
TOKEN = os.environ["REPLICATE_API_TOKEN"]
HEADERS = {"Authorization": f"Token {TOKEN}", "Content-Type": "application/json"}


def api_get(url):
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())


def api_post(url, data):
    body = json.dumps(data).encode()
    req = urllib.request.Request(url, data=body, headers=HEADERS, method="POST")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())


def apply_circle_mask(path):
    """Replace pixels outside the inscribed circle with transparency."""
    if not HAS_PIL:
        print("Pillow not available, skipping circle mask")
        return
    img = Image.open(path).convert("RGBA")
    w, h = img.size
    cx, cy, r = w / 2, h / 2, min(w, h) / 2
    pixels = img.load()
    for y in range(h):
        for x in range(w):
            if (x - cx + 0.5) ** 2 + (y - cy + 0.5) ** 2 > r ** 2:
                pixels[x, y] = (0, 0, 0, 0)
    img.save(path, "PNG")
    print(f"Circle mask applied to {path}")


def generate(prompt, output_path, width=16, height=16, circle_mask=False):
    print(f"Prompt : {prompt}")
    print(f"Size   : {width}x{height}")
    print(f"Output : {output_path}")
    print()

    print("Creating prediction...")
    pred = api_post(f"{API_BASE}/predictions", {
        "version": MODEL_VERSION,
        "input": {
            "style": "single_tile",
            "prompt": prompt,
            "width": width,
            "height": height,
            "num_images": 1,
        },
    })
    pred_id = pred["id"]
    print(f"Prediction ID: {pred_id}")

    print("Waiting", end="", flush=True)
    while True:
        time.sleep(2)
        result = api_get(f"{API_BASE}/predictions/{pred_id}")
        status = result["status"]
        print(".", end="", flush=True)
        if status == "succeeded":
            break
        if status in ("failed", "canceled"):
            print(f"\nFailed: {result.get('error')}")
            sys.exit(1)
    print(" done")

    output_url = result["output"][0]
    print(f"Downloading {output_url}")
    Path(output_path).parent.mkdir(parents=True, exist_ok=True)
    urllib.request.urlretrieve(output_url, output_path)
    print(f"Saved to {output_path}")
    if circle_mask:
        apply_circle_mask(output_path)


if __name__ == "__main__":
    if len(sys.argv) >= 3:
        circle = "--circle" in sys.argv
        args = [a for a in sys.argv[1:] if not a.startswith("--")]
        generate(prompt=args[0], output_path=args[1], circle_mask=circle)
    else:
        generate(
            prompt="dark compacted volcanic magma, muted orange lava glow in cracks, rough stone surface, darker than magma block",
            output_path="src/main/resources/assets/pongon/textures/block/crushed_magma.png",
        )
