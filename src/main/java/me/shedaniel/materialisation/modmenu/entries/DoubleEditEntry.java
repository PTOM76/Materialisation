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

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class DoubleEditEntry extends MaterialisationCreateOverrideListWidget.EditEntry {

    private double defaultValue;
    private TextFieldWidget buttonWidget;
    private ButtonWidget resetButton;
    private List<Element> widgets;
    private ParsePosition parsePosition = new ParsePosition(0);
    private static final DecimalFormat DF = new DecimalFormat("#.##");

    public DoubleEditEntry(String s, double defaultValue) {
        super(s);
        this.defaultValue = defaultValue;
        this.buttonWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 0, 0, 150, 16, NarratorManager.EMPTY) {
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                setEditableColor(isValid() ? 0xe0e0e0 : 0xff5555);
                super.renderWidget(context, mouseX, mouseY, delta);
            }
        };
        buttonWidget.setMaxLength(1000);
        buttonWidget.setText(DF.format(defaultValue));
        buttonWidget.setChangedListener(ss -> DoubleEditEntry.this.setEdited(!ss.equals(DF.format(defaultValue))));
        this.resetButton = ButtonWidget.builder(Text.translatable("text.cloth-config.reset_value"), widget -> {
            buttonWidget.setText(DF.format(defaultValue));
            DoubleEditEntry.this.setEdited(false);
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
        return DF.format(defaultValue);
    }

    @Override
    public String getValueString() {
        return DF.format(getValue());
    }

    @Override
    public Double getValue() {
        parsePosition.setIndex(0);
        Number value = DF.parse(buttonWidget.getText(), parsePosition);
        if (parsePosition.getIndex() != 0) {
            return value.doubleValue();
        }
        return defaultValue;
    }

    @Override
    public boolean isValid() {
        parsePosition.setIndex(0);
        String text = buttonWidget.getText();
        DF.parse(text, parsePosition);
        return parsePosition.getIndex() == text.length();
    }

    @Override
    public List<? extends Element> children() {
        return widgets;
    }
}
