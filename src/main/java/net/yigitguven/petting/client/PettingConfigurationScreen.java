package net.yigitguven.petting.client;

import com.mojang.blaze3d.Blaze3D;
import net.minecraft.ChatFormatting;
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
                Component.literal("Settings are configured in config/petting-common.toml and petting-client.toml").withStyle(ChatFormatting.GRAY),
                this.font
        ));

        // Open Config Folder Button
        this.addRenderableWidget(Button.builder(
                Component.literal("Open Config Folder"),
                b -> {
                    Path configDir = this.minecraft.gameDirectory.toPath().resolve("config");
                    Blaze3D.openPath(configDir);
                }
        ).bounds(centerX - 100, startY + 60, 200, 20).build());

        // Open Wiki Button
        this.addRenderableWidget(Button.builder(
                Component.translatable("petting.configuration.wiki_link"),
                b -> ConfirmLinkScreen.confirmLinkNow(this, WIKI_URI, true)
        ).bounds(centerX - 100, startY + 85, 200, 20).build());

        // Done Button
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                b -> this.onClose()
        ).bounds(centerX - 100, startY + 120, 200, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }
}
