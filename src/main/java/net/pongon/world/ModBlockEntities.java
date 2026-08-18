package net.pongon.world;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;
import net.pongon.block.ModBlocks;

public class ModBlockEntities {
    public static final BlockEntityType<VolcanoVentBlockEntity> VOLCANO_VENT = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(Pongon.MOD_ID, "volcano_vent"),
            BlockEntityType.Builder.create(VolcanoVentBlockEntity::new, ModBlocks.VOLCANO_VENT).build());

    public static void initialize() {}
}
