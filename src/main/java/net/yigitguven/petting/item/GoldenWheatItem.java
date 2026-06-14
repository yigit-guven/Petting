package net.yigitguven.petting.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import java.util.List;

public class GoldenWheatItem extends Item {
	public GoldenWheatItem() {
		super(new Item.Properties().rarity(Rarity.EPIC));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.translatable("item.petting.golden_wheat.tooltip").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
