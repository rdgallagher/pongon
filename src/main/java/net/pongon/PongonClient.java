package net.pongon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.pongon.block.ModBlocks;

public class PongonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Rock Vapor is a translucent gas — render it with alpha blending so you can
        // see through the haze (its texture carries a partial alpha).
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ROCK_VAPOR, RenderLayer.getTranslucent());
    }
}
