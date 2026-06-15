package net.yigitguven.petting.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class PetSettingsScreen extends Screen {
    private final int entityId;
    private boolean sitStill;
    private boolean waiting;
    private boolean isTamed;
    private boolean attackIfOwnerAttacks;
    private boolean attackIfOwnerAttacked;
    private boolean attackIfSelfAttacked;
    private boolean damageOwner;
    private boolean ignoreWhistle;
    private int followDistance;
    private int teleportDistance;
    private final String controlRightMapping;
    private final String controlShiftMapping;
    private EditBox followBox;
    private EditBox tpBox;
    private String followError;
    private String tpError;
    private Button sitButton;
    private Button waitingButton;
    private Button attackOwnerAttacksButton;
    private Button attackOwnerAttackedButton;
    private Button attackSelfButton;
    private Button damageOwnerButton;
    private Button ignoreWhistleButton;
    private Button controlsButton;
    private static final ResourceLocation BARRIER_ICON = new ResourceLocation("textures/item/barrier.png");

    protected PetSettingsScreen(int entityId, boolean sitStill, boolean waiting, boolean isTamed,
                                boolean attackIfOwnerAttacks, boolean attackIfOwnerAttacked, boolean attackIfSelfAttacked,
                                boolean damageOwner, boolean ignoreWhistle, int followDistance, int teleportDistance,
                                String controlRightClick, String controlShiftRightClick) {
        super(Component.translatable("screen.petting.settings.title"));
        this.entityId = entityId;
        this.sitStill = sitStill;
        this.waiting = waiting;
        this.isTamed = isTamed;
        this.attackIfOwnerAttacks = attackIfOwnerAttacks;
        this.attackIfOwnerAttacked = attackIfOwnerAttacked;
        this.attackIfSelfAttacked = attackIfSelfAttacked;
        this.damageOwner = damageOwner;
        this.ignoreWhistle = ignoreWhistle;
        this.followDistance = followDistance;
        this.teleportDistance = teleportDistance;

        this.controlRightMapping = controlRightClick != null && !controlRightClick.isBlank() ? controlRightClick : "RIDE|SADDLE;SIT|NONE";
        this.controlShiftMapping = controlShiftRightClick != null && !controlShiftRightClick.isBlank() ? controlShiftRightClick : "OPEN_INV|NONE";
    }

    public static void open(int entityId, boolean sitStill, boolean waiting, boolean isTamed,
                            boolean attackIfOwnerAttacks, boolean attackIfOwnerAttacked, boolean attackIfSelfAttacked,
                            boolean damageOwner, boolean ignoreWhistle, int followDistance, int teleportDistance,
                            String controlRightClick, String controlShiftRightClick) {
        Minecraft.getInstance().setScreen(new PetSettingsScreen(entityId, sitStill, waiting, isTamed,
                attackIfOwnerAttacks, attackIfOwnerAttacked, attackIfSelfAttacked, damageOwner, ignoreWhistle,
                followDistance, teleportDistance, controlRightClick, controlShiftRightClick));
    }

    private Entity getPetEntity() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null ? mc.level.getEntity(this.entityId) : null;
    }

    private Component getPetName() {
        Entity pet = getPetEntity();
        return pet != null ? pet.getDisplayName() : Component.translatable("screen.petting.pet_unknown");
    }

    private void drawWrappedTooltip(GuiGraphics guiGraphics, Component text, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 500.0F);
        try {
            String msg = text.getString();
            int maxWidth = Math.max(120, this.width / 4);
            java.util.List<String> words = java.util.Arrays.asList(msg.split(" "));
            java.util.List<String> lines = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : words) {
                String next = current.length() == 0 ? word : current + " " + word;
                if (this.font.width(next) > maxWidth) {
                    if (current.length() > 0) {
                        lines.add(current.toString());
                    }
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(next);
                }
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }

            int lineHeight = 10;
            int boxW = maxWidth + 8;
            int boxH = lines.size() * lineHeight + 4;
            int bx = mouseX + 12;
            int by = mouseY - boxH - 6;
            if (bx + boxW > this.width) {
                bx = mouseX - boxW - 12;
            }
            if (by < 10) {
                by = mouseY + 12;
            }
            guiGraphics.fill(bx - 4, by - 4, bx + boxW, by + boxH + 2, 0xEE000000);
            int textY = by;
            for (String line : lines) {
                guiGraphics.drawString(this.font, line, bx, textY, 0xFFFFFF);
                textY += lineHeight;
            }
        } finally {
            guiGraphics.pose().popPose();
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    protected void init() {
        int cardWidth = Math.min(320, this.width - 24);
        int cardLeft = (this.width - cardWidth) / 2;
        int columnWidth = (cardWidth - 10) / 2;
        int leftColumnX = cardLeft;
        int rightColumnX = cardLeft + columnWidth + 10;
        int startY = 38;
        int rowGap = 4;
        int rowHeight = 20;

        this.sitButton = Button.builder(Component.translatable("screen.petting.settings.sitstill",
                this.sitStill ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "sitstill")).bounds(leftColumnX, startY + (rowHeight + rowGap) * 0, columnWidth, rowHeight).build();
        this.waitingButton = Button.builder(Component.translatable("screen.petting.settings.waiting",
                this.waiting ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "waiting")).bounds(leftColumnX, startY + (rowHeight + rowGap) * 1, columnWidth, rowHeight).build();
        this.attackOwnerAttacksButton = Button.builder(Component.translatable("screen.petting.settings.attack_owner_attacks",
                this.attackIfOwnerAttacks ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "attackifownerattacks")).bounds(leftColumnX, startY + (rowHeight + rowGap) * 2, columnWidth, rowHeight).build();
        this.attackOwnerAttackedButton = Button.builder(Component.translatable("screen.petting.settings.attack_owner_attacked",
                this.attackIfOwnerAttacked ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "attackifownerattacked")).bounds(leftColumnX, startY + (rowHeight + rowGap) * 3, columnWidth, rowHeight).build();

        this.attackSelfButton = Button.builder(Component.translatable("screen.petting.settings.attack_self_attacked",
                this.attackIfSelfAttacked ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "attackifselfattacked")).bounds(rightColumnX, startY + (rowHeight + rowGap) * 0, columnWidth, rowHeight).build();
        this.damageOwnerButton = Button.builder(Component.translatable("screen.petting.settings.damage_owner",
                this.damageOwner ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "damageOwner")).bounds(rightColumnX, startY + (rowHeight + rowGap) * 1, columnWidth, rowHeight).build();
        this.ignoreWhistleButton = Button.builder(Component.translatable("screen.petting.settings.ignore_whistle",
                this.ignoreWhistle ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")),
                b -> toggleBoolean(b, "ignoreWhistle")).bounds(rightColumnX, startY + (rowHeight + rowGap) * 2, columnWidth, rowHeight).build();

        this.addRenderableWidget(this.sitButton);
        this.addRenderableWidget(this.waitingButton);
        this.addRenderableWidget(this.attackOwnerAttacksButton);
        this.addRenderableWidget(this.attackOwnerAttackedButton);
        this.addRenderableWidget(this.attackSelfButton);
        this.addRenderableWidget(this.damageOwnerButton);
        this.addRenderableWidget(this.ignoreWhistleButton);

        int distanceRowY = startY + (rowHeight + rowGap) * 4 + 2;
        this.followBox = new EditBox(this.font, leftColumnX, distanceRowY + 10, columnWidth, 20, Component.translatable("gui.petting.followdistance"));
        this.followBox.setValue(Integer.toString(this.followDistance));
        this.addRenderableWidget(this.followBox);

        this.tpBox = new EditBox(this.font, rightColumnX, distanceRowY + 10, columnWidth, 20, Component.translatable("gui.petting.teleportdistance"));
        this.tpBox.setValue(Integer.toString(this.teleportDistance));
        this.addRenderableWidget(this.tpBox);

        int controlsRowY = distanceRowY + 50;
        this.controlsButton = Button.builder(Component.translatable("screen.petting.controls.open"), b -> {
                // Always read fresh mapping strings from entity client NBT so we don't pass stale constructor values
                net.minecraft.world.entity.Entity _pet = getPetEntity();
                String _right = "RIDE|SADDLE;SIT|NONE";
                String _shift = "OPEN_INV|NONE";
                if (_pet != null) {
                    net.minecraft.nbt.CompoundTag _d = ((net.yigitguven.petting.IEntityData)_pet).getPersistentData();
                    if (_d.contains("control_right_click"))       _right = _d.getString("control_right_click");
                    if (_d.contains("control_shift_right_click")) _shift = _d.getString("control_shift_right_click");
                }
                Minecraft.getInstance().setScreen(new PetControlMappingsScreen(this, this.entityId, _right, _shift));
        }).bounds(cardLeft, controlsRowY, cardWidth, 20).build();
        this.addRenderableWidget(this.controlsButton);

        int bottomRowY = controlsRowY + 24;
        int halfButton = (cardWidth - 6) / 2;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.petting.apply"), b -> applyDistances())
                .bounds(cardLeft, bottomRowY, halfButton, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.petting.settings.close"), b -> Minecraft.getInstance().setScreen(null))
                .bounds(cardLeft + halfButton + 6, bottomRowY, halfButton, 20).build());
    }

    private void applyDistances() {
        this.followError = null;
        this.tpError = null;
        try {
            int fd = Integer.parseInt(this.followBox.getValue().trim());
            int td = Integer.parseInt(this.tpBox.getValue().trim());
            int minFollow = 1;
            int maxFollow = 100;
            int minTp = 1;
            int maxTp = 100;
            if (fd < minFollow || fd > maxFollow) {
                this.followError = Component.translatable("screen.petting.settings.invalid_number").getString();
            }
            if (td < minTp || td > maxTp) {
                this.tpError = Component.translatable("screen.petting.settings.invalid_number").getString();
            }
            if (this.followError == null && this.tpError == null) {
                fd = Math.max(minFollow, Math.min(maxFollow, fd));
                td = Math.max(minTp, Math.min(maxTp, td));
                net.yigitguven.petting.client.PettingClientNetworking.sendUpdatePetSetting(this.entityId, "followdistance", Integer.toString(fd));
                net.yigitguven.petting.client.PettingClientNetworking.sendUpdatePetSetting(this.entityId, "teleportdistance", Integer.toString(td));
                this.followDistance = fd;
                this.teleportDistance = td;
            }
        } catch (NumberFormatException ex) {
            this.followError = Component.translatable("screen.petting.settings.invalid_number").getString();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        int cardWidth = Math.min(320, this.width - 24);
        int cardLeft = (this.width - cardWidth) / 2;
        int rowGap = 4;
        int rowHeight = 20;
        int startY = 38;
        int distanceRowY = startY + (rowHeight + rowGap) * 4 + 2;
        int controlsRowY = distanceRowY + 50;
        int bottomRowY = controlsRowY + 24;
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        int columnWidth = (cardWidth - 10) / 2;
        int leftColumnX = cardLeft;
        int rightColumnX = cardLeft + columnWidth + 10;
        int leftColumnX2 = leftColumnX;
        int rightColumnX2 = rightColumnX;

        Entity pet = getPetEntity();
        int portraitX = cardLeft + 2;
        int portraitY = 8;
        int portraitSize = 20;
        guiGraphics.fill(portraitX - 1, portraitY - 1, portraitX + portraitSize + 1, portraitY + portraitSize + 1, 0xFF000000);
        if (pet instanceof LivingEntity living) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, portraitX + portraitSize / 2, portraitY + portraitSize, 20, (float)(portraitX + portraitSize / 2) - mouseX, (float)(portraitY + portraitSize - 50) - mouseY, living);
        } else {
            guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BARRIER), portraitX + 2, portraitY + 2);
        }
        guiGraphics.drawString(this.font, getPetName(), portraitX + portraitSize + 6, portraitY + 2, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.title, portraitX + portraitSize + 6, portraitY + 12, 0xAAAAAA, false);

        guiGraphics.drawString(this.font, Component.translatable("gui.petting.followdistance").getString(), leftColumnX2, distanceRowY, 0xDDDDDD);
        guiGraphics.drawString(this.font, Component.translatable("gui.petting.teleportdistance").getString(), rightColumnX2, distanceRowY, 0xDDDDDD);
        guiGraphics.drawString(this.font, Component.translatable("screen.petting.settings.range_hint", 1, 100).getString(), leftColumnX, distanceRowY + 34, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("screen.petting.settings.range_hint", 1, 100).getString(), rightColumnX, distanceRowY + 34, 0xAAAAAA);

        if (this.followError != null) {
            guiGraphics.drawString(this.font, this.followError, leftColumnX2, distanceRowY + 46, 0xFF5555);
        }
        if (this.tpError != null) {
            guiGraphics.drawString(this.font, this.tpError, rightColumnX2, distanceRowY + 46, 0xFF5555);
        }

        String tooltip = null;
        if (this.sitButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.sitstill";
        else if (this.waitingButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.waiting";
        else if (this.attackOwnerAttacksButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.attack_owner_attacks";
        else if (this.attackOwnerAttackedButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.attack_owner_attacked";
        else if (this.attackSelfButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.attack_self_attacked";
        else if (this.damageOwnerButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.damage_owner";
        else if (this.ignoreWhistleButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.ignore_whistle";
        else if (this.controlsButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls";
        else if (this.followBox.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.followdistance";
        else if (this.tpBox.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.teleportdistance";

        if (tooltip != null) {
            drawWrappedTooltip(guiGraphics, Component.translatable(tooltip), mouseX, mouseY);
        }
    }

    private void toggleBoolean(Button button, String key) {
        boolean newValue;
        switch (key) {
            case "sitstill" -> {
                this.sitStill = !this.sitStill;
                newValue = this.sitStill;
                button.setMessage(Component.translatable("screen.petting.settings.sitstill", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "waiting" -> {
                this.waiting = !this.waiting;
                newValue = this.waiting;
                button.setMessage(Component.translatable("screen.petting.settings.waiting", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "attackifownerattacks" -> {
                this.attackIfOwnerAttacks = !this.attackIfOwnerAttacks;
                newValue = this.attackIfOwnerAttacks;
                button.setMessage(Component.translatable("screen.petting.settings.attack_owner_attacks", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "attackifownerattacked" -> {
                this.attackIfOwnerAttacked = !this.attackIfOwnerAttacked;
                newValue = this.attackIfOwnerAttacked;
                button.setMessage(Component.translatable("screen.petting.settings.attack_owner_attacked", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "attackifselfattacked" -> {
                this.attackIfSelfAttacked = !this.attackIfSelfAttacked;
                newValue = this.attackIfSelfAttacked;
                button.setMessage(Component.translatable("screen.petting.settings.attack_self_attacked", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "damageOwner" -> {
                this.damageOwner = !this.damageOwner;
                newValue = this.damageOwner;
                button.setMessage(Component.translatable("screen.petting.settings.damage_owner", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            case "ignoreWhistle" -> {
                this.ignoreWhistle = !this.ignoreWhistle;
                newValue = this.ignoreWhistle;
                button.setMessage(Component.translatable("screen.petting.settings.ignore_whistle", newValue ? Component.translatable("screen.petting.state.on") : Component.translatable("screen.petting.state.off")));
            }
            default -> newValue = false;
        }
        net.yigitguven.petting.client.PettingClientNetworking.sendUpdatePetSetting(this.entityId, key, Boolean.toString(newValue));
    }
}
