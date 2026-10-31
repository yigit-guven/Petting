package net.yigitguven.petting.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class ItemSupplier<T extends Item> extends RegistrySupplier<T> implements ItemLike {
    public ItemSupplier(T value) {
        super(value);
    }

    @Override
    public Item asItem() {
        return get();
    }
}
