package net.pongon.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;
import net.pongon.block.ModBlocks;

public class ModItemGroups {
    public static final ItemGroup PONGON_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of(Pongon.MOD_ID, "pongon"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.PONGONITE_LUMP))
                    .displayName(Text.translatable("itemGroup.pongon.pongon"))
                    .entries((context, entries) -> {
                        entries.add(ModBlocks.CRUSHED_MAGMA);
                        entries.add(ModBlocks.PONGONITE_ORE);
                        entries.add(ModBlocks.PONGONITE_BLOCK);
                        entries.add(ModBlocks.DINGOLIN_ORE);
                        entries.add(ModBlocks.DINGOLIN_BLOCK);
                        entries.add(ModBlocks.ROCK_VAPOR);
                        entries.add(ModBlocks.PONGOL_DIRT);
                        entries.add(ModBlocks.PONGOL_LOG);
                        entries.add(ModBlocks.PONGOL_WOOD);
                        entries.add(ModBlocks.PONGOL_PLANKS);
                        entries.add(ModBlocks.PONGOL_STAIRS);
                        entries.add(ModBlocks.PONGOL_SLAB);
                        entries.add(ModBlocks.PONGOL_FENCE);
                        entries.add(ModBlocks.PONGOL_FENCE_GATE);
                        entries.add(ModBlocks.PONGOL_DOOR);
                        entries.add(ModBlocks.PONGOL_TRAPDOOR);
                        entries.add(ModBlocks.PONGOL_PRESSURE_PLATE);
                        entries.add(ModBlocks.PONGOL_BUTTON);
                        entries.add(ModBlocks.PONGOL_LEAVES);
                        entries.add(ModItems.LAVA_BLOB_SPAWN_EGG);
                        entries.add(ModItems.PONGONITE_LUMP);
                        entries.add(ModItems.PONGONITE_SWORD);
                        entries.add(ModItems.PONGONITE_PICKAXE);
                        entries.add(ModItems.PONGONITE_AXE);
                        entries.add(ModItems.PONGONITE_SHOVEL);
                        entries.add(ModItems.PONGONITE_HOE);
                        entries.add(ModItems.PONGONITE_HELMET);
                        entries.add(ModItems.PONGONITE_CHESTPLATE);
                        entries.add(ModItems.PONGONITE_LEGGINGS);
                        entries.add(ModItems.PONGONITE_BOOTS);
                        entries.add(ModItems.DINGOLIN_CRYSTAL);
                        entries.add(ModItems.DINGOLIN_BALL);
                        entries.add(ModItems.DINGOLIN_SWORD);
                        entries.add(ModItems.DINGOLIN_PICKAXE);
                        entries.add(ModItems.DINGOLIN_AXE);
                        entries.add(ModItems.DINGOLIN_SHOVEL);
                        entries.add(ModItems.DINGOLIN_HOE);
                        entries.add(ModItems.DINGOLIN_HELMET);
                        entries.add(ModItems.DINGOLIN_CHESTPLATE);
                        entries.add(ModItems.DINGOLIN_LEGGINGS);
                        entries.add(ModItems.DINGOLIN_BOOTS);
                    })
                    .build()
    );

    public static void initialize() {}
}
