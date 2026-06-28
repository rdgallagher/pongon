package net.pongon.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;
import net.pongon.world.ModDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Gives Pongon its own sky: a bright star instead of the sun, and no moon.
 *
 * Within {@code renderSky}, the only celestial texture binds are the sun and the
 * moon. We redirect those binds — and only while the player is in Pongon — to swap
 * the sun for the Pongon star and the moon for a transparent texture (so the moon
 * quad still draws but is invisible). Everything else (sky dome, positioning, stars)
 * stays vanilla.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    private static final Identifier VANILLA_SUN = Identifier.ofVanilla("textures/environment/sun.png");
    private static final Identifier VANILLA_MOON = Identifier.ofVanilla("textures/environment/moon_phases.png");
    private static final Identifier PONGON_STAR = Identifier.of(Pongon.MOD_ID, "textures/environment/star.png");
    private static final Identifier PONGON_EMPTY = Identifier.of(Pongon.MOD_ID, "textures/environment/empty.png");

    @Redirect(
            method = "renderSky",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderTexture(ILnet/minecraft/util/Identifier;)V"))
    private void pongon$swapCelestialBodies(int texture, Identifier id) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null && world.getRegistryKey().equals(ModDimensions.PONGON_WORLD)) {
            if (id.equals(VANILLA_SUN)) {
                id = PONGON_STAR;   // a bright Pongon star in place of the sun
            } else if (id.equals(VANILLA_MOON)) {
                id = PONGON_EMPTY;  // transparent -> no moon
            }
        }
        RenderSystem.setShaderTexture(texture, id);
    }
}
