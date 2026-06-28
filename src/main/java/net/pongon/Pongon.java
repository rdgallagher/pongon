package net.pongon;

import net.fabricmc.api.ModInitializer;
import net.pongon.block.ModBlocks;
import net.pongon.effect.ModEffects;
import net.pongon.entity.ModEntities;
import net.pongon.item.ModItemGroups;
import net.pongon.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pongon implements ModInitializer {
    public static final String MOD_ID = "pongon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Pongon dimension loading...");
        ModBlocks.initialize();
        ModEntities.initialize();
        ModEffects.initialize();
        ModItems.initialize();
        ModItemGroups.initialize();
        DayNightCycle.initialize();
    }
}
