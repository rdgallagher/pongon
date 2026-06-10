package net.pongon.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.pongon.Pongon;

public enum ModToolMaterials implements ToolMaterial {
    PONGONITE(2500, 10.0F, 4.5F, 8,
        TagKey.of(RegistryKeys.BLOCK, Identifier.of(Pongon.MOD_ID, "incorrect_for_pongonite_tool")));

    private final int durability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final TagKey<Block> inverseTag;

    ModToolMaterials(int durability, float miningSpeed, float attackDamage,
                     int enchantability, TagKey<Block> inverseTag) {
        this.durability = durability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.inverseTag = inverseTag;
    }

    @Override public int getDurability()              { return durability; }
    @Override public float getMiningSpeedMultiplier() { return miningSpeed; }
    @Override public float getAttackDamage()          { return attackDamage; }
    @Override public TagKey<Block> getInverseTag()    { return inverseTag; }
    @Override public int getEnchantability()          { return enchantability; }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.PONGONITE_LUMP);
    }
}
