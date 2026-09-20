package net.yigitguven.petting.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.GoldenPawItem;
import net.yigitguven.petting.item.GoldenTreatItem;

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
}
