package net.yigitguven.petting.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PetInventoryScreen extends AbstractContainerScreen<PetInventoryMenu> {
    private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("minecraft", "textures/gui/container/horse.png");

    public PetInventoryScreen(PetInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, HORSE_GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(HORSE_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
        
        // Draw slots highlight if needed, horse texture already has them at the right spots for our menu!
        // Saddle is at 8, 18. Armor at 8, 36. Storage starts at 80, 18.
    }
}
