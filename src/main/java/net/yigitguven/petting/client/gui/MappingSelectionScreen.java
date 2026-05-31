package net.yigitguven.petting.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public class MappingSelectionScreen extends Screen {
    private final Component title;
    private final java.util.List<String> keys;
    private final java.util.List<Component> labels;
    private final java.util.function.Consumer<String> callback;
    private int selectedIndex;

    protected MappingSelectionScreen(Component title, java.util.List<String> keys, java.util.List<Component> labels, int selectedIndex, java.util.function.Consumer<String> callback) {
        super(title);
        this.title = title;
        this.keys = keys;
        this.labels = labels;
        this.callback = callback;
        this.selectedIndex = selectedIndex;
    }

    @Override
    protected void init() {
        int columns = keys.size() > 4 || this.height < 240 ? 2 : 1;
        int rows = (keys.size() + columns - 1) / columns;
        int cardWidth = Math.min(360, this.width - 24);
        int buttonWidth = columns == 1 ? 200 : (cardWidth - 8) / 2;
        int x = (this.width - cardWidth) / 2;
        int y = Math.max(28, (this.height - (rows * 24 + 28)) / 2);

        for (int i = 0; i < keys.size(); i++) {
            final String key = keys.get(i);
            final Component label = labels.get(i);
            final int column = columns == 1 ? 0 : i / rows;
            final int row = columns == 1 ? i : i % rows;
            int xx = columns == 1 ? (this.width - buttonWidth) / 2 : x + column * (buttonWidth + 8);
            int yy = y + row * 22;
            this.addRenderableWidget(Button.builder(label, b -> {
                callback.accept(key);
                Minecraft.getInstance().setScreen(null);
            }).bounds(xx, yy, buttonWidth, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.translatable("screen.petting.settings.close"), b -> Minecraft.getInstance().setScreen(null))
                .bounds((this.width - buttonWidth) / 2, y + rows * 22 + 8, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int columns = keys.size() > 4 || this.height < 240 ? 2 : 1;
        int rows = (keys.size() + columns - 1) / columns;
        int cardWidth = Math.min(360, this.width - 24);
        int buttonWidth = columns == 1 ? 200 : (cardWidth - 8) / 2;
        int x = (this.width - cardWidth) / 2;
        int y = Math.max(28, (this.height - (rows * 24 + 28)) / 2);
        if (selectedIndex >= 0 && selectedIndex < keys.size()) {
            int column = columns == 1 ? 0 : selectedIndex / rows;
            int row = columns == 1 ? selectedIndex : selectedIndex % rows;
            int xx = columns == 1 ? (this.width - buttonWidth) / 2 : x + column * (buttonWidth + 8);
            int yy = y + row * 22;
            guiGraphics.fill(xx - 2, yy - 2, xx + buttonWidth + 2, yy + 22, 0x44FFFFFF);
        }
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Up (keycode 265), Down (264), Enter (257 or 335)
        if (keyCode == 265) {
            int ni = Math.max(0, selectedIndex - 1);
            setSelected(ni);
            return true;
        } else if (keyCode == 264) {
            int ni = Math.min(keys.size() - 1, selectedIndex + 1);
            setSelected(ni);
            return true;
        } else if (keyCode == 257 || keyCode == 335) {
            if (selectedIndex >= 0 && selectedIndex < keys.size()) {
                callback.accept(keys.get(selectedIndex));
                Minecraft.getInstance().setScreen(null);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void setSelected(int idx) {
        // move focus to button at idx
        this.selectedIndex = idx;
    }
}
