package net.ryukazan.petting.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;

public class GoldenWheatRightclickedProcedure {

    public static void execute(Entity sourceentity) {
        execute(null, sourceentity);
    }

    public static void execute(Entity entity, Entity sourceentity) {
        // 1. Safety Checks
        if (entity == null || sourceentity == null) return;
        
        // FIX 1.19.4: Use .level field instead of .level() method
        if (entity.level.isClientSide) return;
        
        if (!(sourceentity instanceof Player player)) return;

        // 2. Check Item
        ItemStack itemInHand = player.getMainHandItem();
        ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(itemInHand.getItem());
        if (!itemID.toString().equals("petting:golden_wheat")) {
            return;
        }

        boolean actionSuccessful = false;
        boolean needsRespawn = false;

        // 3. Handle Vanilla Tamable Mobs
        if (entity instanceof TamableAnimal tamable) {
            if (!tamable.isTame()) {
                tamable.tame(player);
                tamable.setTarget(null);
                actionSuccessful = true;
            }
        }
        // 4. Handle Custom Taming
        else if (entity instanceof Mob oldMob) { 
            CompoundTag data = oldMob.getPersistentData();
            boolean isAlreadyCustomTamed = data.getBoolean("pettingtamed");

            if (!isAlreadyCustomTamed) {
                actionSuccessful = true;
                needsRespawn = true;
            }
        }

        // 5. Success Logic & Respawn
        if (actionSuccessful) {
            // FIX 1.19.4: Use .level field
            Level world = entity.level;

            // A. VISUALS
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.HEART, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(InteractionHand.MAIN_HAND, true);

            // B. CONSUME ITEM
            if (!player.getAbilities().instabuild) {
                itemInHand.shrink(1);
            }

            // C. THE RESPAWN TRICK (AI RESET)
            if (needsRespawn && world instanceof ServerLevel serverLevel) {
                // 1. Create fresh entity
                Entity newEntity = entity.getType().create(world);
                
                if (newEntity instanceof Mob newMob) {
                    // 2. Set Position & Rotation (Using Public Methods)
                    newMob.setPos(entity.getX(), entity.getY(), entity.getZ());
                    
                    newMob.setYRot(entity.getYRot());
                    newMob.setXRot(entity.getXRot());
                    
                    // 3. Set Visual Rotation (Head/Body)
                    newMob.yBodyRot = ((Mob)entity).yBodyRot;
                    newMob.yHeadRot = ((Mob)entity).yHeadRot;

                    // 4. Apply Taming Data to the NEW mob
                    CompoundTag newData = newMob.getPersistentData();
                    
                    String ownerName = player.getDisplayName().getString();
                    String entityName = newMob.getType().getDescription().getString();
                    newMob.setCustomName(Component.literal(ownerName + "'s " + entityName));
                    newMob.setCustomNameVisible(false);

                    newData.putString("ownerUUID", player.getStringUUID());
                    newData.putBoolean("pettingtamed", true);
                    newData.putBoolean("attackifownerattacks", true);
                    newData.putBoolean("attackifownerattacked", true);
                    newData.putBoolean("attackifselfattacked", true);
                    newData.putBoolean("damageOwner", false);
                    newData.putBoolean("sitstill", false);
                    newData.putBoolean("pettingtamed", true);
                    newData.putInt("followdistance", 10);
                    newData.putInt("teleportdistance", 20);

                    // 5. Ensure NO TARGET
                    newMob.setTarget(null);

                    // 6. Swap them
                    world.addFreshEntity(newMob); 
                    entity.discard(); 
                }
            }
        }
    }
}