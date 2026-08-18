package net.pongon.world;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;
import net.pongon.block.ModBlocks;

/**
 * A volcano: a cone of Crushed Magma skinned in Ash, with a lava-filled crater.
 *
 * <p>Terrain shape from a feature rather than the noise router, because density
 * functions cannot read the biome — see {@code docs/adr/0002-volcanoes-as-a-placed-feature.md}.
 * Features run after surface rules, so the cone lays its own Ash skin instead of
 * inheriting the biome's.
 */
public class VolcanoFeature extends Feature<DefaultFeatureConfig> {
    /** Lowest ground level a volcano may stand on: above the magma beach, so it only builds on real land. */
    private static final int MIN_BASE_Y = 138;

    private static final int MIN_RADIUS = 10;
    private static final int MAX_RADIUS = 18;

    /** Cone height as a fraction of radius — a shield volcano's broad, low profile. */
    private static final double HEIGHT_RATIO = 0.8;
    /** Crater mouth as a fraction of radius. */
    private static final double CRATER_RATIO = 0.28;
    /** How far the crater floor sinks below the rim, as a fraction of cone height. */
    private static final double CRATER_DEPTH_RATIO = 0.4;
    /** How far the lava pool sits below the crater rim. */
    private static final int LAVA_BELOW_RIM = 2;

    /** Depth of the Ash skin over the cone's flanks. */
    private static final int ASH_SKIN_DEPTH = 2;

    private static final BlockState BODY = ModBlocks.CRUSHED_MAGMA.getDefaultState();
    private static final BlockState SKIN = ModBlocks.ASH.getDefaultState();
    private static final BlockState LAVA = Blocks.LAVA.getDefaultState();
    private static final BlockState VENT = ModBlocks.VOLCANO_VENT.getDefaultState();

    public VolcanoFeature(Codec<DefaultFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos origin = context.getOrigin();
        Random random = context.getRandom();

        int baseY = groundLevel(world, origin.getX(), origin.getZ());
        if (baseY < MIN_BASE_Y) return false;

        int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
        int coneHeight = (int) Math.round(radius * HEIGHT_RATIO);
        double craterRadius = Math.max(2.0, radius * CRATER_RATIO);
        int craterDepth = (int) Math.round(coneHeight * CRATER_DEPTH_RATIO);

        // Three sine terms at random phases break up the circle, so the rim reads as
        // a weathered cone rather than a cast one.
        double phaseA = random.nextDouble() * Math.PI * 2.0;
        double phaseB = random.nextDouble() * Math.PI * 2.0;
        double phaseC = random.nextDouble() * Math.PI * 2.0;

        int rimY = baseY + coneHeight;
        int craterFloorY = rimY - craterDepth;
        int lavaY = rimY - LAVA_BELOW_RIM;

        // The sine terms can push the edge out past the nominal radius, so scan wider
        // than it — otherwise the widest lobes get cut off square.
        int scan = (int) Math.ceil(radius * (1.0 + 0.12 + 0.08 + 0.05)) + 1;

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -scan; dx <= scan; dx++) {
            for (int dz = -scan; dz <= scan; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                double angle = Math.atan2(dz, dx);
                double edge = radius * (1.0
                        + 0.12 * Math.sin(2.0 * angle + phaseA)
                        + 0.08 * Math.sin(3.0 * angle + phaseB)
                        + 0.05 * Math.sin(5.0 * angle + phaseC));
                if (distance > edge) continue;

                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int columnBase = groundLevel(world, x, z);

                if (distance <= craterRadius) {
                    buildCrater(world, pos, x, z, columnBase, craterRadius, distance, craterFloorY, lavaY);
                } else {
                    buildFlank(world, pos, x, z, columnBase, distance, craterRadius, edge, baseY, coneHeight);
                }
            }
        }

        // The crater's centre column was filled to craterFloorY; swap its top block for
        // the vent, so the world remembers a volcano stands here and can erupt it.
        pos.set(origin.getX(), craterFloorY, origin.getZ());
        setBlockState(world, pos, VENT);
        if (world.getBlockEntity(pos) instanceof VolcanoVentBlockEntity vent) {
            // Only the feature knows the cone it just built; the vent needs the shape to
            // pour lava over the rim and to cool the streams afterwards.
            vent.setCone((int) Math.round(craterRadius), rimY, radius, baseY);
        }
        return true;
    }

    /**
     * A flank column: solid from the existing ground up to the cone's profile at this
     * distance, which falls from the rim at the crater's edge to nothing at the rim of
     * the cone itself.
     */
    private void buildFlank(StructureWorldAccess world, BlockPos.Mutable pos, int x, int z,
                            int columnBase, double distance, double craterRadius, double edge,
                            int baseY, int coneHeight) {
        double slope = 1.0 - (distance - craterRadius) / Math.max(1.0, edge - craterRadius);
        int top = baseY + (int) Math.round(coneHeight * slope);
        if (top <= columnBase) return;

        for (int y = columnBase; y <= top; y++) {
            pos.set(x, y, z);
            setBlockState(world, pos, y > top - ASH_SKIN_DEPTH ? SKIN : BODY);
        }
    }

    /**
     * A crater column: solid up to a floor that dishes down towards the centre, then
     * lava standing in the bowl.
     */
    private void buildCrater(StructureWorldAccess world, BlockPos.Mutable pos, int x, int z,
                             int columnBase, double craterRadius, double distance,
                             int craterFloorY, int lavaY) {
        double dish = distance / craterRadius;
        int floor = craterFloorY + (int) Math.round((lavaY - craterFloorY) * dish);

        for (int y = columnBase; y <= floor; y++) {
            pos.set(x, y, z);
            setBlockState(world, pos, BODY);
        }
        for (int y = floor + 1; y <= lavaY; y++) {
            pos.set(x, y, z);
            setBlockState(world, pos, LAVA);
        }
    }

    /** The top of solid ground, ignoring the lava ocean standing over it. */
    private static int groundLevel(StructureWorldAccess world, int x, int z) {
        return world.getTopY(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
    }
}
