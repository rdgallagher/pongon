package net.pongon.world;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.pongon.Pongon;

public class ModFeatures {
    public static final Feature<DefaultFeatureConfig> VOLCANO =
            register("volcano", new VolcanoFeature(DefaultFeatureConfig.CODEC));

    private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, F feature) {
        return Registry.register(Registries.FEATURE, Identifier.of(Pongon.MOD_ID, name), feature);
    }

    public static void initialize() {}
}
