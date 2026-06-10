package net.pongon.item;

import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

import java.util.List;
import java.util.Map;

public class ModItems {
    public static final Item PONGONITE_LUMP = register("pongonite_lump", new Item(new Item.Settings()));

    public static final RegistryEntry<ArmorMaterial> PONGONITE_ARMOR_MATERIAL = registerArmorMaterial();

    public static final Item PONGONITE_PICKAXE = register("pongonite_pickaxe",
            new PickaxeItem(ModToolMaterials.PONGONITE, new Item.Settings()));
    public static final Item PONGONITE_AXE = register("pongonite_axe",
            new AxeItem(ModToolMaterials.PONGONITE, new Item.Settings()));
    public static final Item PONGONITE_SHOVEL = register("pongonite_shovel",
            new ShovelItem(ModToolMaterials.PONGONITE, new Item.Settings()));
    public static final Item PONGONITE_HOE = register("pongonite_hoe",
            new HoeItem(ModToolMaterials.PONGONITE, new Item.Settings()));
    public static final Item PONGONITE_SWORD = register("pongonite_sword",
            new SwordItem(ModToolMaterials.PONGONITE, new Item.Settings()
                    .attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.PONGONITE, 3, -2.4F))));

    public static final Item PONGONITE_HELMET = register("pongonite_helmet",
            new ArmorItem(PONGONITE_ARMOR_MATERIAL, ArmorItem.Type.HELMET, new Item.Settings()));
    public static final Item PONGONITE_CHESTPLATE = register("pongonite_chestplate",
            new ArmorItem(PONGONITE_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE, new Item.Settings()));
    public static final Item PONGONITE_LEGGINGS = register("pongonite_leggings",
            new ArmorItem(PONGONITE_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS, new Item.Settings()));
    public static final Item PONGONITE_BOOTS = register("pongonite_boots",
            new ArmorItem(PONGONITE_ARMOR_MATERIAL, ArmorItem.Type.BOOTS, new Item.Settings()));

    private static RegistryEntry<ArmorMaterial> registerArmorMaterial() {
        Identifier id = Identifier.of(Pongon.MOD_ID, "pongonite");
        Registry.register(Registries.ARMOR_MATERIAL, id, new ArmorMaterial(
                Map.of(
                        ArmorItem.Type.HELMET, 3,
                        ArmorItem.Type.CHESTPLATE, 9,
                        ArmorItem.Type.LEGGINGS, 7,
                        ArmorItem.Type.BOOTS, 3
                ),
                8,
                SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE,
                () -> Ingredient.ofItems(PONGONITE_LUMP),
                List.of(new ArmorMaterial.Layer(Identifier.of(Pongon.MOD_ID, "pongonite"))),
                3.0F,
                0.1F
        ));
        return Registries.ARMOR_MATERIAL.getEntry(
                RegistryKey.of(RegistryKeys.ARMOR_MATERIAL, id)).orElseThrow();
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Pongon.MOD_ID, name), item);
    }

    public static void initialize() {}
}
