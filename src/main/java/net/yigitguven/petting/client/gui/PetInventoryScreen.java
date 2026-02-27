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
import net.yigitguven.petting.util.PetInventoryUtil;


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
    private static final ResourceLocation SWORD_ICON = new ResourceLocation("minecraft", "textures/item/iron_sword.png");
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation("minecraft", "textures/item/barrier.png");

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        // 1. Draw "Modern Dark Glass" Background
        RenderSystem.enableBlend();
        // Main Container Shadow/Outer Glow
        guiGraphics.fill(i - 2, j - 2, i + this.imageWidth + 2, j + this.imageHeight + 2, 0x33000000);
        // Main Semi-Transparent Dark Glass
        int glassColor = 0xCC050505; // Very dark, high opacity glass
        guiGraphics.fill(i, j, i + this.imageWidth, j + this.imageHeight, glassColor);
        
        // Blue Neon Border (Main Window)
        int neonBlue = 0xFF3AB0FF;
        guiGraphics.hLine(i, i + this.imageWidth - 1, j, neonBlue); // Top
        guiGraphics.hLine(i, i + this.imageWidth - 1, j + this.imageHeight - 1, neonBlue); // Bottom
        guiGraphics.vLine(i, j, j + this.imageHeight - 1, neonBlue); // Left
        guiGraphics.vLine(i + this.imageWidth - 1, j, j + this.imageHeight - 1, neonBlue); // Right

        // 2. Draw Pet Portrait Area (Center)
        int portraitX = i + 53;
        int portraitY = j + 15;
        int portraitWidth = 70;
        int portraitHeight = 56;
        // Subtle background for portrait
        guiGraphics.fill(portraitX, portraitY, portraitX + portraitWidth, portraitY + portraitHeight, 0x22FFFFFF);
        // Border for portrait
        drawSlotBorder(guiGraphics, portraitX, portraitY, portraitWidth, portraitHeight, 0x443AB0FF);

        Entity pet = this.menu.getPet();
        if (pet instanceof LivingEntity living) {
            float bbWidth = living.getBbWidth();
            float bbHeight = living.getBbHeight();
            float scale = 30.0F / Math.max(1.0F, Math.max(bbWidth, bbHeight));
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, portraitX + (portraitWidth / 2), portraitY + portraitHeight - 5, (int)scale, (float)(portraitX + (portraitWidth / 2)) - mouseX, (float)(portraitY + portraitHeight / 2 - 30) - mouseY, living);
        }

        // 3. Draw Equipment Slots (Using custom boxes)
        // Armor (Left 2x2)
        drawCustomSlot(guiGraphics, i + 16, j + 16); // Head
        drawCustomSlot(guiGraphics, i + 34, j + 16); // Chest
        drawCustomSlot(guiGraphics, i + 16, j + 34); // Legs
        drawCustomSlot(guiGraphics, i + 34, j + 34); // Feet
        
        // Right Column
        drawCustomSlot(guiGraphics, i + 141, j + 16); // Saddle
        drawCustomSlot(guiGraphics, i + 141, j + 34); // Mainhand
        drawCustomSlot(guiGraphics, i + 141, j + 52); // Offhand

        // 4. Draw Player Inventory Slots
        int playerInvY = j + 83;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                drawCustomSlot(guiGraphics, i + 7 + col * 18, playerInvY + row * 18);
            }
        }
        for (int col = 0; col < 9; ++col) {
            drawCustomSlot(guiGraphics, i + 7 + col * 18, playerInvY + 58);
        }

        // 5. Draw Ghost Icons
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.4F);
        if (pet instanceof Mob mob) {
            boolean headSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.HEAD);
            boolean chestSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.CHEST);
            boolean legsSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.LEGS);
            boolean feetSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.FEET);
            boolean mainSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            boolean offSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.OFFHAND);

            // Left Grid
            if (headSup) { if (this.menu.getSlot(1).getItem().isEmpty()) guiGraphics.blit(HELMET_ICON, i + 17, j + 17, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 17, j + 17, 0, 0, 16, 16, 16, 16); }

            if (chestSup) { if (this.menu.getSlot(2).getItem().isEmpty()) guiGraphics.blit(CHEST_ICON, i + 35, j + 17, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 35, j + 17, 0, 0, 16, 16, 16, 16); }

            if (legsSup) { if (this.menu.getSlot(3).getItem().isEmpty()) guiGraphics.blit(LEGS_ICON, i + 17, j + 35, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 17, j + 35, 0, 0, 16, 16, 16, 16); }

            if (feetSup) { if (this.menu.getSlot(4).getItem().isEmpty()) guiGraphics.blit(BOOTS_ICON, i + 35, j + 35, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 35, j + 35, 0, 0, 16, 16, 16, 16); }
            
            // Right Column
            if (this.menu.getSlot(0).getItem().isEmpty()) guiGraphics.blit(SADDLE_ICON, i + 142, j + 17, 0, 0, 16, 16, 16, 16);
            if (mainSup) { if (this.menu.getSlot(5).getItem().isEmpty()) guiGraphics.blit(SWORD_ICON, i + 142, j + 35, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 142, j + 35, 0, 0, 16, 16, 16, 16); }
            if (offSup) { if (this.menu.getSlot(6).getItem().isEmpty()) guiGraphics.blit(SHIELD_ICON, i + 142, j + 53, 0, 0, 16, 16, 16, 16); }
            else { guiGraphics.blit(BARRIER_ICON, i + 142, j + 53, 0, 0, 16, 16, 16, 16); }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawCustomSlot(GuiGraphics guiGraphics, int x, int y) {
        // Slot background
        guiGraphics.fill(x, y, x + 18, y + 18, 0x44FFFFFF);
        // Slot inner shadow
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0x22000000);
        // Neon border for slots (subtle)
        drawSlotBorder(guiGraphics, x, y, 18, 18, 0xAA3AB0FF);
    }

    private void drawSlotBorder(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.hLine(x, x + w - 1, y, color);
        guiGraphics.hLine(x, x + w - 1, y + h - 1, color);
        guiGraphics.vLine(x, y, y + h - 1, color);
        guiGraphics.vLine(x + w - 1, y, y + h - 1, color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x3AB0FF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }
}
