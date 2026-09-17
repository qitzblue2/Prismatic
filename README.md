# Prismatic

A Paper 1.21.11 plugin. A Nether Star called **Prismatic** that throws an end-rod
shockwave, and — once you feed it a **Prismatic Key** — opens into a three-page
compendium of abilities.

![textures](resourcepack/assets/prismatic/textures/item/prismatic_star_awakened.png)

---

## Build

```bash
mvn package
```

The jar lands at `target/Prismatic-1.0.0.jar`. Drop it in `plugins/` and restart.

Requires JDK 21 (Paper 1.21.x runs on 21).

## Resource pack

`resourcepack/` holds the custom textures. Zip its **contents** — `pack.mcmeta`
must sit at the root of the zip, not inside a folder:

```bash
cd resourcepack && zip -r ../Prismatic-Pack.zip . && cd ..
```

Then serve it as your server pack, or drop it in `.minecraft/resourcepacks/`.

The items point at it through the `item_model` component. Without the pack they
simply keep their vanilla Nether Star / Trial Key look — nothing breaks. Set
`item.custom-textures: false` in the config to skip the component entirely.

Textures are generated, not hand-painted pixel by pixel: edit the grids in
`tools/gen_textures.py` and re-run `python3 tools/gen_textures.py` (needs Pillow).

---

## The item

| | |
|---|---|
| **Prismatic** | Nether Star, pink→blue gradient name. Right-click for the shockwave. |
| **Prismatic Key** | Trial Key by default (`item.key-material`). The upgrade ingredient. |
| **Prismatic (awakened)** | Right-click shockwave, **shift**-right-click the compendium. |

### Upgrading

Put **Prismatic + Prismatic Key** anywhere in a crafting grid — the 2×2 inventory
grid works. It is shapeless.

A vanilla recipe can only match on *material*, so the registered recipe is just
the trigger: `CraftListener` then verifies both items really carry the Prismatic
tags before letting the result stand. A plain Nether Star and a plain Trial Key
produce nothing.

### Shockwave

Rings of end rod particles race outward, and everything caught in the radius takes:

- **Slowness III**, 10s
- **Shield broken** — the shield is destroyed in their hand, with the vanilla
  item-break sound and shards. Gone, not greyed out.
- **Shields disabled**, 8s on top, so pulling a spare shield out of the hotbar
  does not simply undo the break.

Set `shockwave.shield-break: false` to fall back to a plain timed disable, or
`shield-disable: 0` to break the held shield and nothing more.

Knockback is wired up but defaults to `0.0`, so it will not drag people out of
your combos. Turn it on in the config if you want the shove.

### The compendium

Shift-right-click the awakened star. Clicking an entry runs a gated command —
you must be holding the awakened star *and* have opened the book in the last 60s.

**Page i — Vanish.** A puff of end rod light, then Invisibility for 15s.

**Page ii — Toolkit.** Pick **one**:
- *Miner's Edge* — a pickaxe and Haste V for 60s
- *Ely-Boost* — a bare elytra with 10 uses, plus a hard shove into the air that
  starts you gliding. Your own chestplate is set aside whole — enchants, trim,
  durability — and handed straight back the instant you touch the ground, while
  the loaner elytra is removed.
- *Rainfall* — a fishing rod that calls the rain. Consumed on use by default.

**Page iii — Thunderstep.** Lightning strikes, you go spectator for 5 seconds to
climb, then drop back into survival wherever you floated to — mace in hand.

The lightning is effect-only by default, so it will not set *you* on fire. Flip
`abilities.thunderstep.lightning-damages` if you want a real bolt.

---

## Commands

| Command | |
|---|---|
| `/prismatic give [player]` | a base Prismatic |
| `/prismatic awakened [player]` | skip the upgrade |
| `/prismatic key [player]` | a Prismatic Key |
| `/prismatic reload` | re-read `config.yml` |

Permissions: `prismatic.admin` (op) for the command, `prismatic.use` (everyone)
for the abilities.

`/prismaticability` is the compendium's click target. It is gated in code and is
not meant to be typed, though it works as a fallback if book clicks misbehave on
your client.

---

## Config

Every duration in `config.yml` is in seconds; set any cooldown to `0` to remove
it. Defaults:

| | |
|---|---|
| Shockwave | 20s cooldown, 6 block radius |
| Vanish | 60s cooldown |
| Miner's Edge / Ely-Boost | 90s cooldown |
| Rainfall / Thunderstep | 120s cooldown |

---

## Notes

- Ability state is written to `state.yml`, so a restart or a logout mid-ability
  cannot strand anyone in spectator, and — more importantly — cannot swallow a
  chestplate that an Ely-Boost displaced. Both are put right on rejoin. Coming
  out of spectator inside a block nudges you upward first.
- The Prismatic star is still a Nether Star underneath, so beacons are blocked
  from eating it, and no other recipe will consume it.
- `pack.mcmeta` declares a wide `supported_formats` range. If your client calls
  the pack incompatible, bump `pack_format` to whatever your version reports.
