package net.pongon;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.pongon.world.ModDimensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Day-night cycle for the Pongon ocean floor: the surface magma layer melts to
 * lava during the day and freezes back to magma at night.
 *
 * IMPORTANT: blocks are NEVER modified inside the CHUNK_LOAD callback. Doing so
 * while a chunk is still generating prevents it from finishing and saving, which
 * produces void terrain. Instead CHUNK_LOAD only records the chunk, and all block
 * changes happen later in END_WORLD_TICK, when chunks are fully loaded and ticking.
 */
public class DayNightCycle {
    private static final Set<ChunkPos> loadedChunks = new HashSet<>();
    // Chunks awaiting a melt/freeze pass. A set so re-queuing is idempotent.
    private static final LinkedHashSet<ChunkPos> pending = new LinkedHashSet<>();
    private static Boolean lastIsDay = null;
    // Chunks converted per tick — spreads the work to avoid a lag spike when many load at once.
    private static final int CHUNKS_PER_TICK = 4;
    // Only the flat ocean-floor magma cycles: the topmost solid block sits at sea
    // level, with a 10-block magma layer beneath it. Restricting to this band keeps
    // the magma cladding the island slopes (above sea level) untouched.
    private static final int SEA_LEVEL = 135;
    private static final int FLOOR_BOTTOM = SEA_LEVEL - 9; // 10-block layer, inclusive

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
        boolean isDay = world.getTimeOfDay() % 24000L < 12000L;

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
     * Melt the ocean-floor magma layer in column (x, z) to lava. Only the fixed
     * sea-level band is touched, and only magma converts — so island columns (solid
     * crushed magma through this band) and slope cladding (above sea level) are left
     * alone.
     */
    private static void meltColumn(ServerWorld world, int x, int z) {
        for (int y = SEA_LEVEL; y >= FLOOR_BOTTOM; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (world.getBlockState(pos).isOf(Blocks.MAGMA_BLOCK)) {
                world.setBlockState(pos, Blocks.LAVA.getDefaultState());
            }
        }
    }

    /** Freeze the melted lava in the sea-level band back to magma. */
    private static void freezeColumn(ServerWorld world, int x, int z) {
        for (int y = SEA_LEVEL; y >= FLOOR_BOTTOM; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            FluidState fluid = world.getFluidState(pos);
            if (fluid.isOf(Fluids.LAVA) && fluid.isStill()) {
                world.setBlockState(pos, Blocks.MAGMA_BLOCK.getDefaultState());
            }
        }
    }
}
