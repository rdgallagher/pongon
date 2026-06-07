package net.pongon.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

public class ModItems {
    public static final Item PONGONITE_LUMP = register("pongonite_lump", new Item(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Pongon.MOD_ID, name), item);
    }

    public static void initialize() {}
}
