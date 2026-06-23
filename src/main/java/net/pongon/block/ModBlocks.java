package net.pongon.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.PillarBlock;
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

    public static final Block PONGONITE_BLOCK = register("pongonite_block",
            new Block(AbstractBlock.Settings.create()
                    .strength(30.0f, 1200.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)));

    public static final Block DINGOLIN_ORE = register("dingolin_ore",
            new Block(AbstractBlock.Settings.create()
                    .strength(150.0f, 3600.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)));

    public static final Block DINGOLIN_BLOCK = register("dingolin_block",
            new Block(AbstractBlock.Settings.create()
                    .strength(150.0f, 3600.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)));

    // --- Pongol Forest biome ---
    public static final Block PONGOL_DIRT = register("pongol_dirt",
            new Block(AbstractBlock.Settings.create()
                    .strength(0.5f)
                    .sounds(BlockSoundGroup.GRAVEL)));

    public static final Block PONGOL_LOG = register("pongol_log",
            new PillarBlock(AbstractBlock.Settings.create()
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .luminance((state) -> 5)
                    .burnable()));

    public static final Block PONGOL_WOOD = register("pongol_wood",
            new PillarBlock(AbstractBlock.Settings.create()
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .luminance((state) -> 5)
                    .burnable()));

    public static final Block PONGOL_PLANKS = register("pongol_planks",
            new Block(AbstractBlock.Settings.create()
                    .strength(2.0f)
                    .sounds(BlockSoundGroup.WOOD)
                    .burnable()));

    public static final Block PONGOL_LEAVES = register("pongol_leaves",
            new LeavesBlock(AbstractBlock.Settings.create()
                    .strength(0.2f)
                    .ticksRandomly()
                    .sounds(BlockSoundGroup.GRASS)
                    .nonOpaque()
                    .luminance((state) -> 5)
                    .burnable()
                    .suffocates((state, world, pos) -> false)
                    .blockVision((state, world, pos) -> false)));

    // Unmineable world floor: a glowing, searing gas you fall through and that burns
    // anything inside it (see RockVaporBlock).
    public static final Block ROCK_VAPOR = register("rock_vapor",
            new RockVaporBlock(AbstractBlock.Settings.create()
                    .strength(-1.0f, Float.MAX_VALUE)
                    .luminance((state) -> 15)
                    .nonOpaque()
                    .noBlockBreakParticles()));

    private static Block register(String name, Block block) {
        Identifier id = Identifier.of(Pongon.MOD_ID, name);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, id, block);
    }

    public static void initialize() {}
}
