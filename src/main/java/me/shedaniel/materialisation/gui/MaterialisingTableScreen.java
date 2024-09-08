package me.shedaniel.materialisation.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.materialisation.ModReference;
import me.shedaniel.materialisation.network.RenamePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
public class MaterialisingTableScreen extends MaterialisingScreenBase<MaterialisingTableScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(ModReference.MOD_ID, "textures/gui/container/materialising_table.png");
    private TextFieldWidget nameField;
    
    public MaterialisingTableScreen(MaterialisingTableScreenHandler container, PlayerInventory inventory, Text title) {
        super(container, inventory, title, TEXTURE);
        this.titleX = 60;
    }
    
    @Override
    protected void setup() {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        this.nameField = new TextFieldWidget(this.textRenderer, x + 38, y + 24, 103, 12, Text.translatable("container.repair"));
        this.nameField.setFocusUnlocked(false);
        this.nameField.setEditableColor(-1);
        this.nameField.setUneditableColor(-1);
        this.nameField.setFocused(false); // setHasBorder
        this.nameField.setMaxLength(35);
        this.nameField.setChangedListener(this::onRenamed);
        this.nameField.setDrawsBackground(false);
        this.addDrawableChild(this.nameField);
        this.setInitialFocus(this.nameField);
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String string = this.nameField.getText();
        this.init(client, width, height);
        this.nameField.setText(string);
    }
    
    @Override
    public void removed() {
        super.removed();
        this.handler.removeListener(this);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            assert this.client != null;
            assert this.client.player != null;
            this.client.player.closeHandledScreen();
        }
        return this.nameField.keyPressed(keyCode, scanCode, modifiers) && this.nameField.isFocused() || super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void onRenamed(String name) {
        if (!name.isEmpty()) {
            String string = name;
            Slot slot_1 = this.handler.getSlot(2);
            if (slot_1 != null && slot_1.hasStack() && !slot_1.getStack().contains(DataComponentTypes.CUSTOM_NAME) && name.equals(slot_1.getStack().getName().getString())) {
                string = "";
            }
            
            this.handler.setNewItemName(string);
            ClientPlayNetworking.send(new RenamePayload(string));
        }
    }
    
    protected void render(DrawContext content, int mouseX, int mouseY) {
        RenderSystem.disableBlend();
        content.drawText(textRenderer, this.title, 6, 6, 4210752, false);
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int posX = x;
        int posY = y;
        context.drawTexture(TEXTURE, posX, posY, 0, 0, this.backgroundWidth, this.backgroundHeight);
        context.drawTexture(TEXTURE, posX + 34, posY + 20, 0, this.backgroundHeight + (this.handler.getSlot(0).hasStack() ? 0 : 16), 110, 16);
        if ((this.handler.getSlot(0).hasStack() || this.handler.getSlot(1).hasStack()) && !this.handler.getSlot(2).hasStack()) {
            context.drawTexture(TEXTURE, posX + 99, posY + 45, this.backgroundWidth, 0, 28, 21);
        }
        this.nameField.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (slotId == 2) {
            this.nameField.setChangedListener(null);
            this.nameField.setText(!handler.getSlot(slotId).hasStack() ? "" : stack.getName().getString());
            this.nameField.setEditable(!stack.isEmpty());
            this.nameField.setChangedListener(this::onRenamed);
        }
    }

    public static Identifier getTEXTURE() {
        return TEXTURE;
    }
}
