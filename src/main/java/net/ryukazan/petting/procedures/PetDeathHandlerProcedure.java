package net.ryukazan.petting.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

@EventBusSubscriber
public class PetDeathHandlerProcedure {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event == null || event.getEntity() == null) return;

        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide()) return;

        CompoundTag data = entity.getPersistentData();
        
        // FIX: Removed .orElse(false)
        boolean isTamed = data.getBoolean("pettingtamed");

        if (!isTamed) {
            return;
        }

        // FIX: Removed .orElse("")
        String ownerUUIDString = data.getString("ownerUUID");

        if (ownerUUIDString.isEmpty()) return;

        try {
            UUID ownerUUID = UUID.fromString(ownerUUIDString);
            Component deathMessage = entity.getCombatTracker().getDeathMessage();

            if (entity.level() instanceof ServerLevel serverLevel) {
                ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);

                if (owner != null) {
                    owner.sendSystemMessage(deathMessage);
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Petting: Invalid Owner UUID found on entity " + entity.getDisplayName().getString());
        }
    }
}