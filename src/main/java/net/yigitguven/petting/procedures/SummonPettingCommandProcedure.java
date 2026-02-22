package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent; // CHANGED
import net.neoforged.fml.common.Mod;
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

        for (String tag : entity.getTags().toArray(new String[0])) {
            
            if (tag.startsWith("petowner:")) {
                String inputUUID = tag.substring("petowner:".length());
                String finalUUID = null;
                Player ownerPlayer = null; 

                if (inputUUID.equals("self") || inputUUID.equals("@s") || inputUUID.equals("me")) {
                    ownerPlayer = entity.level().getNearestPlayer(entity, 10.0);
                    if (ownerPlayer != null) {
                        finalUUID = ownerPlayer.getStringUUID();
                    }
                } 
                else {
                    try {
                        UUID uuid = UUID.fromString(inputUUID);
                        finalUUID = inputUUID;
                        ownerPlayer = entity.level().getPlayerByUUID(uuid);
                    } catch (Exception e) {
                        System.err.println("Petting Mod: Invalid UUID format: " + inputUUID);
                    }
                }

                if (finalUUID != null) {
                    if (entity instanceof TamableAnimal tamable && ownerPlayer != null) {
                        if (!tamable.isTame()) {
                            tamable.tame(ownerPlayer);
                            tamable.setTarget(null);
                        }
                    }

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

                    if (ownerPlayer != null) {
                        String ownerName = ownerPlayer.getDisplayName().getString();
                        String entityName = entity.getType().getDescription().getString();
                        entity.setCustomName(Component.literal(ownerName + "'s " + entityName));
                        entity.setCustomNameVisible(false); 
                    }

                    if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                        mob.setTarget(null); 
                    }

                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART, 
                            entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                            7, 0.5, 0.5, 0.5, 0.1);
                    }
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    entity.removeTag(tag);
                    
                    System.out.println("Petting Mod: Summoned pet registered for UUID: " + finalUUID);
                }
            }
        }
    }
}




