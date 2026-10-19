package net.yigitguven.petting.capability;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.network.PettingNetwork;
import net.yigitguven.petting.network.SyncPetDataPayload;
import net.yigitguven.petting.util.PetHelper;

@Mod.EventBusSubscriber(modid = Petting.MODID)
public class PetCapabilityEvents {

    @SubscribeEvent
    public static void attachEntityCapabilities(AttachCapabilitiesEvent.Entities event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(Identifier.fromNamespaceAndPath(Petting.MODID, "pet_data"), new PetDataProvider());
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity living) {
            PetData data = PetHelper.getPetData(living);
            if (data != null && data.isTamed()) {
                PettingNetwork.sendToPlayer(player, new SyncPetDataPayload(living.getId(), data));
            }
        }
    }
}
