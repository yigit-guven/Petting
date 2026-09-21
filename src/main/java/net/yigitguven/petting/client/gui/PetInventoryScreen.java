package net.yigitguven.petting.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.inventory.PetInventoryMenu;

public class PetInventoryScreen extends AbstractContainerScreen<PetInventoryMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Petting.MODID, "textures/gui/container/pet_inventory.png");
    private static final Identifier SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/slot");

    private float xMouse;
    private float yMouse;

    public PetInventoryScreen(PetInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 186);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 91;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = this.leftPos;
        int yo = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Dynamically draw slot background sprite for each active pet slot
        for (int i = 0; i < this.menu.getPetSlotCount(); i++) {
            Slot slot = this.menu.slots.get(i);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, xo + slot.x - 1, yo + slot.y - 1, 18, 18);
        }

        Mob pet = this.menu.getPet();
        if (pet != null) {
            float width = pet.getBbWidth();
            float height = pet.getBbHeight();
            float maxDim = Math.max(width, height);
            int scale = Math.max(14, Math.min(36, (int) (26.0F / Math.max(0.6F, maxDim))));

            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    graphics,
                    xo + 29, yo + 18,
                    xo + 147, yo + 88,
                    scale,
                    0.15F,
                    this.xMouse, this.yMouse,
                    pet
            );
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Mob pet = this.menu.getPet();
        Component petTitle = pet != null ? pet.getDisplayName() : this.title;
        graphics.text(this.font, petTitle, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);
    }
}
