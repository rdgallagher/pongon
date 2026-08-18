package net.pongon.entity;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

/**
 * A glob of molten rock thrown clear of an erupting volcano. On landing it bursts
 * in a small explosion, sets what it hit alight, and leaves magma where it struck.
 */
public class LavaBombEntity extends ThrownItemEntity {
    private static final float EXPLOSION_POWER = 1.2f;
    private static final float IMPACT_DAMAGE = 6.0f;
    private static final int BURN_SECONDS = 8;

    public LavaBombEntity(EntityType<? extends LavaBombEntity> type, World world) {
        super(type, world);
    }

    public LavaBombEntity(World world, double x, double y, double z) {
        super(ModEntities.LAVA_BOMB, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.MAGMA_BLOCK;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            this.getWorld().addParticle(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            this.getWorld().addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + 0.2, this.getZ(), 0.0, 0.01, 0.0);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (hit.getEntity() instanceof LivingEntity living) {
            living.setOnFireFor(BURN_SECONDS);
            living.damage(this.getDamageSources().thrown(this, this.getOwner()), IMPACT_DAMAGE);
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (this.getWorld().isClient) return;

        BlockPos impact = BlockPos.ofFloored(hit.getPos());
        this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(),
                EXPLOSION_POWER, true, World.ExplosionSourceType.MOB);
        // Explosions leave fire but not molten rock; the bomb is molten rock.
        if (this.getWorld().getBlockState(impact).isReplaceable()) {
            this.getWorld().setBlockState(impact, Blocks.MAGMA_BLOCK.getDefaultState());
        }
        this.discard();
    }

    @Override
    protected double getGravity() {
        return 0.06;
    }

    @Override
    public boolean isOnFire() {
        return true;
    }

    /** Molten rock does not care about explosions, including its own. */
    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }
}
