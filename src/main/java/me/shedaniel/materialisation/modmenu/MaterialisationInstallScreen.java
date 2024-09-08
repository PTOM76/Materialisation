package me.shedaniel.materialisation.modmenu;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.shedaniel.materialisation.config.ConfigHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("CanBeFinal")
public class MaterialisationInstallScreen extends Screen {
    public static Identifier OPTIONS_BACKGROUND_TEXTURE = Identifier.of("textures/gui/options_background.png");

    public static final List<OnlinePack> ONLINE_PACKS = Lists.newArrayList();
    public static boolean loaded = false;
    public static ExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Materialisation"));
    public boolean loading = false;
    private Screen parent;
    private MaterialisationInstallListWidget listWidget;

    public ButtonWidget refreshButton, backButton, openFolderButton;

    protected MaterialisationInstallScreen(Screen parent) {
        super(Text.translatable("config.title.materialisation.install_new"));
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
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
        addDrawableChild(listWidget = new MaterialisationInstallListWidget(client, width, height, 28, height - 28, OPTIONS_BACKGROUND_TEXTURE));
        if (!loaded) {
            loaded = true;
            refresh();
        } else if (loading) {
            setUpRefresh();
        } else if (ONLINE_PACKS.isEmpty()) {
            listWidget.clearItemsPublic();
            listWidget.addItem(new MaterialisationInstallListWidget.EmptyEntry(10));
            listWidget.addItem(new MaterialisationInstallListWidget.FailedEntry());
        } else {
            listWidget.clearItemsPublic();
            listWidget.addItem(new MaterialisationInstallListWidget.EmptyEntry(10));
            for (OnlinePack onlinePack : ONLINE_PACKS) {
                listWidget.addItem(new MaterialisationInstallListWidget.PackEntry(listWidget, onlinePack));
            }
        }
        addSelectableChild(refreshButton = ButtonWidget.builder(Text.translatable("config.button.materialisation.refresh"), var1 -> {
            if (!loading)
                refresh();
        }).dimensions(4, 4, 100, 20).build());
        addSelectableChild(backButton = ButtonWidget.builder(Text.translatable("gui.back"), var1 -> {
            assert client != null;
            client.setScreen(parent);
        }).dimensions(4, height - 24, 100, 20).build());
        addSelectableChild(openFolderButton = ButtonWidget.builder(Text.translatable("config.button.materialisation.open_folder"), var1 -> Util.getOperatingSystem().open(ConfigHelper.MATERIALS_DIRECTORY)).dimensions(width - 104, 4, 100, 20).build());
    }

    public void refresh() {
        loading = true;
        setUpRefresh();
        executorService.shutdown();
        executorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Materialisation"));
        executorService.submit(() -> {
            ONLINE_PACKS.clear();
            try {
                URL url = new URL("https://raw.githubusercontent.com/shedaniel/MaterialisationData/master/packs.json");
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                JsonArray array = gson.fromJson(new InputStreamReader(url.openStream()), JsonArray.class);
                for (JsonElement jsonElement : array) {
                    OnlinePack onlinePack = gson.fromJson(jsonElement, OnlinePack.class);
                    ONLINE_PACKS.add(onlinePack);
                }
                listWidget.clearItemsPublic();
                listWidget.addItem(new MaterialisationInstallListWidget.EmptyEntry(10));
                for (OnlinePack onlinePack : ONLINE_PACKS) {
                    listWidget.addItem(new MaterialisationInstallListWidget.PackEntry(listWidget, onlinePack));
                }
            } catch (Exception e) {
                ONLINE_PACKS.clear();
                e.printStackTrace();
                listWidget.clearItemsPublic();
                listWidget.addItem(new MaterialisationInstallListWidget.EmptyEntry(10));
                listWidget.addItem(new MaterialisationInstallListWidget.FailedEntry());
            }
            loading = false;
        });
    }

    public void setUpRefresh() {
        listWidget.clearItemsPublic();
        listWidget.addItem(new MaterialisationInstallListWidget.EmptyEntry(10));
        listWidget.addItem(new MaterialisationInstallListWidget.LoadingEntry());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackgroundTexture(context);
        super.render(context, mouseX, mouseY, delta);
        listWidget.render(context, mouseX, mouseY, delta);
        //overlayBackground(0, height - 28, width, height, 64, 64, 64, 255, 255);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 16777215);
        refreshButton.render(context, mouseX, mouseY, delta);
        backButton.render(context, mouseX, mouseY, delta);
        openFolderButton.render(context, mouseX, mouseY, delta);
    }
}
