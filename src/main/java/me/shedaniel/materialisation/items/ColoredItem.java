package me.shedaniel.materialisation.items;

import me.shedaniel.materialisation.Materialisation;
import me.shedaniel.materialisation.MaterialisationUtils;
import me.shedaniel.materialisation.api.PartMaterial;
import me.shedaniel.materialisation.utils.MaterialisationDataUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ColoredItem extends Item {
    public ColoredItem(Settings item$Settings_1) {
        super(item$Settings_1);
    }
    
    public static Identifier getItemModifierDamage() {
        return BASE_ATTACK_DAMAGE_MODIFIER_ID;
    }
    
    public static Identifier getItemModifierSwingSpeed() {
        return BASE_ATTACK_SPEED_MODIFIER_ID;
    }
    
    public static float getExtraDamage(Item item) {
        if (item == Materialisation.SWORD_BLADE)
            return 4f;
        if (item == Materialisation.PICKAXE_HEAD)
            return 2f;
        if (item == Materialisation.AXE_HEAD)
            return 7f;
        if (item == Materialisation.MEGAAXE_HEAD)
            return 10f;
        if (item == Materialisation.HAMMER_HEAD)
            return 9f;
        if (item == Materialisation.SHOVEL_HEAD)
            return 2.5f;
        return 0f;
    }
    
    private static double getBaseToolBreakingSpeed(ColoredItem item, PartMaterial material) {
        double speed = material.getToolSpeed();
        if (item == Materialisation.HAMMER_HEAD) speed /= 6f;
        if (item == Materialisation.MEGAAXE_HEAD) speed /= 6.5f;
        return speed;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> list, TooltipType type) {
        super.appendTooltip(stack, context, list, type);
        NbtCompound tag = MaterialisationDataUtil.getNbt(stack);
        if (tag.contains("mt_0_material")) {
            PartMaterial material = MaterialisationUtils.getMaterialFromPart(stack);
            if (material != null) {
                if (this == Materialisation.HANDLE) {
                    list.add(Text.translatable("text.materialisation.tool_handle_durability_multiplier", MaterialisationUtils.getColoring(material.getDurabilityMultiplier()).toString() + "x" + MaterialisationUtils.TWO_DECIMAL_FORMATTER.format(material.getDurabilityMultiplier())));
                    list.add(Text.translatable("text.materialisation.tool_handle_speed_multiplier", MaterialisationUtils.getColoring(material.getBreakingSpeedMultiplier()).toString() + "x" + MaterialisationUtils.TWO_DECIMAL_FORMATTER.format(material.getBreakingSpeedMultiplier())));
                }
                if (this == Materialisation.PICKAXE_HEAD || this == Materialisation.AXE_HEAD || this == Materialisation.SHOVEL_HEAD || this == Materialisation.HAMMER_HEAD || this == Materialisation.MEGAAXE_HEAD || this == Materialisation.SWORD_BLADE) {
                    if (this != Materialisation.SWORD_BLADE)
                        list.add(Text.translatable("text.materialisation.head_part_speed", Formatting.YELLOW.toString() + MaterialisationUtils.TWO_DECIMAL_FORMATTER.format(getBaseToolBreakingSpeed(this, material))));
                    list.add(Text.translatable("text.materialisation.head_part_durability", Formatting.YELLOW.toString() + MaterialisationUtils.TWO_DECIMAL_FORMATTER.format(material.getToolDurability())));
                    list.add(Text.translatable("text.materialisation.head_part_damage", Formatting.YELLOW.toString() + MaterialisationUtils.TWO_DECIMAL_FORMATTER.format(getExtraDamage(this) + material.getAttackDamage())));
                }
            }
        }
    }
    
    @Override
    public Text getName(ItemStack itemStack_1) {
        PartMaterial part = MaterialisationUtils.getMaterialFromPart(itemStack_1);
        if (part != null)
            return Text.translatable("item.materialisation.materialised_" + Registries.ITEM.getId(this).getPath(), Text.translatable(part.getMaterialTranslateKey()));
        return Text.translatable(this.getTranslationKey(itemStack_1));
    }
    
}
