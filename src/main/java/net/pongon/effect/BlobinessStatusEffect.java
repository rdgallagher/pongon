package net.pongon.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/**
 * Blobiness — the "tired" effect a Lava Blob inflicts when attacked. This custom
 * effect is the visible, themed marker and the flag the "can't damage passive mobs"
 * rule checks for; the slowed-walk/slowed-mining feel is supplied by accompanying
 * vanilla Slowness + Mining Fatigue applied at the same time (see ModEffects).
 */
public class BlobinessStatusEffect extends StatusEffect {
    public BlobinessStatusEffect() {
        // HARMFUL so it renders red in the HUD; color is a warm lava orange.
        super(StatusEffectCategory.HARMFUL, 0xE25822);
    }
}
