#!/usr/bin/env python3
"""Generates the Prismatic resource pack textures.

Pure pixel art: hand-authored 16x16 grids, coloured with three-tone ramps.
Re-run after editing a grid:  python3 tools/gen_textures.py
"""
from PIL import Image
import math, os, sys

S = 16

# Three-tone ramps per hue. Shading never multiplies toward black - that goes muddy
# on pastels - it steps along a ramp instead.
PINK_HI, PINK_MID, PINK_SH = (0xFF, 0xDA, 0xF1), (0xFF, 0x8F, 0xD4), (0xE0, 0x5F, 0xB0)
BLUE_HI, BLUE_MID, BLUE_SH = (0xD8, 0xEF, 0xFF), (0x6F, 0xBC, 0xFF), (0x3F, 0x8D, 0xDB)
WHITE = (0xFF, 0xFF, 0xFF)

STAR = [
    ".......##.......",
    ".......##.......",
    ".......##.......",
    "......####......",
    "......####......",
    ".....######.....",
    "...##########...",
    "################",
    "################",
    "...##########...",
    ".....######.....",
    "......####......",
    "......####......",
    ".......##.......",
    ".......##.......",
    ".......##.......",
]

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


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def contrast(t):
    """Push the gradient to its ends so pink reads pink and blue reads blue."""
    return max(0.0, min(1.0, (t - 0.22) / 0.56))


def ramp(t):
    return lerp(PINK_HI, BLUE_HI, t), lerp(PINK_MID, BLUE_MID, t), lerp(PINK_SH, BLUE_SH, t)


def render(grid, tfun, core=0.0):
    mask = [[c == "#" for c in row] for row in grid]
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    px = img.load()
    cx = cy = (S - 1) / 2.0
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
            colour = hi if (not up and not left) else sh if (not dn and not rt) else mid
            if core > 0:
                d = math.hypot(x - cx, y - cy)
                if d <= core:
                    colour = lerp(colour, WHITE, (1.0 - d / core) ** 0.7)
            px[x, y] = (*colour, 255)
    return img


def diag(x, y):      # pink top-left -> blue bottom-right
    return contrast(((x / (S - 1)) + (y / (S - 1))) / 2.0)


def vertical(x, y):  # pink bow -> blue teeth
    return contrast((y - 1) / 13.0)


def main():
    out = os.path.join(os.path.dirname(__file__), os.pardir,
                       "resourcepack/assets/prismatic/textures/item")
    out = os.path.abspath(out)
    os.makedirs(out, exist_ok=True)

    star = render(STAR, diag, core=2.6)
    star.save(os.path.join(out, "prismatic_star.png"))

    awakened = render(STAR, diag, core=3.6)
    px = awakened.load()
    for x, y, colour in [(1, 1, WHITE), (14, 14, WHITE),
                         (14, 2, PINK_MID), (1, 13, BLUE_MID)]:
        if px[x, y][3] == 0:
            px[x, y] = (*colour, 255)
    awakened.save(os.path.join(out, "prismatic_star_awakened.png"))

    key = render(KEY, vertical)
    key.save(os.path.join(out, "prismatic_key.png"))

    pack = os.path.abspath(os.path.join(out, "../../../..", "pack.png"))
    awakened.resize((128, 128), Image.NEAREST).save(pack)

    if "--preview" in sys.argv:
        sheet = Image.new("RGBA", (S * 3 + 8, S), (0, 0, 0, 0))
        for i, im in enumerate((star, awakened, key)):
            sheet.paste(im, (i * (S + 4), 0))
        sheet.resize((sheet.width * 20, sheet.height * 20), Image.NEAREST).save(
            os.path.join(os.path.dirname(os.path.abspath(__file__)), "preview.png"))
    print("textures written to", out)


if __name__ == "__main__":
    main()
