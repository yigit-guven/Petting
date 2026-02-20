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
}
