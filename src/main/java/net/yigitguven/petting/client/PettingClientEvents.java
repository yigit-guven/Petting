package net.yigitguven.petting.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yigitguven.petting.network.OpenPetInventoryPayload;
import net.yigitguven.petting.util.PetHelper;

import java.util.Set;

@EventBusSubscriber(modid = Petting.MODID, value = Dist.CLIENT)
public class PettingClientEvents {

    private static final Set<String> POSITIVE_KEYS = Set.of(
            "petting.action.tamed_success",
            "petting.action.pet_healed"
    );

    private static final Set<String> NEGATIVE_KEYS = Set.of(
            "petting.action.taming_failed",
            "petting.action.too_healthy",
            "petting.action.already_vanilla_tamed",
            "petting.action.already_tamed_other",
            "petting.action.pet_full_health",
            "petting.action.pet_limit_reached"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (PettingClient.OPEN_PET_INVENTORY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                        (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
                if (target instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                        ClientPacketDistributor.sendToServer(new OpenPetInventoryPayload(mob.getId()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientChatReceived(ClientChatReceivedEvent.System event) {
        if (event.getMessage().getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (POSITIVE_KEYS.contains(key) && !PettingClientConfig.isPositiveFeedbackEnabled()) {
                event.setCanceled(true);
            } else if (NEGATIVE_KEYS.contains(key) && !PettingClientConfig.isNegativeFeedbackEnabled()) {
                event.setCanceled(true);
            }
        }
    }
}
