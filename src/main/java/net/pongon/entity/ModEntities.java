package net.pongon.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

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
    }
}
