package net.yigitguven.petting.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class BlockSupplier<T extends Block> extends RegistrySupplier<T> implements ItemLike {
    public BlockSupplier(T value) {
        super(value);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }
}
