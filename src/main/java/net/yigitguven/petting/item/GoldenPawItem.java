package net.yigitguven.petting.item;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GoldenPawItem extends Item {

    public GoldenPawItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
