package net.pongon.entity;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.pongon.Pongon;
import net.pongon.mixin.SpawnRestrictionInvoker;

public class ModEntities {
    public static final EntityType<LavaBlobEntity> LAVA_BLOB = register("lava_blob",
            EntityType.Builder.create(LavaBlobEntity::new, SpawnGroup.CREATURE)
                    // Size-1 slime base dimensions; SlimeEntity scales these by its size.
                    .dimensions(0.52f, 0.52f)
                    .build("lava_blob"));

    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Identifier.of(Pongon.MOD_ID, name), type);
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(LAVA_BLOB, LavaBlobEntity.createLavaBlobAttributes());

        // Where a blob may be placed: anywhere on the surface (land or lava top), gated
        // to the Pongon day by LavaBlobEntity.canSpawn.
        SpawnRestrictionInvoker.pongon$register(LAVA_BLOB, SpawnLocationTypes.UNRESTRICTED,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, LavaBlobEntity::canSpawn);

        // Add them to the natural creature spawn pool of every Pongon biome.
        BiomeModifications.addSpawn(
                ctx -> ctx.getBiomeKey().getValue().getNamespace().equals(Pongon.MOD_ID),
                SpawnGroup.CREATURE, LAVA_BLOB, 8, 1, 3);
    }
}
