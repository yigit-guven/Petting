package net.yigitguven.petting.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

public class GoldenDragonEggItem extends BlockItem {
    public GoldenDragonEggItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltipAdder, flag);
        if (stack.getTag() != null && stack.getTag().contains("petting:dragon_egg_owner_name")) {
            String owner = stack.getTag().getString("petting:dragon_egg_owner_name");
            tooltipAdder.add(Component.translatable("petting.tooltip.golden_egg_owner", owner).withStyle(ChatFormatting.GOLD));
        }
        tooltipAdder.add(Component.translatable("petting.tooltip.golden_egg_ritual").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
