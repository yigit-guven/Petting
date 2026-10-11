package net.yigitguven.petting.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.init.PettingDataComponents;

import java.util.function.Consumer;

public class CollarItem extends Item {
    private final DyeColor color;

    public CollarItem(DyeColor color, Properties properties) {
        super(properties.stacksTo(1));
        this.color = color;
    }

    public CollarItem(Properties properties) {
        this(DyeColor.RED, properties);
    }

    public DyeColor getColor() {
        return this.color;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltipAdder, flag);
        if (stack.has(PettingDataComponents.COLLAR_SETTINGS)) {
            CollarSettings settings = stack.get(PettingDataComponents.COLLAR_SETTINGS);
            if (settings != null) {
                tooltipAdder.accept(Component.translatable("petting.collar.tooltip.header").withStyle(ChatFormatting.GOLD));

                Component modeName = switch (settings.combatMode()) {
                    case PASSIVE -> Component.translatable("petting.gui.collar.passive").withStyle(ChatFormatting.AQUA);
                    case DEFENSIVE -> Component.translatable("petting.gui.collar.defensive").withStyle(ChatFormatting.YELLOW);
                    case AGGRESSIVE -> Component.translatable("petting.gui.collar.aggressive").withStyle(ChatFormatting.RED);
                };
                tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("petting.gui.collar.mode").withStyle(ChatFormatting.GRAY))
                        .append(": ").append(modeName));

                tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("petting.gui.collar.follow_distance").withStyle(ChatFormatting.GRAY))
                        .append(": " + settings.followDistance() + " blocks"));

                if (settings.teleportEnabled()) {
                    tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable("petting.gui.collar.teleport_distance").withStyle(ChatFormatting.GRAY))
                            .append(": " + settings.teleportDistance() + " blocks"));
                } else {
                    tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable("petting.gui.collar.teleport").withStyle(ChatFormatting.GRAY))
                            .append(": ").append(Component.translatable("petting.gui.collar.off").withStyle(ChatFormatting.RED)));
                }

                if (settings.preferRangedAttack()) {
                    tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable("petting.gui.collar.ranged").withStyle(ChatFormatting.GRAY))
                            .append(": ").append(Component.translatable("petting.gui.collar.on").withStyle(ChatFormatting.GREEN)));
                }

                if (settings.defendOtherPets()) {
                    tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable("petting.gui.collar.defend_pets").withStyle(ChatFormatting.GRAY))
                            .append(": ").append(Component.translatable("petting.gui.collar.on").withStyle(ChatFormatting.GREEN)));
                }

                if (settings.retaliateWhenHurt()) {
                    tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.translatable("petting.gui.collar.retaliate").withStyle(ChatFormatting.GRAY))
                            .append(": ").append(Component.translatable("petting.gui.collar.on").withStyle(ChatFormatting.GREEN)));
                }
            }
        }
    }
}
