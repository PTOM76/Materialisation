package me.shedaniel.materialisation.modmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.materialisation.config.ConfigHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class MaterialisationInstallListWidget extends DynamicElementListWidget<MaterialisationInstallListWidget.Entry> {
    private PackEntry selected;

    public MaterialisationInstallListWidget(MinecraftClient client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
        super(client, width, height, top, bottom, backgroundLocation);
    }

    @Override
    public int getItemWidth() {
        return width - 11;
    }

    @Override
    protected int getScrollbarPosition() {
        return width - 6;
    }

    @Override
    public int addItem(Entry item) {
        return super.addItem(item);
    }

    public void clearItemsPublic() {
        clearItems();
    }

    @SuppressWarnings("CanBeFinal")
    public static class PackEntry extends Entry {
        private OnlinePack onlinePack;
        private MaterialisationInstallListWidget listWidget;
        private Rect2i bounds;
        private ButtonWidget clickWidget;

        public PackEntry(MaterialisationInstallListWidget listWidget, OnlinePack onlinePack) {
            this.listWidget = listWidget;
            this.onlinePack = onlinePack;
            this.clickWidget = ButtonWidget.builder(Text.translatable("config.button.materialisation.download"), var1 -> {
                MaterialisationInstallScreen screen = (MaterialisationInstallScreen) MinecraftClient.getInstance().currentScreen;
                MinecraftClient.getInstance().setScreen(new MaterialisationDownloadingScreen(Text.translatable("message.materialisation.fetching_file_data"), downloadingScreen -> {
                    long size;
                    String textSize;
                    String name;
                    URL url;
                    File file;
                    try {
                        url = new URL(onlinePack.download);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        size = connection.getContentLengthLong();
                        name = FilenameUtils.getName(url.getPath());
                        if (size <= 0) textSize = "0B";
                        else {
                            final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
                            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
                            textSize = new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
                        }
                        file = new File(ConfigHelper.MATERIALS_DIRECTORY, name);
                        if (file.exists()) throw new FileAlreadyExistsException("File already exists!");
                    } catch (Throwable e) {
                        assert screen != null;
                        downloadingScreen.queueNewScreen(new MaterialisationErrorInstallScreen(screen.getParent(), e));
                        return;
                    }
                    downloadingScreen.queueNewScreen(new ConfirmScreen(t -> {
                        if (t) {
                            MinecraftClient.getInstance().setScreen(new MaterialisationDownloadingScreen(Text.translatable("message.materialisation.file_is_downloading"), screen1 -> {
                                try {
                                    FileUtils.copyURLToFile(url, file);
                                    assert screen != null;
                                    screen1.queueNewScreen(new MaterialisationSimpleMessageScreen(screen.getParent(), Text.translatable("message.materialisation.file_downloaded"), I18n.translate("message.materialisation.file_is_downloaded")));
                                } catch (Exception e) {
                                    assert screen != null;
                                    screen1.queueNewScreen(new MaterialisationErrorInstallScreen(screen.getParent(), e));
                                }
                            }));
                            return;
                        }
                        MinecraftClient.getInstance().setScreen(screen.getParent());
                    }, Text.translatable("message.materialisation.do_you_want_to_download"), Text.translatable("message.materialisation.download_file_details", name, textSize)));
                }));
            }).dimensions(0, 0, 100, 20).build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            this.bounds = new Rect2i(x, y, entryWidth, entryHeight);
            if (listWidget.selectionVisible && listWidget.selected == this) {
                int itemMinX = listWidget.left + listWidget.width / 2 - listWidget.getItemWidth() / 2;
                int itemMaxX = itemMinX + listWidget.getItemWidth();
                        Tessellator tessellator = Tessellator.getInstance();
                float float_2 = listWidget.isFocused() ? 1.0F : 0.5F;
                RenderSystem.setShaderColor(float_2, float_2, float_2, 1.0F);
                BufferBuilder builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                builder.vertex(itemMinX, y + getItemHeight() + 2, 0.0F);
                builder.vertex(itemMaxX, y + getItemHeight() + 2, 0.0F);
                builder.vertex(itemMaxX, y - 2, 0.0F);
                builder.vertex(itemMinX, y - 2, 0.0F);
                BufferRenderer.drawWithGlobalProgram(builder.end());
                RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
                builder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                builder.vertex(itemMinX + 1, y + getItemHeight() + 1, 0.0F);
                builder.vertex(itemMaxX - 1, y + getItemHeight() + 1, 0.0F);
                builder.vertex(itemMaxX - 1, y - 1, 0.0F);
                builder.vertex(itemMinX + 1, y - 1, 0.0F);
                BufferRenderer.drawWithGlobalProgram(builder.end());
                    }
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            context.drawText(font, "§l" + onlinePack.displayName, x + 5, y + 5, 16777215, false);
            int i = 0;
            if (onlinePack.description != null)
                for (OrderedText text : MinecraftClient.getInstance().textRenderer.wrapLines(Text.literal(onlinePack.description), entryWidth)) {
                    context.drawText(font, MaterialisationCloth.color(text, Formatting.GRAY), x + 5, y + 7 + 9 + i * 9, 16777215, false);
                    i++;
                    if (i > 1)
                        break;
                }
            clickWidget.setX(x + entryWidth - 110);
            clickWidget.setY(y + entryHeight / 2 - 10);
            clickWidget.render(context, mouseX, mouseY, delta);
        }

        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            boolean a = super.mouseClicked(double_1, double_2, int_1);
            if (bounds.contains((int) double_1, (int) double_2) && int_1 == 0) {
                if (!a)
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                listWidget.selected = this;
            }
            return a;
        }

        @Override
        public int getItemHeight() {
            return 39;
        }

        @Override
        public List<? extends Selectable> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(clickWidget);
        }
    }

    public static class LoadingEntry extends Entry {
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            String string_3;
            switch ((int) (Util.getMeasuringTimeMs() / 300L % 4L)) {
                case 0:
                default:
                    string_3 = "O o o";
                    break;
                case 1:
                case 3:
                    string_3 = "o O o";
                    break;
                case 2:
                    string_3 = "o o O";
            }
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            context.drawCenteredTextWithShadow(font, Text.translatable("config.text.materialisation.loading_packs"), x + entryWidth / 2, y + 5, 16777215);
            context.drawCenteredTextWithShadow(font, string_3, x + entryWidth / 2, y + 5 + 9, 8421504);
        }

        @Override
        public int getItemHeight() {
            return 20;
        }

        @Override
        public List<? extends Selectable> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }

    public static class FailedEntry extends Entry {
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
            TextRenderer font = MinecraftClient.getInstance().textRenderer;
            context.drawCenteredTextWithShadow(font, Text.translatable("config.text.materialisation.failed"), x + entryWidth / 2, y + 5, 16777215);
        }

        @Override
        public int getItemHeight() {
            return 11;
        }

        @Override
        public List<? extends Selectable> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }

    public static class EmptyEntry extends Entry {
        @SuppressWarnings("CanBeFinal")
        private int height;

        public EmptyEntry(int height) {
            this.height = height;
        }

        @Override
        public void render(DrawContext context, int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, float v) {

        }

        @Override
        public int getItemHeight() {
            return height;
        }

        @Override
        public List<? extends Selectable> narratables() {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Element> children() {
            return Collections.emptyList();
        }
    }

    public static abstract class Entry extends DynamicElementListWidget.ElementEntry<Entry> {

    }
}
