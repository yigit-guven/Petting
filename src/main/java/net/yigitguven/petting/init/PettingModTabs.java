package net.yigitguven.petting.init;

import net.yigitguven.petting.PettingMod;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class PettingModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PettingMod.MODID);
	public static final RegistryObject<net.minecraft.world.item.CreativeModeTab> PETTING = REGISTRY.register("petting",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.petting.petting")).icon(() -> new ItemStack(PettingModItems.GOLDEN_WHEAT.get())).displayItems((parameters, tabData) -> {
				tabData.accept(PettingModItems.GOLDEN_WHEAT.get());
				tabData.accept(PettingModItems.PET_TETHER.get());
				tabData.accept(PettingModItems.TELEPORT_ORB.get());
				tabData.accept(PettingModItems.FOLLOW_WHISTLE.get());
				tabData.accept(PettingModItems.PET_BED.get());
			}).withSearchBar().build());
}



