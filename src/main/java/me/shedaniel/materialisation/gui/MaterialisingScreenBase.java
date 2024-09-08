package me.shedaniel.materialisation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
public class MaterialisingScreenBase<T extends AbstractMaterialisingHandlerBase> extends HandledScreen<T> implements ScreenHandlerListener {
    private Identifier texture;
    
    public MaterialisingScreenBase(T handler, PlayerInventory inventory, Text title, Identifier texture) {
        super(handler, inventory, title);
        this.texture = texture;
    }
    
    protected void setup() {
    }
    
    @Override
    protected void init() {
        super.init();
        this.setup();
        this.handler.addListener(this);
    }
    
    @Override
    public void removed() {
        super.removed();
        this.handler.removeListener(this);
    }
    
    @Override
    public void render(DrawContext content, int mouseX, int mouseY, float delta) {
        this.renderBackground(content);
        super.render(content, mouseX, mouseY, delta);
        RenderSystem.disableBlend();
        this.drawMouseoverTooltip(content, mouseX, mouseY);
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.texture);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(this.texture, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(this.texture, i + 59, j + 20, 0, this.backgroundHeight + (this.handler.getSlot(0).hasStack() ? 0 : 16), 110, 16);
        if ((this.handler.getSlot(0).hasStack() || this.handler.getSlot(1).hasStack()) && !this.handler.getSlot(2).hasStack()) {
            context.drawTexture(this.texture, i + 99, j + 45, this.backgroundWidth, 0, 28, 21);
        }
    }

/*
    @Override
    public void onHandlerRegistered(ScreenHandler handler) {
        this.onSlotUpdate(handler, 0, handler.getSlot(0).getStack());
    }

 */
    
    @Override
    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }
    
    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
    }
}
