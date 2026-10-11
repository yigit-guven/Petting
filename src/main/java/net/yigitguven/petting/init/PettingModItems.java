package net.yigitguven.petting.init;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.item.GoldenPawItem;
import net.yigitguven.petting.item.GoldenTreatItem;

import java.util.Map;

public class PettingModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Petting.MODID);

    public static final DeferredItem<Item> GOLDEN_PAW =
            ITEMS.registerItem("golden_paw", GoldenPawItem::new);

    public static final DeferredItem<Item> GOLDEN_WHEAT =
            ITEMS.registerItem("golden_wheat", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_STAR =
            ITEMS.registerItem("golden_star", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_FLESH =
            ITEMS.registerItem("golden_flesh", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_BONE =
            ITEMS.registerItem("golden_bone", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_FISH =
            ITEMS.registerItem("golden_fish", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_KELP =
            ITEMS.registerItem("golden_kelp", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_FUNGUS =
            ITEMS.registerItem("golden_fungus", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_EYE =
            ITEMS.registerItem("golden_eye", GoldenTreatItem::new);

    public static final DeferredItem<Item> GOLDEN_SLIME =
            ITEMS.registerItem("golden_slime", GoldenTreatItem::new);

    // Default Collar (Red)
    public static final DeferredItem<CollarItem> COLLAR =
            ITEMS.registerItem("collar", CollarItem::new);

    // Dye Color Variants
    public static final DeferredItem<CollarItem> WHITE_COLLAR =
            ITEMS.registerItem("white_collar", props -> new CollarItem(DyeColor.WHITE, props));

    public static final DeferredItem<CollarItem> ORANGE_COLLAR =
            ITEMS.registerItem("orange_collar", props -> new CollarItem(DyeColor.ORANGE, props));

    public static final DeferredItem<CollarItem> MAGENTA_COLLAR =
            ITEMS.registerItem("magenta_collar", props -> new CollarItem(DyeColor.MAGENTA, props));

    public static final DeferredItem<CollarItem> LIGHT_BLUE_COLLAR =
            ITEMS.registerItem("light_blue_collar", props -> new CollarItem(DyeColor.LIGHT_BLUE, props));

    public static final DeferredItem<CollarItem> YELLOW_COLLAR =
            ITEMS.registerItem("yellow_collar", props -> new CollarItem(DyeColor.YELLOW, props));

    public static final DeferredItem<CollarItem> LIME_COLLAR =
            ITEMS.registerItem("lime_collar", props -> new CollarItem(DyeColor.LIME, props));

    public static final DeferredItem<CollarItem> PINK_COLLAR =
            ITEMS.registerItem("pink_collar", props -> new CollarItem(DyeColor.PINK, props));

    public static final DeferredItem<CollarItem> GRAY_COLLAR =
            ITEMS.registerItem("gray_collar", props -> new CollarItem(DyeColor.GRAY, props));

    public static final DeferredItem<CollarItem> LIGHT_GRAY_COLLAR =
            ITEMS.registerItem("light_gray_collar", props -> new CollarItem(DyeColor.LIGHT_GRAY, props));

    public static final DeferredItem<CollarItem> CYAN_COLLAR =
            ITEMS.registerItem("cyan_collar", props -> new CollarItem(DyeColor.CYAN, props));

    public static final DeferredItem<CollarItem> PURPLE_COLLAR =
            ITEMS.registerItem("purple_collar", props -> new CollarItem(DyeColor.PURPLE, props));

    public static final DeferredItem<CollarItem> BLUE_COLLAR =
            ITEMS.registerItem("blue_collar", props -> new CollarItem(DyeColor.BLUE, props));

    public static final DeferredItem<CollarItem> BROWN_COLLAR =
            ITEMS.registerItem("brown_collar", props -> new CollarItem(DyeColor.BROWN, props));

    public static final DeferredItem<CollarItem> GREEN_COLLAR =
            ITEMS.registerItem("green_collar", props -> new CollarItem(DyeColor.GREEN, props));

    public static final DeferredItem<CollarItem> BLACK_COLLAR =
            ITEMS.registerItem("black_collar", props -> new CollarItem(DyeColor.BLACK, props));

    public static final Map<DyeColor, DeferredItem<CollarItem>> COLLARS = Map.ofEntries(
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

    public static DeferredItem<CollarItem> getCollar(DyeColor color) {
        return COLLARS.getOrDefault(color, COLLAR);
    }
}
