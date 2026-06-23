package net.pongon.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Rock Vapor is a searing-hot gas: it has no collision (entities pass straight
 * through it) but burns anything inside it for 20 HP per second.
 */
public class RockVaporBlock extends Block {
    // A full health bar per second — Rock Vapor is meant to be lethal to linger in.
    private static final float DAMAGE_PER_SECOND = 20.0f;

    public RockVaporBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // onEntityCollision fires every tick the entity overlaps the block, so gate
        // to once per second (per entity) and apply the full second's worth at once.
        if (!world.isClient && entity instanceof LivingEntity && entity.age % 20 == 0) {
            entity.damage(world.getDamageSources().inFire(), DAMAGE_PER_SECOND);
        }
    }
}
