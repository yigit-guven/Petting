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
    private static final int MAX_RULES_PER_SIDE  = 8;
    private static final int MAX_VISIBLE_RULES   = 2;
    // Fixed layout constants so bottom buttons never shift
    private static final int ROW_H        = 20;
    private static final int RULE_BLOCK_H = ROW_H * 2 + 2;  // 42 – action line + condition line
    private static final int RULE_SLOT_H  = RULE_BLOCK_H + 6; // 48 – includes gap between blocks
    private static final int RULES_AREA_H = MAX_VISIBLE_RULES * RULE_SLOT_H; // 96
    private static final int SCROLL_H     = 14; // height of prev/next scroll buttons
    private static final int SECTION_H    = RULES_AREA_H + SCROLL_H + 4; // 114 per column

    private static final java.util.List<String> ACTION_KEYS = java.util.List.of("SIT", "TOGGLE_WAIT", "RIDE", "OPEN_INV", "CYCLE", "TOGGLE_FOLLOW_TELEPORT", "OPEN_SETTINGS", "RUN_COMMAND", "NONE");
    private static final java.util.List<String> CONDITION_KEYS = java.util.List.of("HEALTH_LT_50", "SADDLE", "SNEAK", "HOLD_ITEM", "NONE");

    private static final class MappingRule {
        String action;
        String condition;
        String command;

        MappingRule(String action, String condition) {
            this.action = action;
            this.condition = condition;
            this.command = "";
        }

        MappingRule(String action, String condition, String command) {
            this.action = action;
            this.condition = condition;
            this.command = command == null ? "" : command;
        }
    }

    private final Screen parent;
    private final int entityId;
    private final List<MappingRule> rightRules;
    private final List<MappingRule> shiftRules;
    // Scroll offsets survive rebuildWidgets() because they are instance fields
    private int rightScroll = 0;
    private int shiftScroll = 0;
    private Button addRightButton;
    private Button addShiftButton;
    private Button recommendedButton;
    private Button resetButton;
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
                String command = "";
                if (trimmed.contains("|")) {
                    String[] parts = trimmed.split("\\|", 3);
                    action = parts[0].trim();
                    if (parts.length > 1 && !parts[1].isBlank()) condition = parts[1].trim();
                    if (parts.length > 2) command = parts[2];
                } else {
                    action = trimmed;
                }
                if (!ACTION_KEYS.contains(action)) action = fallbackAction;
                if (!CONDITION_KEYS.contains(condition)) condition = "NONE";
                rules.add(new MappingRule(action, condition, command));
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
        return rules.stream().map(r -> {
            String base = r.action + "|" + r.condition;
            if ("RUN_COMMAND".equals(r.action) && r.command != null && !r.command.isBlank()) {
                return base + "|" + r.command;
            }
            return base;
        }).collect(java.util.stream.Collectors.joining(";"));
    }

    private Component buildActionBtnLabel(MappingRule rule) {
        if ("RUN_COMMAND".equals(rule.action) && rule.command != null && !rule.command.isEmpty()) {
            String preview = rule.command.length() > 14 ? rule.command.substring(0, 12) + "..." : rule.command;
            return Component.literal("Run: " + preview);
        }
        return actionLabel(rule.action);
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
        int cardLeft  = (this.width - cardWidth) / 2;
        int topY = 56;
        boolean stacked = this.width < 420;
        int colW  = stacked ? cardWidth : (cardWidth - 10) / 2;
        int leftX = cardLeft;
        int rightX = stacked ? cardLeft : cardLeft + colW + 10;
        // rightTopY is fixed regardless of how many rules exist
        int rightTopY  = stacked ? topY + SECTION_H + 18 : topY;
        // bottomTop is ALWAYS a fixed offset from topY – never depends on rule count
        int bottomTop  = (stacked ? rightTopY + SECTION_H : topY + SECTION_H) + 8;
        int halfW = (cardWidth - 6) / 2;

        createColumnWidgets(false, leftX,  topY,      colW);
        createColumnWidgets(true,  rightX, rightTopY, colW);

        this.recommendedButton = Button.builder(Component.translatable("screen.petting.controls.recommended"), b -> {
            this.rightRules.clear();
            this.rightRules.add(new MappingRule("RIDE", "SADDLE"));
            this.rightRules.add(new MappingRule("OPEN_INV", "NONE"));
            this.shiftRules.clear();
            this.shiftRules.add(new MappingRule("CYCLE", "NONE"));
            rightScroll = 0; shiftScroll = 0;
            sendMapping(false); sendMapping(true);
            rebuildWidgets();
        }).bounds(cardLeft, bottomTop, halfW, 20).build();

        this.resetButton = Button.builder(Component.translatable("screen.petting.controls.reset_basic"), b -> {
            this.rightRules.clear();
            this.rightRules.add(new MappingRule("SIT", "NONE"));
            this.shiftRules.clear();
            this.shiftRules.add(new MappingRule("CYCLE", "NONE"));
            rightScroll = 0; shiftScroll = 0;
            sendMapping(false); sendMapping(true);
            rebuildWidgets();
        }).bounds(cardLeft + halfW + 6, bottomTop, halfW, 20).build();

        this.saveDefaultButton = Button.builder(Component.translatable("screen.petting.controls.save_default"), b ->
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.SavePetControlDefaultsPayload(
                        serializeRules(this.rightRules, "SIT"), serializeRules(this.shiftRules, "CYCLE"))))
                .bounds(cardLeft, bottomTop + 24, halfW, 20).build();

        this.backButton = Button.builder(Component.translatable("screen.petting.settings.close"), b -> Minecraft.getInstance().setScreen(this.parent))
                .bounds(cardLeft + halfW + 6, bottomTop + 24, halfW, 20).build();

        this.addRenderableWidget(this.recommendedButton);
        this.addRenderableWidget(this.resetButton);
        this.addRenderableWidget(this.saveDefaultButton);
        this.addRenderableWidget(this.backButton);
    }

    /** Builds widgets for one column (right-click or shift-click rules). */
    private void createColumnWidgets(boolean shiftSide, int colX, int topY, int colW) {
        List<MappingRule> rules  = shiftSide ? this.shiftRules : this.rightRules;
        int scroll = shiftSide ? this.shiftScroll : this.rightScroll;
        int total  = rules.size();
        int start  = Math.max(0, Math.min(scroll, Math.max(0, total - MAX_VISIBLE_RULES)));
        int end    = Math.min(start + MAX_VISIBLE_RULES, total);

        // [+] button in the header strip
        Button addBtn = Button.builder(Component.literal("+"), b -> {
            if (rules.size() < MAX_RULES_PER_SIDE) {
                rules.add(new MappingRule("NONE", "NONE"));
                int ns = Math.max(0, rules.size() - MAX_VISIBLE_RULES);
                if (shiftSide) shiftScroll = ns; else rightScroll = ns;
                sendMapping(shiftSide);
                rebuildWidgets();
            }
        }).bounds(colX + colW - 20, topY - 14, 20, 14).build();
        addBtn.active = rules.size() < MAX_RULES_PER_SIDE;
        this.addRenderableWidget(addBtn);
        if (shiftSide) addShiftButton = addBtn; else addRightButton = addBtn;

        // Visible rule rows
        int condW = Math.max(70, colW - 44); // leave 44 px for ^  v  - controls
        for (int vi = 0; vi < end - start; vi++) {
            final int idx = start + vi;
            MappingRule rule = rules.get(idx);
            int rowY  = topY + vi * RULE_SLOT_H;
            int rowY2 = rowY + ROW_H + 2;

            // Row 1 – action selector (full width)
            Button actionBtn = Button.builder(buildActionBtnLabel(rule), b ->
                Minecraft.getInstance().setScreen(new MappingSelectionScreen(this,
                    Component.translatable("screen.petting.mapping.select_action"),
                    ACTION_KEYS, ACTION_KEYS.stream().map(this::actionLabel).toList(),
                    Math.max(0, ACTION_KEYS.indexOf(rules.get(idx).action)),
                    sel -> {
                        if ("RUN_COMMAND".equals(sel)) {
                            // Chain to command input screen; action/command set only on confirm
                            Minecraft.getInstance().setScreen(new CommandInputScreen(
                                PetControlMappingsScreen.this,
                                rules.get(idx).command,
                                cmd -> {
                                    rules.get(idx).action = "RUN_COMMAND";
                                    rules.get(idx).command = cmd;
                                    sendMapping(shiftSide);
                                }
                            ));
                        } else {
                            rules.get(idx).action = sel;
                            rules.get(idx).command = "";
                            sendMapping(shiftSide);
                            rebuildWidgets();
                        }
                    }
                ))
            ).bounds(colX, rowY, colW, ROW_H).build();
            this.addRenderableWidget(actionBtn);

            // Row 2 – condition selector
            Button condBtn = Button.builder(conditionLabel(rule.condition), b ->
                Minecraft.getInstance().setScreen(new MappingSelectionScreen(this,
                    Component.translatable("screen.petting.mapping.select_condition"),
                    CONDITION_KEYS, CONDITION_KEYS.stream().map(this::conditionLabel).toList(),
                    Math.max(0, CONDITION_KEYS.indexOf(rules.get(idx).condition)),
                    sel -> { rules.get(idx).condition = sel; sendMapping(shiftSide); rebuildWidgets(); }
                ))
            ).bounds(colX, rowY2, condW, ROW_H).build();
            this.addRenderableWidget(condBtn);

            // Row 2 – ^ v - controls (12 px each, right-aligned)
            int cx = colX + condW + 4;
            Button upBtn = Button.builder(Component.literal("^"), b -> {
                if (idx > 0) { java.util.Collections.swap(rules, idx, idx - 1); sendMapping(shiftSide); rebuildWidgets(); }
            }).bounds(cx, rowY2, 12, ROW_H).build();
            upBtn.active = idx > 0;
            this.addRenderableWidget(upBtn);

            Button downBtn = Button.builder(Component.literal("v"), b -> {
                if (idx < rules.size() - 1) { java.util.Collections.swap(rules, idx, idx + 1); sendMapping(shiftSide); rebuildWidgets(); }
            }).bounds(cx + 14, rowY2, 12, ROW_H).build();
            downBtn.active = idx < rules.size() - 1;
            this.addRenderableWidget(downBtn);

            Button delBtn = Button.builder(Component.literal("-"), b -> {
                if (rules.size() > 1) {
                    rules.remove(idx);
                    int maxS = Math.max(0, rules.size() - MAX_VISIBLE_RULES);
                    if (shiftSide) shiftScroll = Math.min(shiftScroll, maxS);
                    else rightScroll = Math.min(rightScroll, maxS);
                    sendMapping(shiftSide); rebuildWidgets();
                }
            }).bounds(cx + 28, rowY2, 12, ROW_H).build();
            delBtn.active = rules.size() > 1;
            this.addRenderableWidget(delBtn);
        }

        // Prev / Next scroll buttons pinned just below the rules area
        int scrollY = topY + RULES_AREA_H + 2;
        int sbW = (colW - 4) / 2;

        Button prevBtn = Button.builder(Component.literal("\u25C4 Prev"), b -> {
            if (shiftSide) shiftScroll = Math.max(0, shiftScroll - 1);
            else rightScroll = Math.max(0, rightScroll - 1);
            rebuildWidgets();
        }).bounds(colX, scrollY, sbW, SCROLL_H).build();
        prevBtn.active = start > 0;
        this.addRenderableWidget(prevBtn);

        Button nextBtn = Button.builder(Component.literal("Next \u25BA"), b -> {
            if (shiftSide) shiftScroll = Math.min(total - MAX_VISIBLE_RULES, shiftScroll + 1);
            else rightScroll = Math.min(total - MAX_VISIBLE_RULES, rightScroll + 1);
            rebuildWidgets();
        }).bounds(colX + sbW + 4, scrollY, sbW, SCROLL_H).build();
        nextBtn.active = end < total;
        this.addRenderableWidget(nextBtn);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int cardWidth  = Math.min(360, this.width - 24);
        int cardLeft   = (this.width - cardWidth) / 2;
        int topY       = 56;
        boolean stacked = this.width < 420;
        int colW   = stacked ? cardWidth : (cardWidth - 10) / 2;
        int leftX  = cardLeft;
        int rightX = stacked ? cardLeft : cardLeft + colW + 10;
        int rightTopY  = stacked ? topY + SECTION_H + 18 : topY;
        int bottomTop  = (stacked ? rightTopY + SECTION_H : topY + SECTION_H) + 8;

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Portrait
        Entity pet = getPetEntity();
        int px = cardLeft + 4, py = 10, ps = 20;
        guiGraphics.fill(px - 1, py - 1, px + ps + 1, py + ps + 1, 0xFF000000);
        if (pet instanceof LivingEntity living)
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, px, py, px + ps, py + ps, 20, 0.0625F, mouseX, mouseY, living);
        else
            guiGraphics.renderItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BARRIER), px + 2, py + 2);
        guiGraphics.drawString(this.font, getPetName(), px + ps + 6, py + 1,  0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.title,   px + ps + 6, py + 12, 0xAAAAAA, false);

        // Column headers – show rule count and visible range when scrolled
        String rh = Component.translatable("screen.petting.controls.right_header").getString();
        String sh = Component.translatable("screen.petting.controls.shift_header").getString();
        if (rightRules.size() > MAX_VISIBLE_RULES)
            rh += "  " + (rightScroll + 1) + "-" + Math.min(rightScroll + MAX_VISIBLE_RULES, rightRules.size()) + "/" + rightRules.size();
        if (shiftRules.size() > MAX_VISIBLE_RULES)
            sh += "  " + (shiftScroll + 1) + "-" + Math.min(shiftScroll + MAX_VISIBLE_RULES, shiftRules.size()) + "/" + shiftRules.size();
        guiGraphics.drawString(this.font, rh, leftX,  topY      - 12, 0xDDDDDD);
        guiGraphics.drawString(this.font, sh, rightX, rightTopY - 12, 0xDDDDDD);

        // Hint above bottom buttons
        guiGraphics.drawString(this.font, Component.translatable("screen.petting.controls.order_hint").getString(), cardLeft, bottomTop - 10, 0x9A9A9A);

        // Tooltip
        String tooltip = null;
        if (this.addRightButton   != null && this.addRightButton.isMouseOver(mouseX, mouseY))   tooltip = "screen.petting.tooltip.mapping_add_rule";
        else if (this.addShiftButton   != null && this.addShiftButton.isMouseOver(mouseX, mouseY))   tooltip = "screen.petting.tooltip.mapping_add_rule";
        else if (this.recommendedButton != null && this.recommendedButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_recommended";
        else if (this.resetButton       != null && this.resetButton.isMouseOver(mouseX, mouseY))       tooltip = "screen.petting.tooltip.controls_reset_basic";
        else if (this.saveDefaultButton != null && this.saveDefaultButton.isMouseOver(mouseX, mouseY)) tooltip = "screen.petting.tooltip.controls_save_default";
        else if (this.backButton        != null && this.backButton.isMouseOver(mouseX, mouseY))        tooltip = "screen.petting.tooltip.controls_back";
        if (tooltip != null)
            drawWrappedTooltip(guiGraphics, Component.translatable(tooltip), mouseX, mouseY);
    }
}