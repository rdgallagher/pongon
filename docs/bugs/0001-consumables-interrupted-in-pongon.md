# Bug 0001 — Eating/drinking is interrupted (very slow to complete) in the Pongon dimension

Status: **open — narrowed to use-action interruption (not low TPS)**

## Findings

- **Server runs at a full 20 TPS in Pongon, same as the Overworld.** This rules
  out cause (1) below — it is not a uniform tick-rate slowdown. The use action is
  therefore being **periodically cancelled and restarted** (cause 2), which also
  matches "the item goes back into your hand, then the munching continues."
  Remaining work: find what recurringly cancels the active item.

## Symptoms

- Consuming an item in Pongon (enchanted golden apple, potion) takes **much
  longer than in the Overworld**, but does eventually complete.
- The use animation **only advances while right-click is held**, and partway
  through "the item goes back into your hand" and the munching restarts.
- Hold long enough and the item is finally consumed, the effect applies, and the
  next item is drawn from the stack.
- Confirmed in **Survival** (so it is not the Creative "stack never depletes +
  held button + invisible health effects" artifact we first suspected).

## Interpretation

The action is **not blocked** — it is being **interrupted and restarted
repeatedly**. The item only consumes once the player gets a long-enough
uninterrupted window (~32 ticks for food). So the question is: what, in Pongon
specifically, periodically interrupts the use action or stretches game ticks?

Two broad classes of cause, not yet distinguished:

1. **Reduced server tick rate (low TPS / high MSPT).** If the Pongon dimension
   runs below 20 TPS, every tick-based action — including the 32-tick eat —
   stretches out in real time. A uniform slowdown.
2. **Periodic interruption of the use action.** Something near the player
   recurringly cancels the active item (e.g. block-update churn re-syncing the
   client, or recurring damage), resetting the eat timer so it must start over.

**Leading hypothesis (after TPS was ruled out): recurring magma damage.** The
Pongon surface is magma everywhere, and a player standing on/beside a magma block
in Survival takes damage on a cadence. If taking damage cancels the active item,
each hit restarts the eat, and it only completes during a gap — which matches the
symptom exactly and is consistent with full 20 TPS. The first test below isolates
this.

## Suspects (Pongon-specific systems)

- **`DayNightCycle` chunk conversion** (`src/main/java/net/pongon/DayNightCycle.java`):
  - Every `CHUNK_LOAD` queues the chunk; `convertChunk` scans 256 columns ×
    10 blocks (~2560 `getBlockState`) per chunk, 4 chunks/tick. While the player
    moves and chunks stream in, this runs continuously near the player.
  - `meltColumn` calls `setBlockState(Blocks.LAVA.getDefaultState())`. Even though
    that is a *source* state, each placement schedules fluid updates; at island
    coastlines the lava flows and keeps ticking.
  - On every day↔night flip the **entire** `loadedChunks` set is re-queued at once.
  - Note: in a stationary, fully-loaded area with no flip, `pending` is empty and
    this system does almost nothing — so if the slowdown persists *there*, the
    cycle is probably **not** the cause.
- **Lava-ocean fluid ticking (dimension data).** The melted ocean band is a large
  expanse of lava; flowing lava (esp. along coastlines) generates sustained fluid
  ticks independent of the mod's Java.

## Discriminating experiments (run in-game, report results)

Do these standing in Pongon, ideally at a fixed spot:

0. **DONE — Measure MSPT/TPS.** Pongon ran at a full 20 TPS, same as the
   Overworld. Cause (1) ruled out.
1. **Eat away from magma/lava (isolates the leading hypothesis).** Stand deep
   inland on solid Pongol Dirt with no magma or lava underfoot or adjacent (or, in
   Survival, hover/stand on a placed safe block), then eat. Completes normally ⇒
   it is recurring environmental damage from magma/lava cancelling the use, *not*
   an item or dimension-data bug — and the real fix is the planned heat/standing
   protection, not item logic.
2. **Day vs night.** Compare eat speed during the Pongon **day** (ocean is lava)
   vs **night** (ocean is solid magma). Faster at night ⇒ the lava / melt
   conversion is the cost driver.
3. **Stationary + long-loaded vs just-arrived.** Eat after standing still a while
   in explored terrain, vs right after flying into fresh terrain (chunks loading
   and converting). Slower only while chunks stream in ⇒ points at `convertChunk`.
4. **Decisive: disable the cycle.** Temporarily short-circuit
   `DayNightCycle.tick` (early return) or skip `DayNightCycle.initialize()`,
   rebuild, and test. Normal eat speed ⇒ the cycle is the cause. Still slow ⇒ it
   is the dimension's lava fluid ticking or something else.

## Candidate fixes (do NOT implement until a cause is confirmed)

- If conversion churn: only convert chunks **on a day/night flip**, not on every
  `CHUNK_LOAD` (a freshly loaded chunk is already saved in the correct state); or
  skip the column scan when nothing in the band can change.
- If flowing lava: place a lava state that will not flow, or only melt interior
  columns, or reduce the melted band.
- If low TPS generally: profile with `/debug` (server pie chart) to find the hot
  path before optimising.
