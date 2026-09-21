package net.yigitguven.petting.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class PettingClient {
    public static void registerClientExtensions(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, PettingConfigurationScreen::new);
    }
}
