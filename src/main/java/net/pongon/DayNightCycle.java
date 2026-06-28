package net.pongon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.pongon.world.ModDimensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Day-night cycle for the Pongon lava ocean: the ocean surface is lava during the
 * day and freezes back to magma at night.
 *
 * Performance is critical here — an earlier version froze the server thread (bug
 * 0001) by converting a 10-block band of magma↔lava every flip. Two costs did it:
 * light-engine re-propagation on every block (magma emits 3, lava 15), and lava
 * that flowed at the coast but was never cleaned up (the freeze only re-froze still
 * lava). This version avoids both:
 *
 *   - Melt only the **top surface layer** (the only layer players see/touch) — ~10×
 *     fewer light updates than the old band.
 *   - Melt a surface column only when it is **landlocked** (no air neighbour), so the
 *     placed source lava cannot flow.
 *   - At night, freeze **all** lava in the old band back to magma (source or flowing),
 *     which both cleans up any stray flow and self-heals worlds saved by the old
 *     version.
 *   - Use quiet {@code setBlockState} flags (no neighbour-update cascade).
 *
 * IMPORTANT: blocks are NEVER modified inside the CHUNK_LOAD callback. Doing so while
 * a chunk is still generating prevents it from finishing and saving, which produces
 * void terrain. CHUNK_LOAD only records the chunk; block changes happen in
 * END_WORLD_TICK, when chunks are fully loaded and ticking.
 */
public class DayNightCycle {
    private static final Set<ChunkPos> loadedChunks = new HashSet<>();
    // Chunks awaiting a melt/freeze pass. A set so re-queuing is idempotent.
    private static final LinkedHashSet<ChunkPos> pending = new LinkedHashSet<>();
    private static Boolean lastIsDay = null;
    // Chunks converted per tick — spreads the work to avoid a lag spike when many load at once.
    private static final int CHUNKS_PER_TICK = 4;
    // The ocean surface sits at sea level; only this top layer melts to lava.
    private static final int SEA_LEVEL = 135;
    // At night, lava is cleaned out of this whole band (down 10 blocks), so any flow
    // left by the old version — or by a rare edge case — is removed, not accumulated.
    private static final int BAND_BOTTOM = SEA_LEVEL - 9;
    // Quiet update: notify clients (so it renders) but skip the neighbour-update cascade.
    private static final int SET_FLAGS = Block.NOTIFY_LISTENERS;

    public static void initialize() {
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!world.getRegistryKey().equals(ModDimensions.PONGON_WORLD)) return;
            ChunkPos pos = chunk.getPos();
            loadedChunks.add(pos);
            pending.add(pos); // convert it to the current state on a later tick
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            if (!world.getRegistryKey().equals(ModDimensions.PONGON_WORLD)) return;
            loadedChunks.remove(chunk.getPos());
        });

        ServerTickEvents.END_WORLD_TICK.register(DayNightCycle::tick);
    }

    private static void tick(ServerWorld world) {
        if (!world.getRegistryKey().equals(ModDimensions.PONGON_WORLD)) return;
        boolean isDay = PongonTime.isHot(world);

        // On a day<->night flip, re-queue every loaded chunk so the whole ocean transitions.
        if (lastIsDay == null || lastIsDay != isDay) {
            lastIsDay = isDay;
            pending.addAll(loadedChunks);
        }

        // Drain this tick's batch out of `pending` BEFORE editing any blocks.
        // convertChunk's setBlockState can load a neighbour chunk, which fires
        // CHUNK_LOAD and adds to `pending` — mutating it mid-iteration would throw
        // ConcurrentModificationException, so we must finish iterating first.
        List<ChunkPos> batch = new ArrayList<>(CHUNKS_PER_TICK);
        Iterator<ChunkPos> it = pending.iterator();
        while (batch.size() < CHUNKS_PER_TICK && it.hasNext()) {
            batch.add(it.next());
            it.remove();
        }
        for (ChunkPos pos : batch) {
            if (loadedChunks.contains(pos)) {
                convertChunk(world, pos, isDay);
            }
        }
    }

    private static void convertChunk(ServerWorld world, ChunkPos chunkPos, boolean toLava) {
        for (int x = chunkPos.getStartX(); x <= chunkPos.getEndX(); x++) {
            for (int z = chunkPos.getStartZ(); z <= chunkPos.getEndZ(); z++) {
                if (toLava) meltColumn(world, x, z);
                else freezeColumn(world, x, z);
            }
        }
    }

    /**
     * Melt the ocean-surface magma block in column (x, z) to lava — only if the column
     * is a landlocked surface, so the source lava can't flow (see class doc). Island
     * columns (no exposed surface magma) and coastal notches (an air neighbour) are
     * left alone.
     */
    private static void meltColumn(ServerWorld world, int x, int z) {
        BlockPos surface = new BlockPos(x, SEA_LEVEL, z);
        if (world.getBlockState(surface).isOf(Blocks.MAGMA_BLOCK) && isLandlockedSurface(world, x, z)) {
            world.setBlockState(surface, Blocks.LAVA.getDefaultState(), SET_FLAGS);
        }
    }

    /**
     * Freeze any lava in the band back to magma. Scans the full 10-block band (not just
     * the surface) so stray/flowing lava — including leftovers from the old version — is
     * cleaned up rather than left to tick forever.
     */
    private static void freezeColumn(ServerWorld world, int x, int z) {
        for (int y = SEA_LEVEL; y >= BAND_BOTTOM; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).isOf(Blocks.LAVA)) {
                world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState(), SET_FLAGS);
            }
        }
    }

    /**
     * True when the surface block at (x, SEA_LEVEL, z) is the exposed ocean top (air
     * above) and has no air directly beside it, so lava placed there is hemmed in by
     * solid/lava on all sides and won't flow.
     */
    private static boolean isLandlockedSurface(ServerWorld world, int x, int z) {
        if (!world.getBlockState(new BlockPos(x, SEA_LEVEL + 1, z)).isAir()) {
            return false; // covered (e.g. under an island) — not the ocean surface
        }
        for (Direction dir : Direction.Type.HORIZONTAL) {
            BlockPos neighbor = new BlockPos(x + dir.getOffsetX(), SEA_LEVEL, z + dir.getOffsetZ());
            if (world.getBlockState(neighbor).isAir()) {
                return false; // an open side — lava would flow out
            }
        }
        return true;
    }
}
