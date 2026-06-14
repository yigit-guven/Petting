package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.network.OpenPetSettingsPacket;

@Mod.EventBusSubscriber(modid = PettingMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        while (KeyBindings.OPEN_PET_SETTINGS.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.hitResult instanceof EntityHitResult ehr) {
                PettingMod.PACKET_HANDLER.sendToServer(new OpenPetSettingsPacket(ehr.getEntity().getId()));
            }
        }
    }
}
