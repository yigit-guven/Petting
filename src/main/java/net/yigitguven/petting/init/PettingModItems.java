package net.yigitguven.petting.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.item.GoldenWheatItem;
import net.yigitguven.petting.item.PetTetherItem;

import net.yigitguven.petting.item.FollowWhistleItem;
import net.yigitguven.petting.item.TeleportOrbItem;
import net.minecraft.world.item.BlockItem;

public class PettingModItems {
    public static final Item GOLDEN_WHEAT = new GoldenWheatItem();
    public static final Item PET_TETHER = new PetTetherItem();
    public static final Item FOLLOW_WHISTLE = new FollowWhistleItem();
    public static final Item TELEPORT_ORB = new TeleportOrbItem();
    public static Item PET_BED;

    public static void register() {
        PET_BED = new BlockItem(PettingModBlocks.PET_BED, new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "golden_wheat"), GOLDEN_WHEAT);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "pet_tether"), PET_TETHER);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "follow_whistle"), FOLLOW_WHISTLE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "teleport_orb"), TELEPORT_ORB);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(PettingMod.MODID, "pet_bed"), PET_BED);
    }
}
