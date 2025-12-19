package net.ryukazan.petting.procedures;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

@EventBusSubscriber
public class SummonPettingCommandProcedure {
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        // Loop through tags to find our custom trigger
        for (String tag : entity.getTags().toArray(new String[0])) {
            
            if (tag.startsWith("petowner:")) {
                String inputUUID = tag.substring("petowner:".length());
                String finalUUID = null;
                Player ownerPlayer = null; // We try to find the player object if possible

                // --- LOGIC 1: RESOLVE OWNER ---
                
                // Case A: "self", "@s", "me"
                if (inputUUID.equals("self") || inputUUID.equals("@s") || inputUUID.equals("me")) {
                    ownerPlayer = entity.level().getNearestPlayer(entity, 10.0);
                    if (ownerPlayer != null) {
                        finalUUID = ownerPlayer.getStringUUID();
                    }
                } 
                // Case B: Specific UUID
                else {
                    try {
                        UUID uuid = UUID.fromString(inputUUID);
                        finalUUID = inputUUID;
                        // Try to find the player if they are online/nearby
                        ownerPlayer = entity.level().getPlayerByUUID(uuid);
                    } catch (Exception e) {
                        System.err.println("Petting Mod: Invalid UUID format: " + inputUUID);
                    }
                }

                // --- LOGIC 2: APPLY "GOLDEN WHEAT" EFFECTS ---
                if (finalUUID != null) {
                    
                    // 1. Handle Vanilla Tamable Mobs (Wolf, Cat, etc.)
                    if (entity instanceof TamableAnimal tamable && ownerPlayer != null) {
                        if (!tamable.isTame()) {
                            tamable.tame(ownerPlayer);
                            tamable.setTarget(null);
                        }
                    }

                    // 2. Apply Custom Pet Data (Exact copy from Golden Wheat)
                    CompoundTag data = entity.getPersistentData();
                    
                    data.putString("ownerUUID", finalUUID);
                    data.putBoolean("pettingtamed", true);
                    data.putBoolean("attackifownerattacks", true);
                    data.putBoolean("attackifownerattacked", true);
                    data.putBoolean("attackifselfattacked", true);
                    data.putBoolean("damageOwner", false);
                    data.putBoolean("sitstill", false);
                    data.putInt("followdistance", 10);
                    data.putInt("teleportdistance", 20);

                    // 3. Set Custom Name ("Player's Entity")
                    if (ownerPlayer != null) {
                        String ownerName = ownerPlayer.getDisplayName().getString();
                        String entityName = entity.getType().getDescription().getString();
                        entity.setCustomName(Component.literal(ownerName + "'s " + entityName));
                        entity.setCustomNameVisible(false); // Matches your Wheat code
                    }

                    // 4. Prevent Despawn
                    if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                        mob.setTarget(null); // Ensure peaceful start
                    }

                    // 5. Visuals & Sound (Matches Wheat code)
                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART, 
                            entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                            7, 0.5, 0.5, 0.5, 0.1);
                    }
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    // 6. Clean up tag
                    entity.removeTag(tag);
                    
                    System.out.println("Petting Mod: Summoned pet registered for UUID: " + finalUUID);
                }
            }
        }
    }
}