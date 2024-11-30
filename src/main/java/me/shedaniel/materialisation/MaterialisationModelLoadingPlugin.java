package me.shedaniel.materialisation;

import me.shedaniel.materialisation.api.Modifier;
import me.shedaniel.materialisation.api.PartMaterials;
import me.shedaniel.materialisation.api.ToolType;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Function;

public class MaterialisationModelLoadingPlugin implements ModelLoadingPlugin {
    private final List<Identifier> identifiers;

    private final Map<Identifier, UnbakedModel> unbakedModels = new HashMap<>();

    public MaterialisationModelLoadingPlugin(List<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public void onInitializeModelLoader(Context pluginContext) {

        for (Identifier identifier : identifiers) {
            Identifier handleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle");
            Identifier headIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head");
            pluginContext.addModels(handleIdentifier);
            pluginContext.addModels(headIdentifier);
            Identifier brightHandleIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_handle_bright");
            Identifier brightHeadIdentifier = Identifier.of(identifier.getNamespace(), identifier.getPath() + "_head_bright");
            pluginContext.addModels(brightHandleIdentifier);
            pluginContext.addModels(brightHeadIdentifier);
        }

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

        pluginContext.resolveModel().register((context -> {
            Identifier id = context.id();
            if (!id.getNamespace().equals(ModReference.MOD_ID)) return null;

            Identifier id2 = Identifier.of(id.getNamespace(), id.getPath().replace("item/", ""));

            return unbakedModels.get(id2);
        }));

        pluginContext.modifyModelOnLoad().register((original, context) -> {
            final ModelIdentifier modelIdentifier = context.topLevelId();

            if (modelIdentifier == null) return original;

            for (Identifier identifier : identifiers) {
                if (modelIdentifier.id().getNamespace().equals(identifier.getNamespace())
                        && modelIdentifier.id().getPath().equals(identifier.getPath())) {

                    UnbakedModel model = new UnbakedModel() {
                        @Override
                        public Collection<Identifier> getModelDependencies() {
                            return Collections.emptyList();
                        }

                        @Override
                        public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

                        }

                        @Override
                        public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
                            return new MaterialisationClient.DynamicToolBakedModel(identifier, Registries.ITEM.get(identifier));
                        }
                    };

                    unbakedModels.put(identifier, model);

                    return model;
                }
            }

            return original;
        });
    }
}
