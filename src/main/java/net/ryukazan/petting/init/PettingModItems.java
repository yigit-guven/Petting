/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.ryukazan.petting.item.GoldenWheatItem;
import net.ryukazan.petting.PettingMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

import java.util.function.Function;

public class PettingModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(PettingMod.MODID);
	public static final DeferredItem<Item> GOLDEN_WHEAT;
	static {
		GOLDEN_WHEAT = register("golden_wheat", GoldenWheatItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, new Item.Properties());
	}
}