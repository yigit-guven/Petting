package net.yigitguven.petting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.client.gui.PetInventoryScreen;
import net.yigitguven.petting.client.render.PettingClientRender;
import net.yigitguven.petting.init.PettingModMenus;

public class PettingClient {
    public static final KeyMapping.Category PETTING_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Petting.MODID, "petting"));

    public static final KeyMapping OPEN_PET_INVENTORY = new KeyMapping(
            "key.petting.open_pet_inventory",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            PETTING_CATEGORY,
            0
    );

    public static final KeyMapping TOGGLE_PET_SIT = new KeyMapping(
            "key.petting.toggle_pet_sit",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            PETTING_CATEGORY,
            0
    );

    public static final KeyMapping RIDE_PET = new KeyMapping(
            "key.petting.ride_pet",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            PETTING_CATEGORY,
            0
    );

    public static void registerClientExtensions(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new PettingConfigurationScreen(parent))
        );
        RegisterKeyMappingsEvent.BUS.addListener(PettingClient::onRegisterKeyMappings);
        FMLClientSetupEvent.getBus(modBusGroup).addListener(PettingClient::onClientSetup);
        EntityRenderersEvent.AddLayers.BUS.addListener(PettingClientRender::onAddLayers);
        net.minecraftforge.client.event.SystemMessageReceivedEvent.BUS.addListener((java.util.function.Predicate<net.minecraftforge.client.event.SystemMessageReceivedEvent>) PettingClientEvents::onSystemMessageReceived);
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
