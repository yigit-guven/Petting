/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.yigitguven.petting.init;

import net.yigitguven.petting.PettingMod;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;

public class PettingModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), PettingMod.MODID);
	public static final RegistryObject<CreativeModeTab> PETTING = REGISTRY.register("petting",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.petting.petting")).icon(() -> new ItemStack(PettingModItems.GOLDEN_WHEAT.get())).displayItems((parameters, tabData) -> {
				if (PettingModItems.GOLDEN_WHEAT.isPresent()) tabData.accept(PettingModItems.GOLDEN_WHEAT.get());
				if (PettingModItems.PET_TETHER.isPresent()) tabData.accept(PettingModItems.PET_TETHER.get());
				if (PettingModItems.TELEPORT_ORB.isPresent()) tabData.accept(PettingModItems.TELEPORT_ORB.get());
				if (PettingModItems.FOLLOW_WHISTLE.isPresent()) tabData.accept(PettingModItems.FOLLOW_WHISTLE.get());
				if (PettingModItems.PET_BED.isPresent()) tabData.accept(PettingModItems.PET_BED.get());
			}).withSearchBar().build());
}
