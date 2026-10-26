package net.yigitguven.petting.client;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.nio.file.Path;

public class PettingConfigurationScreen extends Screen {
    private static final URI WIKI_URI = URI.create("https://github.com/yigit-guven/Petting/wiki/Configuration");
    private final Screen parent;

    public PettingConfigurationScreen(Screen parent) {
        super(Component.translatable("petting.configuration.title", "Petting"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.addRenderableWidget(new StringWidget(
                centerX - 160, startY, 320, 20,
                Component.translatable("petting.configuration.title", "Petting").withStyle(ChatFormatting.BOLD),
                this.font
        ));

        this.addRenderableWidget(new StringWidget(
                centerX - 160, startY + 25, 320, 20,
                Component.literal("Settings are configured in serverconfig/petting-synced.toml and config/petting-client.toml").withStyle(ChatFormatting.GRAY),
                this.font
        ));

        this.addRenderableWidget(Button.builder(
                Component.literal("Open Config Folder"),
                b -> {
                    Path configDir = this.minecraft.gameDirectory.toPath().resolve("config");
                    Util.getPlatform().openFile(configDir.toFile());
                }
        ).bounds(centerX - 100, startY + 60, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("petting.configuration.wiki_link"),
                b -> ConfirmLinkScreen.confirmLinkNow(WIKI_URI.toString(), this, true)
        ).bounds(centerX - 100, startY + 85, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                b -> this.onClose()
        ).bounds(centerX - 100, startY + 120, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
