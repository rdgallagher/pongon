package net.pongon.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The heart of a volcano, buried under the lava in its crater. Indistinguishable
 * from Crushed Magma to look at; it exists so the world remembers where a volcano
 * is and can erupt it — see {@link VolcanoVentBlockEntity}.
 */
public class VolcanoVentBlock extends BlockWithEntity {
    public static final MapCodec<VolcanoVentBlock> CODEC = createCodec(VolcanoVentBlock::new);

    public VolcanoVentBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    /** BlockWithEntity defaults to INVISIBLE; the vent is meant to read as ordinary rock. */
    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VolcanoVentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return validateTicker(type, ModBlockEntities.VOLCANO_VENT, VolcanoVentBlockEntity::serverTick);
    }
}
