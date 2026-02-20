package net.yigitguven.petting;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.init.PettingModAttributes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerAttributeSyncHandler {
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
            AttributeInstance attr = player.getAttribute(PettingModAttributes.MAX_PETS.get());
            if (attr != null) {
                // We set the base value to the config value
                attr.setBaseValue((double) PettingConfig.MAX_PETS_PER_PLAYER.get());
            }
        }
    }
}
