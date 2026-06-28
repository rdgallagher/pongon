package net.pongon.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SlimeEntityRenderer;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

/**
 * Renders Lava Blobs with the slime model (squish animation reused) but a custom
 * molten-lava texture. A flattened dome shape is a later visual refinement.
 */
public class LavaBlobEntityRenderer extends SlimeEntityRenderer {
    private static final Identifier TEXTURE = Identifier.of(Pongon.MOD_ID, "textures/entity/lava_blob.png");

    public LavaBlobEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(SlimeEntity entity) {
        return TEXTURE;
    }
}
