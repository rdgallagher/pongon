package net.pongon.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@link SpawnRestriction#register} is private; this invoker exposes it so we can
 * register the Lava Blob's spawn placement rule (location + heightmap + predicate).
 */
@Mixin(SpawnRestriction.class)
public interface SpawnRestrictionInvoker {
    @Invoker("register")
    static <T extends MobEntity> void pongon$register(
            EntityType<T> type,
            SpawnLocation location,
            Heightmap.Type heightmapType,
            SpawnRestriction.SpawnPredicate<T> predicate) {
        throw new AssertionError();
    }
}
