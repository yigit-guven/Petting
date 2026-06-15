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
import net.yigitguven.petting.config.PettingConfig;

public class PetInventoryScreen extends AbstractContainerScreen<PetInventoryMenu> {
    private static final ResourceLocation HORSE_GUI_TEXTURES = new ResourceLocation("textures/gui/container/horse.png");
    private static final ResourceLocation SADDLE_ICON = new ResourceLocation("textures/item/saddle.png");
    private static final ResourceLocation HELMET_ICON = new ResourceLocation("textures/item/empty_armor_slot_helmet.png");
    private static final ResourceLocation CHEST_ICON = new ResourceLocation("textures/item/empty_armor_slot_chestplate.png");
    private static final ResourceLocation LEGS_ICON = new ResourceLocation("textures/item/empty_armor_slot_leggings.png");
    private static final ResourceLocation BOOTS_ICON = new ResourceLocation("textures/item/empty_armor_slot_boots.png");
    private static final ResourceLocation SHIELD_ICON = new ResourceLocation("textures/item/empty_armor_slot_shield.png");
    private static final ResourceLocation SWORD_ICON = new ResourceLocation("textures/item/iron_sword.png");
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation("textures/item/barrier.png");

    public PetInventoryScreen(PetInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        
        // Draw Portions of Horse GUI
        guiGraphics.blit(HORSE_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
        
        // Fill the background area with standard GUI gray
        guiGraphics.fill(i + 7, j + 7, i + 169, j + 82, 0xFFC6C6C6); 

        // Draw Slot Backgrounds manually for better compatibility
        drawSlotBg(guiGraphics, i + 16, j + 16);
        drawSlotBg(guiGraphics, i + 34, j + 16);
        drawSlotBg(guiGraphics, i + 16, j + 34);
        drawSlotBg(guiGraphics, i + 34, j + 34);
        
        drawSlotBg(guiGraphics, i + 141, j + 16);
        drawSlotBg(guiGraphics, i + 141, j + 34);
        drawSlotBg(guiGraphics, i + 141, j + 52);

        int portraitX = i + 62;
        int portraitY = j + 14;
        int portraitWidth = 68;
        int portraitHeight = 58;
        
        guiGraphics.fill(portraitX, portraitY, portraitX + portraitWidth, portraitY + portraitHeight, 0xFF000000);
        
        Entity pet = this.menu.getPet();
        if (pet instanceof LivingEntity living) {
            float bbWidth = living.getBbWidth();
            float bbHeight = living.getBbHeight();
            float scale = (float) PettingConfig.petPortraitRenderScale / Math.max(1.0F, Math.max(bbWidth, bbHeight));
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, portraitX + portraitWidth / 2, portraitY + portraitHeight, (int)scale, (float)(portraitX + portraitWidth / 2) - mouseX, (float)(portraitY + portraitHeight - 50) - mouseY, living);
        }

        RenderSystem.enableBlend();
        if (pet instanceof Mob mob) {
            boolean headSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.HEAD);
            boolean chestSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.CHEST);
            boolean legsSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.LEGS);
            boolean feetSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.FEET);
            boolean mainSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            boolean offSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.OFFHAND);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.4F);
            if (!headSup) guiGraphics.blit(BARRIER_ICON, i + 17, j + 17, 0, 0, 16, 16, 16, 16);
            if (!chestSup) guiGraphics.blit(BARRIER_ICON, i + 35, j + 17, 0, 0, 16, 16, 16, 16);
            if (!legsSup) guiGraphics.blit(BARRIER_ICON, i + 17, j + 35, 0, 0, 16, 16, 16, 16);
            if (!feetSup) guiGraphics.blit(BARRIER_ICON, i + 35, j + 35, 0, 0, 16, 16, 16, 16);
            if (!mainSup) guiGraphics.blit(BARRIER_ICON, i + 142, j + 35, 0, 0, 16, 16, 16, 16);
            if (!offSup) guiGraphics.blit(BARRIER_ICON, i + 142, j + 53, 0, 0, 16, 16, 16, 16);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.15F);
            if (headSup && this.menu.getSlot(1).getItem().isEmpty()) guiGraphics.blit(HELMET_ICON, i + 17, j + 17, 0, 0, 16, 16, 16, 16);
            if (chestSup && this.menu.getSlot(2).getItem().isEmpty()) guiGraphics.blit(CHEST_ICON, i + 35, j + 17, 0, 0, 16, 16, 16, 16);
            if (legsSup && this.menu.getSlot(3).getItem().isEmpty()) guiGraphics.blit(LEGS_ICON, i + 17, j + 35, 0, 0, 16, 16, 16, 16);
            if (feetSup && this.menu.getSlot(4).getItem().isEmpty()) guiGraphics.blit(BOOTS_ICON, i + 35, j + 35, 0, 0, 16, 16, 16, 16);
            
            if (this.menu.getSlot(0).getItem().isEmpty()) guiGraphics.blit(SADDLE_ICON, i + 142, j + 17, 0, 0, 16, 16, 16, 16);
            if (mainSup && this.menu.getSlot(5).getItem().isEmpty()) guiGraphics.blit(SWORD_ICON, i + 142, j + 35, 0, 0, 16, 16, 16, 16);
            if (offSup && this.menu.getSlot(6).getItem().isEmpty()) guiGraphics.blit(SHIELD_ICON, i + 142, j + 53, 0, 0, 16, 16, 16, 16);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawSlotBg(GuiGraphics guiGraphics, int x, int y) {
        // Draw a standard "recessed" slot look
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF373737); // Dark border
        guiGraphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF); // White bottom border
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B); // Inner gray
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
