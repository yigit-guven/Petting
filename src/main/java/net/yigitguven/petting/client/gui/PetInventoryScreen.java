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
        RenderSystem.setShaderTexture(0, HORSE_GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        
        // 1. Draw Main Background Frame
        guiGraphics.blit(HORSE_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);

        // 2. Clean up the "Storage Slots" background area to make it look smooth and empty
        // This covers the old horse/llama slot indices in the texture
        guiGraphics.fill(i + 52, j + 15, i + 140, j + 75, 0xFFC6C6C6); // Standard GUI gray
        
        // 3. Draw standard slot background boxes at the current positions
        // Left Armor Grid
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 16, j + 16, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 34, j + 16, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 16, j + 34, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 34, j + 34, 0, 166, 18, 18);
        
        // Right Column
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 141, j + 16, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 141, j + 34, 0, 166, 18, 18);
        guiGraphics.blit(HORSE_GUI_TEXTURES, i + 141, j + 52, 0, 166, 18, 18);

        // 4. Draw Pet Portrait Area (Centered between columns)
        int portraitX = i + 62; // Centered between end of armor (52) and start of equipment (141)
        int portraitY = j + 14;
        int portraitWidth = 68;
        int portraitHeight = 58;
        
        // Dark background for the portrait area (The "Black One")
        guiGraphics.fill(portraitX, portraitY, portraitX + portraitWidth, portraitY + portraitHeight, 0xFF000000);
        
        Entity pet = this.menu.getPet();
        if (pet instanceof LivingEntity living) {
            float bbWidth = living.getBbWidth();
            float bbHeight = living.getBbHeight();
            float scale = 45.0F / Math.max(1.0F, Math.max(bbWidth, bbHeight));
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, portraitX + (portraitWidth / 2), portraitY + portraitHeight - 5, (int)scale, (float)(portraitX + (portraitWidth / 2)) - mouseX, (float)(portraitY + portraitHeight / 2 - 30) - mouseY, living);
        }

        // 5. Draw Ghost Icons & Barriers
        RenderSystem.enableBlend();
        if (pet instanceof Mob mob) {
            boolean headSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.HEAD);
            boolean chestSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.CHEST);
            boolean legsSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.LEGS);
            boolean feetSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.FEET);
            boolean mainSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            boolean offSup = PetInventoryUtil.isSlotSupported(mob, net.minecraft.world.entity.EquipmentSlot.OFFHAND);

            // Barrier icons should be slightly more visible than ghosts
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.4F);
            if (!headSup) guiGraphics.blit(BARRIER_ICON, i + 17, j + 17, 0, 0, 16, 16, 16, 16);
            if (!chestSup) guiGraphics.blit(BARRIER_ICON, i + 35, j + 17, 0, 0, 16, 16, 16, 16);
            if (!legsSup) guiGraphics.blit(BARRIER_ICON, i + 17, j + 35, 0, 0, 16, 16, 16, 16);
            if (!feetSup) guiGraphics.blit(BARRIER_ICON, i + 35, j + 35, 0, 0, 16, 16, 16, 16);
            if (!mainSup) guiGraphics.blit(BARRIER_ICON, i + 142, j + 35, 0, 0, 16, 16, 16, 16);
            if (!offSup) guiGraphics.blit(BARRIER_ICON, i + 142, j + 53, 0, 0, 16, 16, 16, 16);

            // Ghost icons (High transparency as requested: 0.15)
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

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
