package net.yigitguven.petting.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PetInventoryTabButton extends AbstractWidget {
    public static final int TAB_WIDTH = 35;
    public static final int TAB_HEIGHT = 27;

    private static final ResourceLocation RECIPE_BOOK_LOCATION =
            new ResourceLocation("textures/gui/recipe_book.png");

    private final PetInventoryTab tab;
    private final PetInventoryScreen screen;
    private final Supplier<ItemStack> iconSupplier;
    private final Consumer<PetInventoryTab> onSelect;

    public PetInventoryTabButton(
            int x, int y,
            PetInventoryTab tab,
            PetInventoryScreen screen,
            Component tooltipText,
            Supplier<ItemStack> iconSupplier,
            Consumer<PetInventoryTab> onSelect
    ) {
        super(x, y, TAB_WIDTH, TAB_HEIGHT, tooltipText);
        this.tab = tab;
        this.screen = screen;
        this.iconSupplier = iconSupplier;
        this.onSelect = onSelect;
        this.setTooltip(Tooltip.create(tooltipText));
    }

    public PetInventoryTab getTab() {
        return this.tab;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.playDownSound(Minecraft.getInstance().getSoundManager());
        this.onSelect.accept(this.tab);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    public void renderTabBackground(GuiGraphics graphics) {
        if (!this.visible) {
            return;
        }
        boolean selected = this.screen.getCurrentTab() == this.tab;
        int u = selected ? 188 : 153;
        int v = 2;
        graphics.blit(RECIPE_BOOK_LOCATION, this.getX(), this.getY(), u, v, this.width, this.height, 256, 256);
    }

    public void renderTabIcon(GuiGraphics graphics) {
        if (!this.visible) {
            return;
        }
        ItemStack icon = this.iconSupplier.get();
        if (!icon.isEmpty()) {
            boolean selected = this.screen.getCurrentTab() == this.tab;
            int xOffset = selected ? 7 : 9;
            int yOffset = 5;
            graphics.renderFakeItem(icon, this.getX() + xOffset, this.getY() + yOffset);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
