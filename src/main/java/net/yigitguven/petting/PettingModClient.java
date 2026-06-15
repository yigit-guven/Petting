package net.yigitguven.petting;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.yigitguven.petting.client.KeyBindings;
import net.yigitguven.petting.client.PettingClientNetworking;

public class PettingModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PettingClientNetworking.registerReceivers();
        KeyBindings.register();
        net.minecraft.client.gui.screens.MenuScreens.register(net.yigitguven.petting.init.PettingModMenus.PET_INVENTORY, net.yigitguven.petting.client.gui.PetInventoryScreen::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && KeyBindings.OPEN_PET_SETTINGS.consumeClick()) {
                net.minecraft.world.entity.Entity target = client.crosshairPickEntity;
                int targetId = (target != null) ? target.getId() : -1;
                PettingClientNetworking.sendOpenPetSettings(targetId);
            }
        });
    }
}
