package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.item.GoldenDragonEggItem;
import net.yigitguven.petting.item.GoldenPawItem;
import net.yigitguven.petting.item.GoldenTreatItem;

import java.util.Map;
import java.util.function.Function;

public class PettingModItems {

    public static final ItemSupplier<Item> GOLDEN_PAW =
            registerItem("golden_paw", GoldenPawItem::new);

    public static final ItemSupplier<Item> GOLDEN_WHEAT =
            registerItem("golden_wheat", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_STAR =
            registerItem("golden_star", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_FLESH =
            registerItem("golden_flesh", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_BONE =
            registerItem("golden_bone", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_FISH =
            registerItem("golden_fish", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_KELP =
            registerItem("golden_kelp", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_FUNGUS =
            registerItem("golden_fungus", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_EYE =
            registerItem("golden_eye", GoldenTreatItem::new);

    public static final ItemSupplier<Item> GOLDEN_SLIME =
            registerItem("golden_slime", GoldenTreatItem::new);

    public static final ItemSupplier<GoldenDragonEggItem> GOLDEN_DRAGON_EGG =
            registerItem("golden_dragon_egg",
                    props -> new GoldenDragonEggItem(PettingModBlocks.GOLDEN_DRAGON_EGG.get(),
                            props.rarity(Rarity.EPIC)));

    // Default Collar (Red)
    public static final ItemSupplier<CollarItem> COLLAR =
            registerItem("collar", CollarItem::new);

    // Dye Color Variants
    public static final ItemSupplier<CollarItem> WHITE_COLLAR =
            registerItem("white_collar", props -> new CollarItem(DyeColor.WHITE, props));

    public static final ItemSupplier<CollarItem> ORANGE_COLLAR =
            registerItem("orange_collar", props -> new CollarItem(DyeColor.ORANGE, props));

    public static final ItemSupplier<CollarItem> MAGENTA_COLLAR =
            registerItem("magenta_collar", props -> new CollarItem(DyeColor.MAGENTA, props));

    public static final ItemSupplier<CollarItem> LIGHT_BLUE_COLLAR =
            registerItem("light_blue_collar", props -> new CollarItem(DyeColor.LIGHT_BLUE, props));

    public static final ItemSupplier<CollarItem> YELLOW_COLLAR =
            registerItem("yellow_collar", props -> new CollarItem(DyeColor.YELLOW, props));

    public static final ItemSupplier<CollarItem> LIME_COLLAR =
            registerItem("lime_collar", props -> new CollarItem(DyeColor.LIME, props));

    public static final ItemSupplier<CollarItem> PINK_COLLAR =
            registerItem("pink_collar", props -> new CollarItem(DyeColor.PINK, props));

    public static final ItemSupplier<CollarItem> GRAY_COLLAR =
            registerItem("gray_collar", props -> new CollarItem(DyeColor.GRAY, props));

    public static final ItemSupplier<CollarItem> LIGHT_GRAY_COLLAR =
            registerItem("light_gray_collar", props -> new CollarItem(DyeColor.LIGHT_GRAY, props));

    public static final ItemSupplier<CollarItem> CYAN_COLLAR =
            registerItem("cyan_collar", props -> new CollarItem(DyeColor.CYAN, props));

    public static final ItemSupplier<CollarItem> PURPLE_COLLAR =
            registerItem("purple_collar", props -> new CollarItem(DyeColor.PURPLE, props));

    public static final ItemSupplier<CollarItem> BLUE_COLLAR =
            registerItem("blue_collar", props -> new CollarItem(DyeColor.BLUE, props));

    public static final ItemSupplier<CollarItem> BROWN_COLLAR =
            registerItem("brown_collar", props -> new CollarItem(DyeColor.BROWN, props));

    public static final ItemSupplier<CollarItem> GREEN_COLLAR =
            registerItem("green_collar", props -> new CollarItem(DyeColor.GREEN, props));

    public static final ItemSupplier<CollarItem> BLACK_COLLAR =
            registerItem("black_collar", props -> new CollarItem(DyeColor.BLACK, props));

    public static final Map<DyeColor, ItemSupplier<CollarItem>> COLLARS = Map.ofEntries(
            Map.entry(DyeColor.RED, COLLAR),
            Map.entry(DyeColor.WHITE, WHITE_COLLAR),
            Map.entry(DyeColor.ORANGE, ORANGE_COLLAR),
            Map.entry(DyeColor.MAGENTA, MAGENTA_COLLAR),
            Map.entry(DyeColor.LIGHT_BLUE, LIGHT_BLUE_COLLAR),
            Map.entry(DyeColor.YELLOW, YELLOW_COLLAR),
            Map.entry(DyeColor.LIME, LIME_COLLAR),
            Map.entry(DyeColor.PINK, PINK_COLLAR),
            Map.entry(DyeColor.GRAY, GRAY_COLLAR),
            Map.entry(DyeColor.LIGHT_GRAY, LIGHT_GRAY_COLLAR),
            Map.entry(DyeColor.CYAN, CYAN_COLLAR),
            Map.entry(DyeColor.PURPLE, PURPLE_COLLAR),
            Map.entry(DyeColor.BLUE, BLUE_COLLAR),
            Map.entry(DyeColor.BROWN, BROWN_COLLAR),
            Map.entry(DyeColor.GREEN, GREEN_COLLAR),
            Map.entry(DyeColor.BLACK, BLACK_COLLAR)
    );

    public static ItemSupplier<CollarItem> getCollar(DyeColor color) {
        return COLLARS.getOrDefault(color, COLLAR);
    }

    private static <T extends Item> ItemSupplier<T> registerItem(String name, Function<Item.Properties, T> factory) {
        T item = factory.apply(new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Petting.MODID, name), item);
        return new ItemSupplier<>(item);
    }

    public static void register() {
    }
}
