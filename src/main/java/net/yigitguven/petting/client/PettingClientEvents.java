package net.yigitguven.petting.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;

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
