package me.shedaniel.materialisation;

import me.shedaniel.materialisation.api.Modifier;
import me.shedaniel.materialisation.api.PartMaterial;
import me.shedaniel.materialisation.api.PartMaterials;
import me.shedaniel.materialisation.api.ToolType;
import me.shedaniel.materialisation.gui.MaterialPreparerScreen;
import me.shedaniel.materialisation.gui.MaterialisingTableScreen;
import me.shedaniel.materialisation.items.MaterialisedMiningTool;
import me.shedaniel.materialisation.utils.MaterialisationDataUtil;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Lazy;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MaterialisationClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(Materialisation.MATERIALISING_TABLE_SCREEN_HANDLER, MaterialisingTableScreen::new);
        HandledScreens.register(Materialisation.MATERIAL_PREPARER_SCREEN_HANDLER, MaterialPreparerScreen::new);
        Item[] colorableToolParts = {
                Materialisation.MEGAAXE_HEAD,
                Materialisation.HAMMER_HEAD,
                Materialisation.HANDLE,
                Materialisation.SWORD_BLADE,
                Materialisation.SHOVEL_HEAD,
                Materialisation.PICKAXE_HEAD,
                Materialisation.AXE_HEAD
        };
        // UnclampedModelPredicateProvider
        ClampedModelPredicateProvider brightProvider = (itemStack, world, livingEntity, seed) -> MaterialisationUtils.isHandleBright(itemStack) ? 1 : 0;
        ColorProviderRegistry.ITEM.register(MaterialisationUtils::getItemLayerColor, colorableToolParts);
        for (Item colorableToolPart : colorableToolParts) {
            ModelPredicateProviderRegistry.register(colorableToolPart, Identifier.of(ModReference.MOD_ID, "bright"), brightProvider);
        }
        List<Identifier> identifiers = Stream.of(
                Materialisation.MATERIALISED_MEGAAXE,
                Materialisation.MATERIALISED_PICKAXE,
                Materialisation.MATERIALISED_SHOVEL,
                Materialisation.MATERIALISED_AXE,
                Materialisation.MATERIALISED_SWORD,
                Materialisation.MATERIALISED_HAMMER
        ).map(Registries.ITEM::getId).toList();
        Renderer renderer = RendererAccess.INSTANCE.getRenderer();

        List<Identifier> modelIdentifiers = new ArrayList<>();
        for (Identifier identifier : identifiers) {
            Identifier handleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle");
            Identifier headIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head");
            modelIdentifiers.add(handleIdentifier);
            modelIdentifiers.add(headIdentifier);
            Identifier brightHandleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle_bright");
            Identifier brightHeadIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head_bright");
            modelIdentifiers.add(brightHandleIdentifier);
            modelIdentifiers.add(brightHeadIdentifier);
        }

        RRPCallback.BEFORE_VANILLA.register(a -> {
                    RuntimeResourcePack pack = RuntimeResourcePack.create(ModReference.MOD_ID + ":" + ModReference.MOD_ID);
                    PartMaterials.getKnownMaterials().forEach(partMaterial -> {
                        for (Identifier value : partMaterial.getTexturedHeadIdentifiers().values()) {
                            pack.addModel(JModel.model("item/generated").textures(new JTextures().layer0(value.getNamespace() + ":item/" + value.getPath())), Identifier.of(value.getNamespace() + ":item/" + value.getPath()));
                        }
                        for (Identifier value : partMaterial.getTexturedHandleIdentifiers().values()) {
                            pack.addModel(JModel.model("item/generated").textures(new JTextures().layer0(value.getNamespace() + ":item/" + value.getPath())), Identifier.of(value.getNamespace() + ":item/" + value.getPath()));
                        }
                    });
                    a.add(pack);
                }
        );

        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.addModels(modelIdentifiers);

            for (Modifier modifier : Materialisation.MODIFIERS) {
                for (ToolType toolType : ToolType.values()) {
                    Identifier modelIdentifier = modifier.getModelIdentifier(toolType);
                    if (modelIdentifier != null) {
                        pluginContext.addModels(modelIdentifier);
                    }
                }
            }

            PartMaterials.getKnownMaterials().forEach(partMaterial -> {
                pluginContext.addModels(partMaterial.getTexturedHeadIdentifiers().values());
                pluginContext.addModels(partMaterial.getTexturedHandleIdentifiers().values());
            });

            for (Identifier identifier : identifiers) {
                Identifier resourceLocation = Identifier.of(identifier.getNamespace(), "item/" + identifier.getPath());

                UnbakedModel unbakedModel = new UnbakedModel() {
                    @Override
                    public Collection<Identifier> getModelDependencies() {
                        return Collections.emptyList();
                    }

                    @Override
                    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

                    }

                    @Override
                    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
                        return new DynamicToolBakedModel(identifier, Registries.ITEM.get(identifier));
                    }
                };

                pluginContext.resolveModel().register((context) -> {
                    if (resourceLocation.equals(context.id())) {
                        return unbakedModel;
                    }

                    return null;
                });

            }

        });
    }
    
    public static class DynamicToolBakedModel implements BakedModel, FabricBakedModel {
        private final MaterialisedMiningTool tool;
        private final Identifier handleIdentifier;
        private final Identifier headIdentifier;
        private final Identifier brightHandleIdentifier;
        private final Identifier brightHeadIdentifier;
        private final Map<Modifier, Optional<Identifier>> modifierModelMap = new HashMap<>();
        
        public DynamicToolBakedModel(Identifier identifier, Item item) {
            this.handleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle");
            this.headIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head");
            this.brightHandleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle_bright");
            this.brightHeadIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head_bright");
            this.tool = (MaterialisedMiningTool) item;
        }
        
        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            
        }
        
        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            BakedModelManager modelManager = MinecraftClient.getInstance().getBakedModelManager();
            try {
                PartMaterial handleMaterial = MaterialisationUtils.getMatFromString(MaterialisationDataUtil.getNbt(stack).getString("mt_0_material")).orElseGet(() -> PartMaterials.getKnownMaterials().findFirst().get());
                PartMaterial headMaterial = MaterialisationUtils.getMatFromString(MaterialisationDataUtil.getNbt(stack).getString("mt_1_material")).orElseGet(() -> PartMaterials.getKnownMaterials().findFirst().get());
                boolean headBright = headMaterial.isBright();
                int headColor = headMaterial.getToolColor();
                Optional<Identifier> texturedHandleIdentifier = handleMaterial.getTexturedHandleIdentifier(tool.getToolType());
                if (texturedHandleIdentifier.isPresent()) {
                    Identifier modelIdentifier = texturedHandleIdentifier.get();
                    BakedModel handleModel = modelManager.getModel(modelIdentifier);
                    handleModel.emitItemQuads(stack, randomSupplier, context);
                    //context.fallbackConsumer().accept(handleModel);
                } else {
                    int handleColor = handleMaterial.getToolColor();
                    boolean handleBright = handleMaterial.isBright();
                    context.pushTransform(quad -> {
                        quad.nominalFace(GeometryHelper.lightFace(quad));
                        quad.color(handleColor, handleColor, handleColor, handleColor);
                        //quad.spriteColor(0, handleColor, handleColor, handleColor, handleColor);
                        return true;
                    });
                    BakedModel handleModel = modelManager.getModel(handleBright ? brightHandleIdentifier : handleIdentifier);
                    handleModel.emitItemQuads(stack, randomSupplier, context);
                    //context.fallbackConsumer().accept(handleModel);
                    context.popTransform();
                }
                Optional<Identifier> texturedHeadIdentifier = headMaterial.getTexturedHeadIdentifier(tool.getToolType());
                if (texturedHeadIdentifier.isPresent()) {
                    Identifier modelIdentifier = texturedHeadIdentifier.get();
                    BakedModel headModel = modelManager.getModel(modelIdentifier);
                    headModel.emitItemQuads(stack, randomSupplier, context);
                    //context.fallbackConsumer().accept(headModel);
                } else {
                    context.pushTransform(quad -> {
                        quad.nominalFace(GeometryHelper.lightFace(quad));
                        quad.color(headColor, headColor, headColor, headColor);
                        //quad.spriteColor(0, headColor, headColor, headColor, headColor);
                        return true;
                    });
                    BakedModel headModel = modelManager.getModel(headBright ? brightHeadIdentifier : headIdentifier);
                    headModel.emitItemQuads(stack, randomSupplier, context);
                    //context.fallbackConsumer().accept(headModel);
                    context.popTransform();
                }
                for (Map.Entry<Modifier, Integer> entry : MaterialisationUtils.getToolModifiers(stack).entrySet()) {
                    if (entry.getValue() > 0) {
                        Identifier modifierModelId = getModifierModel(entry.getKey());
                        if (modifierModelId != null) {
                            BakedModel modifierModel = modelManager.getModel(modifierModelId);
                            modifierModel.emitItemQuads(stack, randomSupplier, context);
                            //context.fallbackConsumer().accept(modifierModel);
                        }
                    }
                }
            } catch (NullPointerException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        
        public Identifier getModifierModel(Modifier modifier) {
            Optional<Identifier> identifier = modifierModelMap.get(modifier);
            if (identifier != null) return identifier.orElse(null);
            Identifier modelIdentifier = modifier.getModelIdentifier(tool.getToolType());
            modifierModelMap.put(modifier, Optional.ofNullable(modelIdentifier));
            identifier = modifierModelMap.get(modifier);
            return identifier.orElse(null);
        }
        
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }
        
        @Override
        public boolean hasDepth() {
            return false;
        }
        
        @Override
        public boolean isSideLit() {
            return false;
        }
        
        @Override
        public boolean isBuiltin() {
            return false;
        }

        @Override
        public Sprite getParticleSprite() {
            return MinecraftClient.getInstance().getBakedModelManager().getMissingModel().getParticleSprite();
        }
        
        private static final Lazy<ModelTransformation> ITEM_HANDHELD = new Lazy<>(() -> {
            try {
                Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("minecraft:models/item/handheld.json")).get();
                return JsonUnbakedModel.deserialize(new BufferedReader(new InputStreamReader(resource.getInputStream()))).getTransformations();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        });
        
        @Override
        public ModelTransformation getTransformation() {
            return ITEM_HANDHELD.get();
        }
        
        @Override
        public ModelOverrideList getOverrides() {
            return ModelOverrideList.EMPTY;
        }
    }
}
