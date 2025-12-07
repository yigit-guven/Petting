/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.ryukazan.petting.item.GoldenWheatItem;
import net.ryukazan.petting.PettingMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.Item;

public class PettingModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, PettingMod.MODID);
	public static final RegistryObject<Item> GOLDEN_WHEAT;
	static {
		GOLDEN_WHEAT = REGISTRY.register("golden_wheat", GoldenWheatItem::new);
	}
	// Start of user code block custom items
	// End of user code block custom items
}