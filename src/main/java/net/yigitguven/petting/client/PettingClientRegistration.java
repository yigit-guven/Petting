package net.yigitguven.petting.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;

public class PettingClientRegistration {
    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, screen) -> new ConfigGuiScreen(screen));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PettingClientRegistration::registerScreens);
    }

    private static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(net.yigitguven.petting.init.PettingModMenus.PET_INVENTORY.get(), net.yigitguven.petting.client.gui.PetInventoryScreen::new);
    }
}
