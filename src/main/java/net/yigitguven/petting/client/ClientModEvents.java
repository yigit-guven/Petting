package net.yigitguven.petting.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.client.gui.PetInventoryScreen;
import net.yigitguven.petting.init.PettingModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;

@Mod.EventBusSubscriber(modid = PettingMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(PettingModMenus.PET_INVENTORY.get(), PetInventoryScreen::new);
            ItemBlockRenderTypes.setRenderLayer(PettingModBlocks.PET_BED.get(), RenderType.cutout());
        });

        // Register the Config GUI here instead of the main class
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new net.yigitguven.petting.client.ConfigGuiScreen(screen)));
    }
}
