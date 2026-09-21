package net.yigitguven.petting.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

import java.net.URI;
import java.util.Locale;

public class PettingConfigurationScreen extends OptionsSubScreen {
    private static final URI WIKI_URI = URI.create("https://github.com/yigit-guven/Petting/wiki/Configuration");
    private final ModContainer mod;

    public PettingConfigurationScreen(ModContainer mod, Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable(mod.getModId() + ".configuration.title", mod.getModInfo().getDisplayName()));
        this.mod = mod;
    }

    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    private static final String SECTION = LANG_PREFIX + "section";

    @Override
    protected void addOptions() {

        for (ModConfig.Type type : ModConfig.Type.values()) {
            boolean headerAdded = false;
            for (ModConfig modConfig : ModConfigs.getConfigSet(type)) {
                if (modConfig.getModId().equals(mod.getModId())) {
                    String typeName = type.name().toLowerCase(Locale.ROOT);
                    if (!headerAdded) {
                        list.addSmall(new StringWidget(310, Button.DEFAULT_HEIGHT,
                                Component.translatable(LANG_PREFIX + typeName).withStyle(ChatFormatting.UNDERLINE), font), null);
                        headerAdded = true;
                    }

                    Component label = Component.translatable(LANG_PREFIX + "type." + typeName);
                    Component buttonText = Component.translatable(SECTION, label);
                    Component screenTitle = Component.translatable(LANG_PREFIX + "title." + typeName, mod.getModInfo().getDisplayName());

                    Button btn = Button.builder(buttonText, button ->
                            minecraft.gui.setScreen(new ConfigurationScreen.ConfigurationSectionScreen(this, type, modConfig, screenTitle))
                    ).width(310).build();

                    net.minecraft.network.chat.MutableComponent tooltip = Component.empty();
                    if (!((net.neoforged.neoforge.common.ModConfigSpec) modConfig.getSpec()).isLoaded()) {
                        tooltip.append(ConfigurationScreen.TOOLTIP_CANNOT_EDIT_NOT_LOADED).append(Component.literal("\n\n"));
                        btn.active = false;
                    } else if (type == ModConfig.Type.SERVER && minecraft.getCurrentServer() != null && (!minecraft.hasSingleplayerServer() || !minecraft.getSingleplayerServer().isPublished())) {
                        tooltip.append(ConfigurationScreen.TOOLTIP_CANNOT_EDIT_THIS_WHILE_ONLINE).append(Component.literal("\n\n"));
                        btn.active = false;
                    } else if (type == ModConfig.Type.SERVER && minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer().isPublished()) {
                        tooltip.append(ConfigurationScreen.TOOLTIP_CANNOT_EDIT_THIS_WHILE_OPEN_TO_LAN).append(Component.literal("\n\n"));
                        btn.active = false;
                    }
                    tooltip.append(Component.translatable("neoforge.configuration.uitext.filenametooltip", modConfig.getFileName()).withStyle(ChatFormatting.GRAY));
                    btn.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
                    list.addSmall(btn, null);
                }
            }
        }
    }

    private Button wikiBtn;

    @Override
    protected void init() {
        super.init();
        int lineY = this.height - this.layout.getFooterHeight();
        Component text = Component.translatable("petting.configuration.wiki_link").withStyle(ChatFormatting.UNDERLINE);
        this.wikiBtn = Button.builder(text, ConfirmLinkScreen.confirmLink(this, WIKI_URI))
                .bounds(this.width / 2 - 155, lineY - 20 - 4, 310, 20)
                .build(builder -> new Button.Plain(builder) {
                    @Override
                    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
                        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
                    }
                });
        this.addRenderableOnly(this.wikiBtn);
        ((java.util.List<net.minecraft.client.gui.components.events.GuiEventListener>) this.children()).add(0, this.wikiBtn);
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        if (this.wikiBtn != null) {
            int lineY = this.height - this.layout.getFooterHeight();
            this.wikiBtn.setPosition(this.width / 2 - 155, lineY - 20 - 4);
        }
    }
}
