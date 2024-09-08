package me.shedaniel.materialisation;

import me.shedaniel.materialisation.api.Modifier;
import me.shedaniel.materialisation.blocks.MaterialPreparerBlock;
import me.shedaniel.materialisation.blocks.MaterialisingTableBlock;
import me.shedaniel.materialisation.config.ConfigHelper;
import me.shedaniel.materialisation.config.MaterialisationConfig;
import me.shedaniel.materialisation.gui.MaterialPreparerScreenHandler;
import me.shedaniel.materialisation.gui.MaterialisingTableScreenHandler;
import me.shedaniel.materialisation.items.*;
import me.shedaniel.materialisation.network.RenamePayload;
import me.shedaniel.materialisation.utils.ResettableSimpleRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Optional;

@SuppressWarnings("unused")
public class Materialisation implements ModInitializer {
    
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Block MATERIALISING_TABLE = new MaterialisingTableBlock();
    public static final Block MATERIAL_PREPARER = new MaterialPreparerBlock();
    public static final ScreenHandlerType<MaterialPreparerScreenHandler> MATERIAL_PREPARER_SCREEN_HANDLER = new ScreenHandlerType<>(MaterialPreparerScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
    public static final ScreenHandlerType<MaterialisingTableScreenHandler> MATERIALISING_TABLE_SCREEN_HANDLER = new ScreenHandlerType<>(MaterialisingTableScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
    public static final Identifier MATERIALISING_TABLE_RENAME = Identifier.of(ModReference.MOD_ID, "materialising_table_rename");
    public static final Identifier MATERIALISING_TABLE_PLAY_SOUND = Identifier.of(ModReference.MOD_ID, "materialising_table_play_sound");
    public static final Item MATERIALISED_PICKAXE = new MaterialisedPickaxeItem(new Item.Settings());
    public static final Item MATERIALISED_AXE = new MaterialisedAxeItem(new Item.Settings());
    public static final Item MATERIALISED_SHOVEL = new MaterialisedShovelItem(new Item.Settings());
    public static final Item MATERIALISED_SWORD = new MaterialisedSwordItem(new Item.Settings());
    public static final Item MATERIALISED_HAMMER = new MaterialisedHammerItem(new Item.Settings());
    public static final Item MATERIALISED_MEGAAXE = new MaterialisedMegaAxeItem(new Item.Settings());
    public static final Item HANDLE = new ColoredItem(new Item.Settings());
    public static final Item AXE_HEAD = new ColoredItem(new Item.Settings());
    public static final Item PICKAXE_HEAD = new ColoredItem(new Item.Settings());
    public static final Item SHOVEL_HEAD = new ColoredItem(new Item.Settings());
    public static final Item SWORD_BLADE = new ColoredItem(new Item.Settings());
    public static final Item HAMMER_HEAD = new ColoredItem(new Item.Settings());
    public static final Item MEGAAXE_HEAD = new ColoredItem(new Item.Settings());
    public static final Item BLANK_PATTERN = new PatternItem(new Item.Settings());
    public static final Item TOOL_HANDLE_PATTERN = new PatternItem(new Item.Settings());
    public static final Item PICKAXE_HEAD_PATTERN = new PatternItem(new Item.Settings());
    public static final Item AXE_HEAD_PATTERN = new PatternItem(new Item.Settings());
    public static final Item SHOVEL_HEAD_PATTERN = new PatternItem(new Item.Settings());
    public static final Item SWORD_BLADE_PATTERN = new PatternItem(new Item.Settings());
    public static final Item HAMMER_HEAD_PATTERN = new PatternItem(new Item.Settings());
    public static final Item MEGAAXE_HEAD_PATTERN = new PatternItem(new Item.Settings());

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(BLANK_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(TOOL_HANDLE_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(PICKAXE_HEAD_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(AXE_HEAD_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(SHOVEL_HEAD_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(SWORD_BLADE_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(HAMMER_HEAD_PATTERN));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(MEGAAXE_HEAD_PATTERN));
    }
    
    public static final Registry<Modifier> MODIFIERS = new ResettableSimpleRegistry<>("modifiers");
    public static MaterialisationConfig config;

    public static <T> Optional<T> getReflectionField(Object parent, Class<T> clazz, int index) {
        try {
            Field field = parent.getClass().getDeclaredFields()[index];
            if (!field.isAccessible())
                field.setAccessible(true);
            return Optional.ofNullable(clazz.cast(field.get(parent)));
        } catch (Exception e) {
            Materialisation.LOGGER.printf(Level.ERROR, "Reflection failed! Trying to get #%d from %s", index, clazz.getName());
            return Optional.empty();
        }
    }
    
    @Override
    public void onInitialize() {
        MaterialisationModifierMaterials.register();
        MaterialisationComponentTypes.register();
        registerBlock("materialising_table", MATERIALISING_TABLE, ItemGroups.FUNCTIONAL);
        registerBlock("material_preparer", MATERIAL_PREPARER, ItemGroups.FUNCTIONAL);

        PayloadTypeRegistry.playC2S().register(RenamePayload.ID, RenamePayload.CODEC);
        ServerPlayConnectionEvents.INIT.register((handler, server) -> {

            ServerPlayNetworking.registerGlobalReceiver(RenamePayload.ID, (payload, context) -> {
                String string = payload.getData();
                ServerPlayerEntity player = context.player();
                player.server.execute(() -> {
                    if (player.currentScreenHandler instanceof MaterialisingTableScreenHandler) {
                        MaterialisingTableScreenHandler container = (MaterialisingTableScreenHandler) player.currentScreenHandler;
                        if (string.length() <= 35)
                            container.setNewItemName(string);
                    }
                });
            });

        });
        registerItem("materialised_pickaxe", MATERIALISED_PICKAXE);
        registerItem("materialised_axe", MATERIALISED_AXE);
        registerItem("materialised_shovel", MATERIALISED_SHOVEL);
        registerItem("materialised_sword", MATERIALISED_SWORD);
        registerItem("materialised_hammer", MATERIALISED_HAMMER);
        registerItem("materialised_megaaxe", MATERIALISED_MEGAAXE);
        registerItem("handle", HANDLE);
        registerItem("axe_head", AXE_HEAD);
        registerItem("pickaxe_head", PICKAXE_HEAD);
        registerItem("shovel_head", SHOVEL_HEAD);
        registerItem("sword_blade", SWORD_BLADE);
        registerItem("hammer_head", HAMMER_HEAD);
        registerItem("megaaxe_head", MEGAAXE_HEAD);
        registerItem("blank_pattern", BLANK_PATTERN);
        registerItem("handle_pattern", TOOL_HANDLE_PATTERN);
        registerItem("pickaxe_head_pattern", PICKAXE_HEAD_PATTERN);
        registerItem("axe_head_pattern", AXE_HEAD_PATTERN);
        registerItem("shovel_head_pattern", SHOVEL_HEAD_PATTERN);
        registerItem("sword_blade_pattern", SWORD_BLADE_PATTERN);
        registerItem("hammer_head_pattern", HAMMER_HEAD_PATTERN);
        registerItem("megaaxe_head_pattern", MEGAAXE_HEAD_PATTERN);

        registerScreenHandler("material_preparer", MATERIAL_PREPARER_SCREEN_HANDLER);
        registerScreenHandler("materialising_table", MATERIALISING_TABLE_SCREEN_HANDLER);

        try {
            ConfigHelper.loadDefault();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        ConfigHelper.loadConfig();
    }
    
    private void registerBlock(String name, Block block) {
        registerBlock(name, block, new Item.Settings());
    }
    
    @SuppressWarnings("SameParameterValue")
    private void registerBlock(String name, Block block, RegistryKey<ItemGroup> group) {
        registerBlock(name, block, new Item.Settings());
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(block));
    }
    
    private void registerBlock(String name, Block block, Item.Settings settings) {
        Registry.register(Registries.BLOCK, Identifier.of(ModReference.MOD_ID, name), block);
        registerItem(name, new BlockItem(block, settings));
    }
    
    private void registerItem(String name, Item item) {
        Registry.register(Registries.ITEM, Identifier.of(ModReference.MOD_ID, name), item);
    }

    private void registerScreenHandler(String name, ScreenHandlerType<?> screenHandlerType) {
        Registry.register(Registries.SCREEN_HANDLER, Identifier.of(ModReference.MOD_ID, name), screenHandlerType);
    }
}
