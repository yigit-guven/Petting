package net.yigitguven.petting.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.client.ConfigGuiScreen;
import net.yigitguven.petting.client.gui.PetInventoryScreen;
import net.yigitguven.petting.init.PettingModMenus;

@Mod.EventBusSubscriber(modid = PettingMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.gui.screens.MenuScreens.register(PettingModMenus.PET_INVENTORY.get(), PetInventoryScreen::new);
        });

        ModLoadingContext.get().registerExtensionPoint(net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new ConfigGuiScreen(screen)));
    }
}
