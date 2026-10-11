package net.yigitguven.petting.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.inventory.PetInventoryMenu;
import net.yigitguven.petting.util.PetHelper;

public class PetInventoryScreen extends AbstractContainerScreen<PetInventoryMenu> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Petting.MODID, "textures/gui/container/pet_inventory.png");
    private static final Identifier SLOT_SPRITE =
            Identifier.withDefaultNamespace("container/slot");

    private float xMouse;
    private float yMouse;

    private PetInventoryTab currentTab = PetInventoryTab.HOME;
    private PetInventoryTabButton homeTabButton;
    private PetInventoryTabButton collarTabButton;
    private PetInventoryTabButton ridingTabButton;

    public PetInventoryScreen(PetInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 186);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 91;
    }

    public PetInventoryTab getCurrentTab() {
        return this.currentTab;
    }

    public void setCurrentTab(PetInventoryTab tab) {
        this.currentTab = tab;
        this.hoveredSlot = null;
    }

    @Override
    protected void init() {
        super.init();

        this.homeTabButton = new PetInventoryTabButton(
                this.leftPos - 28, this.topPos + 8,
                PetInventoryTab.HOME,
                this,
                Component.translatable("petting.gui.tab.home"),
                () -> new ItemStack(Items.LEAD),
                this::setCurrentTab
        );

        this.collarTabButton = new PetInventoryTabButton(
                this.leftPos - 28, this.topPos + 36,
                PetInventoryTab.COLLAR,
                this,
                Component.translatable("petting.gui.tab.collar"),
                () -> {
                    Mob pet = this.menu.getPet();
                    ItemStack collar = pet != null ? PetHelper.getCollar(pet) : ItemStack.EMPTY;
                    return collar.isEmpty() ? PettingModItems.COLLAR.get().getDefaultInstance() : collar;
                },
                this::setCurrentTab
        );

        this.ridingTabButton = new PetInventoryTabButton(
                this.leftPos - 28, this.topPos + 64,
                PetInventoryTab.RIDING,
                this,
                Component.translatable("petting.gui.tab.riding"),
                () -> new ItemStack(Items.SADDLE),
                this::setCurrentTab
        );

        this.addRenderableWidget(this.homeTabButton);
        this.addRenderableWidget(this.collarTabButton);
        this.addRenderableWidget(this.ridingTabButton);

        this.updateTabButtons();
    }

    private void updateTabButtons() {
        Mob pet = this.menu.getPet();
        boolean collarWorn = pet != null && !PetHelper.getCollar(pet).isEmpty();
        boolean ridingAllowed = pet != null && PetHelper.isPetRidingEnabledFor(pet);
        boolean hasOtherButtons = collarWorn || ridingAllowed;

        this.homeTabButton.visible = hasOtherButtons;
        this.collarTabButton.visible = collarWorn;
        this.ridingTabButton.visible = ridingAllowed;

        int buttonY = this.topPos + 8;
        if (this.homeTabButton.visible) {
            this.homeTabButton.setPosition(this.leftPos - 28, buttonY);
            buttonY += 28;
        }
        if (this.collarTabButton.visible) {
            this.collarTabButton.setPosition(this.leftPos - 28, buttonY);
            buttonY += 28;
        }
        if (this.ridingTabButton.visible) {
            this.ridingTabButton.setPosition(this.leftPos - 28, buttonY);
            buttonY += 28;
        }

        // Return to HOME if current tab becomes unavailable
        if (this.currentTab == PetInventoryTab.COLLAR && !collarWorn) {
            this.currentTab = PetInventoryTab.HOME;
        } else if (this.currentTab == PetInventoryTab.RIDING && !ridingAllowed) {
            this.currentTab = PetInventoryTab.HOME;
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateTabButtons();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        this.updateTabButtons();
        if (this.currentTab != PetInventoryTab.HOME) {
            this.hoveredSlot = null;
        }
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = this.leftPos;
        int yo = this.topPos;

        if (this.currentTab == PetInventoryTab.HOME) {
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
        } else {
            // Blank UI scene: clean container background panel
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

            // Clean fill over the slots area (standard container panel)
            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 179, 0xFFC6C6C6);
            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 18, 0xFF373737);
            graphics.fill(xo + 7, yo + 17, xo + 8, yo + 179, 0xFF373737);
            graphics.fill(xo + 168, yo + 18, xo + 169, yo + 179, 0xFFFFFFFF);
            graphics.fill(xo + 8, yo + 178, xo + 169, yo + 179, 0xFFFFFFFF);

            ItemStack icon = this.currentTab == PetInventoryTab.COLLAR
                    ? (this.menu.getPet() != null && !PetHelper.getCollar(this.menu.getPet()).isEmpty()
                        ? PetHelper.getCollar(this.menu.getPet())
                        : PettingModItems.COLLAR.get().getDefaultInstance())
                    : new ItemStack(Items.SADDLE);

            graphics.item(icon, xo + (this.imageWidth - 16) / 2, yo + 75);
            Component placeholder = Component.translatable("petting.gui.scene.coming_soon");
            int textWidth = this.font.width(placeholder);
            graphics.text(this.font, placeholder, xo + (this.imageWidth - textWidth) / 2, yo + 98, 0xFF707070, false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.currentTab == PetInventoryTab.HOME) {
            Mob pet = this.menu.getPet();
            Component petTitle = pet != null ? pet.getDisplayName() : this.title;
            graphics.text(this.font, petTitle, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
            graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);
        } else if (this.currentTab == PetInventoryTab.COLLAR) {
            graphics.text(this.font, Component.translatable("petting.gui.scene.blank_collar"), this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        } else if (this.currentTab == PetInventoryTab.RIDING) {
            graphics.text(this.font, Component.translatable("petting.gui.scene.blank_riding"), this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        }
    }

    @Override
    protected void extractSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.currentTab == PetInventoryTab.HOME) {
            super.extractSlots(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.currentTab != PetInventoryTab.HOME) {
            for (GuiEventListener listener : this.children()) {
                if (listener.mouseClicked(event, doubleClick)) {
                    this.setFocused(listener);
                    return true;
                }
            }
            return false;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.currentTab != PetInventoryTab.HOME) {
            if (this.minecraft.options.keyInventory.matches(event)) {
                this.onClose();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        if (this.currentTab != PetInventoryTab.HOME) {
            return false;
        }
        return super.isHovering(x, y, width, height, mouseX, mouseY);
    }
}
