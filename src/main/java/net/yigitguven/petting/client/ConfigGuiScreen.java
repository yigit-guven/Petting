package net.yigitguven.petting.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.File;

public class ConfigGuiScreen extends Screen {
    private final Screen previousScreen;

    public ConfigGuiScreen(Screen previousScreen) {
        super(Component.literal("Petting Configuration"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Open Config Folder"), (button) -> {
            File configDir = FabricLoader.getInstance().getConfigDir().toFile();
            Util.getPlatform().openUri(configDir.toURI());
        }).bounds(centerX - 100, centerY - 20, 200, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Back"), (button) -> {
            this.minecraft.setScreen(this.previousScreen);
        }).bounds(centerX - 100, centerY + 20, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics); 
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        
        guiGraphics.drawCenteredString(this.font, "Edit 'petting.json' and restart the game/server to apply.", 
                this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }
}
