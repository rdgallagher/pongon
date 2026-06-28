package net.pongon.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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

    public LavaBlobEntity(EntityType<? extends LavaBlobEntity> entityType, World world) {
        super(entityType, world);
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
}
