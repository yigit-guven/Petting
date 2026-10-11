package net.yigitguven.petting.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PetInventoryTabButton extends AbstractWidget {
    public static final int TAB_WIDTH = 35;
    public static final int TAB_HEIGHT = 27;

    private static final Identifier TAB_SPRITE =
            Identifier.withDefaultNamespace("recipe_book/tab");
    private static final Identifier TAB_SELECTED_SPRITE =
            Identifier.withDefaultNamespace("recipe_book/tab_selected");

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
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        playButtonClickSound(Minecraft.getInstance().getSoundManager());
        this.onSelect.accept(this.tab);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean selected = this.screen.getCurrentTab() == this.tab;
        Identifier sprite = selected ? TAB_SELECTED_SPRITE : TAB_SPRITE;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);

        ItemStack icon = this.iconSupplier.get();
        if (!icon.isEmpty()) {
            graphics.item(icon, this.getX() + 9, this.getY() + 5);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
