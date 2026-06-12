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
                        entries.add(ModBlocks.ROCK_VAPOR);
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
                    })
                    .build()
    );

    public static void initialize() {}
}
