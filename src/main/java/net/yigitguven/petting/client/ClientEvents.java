package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.network.OpenPetInventoryPacket;

@Mod.EventBusSubscriber(modid = PettingMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() != null) {
            // If it's the standard inventory screen, and we are riding something, try to open pet inventory
            if (event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen || 
                event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen) {
                
                // If riding, assume we want pet inventory (riding is restricted to pets anyway)
                event.setCanceled(true);
                PettingMod.PACKET_HANDLER.sendToServer(new OpenPetInventoryPacket());
            }
        }
    }
}
