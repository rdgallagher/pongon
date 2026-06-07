package net.pongon.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

public class ModBlocks {
    public static final Block CRUSHED_MAGMA = register("crushed_magma",
            new Block(AbstractBlock.Settings.create()
                    .strength(1.0f, 3.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.BASALT)));

    public static final Block PONGONITE_ORE = register("pongonite_ore",
            new Block(AbstractBlock.Settings.create()
                    .strength(30.0f, 1200.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)));

    // Unmineable world floor — kill mechanic is a separate TODO
    public static final Block ROCK_VAPOUR = register("rock_vapour",
            new Block(AbstractBlock.Settings.create()
                    .strength(-1.0f, Float.MAX_VALUE)
                    .luminance((state) -> 15)
                    .noBlockBreakParticles()));

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(Pongon.MOD_ID, name);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}
