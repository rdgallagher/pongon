package net.pongon.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.pongon.PongonTime;
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

    /** No day rolled yet — set on the first tick so a volcano never erupts the instant it loads. */
    private static final long NO_DAY = Long.MIN_VALUE;

    private long lastRolledDay = NO_DAY;
    private int eruptionTicksLeft = 0;

    public VolcanoVentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOLCANO_VENT, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, VolcanoVentBlockEntity vent) {
        vent.rollForNewDay(world);
        if (vent.eruptionTicksLeft > 0) {
            vent.eruptionTicksLeft--;
            vent.erupt((ServerWorld) world, pos);
            vent.markDirty();
        }
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

        world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                mouth.getX() + 0.5, mouth.getY() + 0.5, mouth.getZ() + 0.5,
                6, 1.2, 0.6, 1.2, 0.08);
        world.spawnParticles(ParticleTypes.ASH,
                mouth.getX() + 0.5, mouth.getY() + 1.5, mouth.getZ() + 0.5,
                10, 2.0, 1.0, 2.0, 0.02);

        if (world.getRandom().nextInt(TICKS_PER_BOMB) == 0) {
            this.throwBomb(world, mouth);
        }
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

    public boolean isErupting() {
        return this.eruptionTicksLeft > 0;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putLong("LastRolledDay", this.lastRolledDay);
        nbt.putInt("EruptionTicksLeft", this.eruptionTicksLeft);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.lastRolledDay = nbt.contains("LastRolledDay") ? nbt.getLong("LastRolledDay") : NO_DAY;
        this.eruptionTicksLeft = nbt.getInt("EruptionTicksLeft");
    }
}
