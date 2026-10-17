package net.yigitguven.petting.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.yigitguven.petting.init.PettingDataComponents;

import java.util.List;

public class GoldenDragonEggItem extends BlockItem {
    public GoldenDragonEggItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipComponents, flag);
        if (stack.has(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get())) {
            String owner = stack.get(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get());
            tooltipComponents.add(Component.translatable("petting.tooltip.golden_egg_owner", owner).withStyle(ChatFormatting.GOLD));
        }
        tooltipComponents.add(Component.translatable("petting.tooltip.golden_egg_ritual").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
