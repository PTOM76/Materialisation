package me.shedaniel.materialisation.utils;

import me.shedaniel.materialisation.MaterialisationComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class MaterialisationDataUtil {
    public static NbtCompound getNbt(ItemStack stack) {
        return stack.getOrDefault(MaterialisationComponentTypes.MATERIALISED_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    public static void setNbt(ItemStack stack, NbtCompound nbt) {
        stack.set(MaterialisationComponentTypes.MATERIALISED_DATA, NbtComponent.of(nbt));
    }

    public static void removeNbt(ItemStack stack) {
        stack.remove(MaterialisationComponentTypes.MATERIALISED_DATA);
    }

    public static boolean hasNbt(ItemStack stack) {
        return stack.contains(MaterialisationComponentTypes.MATERIALISED_DATA);
    }
}
