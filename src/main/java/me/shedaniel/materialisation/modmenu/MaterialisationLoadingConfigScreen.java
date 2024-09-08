package me.shedaniel.materialisation.modmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.materialisation.config.ConfigHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;

@SuppressWarnings("CanBeFinal")
public class MaterialisationLoadingConfigScreen extends Screen {

    private MaterialisationMaterialsScreen previousScreen;

    public MaterialisationLoadingConfigScreen(MaterialisationMaterialsScreen previousScreen) {
        super(Text.translatable("config.title.materialisation.loading"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void tick() {
        super.tick();
        if (!ConfigHelper.loading) {
            MinecraftClient.getInstance().setScreen(new MaterialisationMaterialsScreen(previousScreen.parent));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackgroundTexture(context);
        //overlayBackground(0, 0, width, 28, 64, 64, 64, 255, 255);
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
        super.render(context, mouseX, mouseY, delta);
    }
}
