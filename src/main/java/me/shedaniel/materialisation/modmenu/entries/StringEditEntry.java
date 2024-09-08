package me.shedaniel.materialisation.modmenu.entries;

import com.google.common.collect.Lists;
import me.shedaniel.materialisation.modmenu.MaterialisationCreateOverrideListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("CanBeFinal")
public class StringEditEntry extends MaterialisationCreateOverrideListWidget.EditEntry {
    
    private String defaultValue;
    private Pattern validation;
    private TextFieldWidget buttonWidget;
    private ButtonWidget resetButton;
    private List<Element> widgets;
    
    public StringEditEntry(String s, String defaultValue) {
        this(s, defaultValue, null);
    }
    
    public StringEditEntry(String s, String defaultValue, Pattern validation) {
        super(s);
        this.defaultValue = defaultValue;
        this.validation = validation;
        this.buttonWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 150, 16, NarratorManager.EMPTY) {
            @Override
            public void renderWidget(DrawContext context, int int_1, int int_2, float float_1) {
                setEditableColor(isValid() ? 0xe0e0e0 : 0xff5555);
                super.renderWidget(context, int_1, int_2, float_1);
            }
        };
        buttonWidget.setMaxLength(1000);
        buttonWidget.setText(defaultValue);
        buttonWidget.setChangedListener(ss -> StringEditEntry.this.setEdited(!ss.equals(defaultValue)));
        this.resetButton = ButtonWidget.builder(Text.translatable("text.cloth-config.reset_value"), widget -> {
            buttonWidget.setText(StringEditEntry.this.defaultValue);
            StringEditEntry.this.setEdited(false);
        }).dimensions(0, 0, MinecraftClient.getInstance().textRenderer.getWidth(Text.translatable("text.cloth-config.reset_value")) + 6, 20).build();
        this.widgets = Lists.newArrayList(buttonWidget, resetButton);
    }
    
    @Override
    public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        this.resetButton.setY(y);
        this.buttonWidget.setY(y + 2);
        this.resetButton.setX(x + entryWidth - resetButton.getWidth());
        this.buttonWidget.setX(x + entryWidth - 150 + 2);
        this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2 - 4);
        resetButton.render(context, mouseX, mouseY, delta);
        buttonWidget.render(context, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends Selectable> narratables() {
        return Collections.emptyList();
    }

    @Override
    public String getDefaultValueString() {
        return defaultValue;
    }
    
    @Override
    public String getValueString() {
        return buttonWidget.getText();
    }
    
    @Override
    public String getValue() {
        return buttonWidget.getText();
    }
    
    @Override
    public boolean isValid() {
        return validation == null || validation.matcher(getValue()).matches();
    }
    
    @Override
    public List<? extends Element> children() {
        return widgets;
    }
}
