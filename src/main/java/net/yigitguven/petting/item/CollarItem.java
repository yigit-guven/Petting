package net.yigitguven.petting.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class CollarItem extends Item {
    private final DyeColor color;

    public CollarItem(DyeColor color, Properties properties) {
        super(properties.stacksTo(1));
        this.color = color;
    }

    public CollarItem(Properties properties) {
        this(DyeColor.RED, properties);
    }

    public DyeColor getColor() {
        return this.color;
    }
}
