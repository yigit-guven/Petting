package net.yigitguven.petting.init;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    private static Item.Properties props() {
        return new Item.Properties();
    }

    public static final RegistryObject<Item> GOLDEN_PAW =
            ITEMS.register("golden_paw", () -> new GoldenPawItem(props()));

    public static final RegistryObject<Item> GOLDEN_WHEAT =
            ITEMS.register("golden_wheat", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_STAR =
            ITEMS.register("golden_star", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_FLESH =
            ITEMS.register("golden_flesh", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_BONE =
            ITEMS.register("golden_bone", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_FISH =
            ITEMS.register("golden_fish", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_KELP =
            ITEMS.register("golden_kelp", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_FUNGUS =
            ITEMS.register("golden_fungus", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_EYE =
            ITEMS.register("golden_eye", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<Item> GOLDEN_SLIME =
            ITEMS.register("golden_slime", () -> new GoldenTreatItem(props()));

    public static final RegistryObject<net.yigitguven.petting.item.GoldenDragonEggItem> GOLDEN_DRAGON_EGG =
            ITEMS.register("golden_dragon_egg",
                    () -> new net.yigitguven.petting.item.GoldenDragonEggItem(PettingModBlocks.GOLDEN_DRAGON_EGG.get(),
                            props().rarity(Rarity.EPIC)));

    // Default Collar (Red)
    public static final RegistryObject<CollarItem> COLLAR =
            ITEMS.register("collar", () -> new CollarItem(props()));

    // Dye Color Variants
    public static final RegistryObject<CollarItem> WHITE_COLLAR =
            ITEMS.register("white_collar", () -> new CollarItem(DyeColor.WHITE, props()));

    public static final RegistryObject<CollarItem> ORANGE_COLLAR =
            ITEMS.register("orange_collar", () -> new CollarItem(DyeColor.ORANGE, props()));

    public static final RegistryObject<CollarItem> MAGENTA_COLLAR =
            ITEMS.register("magenta_collar", () -> new CollarItem(DyeColor.MAGENTA, props()));

    public static final RegistryObject<CollarItem> LIGHT_BLUE_COLLAR =
            ITEMS.register("light_blue_collar", () -> new CollarItem(DyeColor.LIGHT_BLUE, props()));

    public static final RegistryObject<CollarItem> YELLOW_COLLAR =
            ITEMS.register("yellow_collar", () -> new CollarItem(DyeColor.YELLOW, props()));

    public static final RegistryObject<CollarItem> LIME_COLLAR =
            ITEMS.register("lime_collar", () -> new CollarItem(DyeColor.LIME, props()));

    public static final RegistryObject<CollarItem> PINK_COLLAR =
            ITEMS.register("pink_collar", () -> new CollarItem(DyeColor.PINK, props()));

    public static final RegistryObject<CollarItem> GRAY_COLLAR =
            ITEMS.register("gray_collar", () -> new CollarItem(DyeColor.GRAY, props()));

    public static final RegistryObject<CollarItem> LIGHT_GRAY_COLLAR =
            ITEMS.register("light_gray_collar", () -> new CollarItem(DyeColor.LIGHT_GRAY, props()));

    public static final RegistryObject<CollarItem> CYAN_COLLAR =
            ITEMS.register("cyan_collar", () -> new CollarItem(DyeColor.CYAN, props()));

    public static final RegistryObject<CollarItem> PURPLE_COLLAR =
            ITEMS.register("purple_collar", () -> new CollarItem(DyeColor.PURPLE, props()));

    public static final RegistryObject<CollarItem> BLUE_COLLAR =
            ITEMS.register("blue_collar", () -> new CollarItem(DyeColor.BLUE, props()));

    public static final RegistryObject<CollarItem> BROWN_COLLAR =
            ITEMS.register("brown_collar", () -> new CollarItem(DyeColor.BROWN, props()));

    public static final RegistryObject<CollarItem> GREEN_COLLAR =
            ITEMS.register("green_collar", () -> new CollarItem(DyeColor.GREEN, props()));

    public static final RegistryObject<CollarItem> BLACK_COLLAR =
            ITEMS.register("black_collar", () -> new CollarItem(DyeColor.BLACK, props()));

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
