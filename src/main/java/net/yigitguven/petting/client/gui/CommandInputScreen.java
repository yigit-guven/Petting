package net.yigitguven.petting.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CommandInputScreen extends Screen {
    private final Screen parent;
    private final String initialCommand;
    private final java.util.function.Consumer<String> onConfirm;
    private EditBox commandBox;

    public CommandInputScreen(Screen parent, String initialCommand, java.util.function.Consumer<String> onConfirm) {
        super(Component.translatable("screen.petting.mapping.cmd_title"));
        this.parent = parent;
        this.initialCommand = initialCommand == null ? "" : initialCommand;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;
        int bw = Math.min(240, this.width - 24);
        this.commandBox = new EditBox(this.font, cx - bw / 2, cy - 16, bw, 20,
                Component.translatable("screen.petting.mapping.cmd_hint"));
        this.commandBox.setMaxLength(256);
        this.commandBox.setValue(this.initialCommand);
        this.commandBox.setHint(Component.translatable("screen.petting.mapping.cmd_hint"));
        this.addRenderableWidget(this.commandBox);

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.petting.mapping.cmd_confirm"), b -> confirm())
                .bounds(cx - bw / 2, cy + 10, bw, 20).build());
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.petting.settings.close"), b -> cancel())
                .bounds(cx - bw / 2, cy + 34, bw, 20).build());

        this.setInitialFocus(this.commandBox);
    }

    private void confirm() {
        String cmd = this.commandBox.getValue().trim();
        this.onConfirm.accept(cmd);
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void cancel() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { confirm(); return true; } // Enter / NumpadEnter
        if (keyCode == 256) { cancel(); return true; } // Escape
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 36, 0xFFFFFF);
    }
}
