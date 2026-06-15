package net.yigitguven.petting.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class PetTetherItem extends Item {
	public PetTetherItem() {
		super(new Item.Properties().rarity(Rarity.RARE));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean isFoil(ItemStack itemstack) {
		return false;
	}

	@Override
	public void appendHoverText(ItemStack itemstack, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level, java.util.List<net.minecraft.network.chat.Component> list, net.minecraft.world.item.TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(net.minecraft.network.chat.Component.translatable("item.petting.pet_tether.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
	}
}
