package net.yigitguven.petting.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class PetControlMappingsScreen extends Screen {
    private static final java.util.List<String> ACTION_KEYS = java.util.List.of("SIT", "TOGGLE_WAIT", "RIDE", "OPEN_INV", "CYCLE", "TOGGLE_FOLLOW_TELEPORT", "OPEN_SETTINGS", "RUN_COMMAND", "NONE");
    private static final java.util.List<String> CONDITION_KEYS = java.util.List.of("HEALTH_LT_50", "SADDLE", "SNEAK", "HOLD_ITEM", "NONE");

    private final Screen parent;
    private final int entityId;
    private String controlRightAction;
    private String controlRightCondition;
    private String controlShiftAction;
    private String controlShiftCondition;
    private Button rightActionButton;
    private Button rightConditionButton;
    private Button shiftActionButton;
    private Button shiftConditionButton;
    private Button saveDefaultButton;
    private Button backButton;

    public PetControlMappingsScreen(Screen parent, int entityId, String controlRightAction, String controlRightCondition, String controlShiftAction, String controlShiftCondition) {
        super(Component.translatable("screen.petting.controls.title"));
        this.parent = parent;
        this.entityId = entityId;
        this.controlRightAction = controlRightAction != null ? controlRightAction : "SIT";
        this.controlRightCondition = controlRightCondition != null ? controlRightCondition : "NONE";
        this.controlShiftAction = controlShiftAction != null ? controlShiftAction : "CYCLE";
        this.controlShiftCondition = controlShiftCondition != null ? controlShiftCondition : "NONE";
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
            java.util.List<String> lines = new java.util.ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : msg.split(" ")) {
                String next = current.length() == 0 ? word : current + " " + word;
                if (this.font.width(next) > maxWidth) {
                    if (current.length() > 0) lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(next);
                }
            }
            if (current.length() > 0) lines.add(current.toString());

            int lineHeight = 10;
            int boxW = maxWidth + 8;
            int boxH = lines.size() * lineHeight + 4;
            int bx = mouseX + 12;
            int by = mouseY - boxH - 6;
            if (bx + boxW > this.width) bx = mouseX - boxW - 12;
            if (by < 10) by = mouseY + 12;
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

    private Component actionLabel(String action) {
        return Component.translatable("screen.petting.mapping.action." + action);
    }

    private Component conditionLabel(String condition) {
        return Component.translatable("screen.petting.mapping.cond." + condition);
    }

    private void sendMapping(boolean shiftSide) {
        String action = shiftSide ? this.controlShiftAction : this.controlRightAction;
        String condition = shiftSide ? this.controlShiftCondition : this.controlRightCondition;
        String key = shiftSide ? "control_shift_right_click" : "control_right_click";
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.UpdatePetSettingPayload(this.entityId, key, action + "|" + condition));
    }

    @Override
    protected void init() {
        int cardWidth = Math.min(360, this.width - 24);
        int cardLeft = (this.width - cardWidth) / 2;
        int topY = 56;
        int rowGap = 6;
        int rowHeight = 20;
        boolean stacked = cardWidth < 300 || this.width < 420;
        int columnWidth = stacked ? cardWidth : (cardWidth - 10) / 2;
        int leftX = cardLeft;
        int rightX = stacked ? cardLeft : cardLeft + columnWidth + 10;
        int rightTopY = stacked ? topY + 52 : topY;

        this.rightActionButton = Button.builder(Component.translatable("screen.petting.controls.mapping_right", actionLabel(this.controlRightAction)), b -> {
            Minecraft.getInstance().setScreen(new MappingSelectionScreen(Component.translatable("screen.petting.mapping.select_action"), ACTION_KEYS, ACTION_KEYS.stream().map(this::actionLabel).toList(), ACTION_KEYS.indexOf(this.controlRightAction), sel -> {
                this.controlRightAction = sel;
                this.rightActionButton.setMessage(Component.translatable("screen.petting.controls.mapping_right", actionLabel(this.controlRightAction)));
                sendMapping(false);
            }));
        }).bounds(leftX, topY, columnWidth, rowHeight).build();

        this.rightConditionButton = Button.builder(Component.translatable("screen.petting.controls.condition", conditionLabel(this.controlRightCondition)), b -> {
            Minecraft.getInstance().setScreen(new MappingSelectionScreen(Component.translatable("screen.petting.mapping.select_condition"), CONDITION_KEYS, CONDITION_KEYS.stream().map(this::conditionLabel).toList(), CONDITION_KEYS.indexOf(this.controlRightCondition), sel -> {
                this.controlRightCondition = sel;
                this.rightConditionButton.setMessage(Component.translatable("screen.petting.controls.condition", conditionLabel(this.controlRightCondition)));
                sendMapping(false);
            }));
        }).bounds(leftX, topY + rowHeight + rowGap, columnWidth, rowHeight).build();

        this.shiftActionButton = Button.builder(Component.translatable("screen.petting.controls.mapping_shift", actionLabel(this.controlShiftAction)), b -> {
            Minecraft.getInstance().setScreen(new MappingSelectionScreen(Component.translatable("screen.petting.mapping.select_action"), ACTION_KEYS, ACTION_KEYS.stream().map(this::actionLabel).toList(), ACTION_KEYS.indexOf(this.controlShiftAction), sel -> {
                this.controlShiftAction = sel;
                this.shiftActionButton.setMessage(Component.translatable("screen.petting.controls.mapping_shift", actionLabel(this.controlShiftAction)));
                sendMapping(true);
            }));
        }).bounds(rightX, rightTopY, columnWidth, rowHeight).build();

        this.shiftConditionButton = Button.builder(Component.translatable("screen.petting.controls.condition", conditionLabel(this.controlShiftCondition)), b -> {
            Minecraft.getInstance().setScreen(new MappingSelectionScreen(Component.translatable("screen.petting.mapping.select_condition"), CONDITION_KEYS, CONDITION_KEYS.stream().map(this::conditionLabel).toList(), CONDITION_KEYS.indexOf(this.controlShiftCondition), sel -> {
                this.controlShiftCondition = sel;
                this.shiftConditionButton.setMessage(Component.translatable("screen.petting.controls.condition", conditionLabel(this.controlShiftCondition)));
                sendMapping(true);
            }));
        }).bounds(rightX, rightTopY + rowHeight + rowGap, columnWidth, rowHeight).build();

        int actionBottom = Math.max(topY + 2 * (rowHeight + rowGap), rightTopY + 2 * (rowHeight + rowGap)) + 16;
        int bottomWidth = (cardWidth - 6) / 2;
        this.saveDefaultButton = Button.builder(Component.translatable("screen.petting.controls.save_default"), b ->
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.SavePetControlDefaultsPayload(this.controlRightAction + "|" + this.controlRightCondition, this.controlShiftAction + "|" + this.controlShiftCondition)))
                .bounds(cardLeft, actionBottom, bottomWidth, 20).build();

        this.backButton = Button.builder(Component.translatable("screen.petting.settings.close"), b -> Minecraft.getInstance().setScreen(this.parent))
                .bounds(cardLeft + bottomWidth + 6, actionBottom, bottomWidth, 20).build();

        this.addRenderableWidget(this.rightActionButton);
        this.addRenderableWidget(this.rightConditionButton);
        this.addRenderableWidget(this.shiftActionButton);
        this.addRenderableWidget(this.shiftConditionButton);
        this.addRenderableWidget(this.saveDefaultButton);
        this.addRenderableWidget(this.backButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int cardWidth = Math.min(360, this.width - 24);
        int cardLeft = (this.width - cardWidth) / 2;
        int topY = 56;
        int rowGap = 6;
        int rowHeight = 20;
        boolean stacked = cardWidth < 300 || this.width < 420;
        int columnWidth = stacked ? cardWidth : (cardWidth - 10) / 2;
        int leftX = cardLeft;
        int rightX = stacked ? cardLeft : cardLeft + columnWidth + 10;
        int rightTopY = stacked ? topY + 52 : topY;
        int actionBottom = Math.max(topY + 2 * (rowHeight + rowGap), rightTopY + 2 * (rowHeight + rowGap)) + 16;

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Entity pet = getPetEntity();
        int portraitX = cardLeft + 4;
        int portraitY = 10;
        int portraitSize = 20;
        guiGraphics.fill(portraitX - 1, portraitY - 1, portraitX + portraitSize + 1, portraitY + portraitSize + 1, 0xFF000000);
        if (pet instanceof LivingEntity living) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, portraitX, portraitY, portraitX + portraitSize, portraitY + portraitSize, 20, 0.0625F, mouseX, mouseY, living);
        } else {
            guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BARRIER), portraitX + 2, portraitY + 2);
        }
        guiGraphics.drawString(this.font, getPetName(), portraitX + portraitSize + 6, portraitY + 1, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.title, portraitX + portraitSize + 6, portraitY + 12, 0xAAAAAA, false);

        guiGraphics.drawString(this.font, Component.translatable("screen.petting.controls.right_header").getString(), leftX, topY - 12, 0xDDDDDD);
        guiGraphics.drawString(this.font, Component.translatable("screen.petting.controls.shift_header").getString(), rightX, rightTopY - 12, 0xDDDDDD);

        String tooltip = null;
        if (this.rightActionButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_action";
        else if (this.rightConditionButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_condition";
        else if (this.shiftActionButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_action";
        else if (this.shiftConditionButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_condition";
        else if (this.saveDefaultButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_save_default";
        else if (this.backButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_back";

        if (tooltip != null) {
            drawWrappedTooltip(guiGraphics, Component.translatable(tooltip), mouseX, mouseY);
        }
    }
}