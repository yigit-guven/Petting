package net.ryukazan.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

// We specify Bus.FORGE because LivingDeathEvent is a gameplay event
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PetDeathHandlerProcedure {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event == null || event.getEntity() == null) return;

        LivingEntity entity = event.getEntity();

        // 1. Check if the entity is on the Server
        if (entity.level().isClientSide()) return;

        // 2. Check if this entity has our custom "pettingtamed" tag
        CompoundTag data = entity.getPersistentData();
        
        boolean isTamed = data.getBoolean("pettingtamed");

        if (!isTamed) {
            return;
        }

        // 3. Get the Owner's UUID stored in the entity
        String ownerUUIDString = data.getString("ownerUUID");

        if (ownerUUIDString.isEmpty()) return;

        try {
            UUID ownerUUID = UUID.fromString(ownerUUIDString);

            // 4. Generate the standard Death Message
            Component deathMessage = entity.getCombatTracker().getDeathMessage();

            // 5. Find the owner and send the message
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