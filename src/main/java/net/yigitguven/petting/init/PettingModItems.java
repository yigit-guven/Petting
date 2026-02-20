package net.yigitguven.petting.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.item.GoldenWheatItem;
import net.yigitguven.petting.item.PetTetherItem;

public class PettingModItems {
    public static final Item GOLDEN_WHEAT = new GoldenWheatItem();
    public static final Item PET_TETHER = new PetTetherItem();

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "golden_wheat"), GOLDEN_WHEAT);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "pet_tether"), PET_TETHER);
    }
}
