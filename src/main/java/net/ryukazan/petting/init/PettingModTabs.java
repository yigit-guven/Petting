/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.CreativeModeTabEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PettingModTabs {
	@SubscribeEvent
	public static void buildTabContentsModded(CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation("petting", "petting"),
				builder -> builder.title(Component.translatable("item_group.petting.petting")).icon(() -> new ItemStack(PettingModItems.GOLDEN_WHEAT.get())).displayItems((parameters, tabData) -> {
					tabData.accept(PettingModItems.GOLDEN_WHEAT.get());
				}).withSearchBar());
	}
}