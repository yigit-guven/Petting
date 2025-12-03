/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.ryukazan.petting.PettingMod;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class PettingModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PettingMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PETTING = REGISTRY.register("petting",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.petting.petting")).icon(() -> new ItemStack(PettingModItems.GOLDEN_WHEAT.get())).displayItems((parameters, tabData) -> {
				tabData.accept(PettingModItems.GOLDEN_WHEAT.get());
			}).withSearchBar().build());
}