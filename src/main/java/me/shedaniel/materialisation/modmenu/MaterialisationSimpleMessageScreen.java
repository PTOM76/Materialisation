package me.shedaniel.materialisation.modmenu;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import static me.shedaniel.materialisation.modmenu.MaterialisationMaterialsScreen.overlayBackground;

@SuppressWarnings("CanBeFinal")
public class MaterialisationSimpleMessageScreen extends Screen {
    private Screen parent;
    private String text;
    
    public MaterialisationSimpleMessageScreen(Screen parent, Text title, String text) {
        super(title);
        this.parent = parent;
        this.text = text;
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
        addSelectableChild(ButtonWidget.builder(Text.translatable("gui.back"), var1 -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(4, 4, 75, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        overlayBackground(0, 0, width, height, 32, 32, 32, 255, 255);
        overlayBackground(0, 0, width, 28, 64, 64, 64, 255, 255);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(0, 28 + 4, 0.0F).color(0, 0, 0, 0).texture(0.0F, 1.0F);
        builder.vertex(this.width, 28 + 4, 0.0F).color(0, 0, 0, 0).texture(1.0F, 1.0F);
        builder.vertex(this.width, 28, 0.0F).color(0, 0, 0, 255).texture(1.0F, 0.0F);
        builder.vertex(0, 28, 0.0F).color(0, 0, 0, 255).texture(0.0F, 0.0F);
        BufferRenderer.drawWithGlobalProgram(builder.end());
        RenderSystem.disableBlend();
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 16777215);
        int y = 40;
        for (String s : text.split("\n")) {
            for (OrderedText s1 : textRenderer.wrapLines(Text.literal(s), width - 20)) {
                context.drawText(textRenderer, s1, width / 2 - textRenderer.getWidth(s1) / 2, y, 16777215, false);
                y += 9;
            }
        }
        super.render(context, mouseX, mouseY, delta);
    }
}
