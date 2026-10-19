package net.yigitguven.petting.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class PetButton extends Button {

    public PetButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static Builder petBuilder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Component message;
        private final OnPress onPress;
        private @Nullable Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;

        public Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public PetButton build() {
            PetButton button = new PetButton(this.x, this.y, this.width, this.height, this.message, this.onPress);
            if (this.tooltip != null) {
                button.setTooltip(this.tooltip);
            }
            return button;
        }
    }
}
