package me.shedaniel.materialisation.modmenu;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class MaterialisationErrorInstallScreen extends Screen {
    public static Identifier OPTIONS_BACKGROUND_TEXTURE = Identifier.of("textures/gui/options_background.png");

    private Screen parent;
    private Throwable throwable;
    private MaterialisationOverridesListWidget listWidget;

    public MaterialisationErrorInstallScreen(Screen parent, Throwable throwable) {
        super(Text.translatable("message.materialisation.installation_errored").formatted(Formatting.RED));
        this.parent = parent;
        this.throwable = throwable;
        throwable.printStackTrace();
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
        List<MaterialisationOverridesListWidget.Entry> entries = Lists.newArrayList();
        List<String> s = new ArrayList<>();
        s.add("An error occurred during materialisation pack installation: " + throwable.toString());
        for (StackTraceElement traceElement : throwable.getStackTrace()) {
            s.add("  at " + traceElement);
        }
        for (String s1 : s) {
            for (OrderedText s2 : textRenderer.wrapLines(Text.literal(s1), width - 40)) {
                if (s2 instanceof Text)
                    entries.add(new MaterialisationOverridesListWidget.TextEntry((Text)s2));
            }
        }
        addDrawableChild(listWidget = new MaterialisationOverridesListWidget(client, width, height, 28, height, OPTIONS_BACKGROUND_TEXTURE));
        for (MaterialisationOverridesListWidget.Entry entry : entries) {
            listWidget.addItem(entry);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackgroundTexture(context);
        listWidget.render(context, mouseX, mouseY, delta);
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
