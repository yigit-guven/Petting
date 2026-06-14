package net.yigitguven.petting.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;

public class TeleportOrbItem extends Item {
	public TeleportOrbItem() {
		super(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, net.minecraft.world.level.Level context, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, context, list, flag);
		list.add(Component.translatable("item.petting.teleport_orb.tooltip").withStyle(ChatFormatting.GRAY));
	}
}



