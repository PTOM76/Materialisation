package me.shedaniel.materialisation.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.materialisation.MaterialisationUtils;
import me.shedaniel.materialisation.api.ToolType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class MaterialisedSwordItem extends SwordItem implements MaterialisedMiningTool {
    private Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public MaterialisedSwordItem(Settings settings) {
        super(MaterialisationUtils.DUMMY_MATERIAL, 0, 0, settings.maxDamage(0));
        
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", -2.4F, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return MaterialisationUtils.getToolDurability(stack) <= 0 ? -1 : MaterialisedMiningTool.super.getMiningSpeedMultiplier(stack, state);
    }
    
    @Nonnull
    @Override
    public ToolType getToolType() {
        return ToolType.SWORD;
    }
    
    @Override
    public boolean canRepair(ItemStack itemStack_1, ItemStack itemStack_2) {
        return false;
    }
    
    @Override
    public boolean postHit(ItemStack stack, LivingEntity livingEntity_1, LivingEntity livingEntity_2) {
        if (!livingEntity_1.getWorld().isClient && (!(livingEntity_1 instanceof PlayerEntity) || !((PlayerEntity) livingEntity_1).getAbilities().creativeMode))
            if (MaterialisationUtils.getToolDurability(stack) > 0)
                if (MaterialisationUtils.applyDamage(stack, 1, livingEntity_1.getRandom())) {
                    livingEntity_1.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                    Item item_1 = stack.getItem();
                    stack.decrement(1);
                    if (livingEntity_1 instanceof PlayerEntity) {
                        ((PlayerEntity) livingEntity_1).incrementStat(Stats.BROKEN.getOrCreateStat(item_1));
                    }
                    MaterialisationUtils.setToolDurability(stack, 0);
                }
        return true;
    }
    
    @Override
    public boolean postMine(ItemStack stack, World world_1, BlockState blockState_1, BlockPos blockPos_1, LivingEntity livingEntity_1) {
        if (!world_1.isClient && blockState_1.getHardness(world_1, blockPos_1) != 0.0F)
            if (!livingEntity_1.getWorld().isClient && (!(livingEntity_1 instanceof PlayerEntity) || !((PlayerEntity) livingEntity_1).getAbilities().creativeMode))
                if (MaterialisationUtils.getToolDurability(stack) > 0)
                    if (MaterialisationUtils.applyDamage(stack, 2, livingEntity_1.getRandom())) {
                        livingEntity_1.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                        Item item_1 = stack.getItem();
                        stack.decrement(1);
                        if (livingEntity_1 instanceof PlayerEntity) {
                            ((PlayerEntity) livingEntity_1).incrementStat(Stats.BROKEN.getOrCreateStat(item_1));
                        }
                        MaterialisationUtils.setToolDurability(stack, 0);
                    }
        return true;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world_1, List<Text> list_1, TooltipContext tooltipContext_1) {
        MaterialisationUtils.appendToolTooltip(stack, this, world_1, list_1, tooltipContext_1);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return MaterialisedMiningTool.getItemBarStep(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MaterialisedMiningTool.getItemBarColor(stack);
    }

}
