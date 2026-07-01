package net.pongon.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.joml.Vector3f;
import net.pongon.PongonTime;
import net.pongon.block.ModBlocks;
import net.pongon.effect.ModEffects;

/**
 * Lava Blob — Pongon's springy lava creature.
 *
 * Built on {@link SlimeEntity} to reuse its hop movement and squish animation, but
 * it is friendly to players and (later) a protective ally rather than a hostile
 * slime: the inherited player/golem targeting is stripped in {@link #initGoals()}.
 *
 * This is the wild blob. Invincibility, Potion of Blobiness, day-gated spawning,
 * despawn/keep-alive, and the Rock Vapor beam are layered on in later passes.
 */
public class LavaBlobEntity extends SlimeEntity {
    // One fixed size — Lava Blobs don't split or vary like vanilla slimes.
    private static final int FIXED_SIZE = 2;
    // At night a wild blob cools: every 5 minutes it has a 50% chance to despawn,
    // unless it stays warm (see isOnHeatSource). Daytime is always safe.
    private static final int NIGHT_DESPAWN_INTERVAL = 5 * 60 * 20; // ticks (5 min)
    private static final float NIGHT_DESPAWN_CHANCE = 0.5f;

    // --- Rock Vapor beam ---
    static final double BEAM_RANGE = 16.0;                  // aggro + max reach (blocks)
    private static final double BEAM_REACH_PER_TICK = 16.0 / 20.0; // extends at 16 blocks/s
    private static final double BEAM_STEP = 0.5;            // ray-march granularity
    // The current beam target (server-authoritative). null = not firing.
    private LivingEntity beamTarget;
    // How far the beam has extended this engagement (ramps at 16 blocks/s).
    private double beamReach;
    // The visible stream: a Rock-Vapor-yellow dust column from mouth to endpoint.
    private static final DustParticleEffect BEAM_PARTICLE =
            new DustParticleEffect(new Vector3f(1.0f, 0.85f, 0.25f), 1.4f);

    public LavaBlobEntity(EntityType<? extends LavaBlobEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Spawn rule: only during the Pongon day (the 300 °C "hot" gate), on a surface
     * (land or the lava-ocean top) with headroom. Used with an UNRESTRICTED spawn
     * location + surface heightmap, so this predicate does the gating itself.
     */
    public static boolean canSpawn(EntityType<LavaBlobEntity> type, ServerWorldAccess world,
                                   SpawnReason reason, BlockPos pos, Random random) {
        return PongonTime.isHot(world.toServerWorld())
                && world.getBlockState(pos.up()).isAir()      // headroom
                && !world.getBlockState(pos.down()).isAir();   // standing on land or lava
    }

    public static DefaultAttributeContainer.Builder createLavaBlobAttributes() {
        // setSize() sets base values on MAX_HEALTH, MOVEMENT_SPEED and ATTACK_DAMAGE,
        // so ATTACK_DAMAGE must be present (createMobAttributes lacks it) or it NPEs.
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void initGoals() {
        // Keep the slime hop/wander/look goals, then drop the hostile slime targeting.
        super.initGoals();
        this.targetSelector.clear(goal -> true);
        // The protective Rock Vapor beam: high priority, its own hostile-only targeting.
        this.goalSelector.add(1, new RockVaporBeamGoal(this));
    }

    @Override
    public void setSize(int size, boolean heal) {
        // Pin to a single size regardless of what vanilla logic requests.
        super.setSize(FIXED_SIZE, heal);
    }

    @Override
    public boolean isSmall() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient && this.age > 0 && this.age % NIGHT_DESPAWN_INTERVAL == 0) {
            rollNightDespawn();
        }
    }

    /**
     * Once every {@link #NIGHT_DESPAWN_INTERVAL}, a wild blob that has cooled (it is
     * night and it is not sitting on a heat source) has a 50% chance to despawn.
     * (Tamed blobs will be exempt once taming exists — Canyons biome.)
     */
    private void rollNightDespawn() {
        if (PongonTime.isHot(this.getWorld())) return; // daytime: still hot, safe
        if (isOnHeatSource()) return;                  // kept warm, safe
        if (this.random.nextFloat() < NIGHT_DESPAWN_CHANCE) {
            this.discard();
        }
    }

    private boolean isOnHeatSource() {
        BlockState below = this.getWorld().getBlockState(this.getBlockPos().down());
        return below.isOf(Blocks.FIRE)
                || below.isOf(Blocks.LAVA)
                || below.isOf(Blocks.MAGMA_BLOCK)
                || below.isOf(ModBlocks.CRUSHED_MAGMA);
    }

    /**
     * Invincible. Any player attack (melee or projectile — both arrive here with the
     * player as attacker) inflicts Blobiness on that player; melee additionally gives
     * a springy recoil. No damage is ever taken.
     */
    @Override
    public boolean damage(DamageSource source, float amount) {
        // Let the /kill command (and similar invulnerability-bypassing kills) through,
        // so blobs can still be removed; everything else is shrugged off.
        if (source.isOf(DamageTypes.GENERIC_KILL)) {
            return super.damage(source, amount);
        }
        if (!this.getWorld().isClient && source.getAttacker() instanceof PlayerEntity player) {
            ModEffects.applyBlobiness(player);
            boolean melee = source.getSource() == source.getAttacker();
            if (melee) {
                Vec3d away = this.getPos().subtract(player.getPos());
                if (away.lengthSquared() > 1.0e-4) {
                    away = away.normalize();
                    this.setVelocity(away.x * 0.5, 0.42, away.z * 0.5);
                    this.velocityModified = true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    /**
     * Never deal contact damage. SlimeEntity.onPlayerCollision damages the player
     * whenever canAttack() is true, so a friendly blob must report false. (Contact is
     * meant to be a harmless springy push.)
     */
    @Override
    protected boolean canAttack() {
        return false;
    }

    /** Squish with molten-lava particles instead of the green slime particle. */
    @Override
    protected ParticleEffect getParticles() {
        return ParticleTypes.LAVA;
    }

    // --- Rock Vapor beam (driven by RockVaporBeamGoal) ---

    void startBeam(LivingEntity target) {
        this.beamTarget = target;
        this.beamReach = 0.0;
    }

    void stopBeam() {
        this.beamTarget = null;
        this.beamReach = 0.0;
    }

    public LivingEntity getBeamTarget() {
        return this.beamTarget;
    }

    /**
     * Advance and apply the beam for one tick: extend the stream toward the target at
     * 16 blocks/s, melt the block it terminates on to lava (boring forward over time),
     * and burn every entity in its line for the per-tick share of 30/20/10 HP/s.
     */
    void tickBeam() {
        if (this.beamTarget == null) return;
        World world = this.getWorld();
        Vec3d start = this.getEyePos();
        Vec3d dir = this.beamTarget.getBoundingBox().getCenter().subtract(start).normalize();
        this.beamReach = Math.min(BEAM_RANGE, this.beamReach + BEAM_REACH_PER_TICK);
        Vec3d end = rayMarchAndMelt(world, start, dir, this.beamReach);
        burnAlongBeam(world, start, end);
        if (world instanceof ServerWorld serverWorld) {
            emitBeamParticles(serverWorld, start, end);
        }
    }

    /** The visible beam: a dense yellow dust stream from the blob's mouth to the endpoint. */
    private void emitBeamParticles(ServerWorld world, Vec3d start, Vec3d end) {
        Vec3d segment = end.subtract(start);
        double length = segment.length();
        if (length <= 0.0) return;
        Vec3d dir = segment.multiply(1.0 / length);
        for (double d = 0.0; d <= length; d += 0.4) {
            Vec3d p = start.add(dir.multiply(d));
            world.spawnParticles(BEAM_PARTICLE, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    /** Walk from start along dir; stop at the first solid block, melting it to lava
     *  unless it is unbreakable. Air and fluids (lava) are passed through. */
    private Vec3d rayMarchAndMelt(World world, Vec3d start, Vec3d dir, double reach) {
        for (double d = BEAM_STEP; d <= reach; d += BEAM_STEP) {
            Vec3d p = start.add(dir.multiply(d));
            BlockPos bp = BlockPos.ofFloored(p);
            BlockState state = world.getBlockState(bp);
            boolean solid = !state.isAir() && state.getFluidState().isEmpty();
            if (solid) {
                if (!world.isClient && !isUnbreakable(state)) {
                    world.setBlockState(bp, Blocks.LAVA.getDefaultState());
                }
                return p;
            }
        }
        return start.add(dir.multiply(reach));
    }

    private static boolean isUnbreakable(BlockState state) {
        return state.isOf(Blocks.BEDROCK)
                || state.isOf(Blocks.END_PORTAL_FRAME)
                || state.isOf(ModBlocks.ROCK_VAPOR);
    }

    /** Burn every living entity the beam passes through (piercing) — players and pets
     *  included; it's hot. Damage respects the per-tick share so it averages to the
     *  difficulty-scaled HP/s. */
    private void burnAlongBeam(World world, Vec3d start, Vec3d end) {
        if (world.isClient) return;
        float perTick = beamDamagePerSecond(world) / 20.0f;
        if (perTick <= 0.0f) return;
        DamageSource source = world.getDamageSources().inFire();
        Box beamBox = new Box(start, end).expand(0.25);
        for (Entity e : world.getOtherEntities(this, beamBox,
                other -> other instanceof LivingEntity && other.isAlive())) {
            if (e.getBoundingBox().expand(0.1).raycast(start, end).isPresent()) {
                ((LivingEntity) e).timeUntilRegen = 0; // continuous burn, bypass i-frames
                e.damage(source, perTick);
            }
        }
    }

    private static float beamDamagePerSecond(World world) {
        Difficulty difficulty = world.getDifficulty();
        return switch (difficulty) {
            case PEACEFUL -> 0.0f;
            case EASY -> 10.0f;
            case HARD -> 30.0f;
            default -> 20.0f; // NORMAL
        };
    }
}
