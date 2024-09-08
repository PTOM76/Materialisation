package me.shedaniel.materialisation.modmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.materialisation.api.PartMaterial;
import me.shedaniel.materialisation.api.PartMaterials;
import me.shedaniel.materialisation.config.ConfigHelper;
import me.shedaniel.materialisation.config.ConfigPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class MaterialisationMaterialsScreen extends Screen {
    Screen parent;
    private Object lastDescription;
    private MaterialisationMaterialListWidget materialList;
    private MaterialisationDescriptionListWidget descriptionList;
    private ButtonWidget installButton, reloadButton, backButton;
    
    protected MaterialisationMaterialsScreen(Screen parent) {
        super(Text.translatable("config.title.materialisation"));
        this.parent = parent;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (ConfigHelper.loading) {
            MinecraftClient.getInstance().setScreen(new MaterialisationLoadingConfigScreen(this));
        }
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            assert client != null;
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    protected void init() {
        super.init();
        addDrawableChild(materialList = new MaterialisationMaterialListWidget(client, width / 2 - 10, height, 28 + 5, height - 5));
        addDrawableChild(descriptionList = new MaterialisationDescriptionListWidget(client, width / 2 - 10, height, 28 + 5, height - 5));
        addSelectableChild(installButton = ButtonWidget.builder(Text.translatable("config.button.materialisation.install"), var1 -> {
            assert client != null;
            client.setScreen(new MaterialisationInstallScreen(this));
        }).dimensions(width - 104, 4, 100, 20).build());
        addSelectableChild(reloadButton = ButtonWidget.builder(Text.translatable("config.button.materialisation.reload"), var1 -> {
            if (!ConfigHelper.loading) {
                MinecraftClient.getInstance().setScreen(new MaterialisationLoadingConfigScreen(this));
                ConfigHelper.loadConfigAsync();
            }
        }).dimensions(59, 4, 85, 20).build());
        addSelectableChild(backButton = ButtonWidget.builder(Text.translatable("gui.back"), var1 -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(4, 4, 50, 20).build());
        materialList.setLeftPos(5);
        descriptionList.setLeftPos(width / 2 + 5);
        if (lastDescription != null) {
            if (lastDescription instanceof ConfigPack)
                descriptionList.addPack(((ConfigPack) lastDescription).getConfigPackInfo(), (ConfigPack) lastDescription);
            if (lastDescription instanceof PartMaterial)
                descriptionList.addMaterial(this, (PartMaterial) lastDescription);
        }
        ConfigPack defaultPack = PartMaterials.getDefaultPack();
        PartMaterials.getMaterialPacks().forEach(materialsPack -> {
            if (materialsPack == defaultPack)
                return;
            materialList.addItem(new MaterialisationMaterialListWidget.PackEntry(materialsPack.getConfigPackInfo()) {
                @Override
                public List<? extends Selectable> narratables() {
                    return Collections.emptyList();
                }

                @Override
                public void onClick() {
                    lastDescription = materialsPack;
                    descriptionList.addPack(materialsPack.getConfigPackInfo(), materialsPack);
                }
            });
            materialsPack.getKnownMaterials().forEach(partMaterial -> materialList.addItem(new MaterialisationMaterialListWidget.MaterialEntry(partMaterial) {
                @Override
                public List<? extends Selectable> narratables() {
                    return Collections.emptyList();
                }

                @Override
                public void onClick() {
                    lastDescription = partMaterial;
                    descriptionList.addMaterial(MaterialisationMaterialsScreen.this, partMaterial);
                }
            }));
        });
        if (defaultPack.getKnownMaterials().count() > 0) {
            materialList.addItem(new MaterialisationMaterialListWidget.PackEntry(defaultPack.getConfigPackInfo()) {
                @Override
                public List<? extends Selectable> narratables() {
                    return Collections.emptyList();
                }

                @Override
                public void onClick() {
                    lastDescription = defaultPack;
                    descriptionList.addPack(defaultPack.getConfigPackInfo(), defaultPack);
                }
            });
            defaultPack.getKnownMaterials().forEach(partMaterial -> materialList.addItem(new MaterialisationMaterialListWidget.MaterialEntry(partMaterial) {
                @Override
                public List<? extends Selectable> narratables() {
                    return Collections.emptyList();
                }

                @Override
                public void onClick() {
                    lastDescription = partMaterial;
                    descriptionList.addMaterial(MaterialisationMaterialsScreen.this, partMaterial);
                }
            }));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);
        materialList.render(context, mouseX, mouseY, delta);
        descriptionList.render(context, mouseX, mouseY, delta);
        //overlayBackground(0, 0, width, 28, 64, 64, 64, 255, 255);
        //overlayBackground(0, height - 5, width, height, 64, 64, 64, 255, 255);
        installButton.render(context, mouseX, mouseY, delta);
        reloadButton.render(context, mouseX, mouseY, delta);
        backButton.render(context, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(0, 28 + 4, 0.0F).color(0, 0, 0, 0).texture(0.0F, 1.0F);
        builder.vertex(this.width, 28 + 4, 0.0F).color(0, 0, 0, 0).texture(1.0F, 1.0F);
        builder.vertex(this.width, 28, 0.0F).color(0, 0, 0, 255).texture(1.0F, 0.0F);
        builder.vertex(0, 28, 0.0F).color(0, 0, 0, 255).texture(0.0F, 0.0F);
        BufferRenderer.drawWithGlobalProgram(builder.end());
        RenderSystem.disableBlend();

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 16777215);
    }
    
}
