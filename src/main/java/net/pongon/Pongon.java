package net.pongon;

import net.fabricmc.api.ModInitializer;
import net.pongon.block.ModBlocks;
import net.pongon.effect.ModEffects;
import net.pongon.entity.ModEntities;
import net.pongon.entity.LavaBlobSpawner;
import net.pongon.item.ModItemGroups;
import net.pongon.item.ModItems;
import net.pongon.world.ModBlockEntities;
import net.pongon.world.ModFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pongon implements ModInitializer {
    public static final String MOD_ID = "pongon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Pongon dimension loading...");
        ModBlocks.initialize();
        ModFeatures.initialize();
        ModBlockEntities.initialize();
        ModEntities.initialize();
        ModEffects.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        DayNightCycle.initialize();
        LavaBlobSpawner.initialize();
    }
}
