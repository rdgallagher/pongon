# Bug 0001 — Eating/drinking is interrupted (very slow to complete) in the Pongon dimension

Status: **FIXED (pending in-game verification) — root cause was the day/night lava
cycle freezing the server thread. Disabling it confirmed the diagnosis (lag + mining
bug gone); the cycle has now been redesigned and re-enabled (option 1 below).**

## Root cause (found)

`DayNightCycle` melts/freezes the 10-block sea-level magma band by calling
`setBlockState` on up to 2560 blocks per chunk with default flags. Two compounding
costs freeze the **server thread** for tens of seconds at a time:

1. **Light-engine thrashing.** Magma Block emits light 3, Lava emits 15. Every
   conversion toggles a light source by 12 levels, forcing the light engine to
   re-propagate over a ~15-block radius — thousands of times per day/night flip.
2. **A flowing-lava ratchet.** The placed lava flows at island edges / the magma
   beach (lava next to air). But `freezeColumn` only re-freezes **still** lava
   (`fluid.isStill()`), so flowing lava is **never cleaned up**. Each day adds more
   permanently-flowing lava → ever-growing fluid-tick load.

This is the same bug as the earlier "consumables never finish": a frozen server
thread resets the player's in-progress actions (eating, **and block-breaking** —
which is why it also reproduces in the Overworld; it is one integrated server
thread, so Pongon's stall lags every dimension).

### Evidence

- `run/logs/latest.log`: repeated `Can't keep up! Running 22000–38000ms or
  440–777 ticks behind` — **single-tick freezes of 22–38 s**, not steady slowness
  (so the earlier F3 "20 TPS" read, taken between stalls, looked fine).
- The **first stall hits 2 s after the player teleported into Pongon** (to
  `0.5, 145, 0.5`), and stalls then recur continuously (~40 s apart), consistent
  with the flowing-lava ratchet, not just the day/night flips.

### Resolution

- **Confirmed:** disabling `DayNightCycle` removed the lag and the mining/eating bug
  in-game.
- **Fixed:** `DayNightCycle` redesigned per option 1 below and re-enabled — melt only
  the landlocked ocean **surface** layer (no flow, ~10× fewer light updates), freeze
  the whole band back to magma at night (cleans stray/flowing lava and self-heals old
  saves), and use quiet `setBlockState` flags.
- **Pending:** in-game verification that the surface still toggles lava↔magma with no
  "Can't keep up" stalls (watch `run/logs/latest.log`).

## Findings (earlier)

- **Server runs at a full 20 TPS in Pongon, same as the Overworld.** Superseded:
  this was a between-stalls reading. The freezes are intermittent multi-second
  single-tick stalls, which an instantaneous TPS read misses.

## Symptoms

- Consuming an item in Pongon (enchanted golden apple, potion) takes **much
  longer than in the Overworld**, but does eventually complete.
- The use animation **only advances while right-click is held**, and partway
  through "the item goes back into your hand" and the munching restarts.
- Hold long enough and the item is finally consumed, the effect applies, and the
  next item is drawn from the stack.
- Confirmed in **Survival** (so it is not the Creative "stack never depletes +
  held button + invisible health effects" artifact we first suspected).

## Redesign options (for the proper fix, once the diagnosis is confirmed)

The goal: keep "the ocean surface is lava by day, magma by night" without freezing
the server. Each option attacks the two costs (light thrashing + flowing-lava
ratchet). They can be combined.

1. **Surface-only + landlocked, source lava, cheap flags.**
   - Convert only the **top layer** (y = sea level), not a 10-block band — 10× less
     work, and players only ever see/touch the surface.
   - Only convert columns that are **fully surrounded by ocean** (not the magma
     beach), so the placed lava is landlocked source lava that never flows — kills
     the ratchet.
   - `setBlockState` with `NOTIFY_LISTENERS | FORCE_STATE` (skip neighbor-update
     cascade). Freeze **all** lava in range (drop the `isStill()` check) as a
     belt-and-braces cleanup.
   - Light thrashing remains (magma 3 ↔ lava 15) but over 1 layer of landlocked
     columns it is far smaller.

2. **No real fluid — a custom "hot magma" surface block.**
   - Add a block that *looks* like lava and burns on contact but is a normal solid
     block (no fluid ticks, no flow). Swap magma ↔ this block by day/night. Removes
     the fluid cost entirely; loses real swimmable lava on the surface.

3. **Visual-only day/night (no block swaps).**
   - Drive the day/night look through a biome/shader tint or block emissive state
     rather than physically swapping blocks. Cheapest by far; changes the feel
     (you wouldn't actually walk on solid magma at night).

Recommendation: **option 1** — keeps the real mechanic, removes both cost drivers,
smallest change to the existing code.

## Historical: earlier suspects & experiments (pre-root-cause)

These were the working hypotheses before the log analysis above pinned the cause;
kept for context. The "magma damage" lead turned out not to be it.

- **`DayNightCycle` chunk conversion** — confirmed the culprit (see Root cause).
- **Recurring magma damage** — once-leading hypothesis for the consumable symptom;
  not the cause.
- The discriminating experiments (measure MSPT, day vs night, disable the cycle)
  led here; experiment 4 (disable the cycle) is now done in code.
