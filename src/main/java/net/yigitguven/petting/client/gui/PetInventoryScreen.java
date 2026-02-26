package net.yigitguven.petting.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PetInventoryScreen extends AbstractContainerScreen<PetInventoryMenu> {
    private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("minecraft", "textures/gui/container/horse.png");

    public PetInventoryScreen(PetInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
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

    private static final ResourceLocation SADDLE_ICON = new ResourceLocation("minecraft", "textures/item/saddle.png");
    private static final ResourceLocation HELMET_ICON = new ResourceLocation("minecraft", "textures/item/empty_armor_slot_helmet.png");
    private static final ResourceLocation CHEST_ICON = new ResourceLocation("minecraft", "textures/item/empty_armor_slot_chestplate.png");
    private static final ResourceLocation LEGS_ICON = new ResourceLocation("minecraft", "textures/item/empty_armor_slot_leggings.png");
    private static final ResourceLocation BOOTS_ICON = new ResourceLocation("minecraft", "textures/item/empty_armor_slot_boots.png");
    private static final ResourceLocation SHIELD_ICON = new ResourceLocation("minecraft", "textures/item/empty_armor_slot_shield.png");

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, HORSE_GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(HORSE_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
        
        Entity pet = this.menu.getPet();
        if (pet instanceof LivingEntity living) {
            // DYNAMIC SCALE: Fit to a 30x30 area
            float bbWidth = living.getBbWidth();
            float bbHeight = living.getBbHeight();
            float scale = 22.0F / Math.max(1.0F, Math.max(bbWidth, bbHeight));
            
            // Center of portrait area is approx i + 42
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, i + 42, j + 50, (int)scale, (float)(i + 42) - mouseX, (float)(j + 50 - 30) - mouseY, living);
        }

        // 1. Draw Slot Backgrounds (Boxes) for all 7 equipment slots
        RenderSystem.setShaderTexture(0, HORSE_GUI_TEXTURES);
        // Left: Saddle, Head, Chest
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 7, j + 17, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 7, j + 35, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 7, j + 53, 0, 166, 18, 18);
        // Right: Legs, Feet, Mainhand
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 60, j + 17, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 60, j + 35, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 60, j + 53, 0, 166, 18, 18);
        // Center Bottom: Offhand
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 33, j + 53, 0, 166, 18, 18);

        // 2. Draw Ghost Icons for clarity
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.4F);
        
        // Saddle (Slot 0)
        if (this.menu.getSlot(0).getItem().isEmpty()) {
            guiGraphics.blit(SADDLE_ICON, i + 8, j + 18, 0, 0, 16, 16, 16, 16);
        }

        if (pet instanceof Mob) {
            if (this.menu.getSlot(1).getItem().isEmpty()) guiGraphics.blit(HELMET_ICON, i + 8, j + 36, 0, 0, 16, 16, 16, 16);
            if (this.menu.getSlot(2).getItem().isEmpty()) guiGraphics.blit(CHEST_ICON, i + 8, j + 54, 0, 0, 16, 16, 16, 16);
            if (this.menu.getSlot(3).getItem().isEmpty()) guiGraphics.blit(LEGS_ICON, i + 61, j + 18, 0, 0, 16, 16, 16, 16);
            if (this.menu.getSlot(4).getItem().isEmpty()) guiGraphics.blit(BOOTS_ICON, i + 61, j + 36, 0, 0, 16, 16, 16, 16);
            if (this.menu.getSlot(6).getItem().isEmpty()) guiGraphics.blit(SHIELD_ICON, i + 34, j + 54, 0, 0, 16, 16, 16, 16);
        } else {
            // Darken equipment slots if not a Mob
            guiGraphics.fill(i + 7, j + 35, i + 25, j + 71, 0xAA000000); // Helmet/Chest
            guiGraphics.fill(i + 60, j + 17, i + 78, j + 71, 0xAA000000); // Legs/Feet/Main
            guiGraphics.fill(i + 33, j + 53, i + 51, j + 71, 0xAA000000); // Offhand
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        // 3. Storage Grid (Only if capability has storage)
        if (this.menu.getPetInventory().getSlots() > 1) {
            guiGraphics.blit(HORSE_GUI_TEXTURES, i + 79, j + 17, 0, 166, 90, 54);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
