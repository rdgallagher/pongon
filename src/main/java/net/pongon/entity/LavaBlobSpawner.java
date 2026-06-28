package net.pongon.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.pongon.PongonTime;
import net.pongon.world.ModDimensions;

/**
 * Reliable, capped daytime spawner for wild Lava Blobs.
 *
 * Vanilla creature-group spawning populates almost entirely at chunk generation and
 * is far too sparse for a mob meant to roam the surface in numbers. Instead, while
 * it is daytime in Pongon, this periodically tops up the blobs around each player up
 * to a cap — so they're a common sight near you, but can't run away and lag the
 * server (the bug-0001 lesson). Night-time despawn (in LavaBlobEntity) thins them
 * back out.
 */
public class LavaBlobSpawner {
    private static final int ATTEMPT_INTERVAL = 100;  // try every 5 s
    private static final int NEARBY_RADIUS = 64;      // cap is measured within this of a player
    private static final int MAX_NEARBY = 10;         // don't exceed this many per player
    private static final int RING_MIN = 16;           // spawn this far from the player...
    private static final int RING_MAX = 40;           // ...out to here
    private static final int GROUP_MIN = 2;
    private static final int GROUP_MAX = 3;

    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(LavaBlobSpawner::tick);
    }

    private static void tick(ServerWorld world) {
        if (!world.getRegistryKey().equals(ModDimensions.PONGON_WORLD)) return;
        if (world.getTime() % ATTEMPT_INTERVAL != 0) return;
        if (!PongonTime.isHot(world)) return; // daytime / 300C+ only
        for (ServerPlayerEntity player : world.getPlayers()) {
            trySpawnNear(world, player);
        }
    }

    private static void trySpawnNear(ServerWorld world, ServerPlayerEntity player) {
        Box nearby = player.getBoundingBox().expand(NEARBY_RADIUS);
        if (world.getEntitiesByType(ModEntities.LAVA_BLOB, nearby, e -> true).size() >= MAX_NEARBY) {
            return;
        }

        Random rng = world.getRandom();
        double angle = rng.nextDouble() * Math.PI * 2.0;
        int dist = RING_MIN + rng.nextInt(RING_MAX - RING_MIN + 1);
        int x = MathHelper.floor(player.getX() + Math.cos(angle) * dist);
        int z = MathHelper.floor(player.getZ() + Math.sin(angle) * dist);
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);

        // Valid surface: ground (land or lava) below, clear space to stand.
        if (world.getBlockState(pos.down()).isAir()) return;
        if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.up()).isAir()) return;

        int group = GROUP_MIN + rng.nextInt(GROUP_MAX - GROUP_MIN + 1);
        for (int i = 0; i < group; i++) {
            LavaBlobEntity blob = ModEntities.LAVA_BLOB.create(world);
            if (blob == null) continue;
            blob.refreshPositionAndAngles(x + 0.5, y, z + 0.5, rng.nextFloat() * 360f, 0f);
            blob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null);
            world.spawnEntity(blob);
        }
    }
}
