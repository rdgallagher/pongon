package net.pongon.effect;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

public class ModEffects {
    public static final RegistryEntry<StatusEffect> BLOBINESS = register("blobiness", new BlobinessStatusEffect());

    // 2 minutes, matching the spec.
    private static final int BLOBINESS_DURATION_TICKS = 2 * 60 * 20;

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        Identifier id = Identifier.of(Pongon.MOD_ID, name);
        Registry.register(Registries.STATUS_EFFECT, id, effect);
        return Registries.STATUS_EFFECT.getEntry(
                RegistryKey.of(RegistryKeys.STATUS_EFFECT, id)).orElseThrow();
    }

    /**
     * Inflict the full Blobiness package on a player who attacked a Lava Blob:
     * the themed Blobiness marker plus Slowness II + Mining Fatigue I for the tired
     * feel. Re-applying refreshes the duration.
     */
    public static void applyBlobiness(PlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(BLOBINESS, BLOBINESS_DURATION_TICKS, 0));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, BLOBINESS_DURATION_TICKS, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, BLOBINESS_DURATION_TICKS, 0));
    }

    public static void initialize() {
        // While Blobied, a player can't bring themselves to harm passive creatures:
        // cancel attacks on PassiveEntity targets (hostiles & neutrals unaffected).
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof PassiveEntity && player.hasStatusEffect(BLOBINESS)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }
}
