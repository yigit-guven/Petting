package net.yigitguven.petting.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.yigitguven.petting.config.PettingConfig;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BossBarHandler {

    @SubscribeEvent
    public static void onBossBarRender(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (!PettingConfig.HIDE_TAMED_BOSSBARS.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Iterate through all loaded entities on the client to find a matching tamed pet
        // We match by name since BossEvent only has name and UUID, but the BossEvent UUID 
        // doesn't always match the Entity UUID for some bosses (like the Dragon).
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity.getPersistentData().getBoolean("pettingtamed")) {
                if (entity.getDisplayName().getString().equals(event.getBossEvent().getName().getString())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
