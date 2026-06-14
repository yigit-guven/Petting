package net.yigitguven.petting.init;

import net.yigitguven.petting.item.GoldenWheatItem;
import net.yigitguven.petting.item.PetTetherItem;
import net.yigitguven.petting.PettingMod;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.Item;

public class PettingModItems {
	public static final DeferredRegister<net.minecraft.world.item.Item> REGISTRY = DeferredRegister.create(net.minecraftforge.registries.ForgeRegistries.ITEMS, PettingMod.MODID);
	public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> GOLDEN_WHEAT = REGISTRY.register("golden_wheat", () -> new GoldenWheatItem());
	public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> PET_TETHER = REGISTRY.register("pet_tether", () -> new PetTetherItem());
	public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> TELEPORT_ORB = REGISTRY.register("teleport_orb", () -> new net.yigitguven.petting.item.TeleportOrbItem());
	public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> FOLLOW_WHISTLE = REGISTRY.register("follow_whistle", () -> new net.yigitguven.petting.item.FollowWhistleItem());
	public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.item.Item> PET_BED = REGISTRY.register("pet_bed", () -> new net.minecraft.world.item.BlockItem(net.yigitguven.petting.init.PettingModBlocks.PET_BED.get(), new Item.Properties()));
}



