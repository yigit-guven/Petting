package net.yigitguven.petting.init;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.item.GoldenPawItem;
import net.yigitguven.petting.item.GoldenTreatItem;

import java.util.Map;

public class PettingModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Petting.MODID);

    private static Item.Properties props(String name) {
        return new Item.Properties().setId(ITEMS.key(name));
    }

    public static final RegistryObject<Item> GOLDEN_PAW =
            ITEMS.register("golden_paw", () -> new GoldenPawItem(props("golden_paw")));

    public static final RegistryObject<Item> GOLDEN_WHEAT =
            ITEMS.register("golden_wheat", () -> new GoldenTreatItem(props("golden_wheat")));

    public static final RegistryObject<Item> GOLDEN_STAR =
            ITEMS.register("golden_star", () -> new GoldenTreatItem(props("golden_star")));

    public static final RegistryObject<Item> GOLDEN_FLESH =
            ITEMS.register("golden_flesh", () -> new GoldenTreatItem(props("golden_flesh")));

    public static final RegistryObject<Item> GOLDEN_BONE =
            ITEMS.register("golden_bone", () -> new GoldenTreatItem(props("golden_bone")));

    public static final RegistryObject<Item> GOLDEN_FISH =
            ITEMS.register("golden_fish", () -> new GoldenTreatItem(props("golden_fish")));

    public static final RegistryObject<Item> GOLDEN_KELP =
            ITEMS.register("golden_kelp", () -> new GoldenTreatItem(props("golden_kelp")));

    public static final RegistryObject<Item> GOLDEN_FUNGUS =
            ITEMS.register("golden_fungus", () -> new GoldenTreatItem(props("golden_fungus")));

    public static final RegistryObject<Item> GOLDEN_EYE =
            ITEMS.register("golden_eye", () -> new GoldenTreatItem(props("golden_eye")));

    public static final RegistryObject<Item> GOLDEN_SLIME =
            ITEMS.register("golden_slime", () -> new GoldenTreatItem(props("golden_slime")));

    public static final RegistryObject<net.yigitguven.petting.item.GoldenDragonEggItem> GOLDEN_DRAGON_EGG =
            ITEMS.register("golden_dragon_egg",
                    () -> new net.yigitguven.petting.item.GoldenDragonEggItem(PettingModBlocks.GOLDEN_DRAGON_EGG.get(),
                            props("golden_dragon_egg").rarity(net.minecraft.world.item.Rarity.EPIC)));

    // Default Collar (Red)
    public static final RegistryObject<CollarItem> COLLAR =
            ITEMS.register("collar", () -> new CollarItem(props("collar")));

    // Dye Color Variants
    public static final RegistryObject<CollarItem> WHITE_COLLAR =
            ITEMS.register("white_collar", () -> new CollarItem(DyeColor.WHITE, props("white_collar")));

    public static final RegistryObject<CollarItem> ORANGE_COLLAR =
            ITEMS.register("orange_collar", () -> new CollarItem(DyeColor.ORANGE, props("orange_collar")));

    public static final RegistryObject<CollarItem> MAGENTA_COLLAR =
            ITEMS.register("magenta_collar", () -> new CollarItem(DyeColor.MAGENTA, props("magenta_collar")));

    public static final RegistryObject<CollarItem> LIGHT_BLUE_COLLAR =
            ITEMS.register("light_blue_collar", () -> new CollarItem(DyeColor.LIGHT_BLUE, props("light_blue_collar")));

    public static final RegistryObject<CollarItem> YELLOW_COLLAR =
            ITEMS.register("yellow_collar", () -> new CollarItem(DyeColor.YELLOW, props("yellow_collar")));

    public static final RegistryObject<CollarItem> LIME_COLLAR =
            ITEMS.register("lime_collar", () -> new CollarItem(DyeColor.LIME, props("lime_collar")));

    public static final RegistryObject<CollarItem> PINK_COLLAR =
            ITEMS.register("pink_collar", () -> new CollarItem(DyeColor.PINK, props("pink_collar")));

    public static final RegistryObject<CollarItem> GRAY_COLLAR =
            ITEMS.register("gray_collar", () -> new CollarItem(DyeColor.GRAY, props("gray_collar")));

    public static final RegistryObject<CollarItem> LIGHT_GRAY_COLLAR =
            ITEMS.register("light_gray_collar", () -> new CollarItem(DyeColor.LIGHT_GRAY, props("light_gray_collar")));

    public static final RegistryObject<CollarItem> CYAN_COLLAR =
            ITEMS.register("cyan_collar", () -> new CollarItem(DyeColor.CYAN, props("cyan_collar")));

    public static final RegistryObject<CollarItem> PURPLE_COLLAR =
            ITEMS.register("purple_collar", () -> new CollarItem(DyeColor.PURPLE, props("purple_collar")));

    public static final RegistryObject<CollarItem> BLUE_COLLAR =
            ITEMS.register("blue_collar", () -> new CollarItem(DyeColor.BLUE, props("blue_collar")));

    public static final RegistryObject<CollarItem> BROWN_COLLAR =
            ITEMS.register("brown_collar", () -> new CollarItem(DyeColor.BROWN, props("brown_collar")));

    public static final RegistryObject<CollarItem> GREEN_COLLAR =
            ITEMS.register("green_collar", () -> new CollarItem(DyeColor.GREEN, props("green_collar")));

    public static final RegistryObject<CollarItem> BLACK_COLLAR =
            ITEMS.register("black_collar", () -> new CollarItem(DyeColor.BLACK, props("black_collar")));

    public static final Map<DyeColor, RegistryObject<CollarItem>> COLLARS = Map.ofEntries(
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

    public static RegistryObject<CollarItem> getCollar(DyeColor color) {
        return COLLARS.getOrDefault(color, COLLAR);
    }
}
