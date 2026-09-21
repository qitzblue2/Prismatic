#!/usr/bin/env python3
"""Generates the Prismatic Key texture.

The two star textures are supplied artwork and are checked in as-is - this
script must never touch them. Only the key is generated here: hand-authored
16x16 pixel grid, coloured with three-tone ramps.

    python3 tools/gen_textures.py [--preview]
"""
from PIL import Image
import os
import sys

S = 16

# Three-tone ramps per hue. Shading never multiplies toward black - that goes
# muddy on pastels - it steps along a ramp instead.
PINK_HI, PINK_MID, PINK_SH = (0xFF, 0xDA, 0xF1), (0xFF, 0x8F, 0xD4), (0xE0, 0x5F, 0xB0)
BLUE_HI, BLUE_MID, BLUE_SH = (0xD8, 0xEF, 0xFF), (0x6F, 0xBC, 0xFF), (0x3F, 0x8D, 0xDB)

KEY = [
    "................",
    "......####......",
    ".....######.....",
    "....###..###....",
    "....##....##....",
    "....###..###....",
    ".....######.....",
    "......####......",
    ".......##.......",
    ".......##.......",
    ".......##.......",
    ".......####.....",
    ".......##.......",
    ".......####.....",
    ".......##.......",
    "................",
]

# Textures owned by the artwork, not by this script.
SUPPLIED = ("prismatic_star.png", "prismatic_star_awakened.png")


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def contrast(t):
    """Push the gradient to its ends so pink reads pink and blue reads blue."""
    return max(0.0, min(1.0, (t - 0.22) / 0.56))


def ramp(t):
    return lerp(PINK_HI, BLUE_HI, t), lerp(PINK_MID, BLUE_MID, t), lerp(PINK_SH, BLUE_SH, t)


def render(grid, tfun):
    mask = [[c == "#" for c in row] for row in grid]
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    px = img.load()
    for y in range(S):
        for x in range(S):
            if not mask[y][x]:
                continue
            hi, mid, sh = ramp(tfun(x, y))
            # Only true corners get lit or darkened; on 2px-wide arms every pixel
            # is an "edge", and shading them all bleaches the whole sprite.
            up   = y > 0     and mask[y - 1][x]
            left = x > 0     and mask[y][x - 1]
            dn   = y < S - 1 and mask[y + 1][x]
            rt   = x < S - 1 and mask[y][x + 1]
            px[x, y] = (*(hi if (not up and not left)
                          else sh if (not dn and not rt)
                          else mid), 255)
    return img


def main():
    out = os.path.abspath(os.path.join(
        os.path.dirname(__file__), os.pardir,
        "resourcepack/assets/prismatic/textures/item"))
    os.makedirs(out, exist_ok=True)

    rows = [y for y, r in enumerate(KEY) if "#" in r]
    top, bottom = min(rows), max(rows)
    key = render(KEY, lambda x, y: contrast((y - top) / (bottom - top)))
    key.save(os.path.join(out, "prismatic_key.png"))
    print("wrote prismatic_key.png ->", out)
    print("left untouched (supplied artwork):", ", ".join(SUPPLIED))

    if "--preview" in sys.argv:
        tiles = [key] + [Image.open(os.path.join(out, n)).convert("RGBA") for n in SUPPLIED]
        sheet = Image.new("RGBA", (S * len(tiles) + 4 * (len(tiles) - 1), S), (0, 0, 0, 0))
        for i, tile in enumerate(tiles):
            sheet.paste(tile, (i * (S + 4), 0))
        path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "preview.png")
        sheet.resize((sheet.width * 20, sheet.height * 20), Image.NEAREST).save(path)
        print("preview ->", path)


if __name__ == "__main__":
    main()
