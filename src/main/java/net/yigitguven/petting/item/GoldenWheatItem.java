package net.yigitguven.petting.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import java.util.function.Consumer;

public class GoldenWheatItem extends Item {
	public GoldenWheatItem() {
		super(new Item.Properties().rarity(Rarity.EPIC).component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Item.TooltipContext context, Consumer<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);
		list.accept(Component.translatable("item.petting.golden_wheat.tooltip").withStyle(ChatFormatting.GRAY));
	}


}



