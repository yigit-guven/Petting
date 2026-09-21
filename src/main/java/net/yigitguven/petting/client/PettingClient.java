package net.yigitguven.petting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

import net.minecraft.resources.Identifier;
import net.yigitguven.petting.Petting;

public class PettingClient {
    public static final KeyMapping.Category PETTING_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Petting.MODID, "petting"));

    public static final KeyMapping OPEN_PET_INVENTORY = new KeyMapping(
            "key.petting.open_pet_inventory",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            PETTING_CATEGORY
    );

    public static void registerClientExtensions(ModContainer modContainer, IEventBus modEventBus) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, PettingConfigurationScreen::new);
        modEventBus.addListener(RegisterKeyMappingsEvent.class, PettingClient::onRegisterKeyMappings);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(PETTING_CATEGORY);
        event.register(OPEN_PET_INVENTORY);
    }
}
