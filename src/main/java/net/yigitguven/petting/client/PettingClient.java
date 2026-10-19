package net.yigitguven.petting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.client.gui.PetInventoryScreen;
import net.yigitguven.petting.client.render.PettingClientRender;
import net.yigitguven.petting.init.PettingModMenus;

public class PettingClient {
    public static final String KEY_CATEGORY_PETTING = "key.categories.petting";

    public static final KeyMapping OPEN_PET_INVENTORY = new KeyMapping(
            "key.petting.open_pet_inventory",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            KEY_CATEGORY_PETTING
    );

    public static final KeyMapping TOGGLE_PET_SIT = new KeyMapping(
            "key.petting.toggle_pet_sit",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            KEY_CATEGORY_PETTING
    );

    public static final KeyMapping RIDE_PET = new KeyMapping(
            "key.petting.ride_pet",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            KEY_CATEGORY_PETTING
    );

    public static void registerClientExtensions(IEventBus modEventBus) {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new PettingConfigurationScreen(parent))
        );
        modEventBus.addListener(PettingClient::onRegisterKeyMappings);
        modEventBus.addListener(PettingClient::onClientSetup);
        modEventBus.addListener(PettingClientRender::onAddLayers);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PET_INVENTORY);
        event.register(TOGGLE_PET_SIT);
        event.register(RIDE_PET);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(PettingModMenus.PET_MENU.get(), PetInventoryScreen::new));
    }
}
