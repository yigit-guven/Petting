package net.yigitguven.petting.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class GoldenTreatItem extends Item {

    public GoldenTreatItem(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
