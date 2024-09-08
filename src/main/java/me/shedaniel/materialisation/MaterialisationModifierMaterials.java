package me.shedaniel.materialisation;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class MaterialisationModifierMaterials {
    public static final Item REINFORCER = new Item(new Item.Settings().maxCount(4));
    public static final Item GOLD_INFUSED_CREAM = new Item(new Item.Settings().maxCount(4));
    
    public static void register() {
        registerItem("reinforcer", REINFORCER);
        registerItem("gold_infused_cream", GOLD_INFUSED_CREAM);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(REINFORCER));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(GOLD_INFUSED_CREAM));
    }

    @SuppressWarnings("unused")
    private static void registerBlock(String name, Block block) {
        registerBlock(name, block, new Item.Settings());
    }

    @SuppressWarnings("unused")
    private static void registerBlock(String name, Block block, RegistryKey<ItemGroup> group) {
        registerBlock(name, block, new Item.Settings());
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(block));
    }
    
    private static void registerBlock(String name, Block block, Item.Settings settings) {
        Registry.register(Registries.BLOCK, new Identifier(ModReference.MOD_ID, name), block);
        registerItem(name, new BlockItem(block, settings));
    }
    
    private static void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, new Identifier(ModReference.MOD_ID, name), item);
    }
}
