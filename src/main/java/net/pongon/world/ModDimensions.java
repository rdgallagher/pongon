package net.pongon.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.pongon.Pongon;

public class ModDimensions {
    public static final RegistryKey<World> PONGON_WORLD = RegistryKey.of(
        RegistryKeys.WORLD,
        Identifier.of(Pongon.MOD_ID, "pongon")
    );
}
