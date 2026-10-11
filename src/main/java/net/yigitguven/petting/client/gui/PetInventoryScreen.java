package net.yigitguven.petting.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.init.PettingDataComponents;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.inventory.PetInventoryMenu;
import net.yigitguven.petting.network.UpdateCollarSettingsPayload;
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

    // Collar tab widgets
    private PetButton combatModeButton;
    private PetIntSliderButton followDistanceSlider;
    private PetIntSliderButton teleportDistanceSlider;
    private PetButton teleportEnabledButton;
    private PetButton preferRangedButton;
    private PetButton defendOtherPetsButton;
    private PetButton retaliateButton;
    private PetButton resetDefaultsButton;

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
        this.refreshCollarWidgetStates(this.getCurrentCollarSettings());
    }

    @Override
    protected void init() {
        super.init();

        this.homeTabButton = new PetInventoryTabButton(
                this.leftPos - 30, this.topPos + 8,
                PetInventoryTab.HOME,
                this,
                Component.translatable("petting.gui.tab.home"),
                () -> new ItemStack(Items.LEAD),
                this::setCurrentTab
        );

        this.collarTabButton = new PetInventoryTabButton(
                this.leftPos - 30, this.topPos + 35,
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
                this.leftPos - 30, this.topPos + 62,
                PetInventoryTab.RIDING,
                this,
                Component.translatable("petting.gui.tab.riding"),
                () -> new ItemStack(Items.SADDLE),
                this::setCurrentTab
        );

        this.addWidget(this.homeTabButton);
        this.addWidget(this.collarTabButton);
        this.addWidget(this.ridingTabButton);

        // Collar tab widgets initialization
        Mob pet = this.menu.getPet();
        CollarSettings initialSettings = pet != null ? PetHelper.getCollarSettings(pet) : CollarSettings.fromConfig();

        int xo = this.leftPos;
        int yo = this.topPos;

        // 1. Combat mode cycle button
        this.combatModeButton = PetButton.petBuilder(getCombatModeText(initialSettings.combatMode()), b -> {
            CollarSettings cur = getCurrentCollarSettings();
            CombatMode next = switch (cur.combatMode()) {
                case PASSIVE -> CombatMode.DEFENSIVE;
                case DEFENSIVE -> CombatMode.AGGRESSIVE;
                case AGGRESSIVE -> CombatMode.WANDER;
                case WANDER -> CombatMode.GUARD;
                case GUARD -> CombatMode.PASSIVE;
            };
            applyCollarSettings(cur.withCombatMode(next));
        }).bounds(xo + 10, yo + 40, 156, 18).build();

        // 2. Follow distance / Wander / Guard radius slider
        String distKey = initialSettings.combatMode() == CombatMode.WANDER ? "petting.gui.collar.wander_radius"
                : initialSettings.combatMode() == CombatMode.GUARD ? "petting.gui.collar.guard_radius"
                : "petting.gui.collar.follow_distance";
        String distTooltipKey = initialSettings.combatMode() == CombatMode.WANDER ? "petting.gui.collar.wander_radius.tooltip"
                : initialSettings.combatMode() == CombatMode.GUARD ? "petting.gui.collar.guard_radius.tooltip"
                : "petting.gui.collar.follow_distance.tooltip";

        this.followDistanceSlider = new PetIntSliderButton(
                xo + 10, yo + 61, 156, 18,
                distKey,
                "blocks",
                PettingServerConfig.getMinFollowDistance(),
                PettingServerConfig.getMaxFollowDistance(),
                initialSettings.followDistance(),
                dist -> {
                    CollarSettings cur = getCurrentCollarSettings();
                    applyCollarSettings(cur.withFollowDistance(dist));
                }
        );
        this.followDistanceSlider.setTooltip(Tooltip.create(Component.translatable(distTooltipKey)));

        // 3. Teleport distance slider
        this.teleportDistanceSlider = new PetIntSliderButton(
                xo + 10, yo + 82, 156, 18,
                "petting.gui.collar.teleport_distance",
                "blocks",
                PettingServerConfig.getMinTeleportDistance(),
                PettingServerConfig.getMaxTeleportDistance(),
                initialSettings.teleportDistance(),
                dist -> {
                    CollarSettings cur = getCurrentCollarSettings();
                    applyCollarSettings(cur.withTeleportDistance(dist));
                }
        );
        this.teleportDistanceSlider.setTooltip(Tooltip.create(Component.translatable("petting.gui.collar.teleport_distance.tooltip")));

        // 4. Teleport enabled toggle
        this.teleportEnabledButton = PetButton.petBuilder(getToggleText("petting.gui.collar.teleport", initialSettings.teleportEnabled()), b -> {
            CollarSettings cur = getCurrentCollarSettings();
            applyCollarSettings(cur.withTeleportEnabled(!cur.teleportEnabled()));
        }).bounds(xo + 10, yo + 103, 76, 18).tooltip(Tooltip.create(Component.translatable("petting.gui.collar.teleport.tooltip"))).build();

        // 5. Prefer ranged attack toggle
        this.preferRangedButton = PetButton.petBuilder(getToggleText("petting.gui.collar.ranged", initialSettings.preferRangedAttack()), b -> {
            CollarSettings cur = getCurrentCollarSettings();
            applyCollarSettings(cur.withPreferRangedAttack(!cur.preferRangedAttack()));
        }).bounds(xo + 90, yo + 103, 76, 18).tooltip(Tooltip.create(Component.translatable("petting.gui.collar.ranged.tooltip"))).build();

        // 6. Defend other pets toggle
        this.defendOtherPetsButton = PetButton.petBuilder(getToggleText("petting.gui.collar.defend_pets", initialSettings.defendOtherPets()), b -> {
            CollarSettings cur = getCurrentCollarSettings();
            applyCollarSettings(cur.withDefendOtherPets(!cur.defendOtherPets()));
        }).bounds(xo + 10, yo + 124, 76, 18).tooltip(Tooltip.create(Component.translatable("petting.gui.collar.defend_pets.tooltip"))).build();

        // 7. Retaliate when hurt toggle
        this.retaliateButton = PetButton.petBuilder(getToggleText("petting.gui.collar.retaliate", initialSettings.retaliateWhenHurt()), b -> {
            CollarSettings cur = getCurrentCollarSettings();
            applyCollarSettings(cur.withRetaliateWhenHurt(!cur.retaliateWhenHurt()));
        }).bounds(xo + 90, yo + 124, 76, 18).tooltip(Tooltip.create(Component.translatable("petting.gui.collar.retaliate.tooltip"))).build();

        // 8. Reset defaults button
        this.resetDefaultsButton = PetButton.petBuilder(Component.translatable("petting.gui.collar.reset"), b -> {
            CollarSettings defaults = CollarSettings.fromConfig();
            applyCollarSettings(defaults);
        }).bounds(xo + 10, yo + 148, 156, 18).tooltip(Tooltip.create(Component.translatable("petting.gui.collar.reset.tooltip"))).build();

        this.addRenderableWidget(this.combatModeButton);
        this.addRenderableWidget(this.followDistanceSlider);
        this.addRenderableWidget(this.teleportDistanceSlider);
        this.addRenderableWidget(this.teleportEnabledButton);
        this.addRenderableWidget(this.preferRangedButton);
        this.addRenderableWidget(this.defendOtherPetsButton);
        this.addRenderableWidget(this.retaliateButton);
        this.addRenderableWidget(this.resetDefaultsButton);

        this.updateTabButtons();
        this.refreshCollarWidgetStates(initialSettings);
    }

    private CollarSettings getCurrentCollarSettings() {
        Mob pet = this.menu.getPet();
        if (pet != null) {
            return PetHelper.getCollarSettings(pet);
        }
        return CollarSettings.fromConfig();
    }

    private void applyCollarSettings(CollarSettings newSettings) {
        Mob pet = this.menu.getPet();
        if (pet != null) {
            ItemStack collar = PetHelper.getCollar(pet);
            if (!collar.isEmpty()) {
                collar.set(PettingDataComponents.COLLAR_SETTINGS, newSettings);
                PetHelper.setCollarSettings(pet, newSettings);
            }
            ClientPacketDistributor.sendToServer(new UpdateCollarSettingsPayload(pet.getId(), newSettings));
        }
        this.refreshCollarWidgetStates(newSettings);
    }

    private void refreshCollarWidgetStates(CollarSettings settings) {
        boolean isCollarTab = this.currentTab == PetInventoryTab.COLLAR;

        if (this.combatModeButton != null) this.combatModeButton.visible = isCollarTab;
        if (this.followDistanceSlider != null) this.followDistanceSlider.visible = isCollarTab;
        if (this.teleportDistanceSlider != null) this.teleportDistanceSlider.visible = isCollarTab;
        if (this.teleportEnabledButton != null) this.teleportEnabledButton.visible = isCollarTab;
        if (this.preferRangedButton != null) this.preferRangedButton.visible = isCollarTab;
        if (this.defendOtherPetsButton != null) this.defendOtherPetsButton.visible = isCollarTab;
        if (this.retaliateButton != null) this.retaliateButton.visible = isCollarTab;
        if (this.resetDefaultsButton != null) this.resetDefaultsButton.visible = isCollarTab;

        if (isCollarTab && this.combatModeButton != null) {
            this.combatModeButton.setMessage(getCombatModeText(settings.combatMode()));
            this.combatModeButton.setTooltip(Tooltip.create(getCombatModeTooltip(settings)));

            if (settings.combatMode() == CombatMode.WANDER) {
                this.followDistanceSlider.setTranslationKey("petting.gui.collar.wander_radius");
                this.followDistanceSlider.setTooltip(Tooltip.create(Component.translatable("petting.gui.collar.wander_radius.tooltip")));
            } else if (settings.combatMode() == CombatMode.GUARD) {
                this.followDistanceSlider.setTranslationKey("petting.gui.collar.guard_radius");
                this.followDistanceSlider.setTooltip(Tooltip.create(Component.translatable("petting.gui.collar.guard_radius.tooltip")));
            } else {
                this.followDistanceSlider.setTranslationKey("petting.gui.collar.follow_distance");
                this.followDistanceSlider.setTooltip(Tooltip.create(Component.translatable("petting.gui.collar.follow_distance.tooltip")));
            }

            this.followDistanceSlider.setIntValue(settings.followDistance());
            this.teleportDistanceSlider.setIntValue(settings.teleportDistance());

            this.teleportEnabledButton.setMessage(getToggleText("petting.gui.collar.teleport", settings.teleportEnabled()));
            this.teleportEnabledButton.setTooltip(Tooltip.create(settings.teleportEnabled()
                    ? Component.translatable("petting.gui.collar.teleport.on.tooltip", settings.teleportDistance())
                    : Component.translatable("petting.gui.collar.teleport.off.tooltip")));

            this.preferRangedButton.setMessage(getToggleText("petting.gui.collar.ranged", settings.preferRangedAttack()));
            this.preferRangedButton.setTooltip(Tooltip.create(settings.preferRangedAttack()
                    ? Component.translatable("petting.gui.collar.ranged.on.tooltip")
                    : Component.translatable("petting.gui.collar.ranged.off.tooltip")));

            this.defendOtherPetsButton.setMessage(getToggleText("petting.gui.collar.defend_pets", settings.defendOtherPets()));
            this.defendOtherPetsButton.setTooltip(Tooltip.create(settings.defendOtherPets()
                    ? Component.translatable("petting.gui.collar.defend_pets.on.tooltip")
                    : Component.translatable("petting.gui.collar.defend_pets.off.tooltip")));

            this.retaliateButton.setMessage(getToggleText("petting.gui.collar.retaliate", settings.retaliateWhenHurt()));
            this.retaliateButton.setTooltip(Tooltip.create(settings.retaliateWhenHurt()
                    ? Component.translatable("petting.gui.collar.retaliate.on.tooltip")
                    : Component.translatable("petting.gui.collar.retaliate.off.tooltip")));
        }
    }

    private static Component getCombatModeText(CombatMode mode) {
        Component modeName = switch (mode) {
            case PASSIVE -> Component.translatable("petting.gui.collar.passive").withStyle(ChatFormatting.AQUA);
            case DEFENSIVE -> Component.translatable("petting.gui.collar.defensive").withStyle(ChatFormatting.YELLOW);
            case AGGRESSIVE -> Component.translatable("petting.gui.collar.aggressive").withStyle(ChatFormatting.RED);
            case WANDER -> Component.translatable("petting.gui.collar.wander").withStyle(ChatFormatting.GREEN);
            case GUARD -> Component.translatable("petting.gui.collar.guard").withStyle(ChatFormatting.GOLD);
        };
        return Component.translatable("petting.gui.collar.mode").append(": ").append(modeName);
    }

    private static Component getCombatModeTooltip(CollarSettings settings) {
        net.minecraft.network.chat.MutableComponent tooltip = Component.empty();
        switch (settings.combatMode()) {
            case PASSIVE -> {
                tooltip.append(Component.translatable("petting.gui.collar.mode.passive.title").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                        .append("\n")
                        .append(Component.translatable("petting.gui.collar.mode.passive.desc").withStyle(ChatFormatting.GRAY));

                if (settings.retaliateWhenHurt() || settings.defendOtherPets()) {
                    tooltip.append("\n")
                            .append(Component.translatable("petting.gui.collar.mode.passive.inactive_hint").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
                }
            }
            case DEFENSIVE -> {
                tooltip.append(Component.translatable("petting.gui.collar.mode.defensive.title").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append("\n")
                        .append(Component.translatable("petting.gui.collar.mode.defensive.desc").withStyle(ChatFormatting.GRAY))
                        .append("\n")
                        .append(getTacticsStatusLine(settings));
            }
            case AGGRESSIVE -> {
                tooltip.append(Component.translatable("petting.gui.collar.mode.aggressive.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                        .append("\n")
                        .append(Component.translatable("petting.gui.collar.mode.aggressive.desc").withStyle(ChatFormatting.GRAY))
                        .append("\n")
                        .append(getTacticsStatusLine(settings));
            }
            case WANDER -> {
                tooltip.append(Component.translatable("petting.gui.collar.mode.wander.title").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .append("\n")
                        .append(Component.translatable("petting.gui.collar.mode.wander.desc").withStyle(ChatFormatting.GRAY))
                        .append("\n")
                        .append(getTacticsStatusLine(settings));
            }
            case GUARD -> {
                tooltip.append(Component.translatable("petting.gui.collar.mode.guard.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append("\n")
                        .append(Component.translatable("petting.gui.collar.mode.guard.desc").withStyle(ChatFormatting.GRAY))
                        .append("\n")
                        .append(getTacticsStatusLine(settings));
            }
        }
        return tooltip;
    }

    private static Component getTacticsStatusLine(CollarSettings settings) {
        Component retStatus = Component.translatable(settings.retaliateWhenHurt() ? "petting.gui.collar.on" : "petting.gui.collar.off")
                .withStyle(settings.retaliateWhenHurt() ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component defStatus = Component.translatable(settings.defendOtherPets() ? "petting.gui.collar.on" : "petting.gui.collar.off")
                .withStyle(settings.defendOtherPets() ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component rangedStatus = Component.translatable(settings.preferRangedAttack() ? "petting.gui.collar.on" : "petting.gui.collar.off")
                .withStyle(settings.preferRangedAttack() ? ChatFormatting.AQUA : ChatFormatting.GRAY);

        return Component.literal("• ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("petting.gui.collar.mode.status_retaliate", retStatus).withStyle(ChatFormatting.WHITE))
                .append("  • ")
                .append(Component.translatable("petting.gui.collar.mode.status_defend", defStatus).withStyle(ChatFormatting.WHITE))
                .append("  • ")
                .append(Component.translatable("petting.gui.collar.mode.status_ranged", rangedStatus).withStyle(ChatFormatting.WHITE));
    }

    private static Component getToggleText(String key, boolean enabled) {
        Component state = enabled
                ? Component.translatable("petting.gui.collar.on").withStyle(ChatFormatting.GREEN)
                : Component.translatable("petting.gui.collar.off").withStyle(ChatFormatting.RED);
        return Component.translatable(key).append(": ").append(state);
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
            this.homeTabButton.setPosition(this.leftPos - 30, buttonY);
            buttonY += 27;
        }
        if (this.collarTabButton.visible) {
            this.collarTabButton.setPosition(this.leftPos - 30, buttonY);
            buttonY += 27;
        }
        if (this.ridingTabButton.visible) {
            this.ridingTabButton.setPosition(this.leftPos - 30, buttonY);
            buttonY += 27;
        }

        // Return to HOME if current tab becomes unavailable
        if (this.currentTab == PetInventoryTab.COLLAR && !collarWorn) {
            this.setCurrentTab(PetInventoryTab.HOME);
        } else if (this.currentTab == PetInventoryTab.RIDING && !ridingAllowed) {
            this.setCurrentTab(PetInventoryTab.HOME);
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

        // Queue tab tooltips and cursor states (sprites and icons are rendered during extractBackground)
        PetInventoryTabButton[] buttons = {this.homeTabButton, this.collarTabButton, this.ridingTabButton};
        for (PetInventoryTabButton button : buttons) {
            if (button != null && button.visible) {
                button.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = this.leftPos;
        int yo = this.topPos;

        // 1. Draw UNSELECTED tabs background FIRST (behind the main container frame, tucked under container border)
        this.renderTabBackgrounds(graphics, false);

        // 2. Draw Main container background (and scene contents)
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
        } else if (this.currentTab == PetInventoryTab.COLLAR) {
            // Container panel fill & border
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 179, 0xFFC6C6C6);
            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 18, 0xFF373737);
            graphics.fill(xo + 7, yo + 17, xo + 8, yo + 179, 0xFF373737);
            graphics.fill(xo + 168, yo + 18, xo + 169, yo + 179, 0xFFFFFFFF);
            graphics.fill(xo + 8, yo + 178, xo + 169, yo + 179, 0xFFFFFFFF);

            // Collar header banner
            graphics.fill(xo + 10, yo + 19, xo + 166, yo + 37, 0xFFB0B0B0);
            graphics.fill(xo + 10, yo + 19, xo + 166, yo + 20, 0xFF8F8F8F);
            graphics.fill(xo + 10, yo + 19, xo + 11, yo + 37, 0xFF8F8F8F);
            graphics.fill(xo + 165, yo + 20, xo + 166, yo + 37, 0xFFD8D8D8);
            graphics.fill(xo + 11, yo + 36, xo + 166, yo + 37, 0xFFD8D8D8);

            Mob pet = this.menu.getPet();
            ItemStack collar = (pet != null && !PetHelper.getCollar(pet).isEmpty())
                    ? PetHelper.getCollar(pet)
                    : PettingModItems.COLLAR.get().getDefaultInstance();

            graphics.item(collar, xo + 12, yo + 20);

            Component collarName = collar.getHoverName();
            graphics.text(this.font, collarName, xo + 32, yo + 24, 0xFF202020, false);
        } else {
            // Blank UI scene: clean container background panel for Riding tab
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 179, 0xFFC6C6C6);
            graphics.fill(xo + 7, yo + 17, xo + 169, yo + 18, 0xFF373737);
            graphics.fill(xo + 7, yo + 17, xo + 8, yo + 179, 0xFF373737);
            graphics.fill(xo + 168, yo + 18, xo + 169, yo + 179, 0xFFFFFFFF);
            graphics.fill(xo + 8, yo + 178, xo + 169, yo + 179, 0xFFFFFFFF);

            graphics.item(new ItemStack(Items.SADDLE), xo + (this.imageWidth - 16) / 2, yo + 75);
            Component placeholder = Component.translatable("petting.gui.scene.coming_soon");
            int textWidth = this.font.width(placeholder);
            graphics.text(this.font, placeholder, xo + (this.imageWidth - textWidth) / 2, yo + 98, 0xFF707070, false);
        }

        // 3. Draw SELECTED tab background (in front of container frame, seamlessly connected)
        this.renderTabBackgrounds(graphics, true);

        // 4. Draw tab item icons
        this.renderTabIcons(graphics);
    }

    private void renderTabBackgrounds(GuiGraphicsExtractor graphics, boolean selectedOnly) {
        PetInventoryTabButton[] buttons = {this.homeTabButton, this.collarTabButton, this.ridingTabButton};
        for (PetInventoryTabButton button : buttons) {
            if (button != null && button.visible) {
                boolean isCurrent = button.getTab() == this.currentTab;
                if (isCurrent == selectedOnly) {
                    button.renderTabBackground(graphics);
                }
            }
        }
    }

    private void renderTabIcons(GuiGraphicsExtractor graphics) {
        PetInventoryTabButton[] buttons = {this.homeTabButton, this.collarTabButton, this.ridingTabButton};
        for (PetInventoryTabButton button : buttons) {
            if (button != null && button.visible) {
                button.renderTabIcon(graphics);
            }
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
            graphics.text(this.font, Component.translatable("petting.gui.scene.collar_settings"), this.titleLabelX, this.titleLabelY, 0xFF404040, false);
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
                    this.setDragging(true);
                    return true;
                }
            }
            return false;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.currentTab != PetInventoryTab.HOME) {
            if (this.getFocused() != null && this.isDragging()) {
                return this.getFocused().mouseDragged(event, dx, dy);
            }
            return false;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.currentTab != PetInventoryTab.HOME) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(event);
            }
            return false;
        }
        return super.mouseReleased(event);
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
