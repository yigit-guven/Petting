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
import java.util.ArrayList;
import java.util.List;

public class PetControlMappingsScreen extends Screen {
    private static final int MAX_RULES_PER_SIDE = 4;
    private static final java.util.List<String> ACTION_KEYS = java.util.List.of("SIT", "TOGGLE_WAIT", "RIDE", "OPEN_INV", "CYCLE", "TOGGLE_FOLLOW_TELEPORT", "OPEN_SETTINGS", "RUN_COMMAND", "NONE");
    private static final java.util.List<String> CONDITION_KEYS = java.util.List.of("HEALTH_LT_50", "SADDLE", "SNEAK", "HOLD_ITEM", "NONE");

    private static final class MappingRule {
        String action;
        String condition;

        MappingRule(String action, String condition) {
            this.action = action;
            this.condition = condition;
        }
    }

    private final Screen parent;
    private final int entityId;
    private final List<MappingRule> rightRules;
    private final List<MappingRule> shiftRules;
    private Button addRightButton;
    private Button addShiftButton;
    private Button saveDefaultButton;
    private Button backButton;

    public PetControlMappingsScreen(Screen parent, int entityId, String controlRightMapping, String controlShiftMapping) {
        super(Component.translatable("screen.petting.controls.title"));
        this.parent = parent;
        this.entityId = entityId;
        this.rightRules = parseRules(controlRightMapping, "SIT");
        this.shiftRules = parseRules(controlShiftMapping, "CYCLE");
    }

    private static List<MappingRule> parseRules(String mapping, String fallbackAction) {
        List<MappingRule> rules = new ArrayList<>();
        if (mapping != null && !mapping.isBlank()) {
            String[] rawRules = mapping.split(";");
            for (String raw : rawRules) {
                String trimmed = raw.trim();
                if (trimmed.isEmpty()) continue;
                String action;
                String condition = "NONE";
                if (trimmed.contains("|")) {
                    String[] parts = trimmed.split("\\|", 2);
                    action = parts[0].trim();
                    if (parts.length > 1 && !parts[1].isBlank()) condition = parts[1].trim();
                } else {
                    action = trimmed;
                }
                if (!ACTION_KEYS.contains(action)) action = fallbackAction;
                if (!CONDITION_KEYS.contains(condition)) condition = "NONE";
                rules.add(new MappingRule(action, condition));
            }
        }
        if (rules.isEmpty()) {
            rules.add(new MappingRule(fallbackAction, "NONE"));
        }
        return rules;
    }

    private static String serializeRules(List<MappingRule> rules, String fallbackAction) {
        if (rules.isEmpty()) {
            return fallbackAction + "|NONE";
        }
        return rules.stream().map(r -> r.action + "|" + r.condition).collect(java.util.stream.Collectors.joining(";"));
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
        String mapping = shiftSide ? serializeRules(this.shiftRules, "CYCLE") : serializeRules(this.rightRules, "SIT");
        String key = shiftSide ? "control_shift_right_click" : "control_right_click";
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.UpdatePetSettingPayload(this.entityId, key, mapping));
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
        int rightTopY = stacked ? topY + Math.max(1, this.rightRules.size()) * (rowHeight + rowGap) + 34 : topY;

        createRuleWidgets(false, leftX, topY, columnWidth, rowHeight, rowGap);
        createRuleWidgets(true, rightX, rightTopY, columnWidth, rowHeight, rowGap);

        int leftBottom = topY + Math.max(1, this.rightRules.size()) * (rowHeight + rowGap);
        int rightBottom = rightTopY + Math.max(1, this.shiftRules.size()) * (rowHeight + rowGap);
        int actionBottom = Math.max(leftBottom, rightBottom) + 16;
        int bottomWidth = (cardWidth - 6) / 2;
        this.saveDefaultButton = Button.builder(Component.translatable("screen.petting.controls.save_default"), b ->
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.SavePetControlDefaultsPayload(serializeRules(this.rightRules, "SIT"), serializeRules(this.shiftRules, "CYCLE"))))
                .bounds(cardLeft, actionBottom, bottomWidth, 20).build();

        this.backButton = Button.builder(Component.translatable("screen.petting.settings.close"), b -> Minecraft.getInstance().setScreen(this.parent))
                .bounds(cardLeft + bottomWidth + 6, actionBottom, bottomWidth, 20).build();

        this.addRenderableWidget(this.saveDefaultButton);
        this.addRenderableWidget(this.backButton);
    }

    private void createRuleWidgets(boolean shiftSide, int columnX, int topY, int columnWidth, int rowHeight, int rowGap) {
        List<MappingRule> rules = shiftSide ? this.shiftRules : this.rightRules;
        int shownRules = Math.min(MAX_RULES_PER_SIDE, rules.size());
        int addX = columnX + columnWidth - 20;
        Button addButton = Button.builder(Component.literal("+"), b -> {
            if (rules.size() < MAX_RULES_PER_SIDE) {
                rules.add(new MappingRule("NONE", "NONE"));
                sendMapping(shiftSide);
                this.init();
            }
        }).bounds(addX, topY - 14, 20, 14).build();
        this.addRenderableWidget(addButton);
        if (shiftSide) {
            this.addShiftButton = addButton;
        } else {
            this.addRightButton = addButton;
        }

        for (int i = 0; i < shownRules; i++) {
            final int idx = i;
            MappingRule rule = rules.get(i);
            int rowY = topY + i * (rowHeight + rowGap);
            int removeW = 20;
            int condW = 96;
            int actionW = Math.max(78, columnWidth - condW - removeW - 8);

            Button actionButton = Button.builder(actionLabel(rule.action), b -> {
                Minecraft.getInstance().setScreen(new MappingSelectionScreen(this,
                        Component.translatable("screen.petting.mapping.select_action"),
                        ACTION_KEYS,
                        ACTION_KEYS.stream().map(this::actionLabel).toList(),
                        Math.max(0, ACTION_KEYS.indexOf(rules.get(idx).action)),
                        sel -> {
                            rules.get(idx).action = sel;
                            sendMapping(shiftSide);
                            this.init();
                        }
                ));
            }).bounds(columnX, rowY, actionW, rowHeight).build();
            this.addRenderableWidget(actionButton);

            Button conditionButton = Button.builder(conditionLabel(rule.condition), b -> {
                Minecraft.getInstance().setScreen(new MappingSelectionScreen(this,
                        Component.translatable("screen.petting.mapping.select_condition"),
                        CONDITION_KEYS,
                        CONDITION_KEYS.stream().map(this::conditionLabel).toList(),
                        Math.max(0, CONDITION_KEYS.indexOf(rules.get(idx).condition)),
                        sel -> {
                            rules.get(idx).condition = sel;
                            sendMapping(shiftSide);
                            this.init();
                        }
                ));
            }).bounds(columnX + actionW + 4, rowY, condW, rowHeight).build();
            this.addRenderableWidget(conditionButton);

            Button removeButton = Button.builder(Component.literal("-"), b -> {
                if (rules.size() > 1 && idx < rules.size()) {
                    rules.remove(idx);
                    sendMapping(shiftSide);
                    this.init();
                }
            }).bounds(columnX + actionW + 4 + condW + 4, rowY, removeW, rowHeight).build();
            removeButton.active = rules.size() > 1;
            this.addRenderableWidget(removeButton);
        }
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
        int rightTopY = stacked ? topY + Math.max(1, this.rightRules.size()) * (rowHeight + rowGap) + 34 : topY;
        int leftBottom = topY + Math.max(1, this.rightRules.size()) * (rowHeight + rowGap);
        int rightBottom = rightTopY + Math.max(1, this.shiftRules.size()) * (rowHeight + rowGap);
        int actionBottom = Math.max(leftBottom, rightBottom) + 16;

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
        guiGraphics.drawString(this.font, Component.translatable("screen.petting.controls.order_hint").getString(), cardLeft, actionBottom - 10, 0x9A9A9A);

        String tooltip = null;
        if (this.addRightButton != null && this.addRightButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_add_rule";
        else if (this.addShiftButton != null && this.addShiftButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.mapping_add_rule";
        else if (this.saveDefaultButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_save_default";
        else if (this.backButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_back";

        if (tooltip != null) {
            drawWrappedTooltip(guiGraphics, Component.translatable(tooltip), mouseX, mouseY);
        }
    }
}