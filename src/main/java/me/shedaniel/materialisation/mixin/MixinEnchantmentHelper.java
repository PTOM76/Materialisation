package me.shedaniel.materialisation.mixin;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.materialisation.items.MaterialisedMiningTool;
import net.minecraft.component.ComponentType;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.Weighting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {

    @Shadow
    private static void forEachEnchantment(ItemStack stack, EnchantmentHelper.Consumer consumer) {
        throw new AssertionError();
    }

    @Inject(method = "getEffectListAndLevel", at = @At("RETURN"), cancellable = true)
    private static <T> void getFireAspect(ItemStack stack, ComponentType<T> componentType, CallbackInfoReturnable<Pair<T, Integer>> cir) {
        if (stack.getItem() instanceof MaterialisedMiningTool) {
            if (!componentType.equals(EnchantmentEffectComponentTypes.POST_ATTACK)) return;

            int fireLevel = ((MaterialisedMiningTool) stack.getItem()).getModifierLevel(stack, "materialisation:fire");
            if (fireLevel != 0) {
                MutableObject<Pair<T, Integer>> mutableObject = new MutableObject<>();
                forEachEnchantment(stack, (enchantment, level) -> {
                    if (mutableObject.getValue() == null || (Integer) ((Pair<?, ?>) mutableObject.getValue()).getSecond() < level) {
                        T object = (enchantment.value()).effects().get(componentType);
                        if (object != null) {
                            mutableObject.setValue(Pair.of(object, level + fireLevel * 2));
                        }
                    }
                });
                cir.setReturnValue(mutableObject.getValue());
            }
        }
    }
    
    @Inject(method = "getEffectListAndLevel", at = @At("RETURN"), cancellable = true)
    private static <T> void getLooting(ItemStack stack, ComponentType<T> componentType, CallbackInfoReturnable<Pair<T, Integer>> cir) {
        if (stack.getItem() instanceof MaterialisedMiningTool) {
            if (!componentType.equals(EnchantmentEffectComponentTypes.EQUIPMENT_DROPS)) return;

            int luck = ((MaterialisedMiningTool) stack.getItem()).getModifierLevel(stack, "materialisation:luck");
            if (luck != 0) {
                MutableObject<Pair<T, Integer>> mutableObject = new MutableObject<>();
                forEachEnchantment(stack, (enchantment, level) -> {
                    if (mutableObject.getValue() == null || (Integer) ((Pair<?, ?>) mutableObject.getValue()).getSecond() < level) {
                        T object = (enchantment.value()).effects().get(componentType);
                        if (object != null) {
                            mutableObject.setValue(Pair.of(object, level + luck));
                        }
                    }
                });
                cir.setReturnValue(mutableObject.getValue());
            }
        }
    }
    
    @Inject(method = "getLevel", at = @At("RETURN"), cancellable = true)
    private static void getFortune(RegistryEntry<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (enchantment != Enchantments.FORTUNE) return;
        if (stack.getItem() instanceof MaterialisedMiningTool) {
            int luck = ((MaterialisedMiningTool) stack.getItem()).getModifierLevel(stack, "materialisation:luck");
            if (luck != 0)
                cir.setReturnValue(cir.getReturnValue() + luck);
        }
    }
    
    @Inject(method = "calculateRequiredExperienceLevel", at = @At("HEAD"), cancellable = true)
    private static void calculateRequiredExperienceLevel(Random random_1, int int_1, int int_2, ItemStack itemStack_1, CallbackInfoReturnable<Integer> callbackInfo) {
        if (itemStack_1.getItem() instanceof MaterialisedMiningTool) {
            MaterialisedMiningTool item_1 = (MaterialisedMiningTool) itemStack_1.getItem();
            int int_3 = item_1.getEnchantability(itemStack_1);
            if (int_3 <= 0) {
                callbackInfo.setReturnValue(0);
            } else {
                if (int_2 > 15)
                    int_2 = 15;
                int int_4 = random_1.nextInt(8) + 1 + (int_2 >> 1) + random_1.nextInt(int_2 + 1);
                if (int_1 == 0) {
                    callbackInfo.setReturnValue(Math.max(int_4 / 3, 1));
                } else {
                    callbackInfo.setReturnValue(int_1 == 1 ? int_4 * 2 / 3 + 1 : Math.max(int_4, int_2 * 2));
                }
            }
        }
    }
    
    @Inject(method = "generateEnchantments", at = @At("HEAD"), cancellable = true)
    private static void getEnchantments(Random random_1, ItemStack itemStack_1, int level, Stream<RegistryEntry<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        if (itemStack_1.getItem() instanceof MaterialisedMiningTool) {
            MaterialisedMiningTool item_1 = (MaterialisedMiningTool) itemStack_1.getItem();
            List<EnchantmentLevelEntry> list_1 = Lists.newArrayList();
            int int_2 = item_1.getEnchantability(itemStack_1);
            if (int_2 > 0) {
                level += 1 + random_1.nextInt(int_2 / 4 + 1) + random_1.nextInt(int_2 / 4 + 1);
                float float_1 = (random_1.nextFloat() + random_1.nextFloat() - 1.0F) * 0.15F;
                level = MathHelper.clamp(Math.round((float) level + (float) level * float_1), 1, Integer.MAX_VALUE);
                List<EnchantmentLevelEntry> list_2 = EnchantmentHelper.getPossibleEntries(level, itemStack_1, possibleEnchantments);
                if (!list_2.isEmpty()) {
                    (Weighting.getRandom(random_1, list_2)).ifPresent(list_1::add);
                    
                    while (random_1.nextInt(50) <= level) {
                        level = level * 4 / 5 + 1;
                        list_2 = EnchantmentHelper.getPossibleEntries(level, itemStack_1, possibleEnchantments);
                        
                        for (EnchantmentLevelEntry infoEnchantment : list_1) {
                            EnchantmentHelper.removeConflicts(list_2, infoEnchantment);
                        }
                        
                        if (list_2.isEmpty()) {
                            break;
                        }
                        (Weighting.getRandom(random_1, list_2)).ifPresent(list_1::add);
                        level /= 2;
                    }
                }
                
            }
            cir.setReturnValue(list_1);
        }
    }
}
