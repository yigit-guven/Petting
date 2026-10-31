package net.yigitguven.petting.client;

import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;

import java.net.URI;

public class PettingConfigurationScreen extends OptionsSubScreen {
    private static final URI WIKI_URI = URI.create("https://github.com/yigit-guven/Petting/wiki/Configuration");

    public PettingConfigurationScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("petting.configuration.title", "Petting"));
    }

    @Override
    protected void addOptions() {
        if (this.list != null) {
            Button wikiButton = Button.builder(Component.translatable("petting.configuration.open_wiki"), button ->
                    ConfirmLinkScreen.confirmLinkNow(this, WIKI_URI)
            ).width(200).build();

            Button openFolderButton = Button.builder(Component.translatable("petting.configuration.open_config_folder"), button ->
                    com.mojang.blaze3d.Blaze3D.openPath(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir())
            ).width(200).build();

            this.list.addSmall(wikiButton, openFolderButton);
        }
    }
}
