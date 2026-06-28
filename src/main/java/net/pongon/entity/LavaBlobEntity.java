package net.pongon.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
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
        // Keep the slime hop/wander/look goals, then drop the hostile targeting so a
        // wild blob bounces around harmlessly. (The Rock Vapor beam goal is added in a
        // later pass and brings its own, hostile-only, targeting.)
        super.initGoals();
        this.targetSelector.clear(goal -> true);
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
}
