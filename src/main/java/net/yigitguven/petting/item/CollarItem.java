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
                    case WANDER -> Component.translatable("petting.gui.collar.wander").withStyle(ChatFormatting.GREEN);
                    case GUARD -> Component.translatable("petting.gui.collar.guard").withStyle(ChatFormatting.GOLD);
                };
                tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("petting.gui.collar.mode").withStyle(ChatFormatting.GRAY))
                        .append(": ").append(modeName));

                String distKey = switch (settings.combatMode()) {
                    case WANDER -> "petting.gui.collar.wander_radius";
                    case GUARD -> "petting.gui.collar.guard_radius";
                    default -> "petting.gui.collar.follow_distance";
                };
                tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable(distKey).withStyle(ChatFormatting.GRAY))
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

        if (stack.has(PettingDataComponents.COLLAR_RESPAWN_POINT)) {
            net.yigitguven.petting.data.CollarRespawnPoint respawn = stack.get(PettingDataComponents.COLLAR_RESPAWN_POINT);
            if (respawn != null) {
                Component targetType = Component.translatable(respawn.isCushion() ? "petting.term.cushion" : "petting.term.bed");
                net.minecraft.core.BlockPos pos = respawn.pos().pos();
                tooltipAdder.accept(Component.literal(" • ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("petting.collar.tooltip.respawn_bound", targetType).withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(Component.literal(" [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]").withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(PettingDataComponents.COLLAR_RESPAWN_POINT) || super.isFoil(stack);
    }
}
