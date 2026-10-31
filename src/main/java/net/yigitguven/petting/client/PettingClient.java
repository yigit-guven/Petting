package net.yigitguven.petting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.Identifier;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.client.gui.PetInventoryScreen;
import net.yigitguven.petting.client.render.PetCollarLayer;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.network.DismountPetPayload;

public class PettingClient implements ClientModInitializer {
    public static final KeyMapping.Category PETTING_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Petting.MODID, "petting"));

    public static final KeyMapping OPEN_PET_INVENTORY = new KeyMapping(
            "key.petting.open_pet_inventory",
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            PETTING_CATEGORY
    );

    public static final KeyMapping TOGGLE_PET_SIT = new KeyMapping(
            "key.petting.toggle_pet_sit",
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_RIGHT,
            PETTING_CATEGORY
    );

    public static final KeyMapping RIDE_PET = new KeyMapping(
            "key.petting.ride_pet",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            PETTING_CATEGORY
    );

    @Override
    public void onInitializeClient() {
        PettingClientConfig.load();

        KeyMappingHelper.registerKeyMapping(OPEN_PET_INVENTORY);
        KeyMappingHelper.registerKeyMapping(TOGGLE_PET_SIT);
        KeyMappingHelper.registerKeyMapping(RIDE_PET);

        MenuScreens.register(PettingModMenus.PET_MENU.get(), PetInventoryScreen::new);

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            registerCollarLayer(registrationHelper, entityRenderer);
        });

        ClientPlayNetworking.registerGlobalReceiver(DismountPetPayload.TYPE, DismountPetPayload::handle);

        ClientTickEvents.END_CLIENT_TICK.register(PettingClientEvents::onClientTick);

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> PettingClientEvents.onClientChatReceived(message));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerCollarLayer(LivingEntityRenderLayerRegistrationCallback.RegistrationHelper registrationHelper, net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?, ?> entityRenderer) {
        registrationHelper.register(new PetCollarLayer(entityRenderer));
    }
}
