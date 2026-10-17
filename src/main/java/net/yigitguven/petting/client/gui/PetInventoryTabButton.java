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

    private static final ResourceLocation TAB_SPRITE =
            ResourceLocation.withDefaultNamespace("recipe_book/tab");
    private static final ResourceLocation TAB_SELECTED_SPRITE =
            ResourceLocation.withDefaultNamespace("recipe_book/tab_selected");

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
        playDownSound(Minecraft.getInstance().getSoundManager());
        this.onSelect.accept(this.tab);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Tab background and icons are rendered from PetInventoryScreen in proper layer order.
    }

    public void renderTabBackground(GuiGraphics guiGraphics) {
        if (!this.visible) {
            return;
        }
        boolean selected = this.screen.getCurrentTab() == this.tab;
        ResourceLocation sprite = selected ? TAB_SELECTED_SPRITE : TAB_SPRITE;
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
    }

    public void renderTabIcon(GuiGraphics guiGraphics) {
        if (!this.visible) {
            return;
        }
        ItemStack icon = this.iconSupplier.get();
        if (!icon.isEmpty()) {
            boolean selected = this.screen.getCurrentTab() == this.tab;
            int xOffset = selected ? 7 : 9;
            int yOffset = 5;
            guiGraphics.renderFakeItem(icon, this.getX() + xOffset, this.getY() + yOffset);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
