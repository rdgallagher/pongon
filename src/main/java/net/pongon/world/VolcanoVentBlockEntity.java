package net.pongon.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.pongon.DayNightCycle;
import net.pongon.PongonTime;
import net.pongon.block.ModBlocks;
import net.pongon.entity.LavaBombEntity;

/**
 * Drives one volcano's eruptions. Rolls once at each Pongon dawn; on a hit the
 * volcano erupts for well under a minute — a sharp, violent burst.
 *
 * <p>Only ticks while its chunk is loaded, so volcanoes nobody is near never erupt.
 * That is deliberate: an eruption with no witness is only a lag cost.
 */
public class VolcanoVentBlockEntity extends BlockEntity {
    private static final double ERUPTION_CHANCE = 0.2;
    private static final int ERUPTION_TICKS = 700;

    /** How far above the vent to look for open air to erupt through. */
    private static final int VENT_SEARCH_HEIGHT = 24;

    /** Average ticks between lava bombs, so an eruption throws roughly twenty. */
    private static final int TICKS_PER_BOMB = 30;
    private static final double BOMB_LAUNCH_SPEED = 0.85;
    private static final double BOMB_LAUNCH_SPEED_VARIANCE = 0.45;
    /** Sideways kick. Kept low so bombs rain onto the cone and its skirt, not the whole biome. */
    private static final double BOMB_SPREAD = 0.30;

    /** Ticks between lava welling over the rim. */
    private static final int TICKS_PER_STREAM = 60;

    /** Height of the ash column thrown above the crater. */
    private static final int PLUME_HEIGHT = 18;
    /** Ticks between ash settling somewhere downwind. */
    private static final int TICKS_PER_ASHFALL = 3;
    /** How far past the cone's foot ash settles. */
    private static final int ASHFALL_MARGIN = 16;

    /** How far past the cone's foot to look for streams when cooling them. */
    private static final int COOLING_MARGIN = 8;
    /** Quiet flags, as the day/night cycle learned to use — see docs/bugs/0001. */
    private static final int COOLING_FLAGS = Block.NOTIFY_LISTENERS;

    /** No day rolled yet — set on the first tick so a volcano never erupts the instant it loads. */
    private static final long NO_DAY = Long.MIN_VALUE;
    /** Not currently cooling a set of streams. */
    private static final int NOT_COOLING = Integer.MIN_VALUE;

    private long lastRolledDay = NO_DAY;
    private int eruptionTicksLeft = 0;
    private int coolingSliceY = NOT_COOLING;

    // The cone this vent sits in, recorded by VolcanoFeature. Zero when a vent was
    // placed by hand rather than generated, in which case it erupts without streams.
    private int rimRadius = 0;
    private int rimY = 0;
    private int outerRadius = 0;
    private int baseY = 0;

    public VolcanoVentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLCANO_VENT, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, VolcanoVentBlockEntity vent) {
        vent.rollForNewDay(world);
        if (vent.eruptionTicksLeft > 0) {
            vent.eruptionTicksLeft--;
            vent.erupt((ServerWorld) world, pos);
            if (vent.eruptionTicksLeft == 0) vent.beginCooling();
            vent.markDirty();
        } else if (vent.coolingSliceY != NOT_COOLING) {
            vent.coolOneSlice((ServerWorld) world, pos);
            vent.markDirty();
        }
    }

    /** Records the cone this vent sits in. Called by VolcanoFeature as it builds it. */
    public void setCone(int rimRadius, int rimY, int outerRadius, int baseY) {
        this.rimRadius = rimRadius;
        this.rimY = rimY;
        this.outerRadius = outerRadius;
        this.baseY = baseY;
        this.markDirty();
    }

    private boolean knowsItsCone() {
        return this.outerRadius > 0;
    }

    private void rollForNewDay(World world) {
        long today = PongonTime.dayNumber(world);
        if (this.lastRolledDay == today) return;

        boolean firstEverRoll = this.lastRolledDay == NO_DAY;
        this.lastRolledDay = today;
        this.markDirty();
        if (firstEverRoll) return;

        if (this.eruptionTicksLeft <= 0 && world.getRandom().nextDouble() < ERUPTION_CHANCE) {
            this.begin(world);
        }
    }

    private void begin(World world) {
        this.eruptionTicksLeft = ERUPTION_TICKS;
        world.playSound(null, this.pos, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                4.0f, 0.4f);
    }

    /** One tick of an eruption. */
    private void erupt(ServerWorld world, BlockPos pos) {
        BlockPos mouth = mouthAbove(world, pos);
        if (mouth == null) return;

        this.raisePlume(world, mouth);

        if (world.getRandom().nextInt(TICKS_PER_BOMB) == 0) {
            this.throwBomb(world, mouth);
        }
        if (this.knowsItsCone() && world.getRandom().nextInt(TICKS_PER_STREAM) == 0) {
            this.spillOverRim(world, pos);
        }
        if (this.knowsItsCone() && world.getRandom().nextInt(TICKS_PER_ASHFALL) == 0) {
            this.settleAsh(world, pos);
        }
    }

    /** The ash cloud: a column of smoke and ash standing over the crater, widening as it rises. */
    private void raisePlume(ServerWorld world, BlockPos mouth) {
        double x = mouth.getX() + 0.5;
        double z = mouth.getZ() + 0.5;

        world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, mouth.getY() + 0.5, z,
                6, 1.2, 0.6, 1.2, 0.08);

        for (int height = 2; height < PLUME_HEIGHT; height += 4) {
            double spread = 1.5 + height * 0.35;
            world.spawnParticles(ParticleTypes.ASH, x, mouth.getY() + height, z,
                    8, spread, 1.0, spread, 0.02);
            world.spawnParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, mouth.getY() + height, z,
                    2, spread * 0.6, 0.5, spread * 0.6, 0.01);
        }
    }

    /**
     * Drops one block of ash somewhere around the volcano, so the fall accumulates into
     * a real grey blanket rather than clearing when the eruption stops.
     *
     * <p>Skips unloaded chunks rather than dragging them in, and never settles on the
     * lava ocean.
     */
    private void settleAsh(ServerWorld world, BlockPos pos) {
        Random random = world.getRandom();
        int reach = this.outerRadius + ASHFALL_MARGIN;
        int x = pos.getX() + random.nextBetween(-reach, reach);
        int z = pos.getZ() + random.nextBetween(-reach, reach);
        if (!world.isChunkLoaded(x >> 4, z >> 4)) return;

        int surface = world.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
        if (surface <= DayNightCycle.SEA_LEVEL + 1) return;

        BlockPos landing = new BlockPos(x, surface, z);
        if (!world.getBlockState(landing).isReplaceable()) return;
        if (world.getBlockState(landing.down()).isOf(Blocks.LAVA)) return;

        world.setBlockState(landing, ModBlocks.ASH.getDefaultState(), COOLING_FLAGS);
    }

    /**
     * Wells a lava source over a random point of the crater rim, which then runs down
     * the outside of the cone under vanilla flow. The source is cleaned up by the
     * cooling sweep when the eruption ends — otherwise it would pour forever.
     */
    private void spillOverRim(ServerWorld world, BlockPos pos) {
        double angle = world.getRandom().nextDouble() * Math.PI * 2.0;
        BlockPos lip = new BlockPos(
                pos.getX() + (int) Math.round(Math.cos(angle) * this.rimRadius),
                this.rimY + 1,
                pos.getZ() + (int) Math.round(Math.sin(angle) * this.rimRadius));
        if (world.getBlockState(lip).isReplaceable()) {
            world.setBlockState(lip, Blocks.LAVA.getDefaultState());
        }
    }

    private void beginCooling() {
        if (this.knowsItsCone()) this.coolingSliceY = this.rimY + 2;
    }

    /**
     * Freezes one horizontal slice of spilt lava into Crushed Magma, so the streams
     * become permanent rock. Done a slice per tick rather than all at once: the
     * day/night cycle's freeze taught us what a big one-tick block sweep costs.
     *
     * <p>Lava inside the rim is left alone — the crater keeps its pool.
     */
    private void coolOneSlice(ServerWorld world, BlockPos pos) {
        int reach = this.outerRadius + COOLING_MARGIN;
        int y = this.coolingSliceY;
        BlockPos.Mutable cursor = new BlockPos.Mutable();

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz <= this.rimRadius * this.rimRadius) continue;
                cursor.set(pos.getX() + dx, y, pos.getZ() + dz);
                if (world.getBlockState(cursor).isOf(Blocks.LAVA)) {
                    world.setBlockState(cursor, ModBlocks.CRUSHED_MAGMA.getDefaultState(), COOLING_FLAGS);
                }
            }
        }

        this.coolingSliceY--;
        if (this.coolingSliceY < this.lowestSliceToCool()) this.coolingSliceY = NOT_COOLING;
    }

    /**
     * Never cool at or below the lava ocean's surface. A stream that reaches the shore
     * has joined the ocean, and freezing that would punch a magma disc out of the sea.
     */
    private int lowestSliceToCool() {
        return Math.max(this.baseY - COOLING_MARGIN, DayNightCycle.SEA_LEVEL + 1);
    }

    private void throwBomb(ServerWorld world, BlockPos mouth) {
        Random random = world.getRandom();
        LavaBombEntity bomb = new LavaBombEntity(world,
                mouth.getX() + 0.5, mouth.getY() + 1.0, mouth.getZ() + 0.5);
        bomb.setVelocity(
                (random.nextDouble() - 0.5) * 2.0 * BOMB_SPREAD,
                BOMB_LAUNCH_SPEED + random.nextDouble() * BOMB_LAUNCH_SPEED_VARIANCE,
                (random.nextDouble() - 0.5) * 2.0 * BOMB_SPREAD);
        world.spawnEntity(bomb);
        world.playSound(null, mouth, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS,
                1.6f, 0.7f + random.nextFloat() * 0.3f);
    }

    /**
     * The first open block above the vent — the eruption has to come out through the
     * lava standing in the crater, not from inside the rock.
     */
    private static BlockPos mouthAbove(ServerWorld world, BlockPos pos) {
        BlockPos.Mutable cursor = pos.mutableCopy();
        for (int i = 0; i < VENT_SEARCH_HEIGHT; i++) {
            cursor.move(0, 1, 0);
            if (world.getBlockState(cursor).isAir()) return cursor.toImmutable();
        }
        return null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putLong("LastRolledDay", this.lastRolledDay);
        nbt.putInt("EruptionTicksLeft", this.eruptionTicksLeft);
        nbt.putInt("CoolingSliceY", this.coolingSliceY);
        nbt.putInt("RimRadius", this.rimRadius);
        nbt.putInt("RimY", this.rimY);
        nbt.putInt("OuterRadius", this.outerRadius);
        nbt.putInt("BaseY", this.baseY);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.lastRolledDay = nbt.contains("LastRolledDay") ? nbt.getLong("LastRolledDay") : NO_DAY;
        this.eruptionTicksLeft = nbt.getInt("EruptionTicksLeft");
        this.coolingSliceY = nbt.contains("CoolingSliceY") ? nbt.getInt("CoolingSliceY") : NOT_COOLING;
        this.rimRadius = nbt.getInt("RimRadius");
        this.rimY = nbt.getInt("RimY");
        this.outerRadius = nbt.getInt("OuterRadius");
        this.baseY = nbt.getInt("BaseY");
    }
}
