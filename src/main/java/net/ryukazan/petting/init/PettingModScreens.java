/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.ryukazan.petting.init;

import net.ryukazan.petting.client.gui.PetConfigurationGUIScreen;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

@EventBusSubscriber(Dist.CLIENT)
public class PettingModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(PettingModMenus.PET_CONFIGURATION_GUI.get(), PetConfigurationGUIScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}