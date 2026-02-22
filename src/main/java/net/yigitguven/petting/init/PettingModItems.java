package net.yigitguven.petting.init;

import net.yigitguven.petting.item.GoldenWheatItem;
import net.yigitguven.petting.item.PetTetherItem;
import net.yigitguven.petting.PettingMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

public class PettingModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(PettingMod.MODID);
	public static final DeferredItem<Item> GOLDEN_WHEAT = REGISTRY.register("golden_wheat", () -> new GoldenWheatItem());
	public static final DeferredItem<Item> PET_TETHER = REGISTRY.register("pet_tether", () -> new PetTetherItem());
	public static final DeferredItem<Item> TELEPORT_ORB = REGISTRY.register("teleport_orb", () -> new net.yigitguven.petting.item.TeleportOrbItem());
	public static final DeferredItem<Item> FOLLOW_WHISTLE = REGISTRY.register("follow_whistle", () -> new net.yigitguven.petting.item.FollowWhistleItem());
}



