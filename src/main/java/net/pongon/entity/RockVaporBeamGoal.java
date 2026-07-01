package net.pongon.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;

import java.util.EnumSet;

/**
 * The Lava Blob's protective attack: breathe a continuous Rock Vapor beam at the
 * nearest hostile within range, on sight. (Owner-guarding when tamed is deferred to
 * the Canyons biome work.) Targeting ignores line of sight — the beam bores through
 * blocks toward the target, which is the intended behaviour.
 */
public class RockVaporBeamGoal extends Goal {
    private final LavaBlobEntity blob;
    private LivingEntity target;

    public RockVaporBeamGoal(LavaBlobEntity blob) {
        this.blob = blob;
        // Controls LOOK so the blob faces its target; movement (the hop) is left alone.
        this.setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.target = findNearestHostile();
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        return this.target != null
                && this.target.isAlive()
                && this.blob.squaredDistanceTo(this.target) <= LavaBlobEntity.BEAM_RANGE * LavaBlobEntity.BEAM_RANGE;
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.blob.startBeam(this.target);
    }

    @Override
    public void stop() {
        this.blob.stopBeam();
        this.target = null;
    }

    @Override
    public void tick() {
        this.blob.getLookControl().lookAt(this.target, 30.0f, 30.0f);
        this.blob.tickBeam();
    }

    private LivingEntity findNearestHostile() {
        Box box = this.blob.getBoundingBox().expand(LavaBlobEntity.BEAM_RANGE);
        LivingEntity nearest = null;
        double bestSq = Double.MAX_VALUE;
        for (Entity e : this.blob.getWorld().getOtherEntities(this.blob, box,
                other -> other instanceof HostileEntity && other.isAlive())) {
            double dSq = this.blob.squaredDistanceTo(e);
            if (dSq < bestSq) {
                bestSq = dSq;
                nearest = (LivingEntity) e;
            }
        }
        return nearest;
    }
}
