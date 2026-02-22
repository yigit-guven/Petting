/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.yigitguven.petting.init;

import net.yigitguven.petting.item.GoldenWheatItem;
import net.yigitguven.petting.item.PetTetherItem;
import net.yigitguven.petting.PettingMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class PettingModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, PettingMod.MODID);
	public static final RegistryObject<Item> GOLDEN_WHEAT;
	public static final RegistryObject<Item> PET_TETHER;
	public static final RegistryObject<Item> TELEPORT_ORB;
	public static final RegistryObject<Item> FOLLOW_WHISTLE;

	static {
		GOLDEN_WHEAT = REGISTRY.register("golden_wheat", GoldenWheatItem::new);
		PET_TETHER = REGISTRY.register("pet_tether", PetTetherItem::new);
		TELEPORT_ORB = REGISTRY.register("teleport_orb", net.yigitguven.petting.item.TeleportOrbItem::new);
		FOLLOW_WHISTLE = REGISTRY.register("follow_whistle", net.yigitguven.petting.item.FollowWhistleItem::new);
	}
}
