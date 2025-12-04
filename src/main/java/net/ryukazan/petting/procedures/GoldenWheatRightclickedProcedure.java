package net.ryukazan.petting.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
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

    /**
     * COMPATIBILITY FIX:
     * This method exists to prevent crashes if the Item trigger calls execute(player).
     * Since the Item trigger has no target entity (you clicked air), we just ignore it.
     */
    public static void execute(Entity sourceentity) {
        execute(null, sourceentity);
    }

    // Main Logic
    public static void execute(Entity entity, Entity sourceentity) {
        // 1. Safety Checks
        if (entity == null || sourceentity == null)
            return;

        // 2. Server-Side Check
        if (entity.level().isClientSide())
            return;

        // 3. CHECK THE ITEM
        if (!(sourceentity instanceof Player player)) 
            return;

        ItemStack itemInHand = player.getMainHandItem();
        ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(itemInHand.getItem());
        
        if (!itemID.toString().equals("petting:golden_wheat")) {
            return;
        }

        boolean actionSuccessful = false;

        // 4. Handle Vanilla Tamable Mobs
        if (entity instanceof TamableAnimal tamable) {
            if (!tamable.isTame()) {
                tamable.tame(player);
                actionSuccessful = true;
            }
        }
        // 5. Handle Custom Taming
        else if (!(entity instanceof Player)) { 
            CompoundTag data = entity.getPersistentData();
            
            // FIX: "getBoolean" returns Optional<Boolean> in this environment.
            // We use .orElse(false) to get the value safely.
            boolean isAlreadyCustomTamed = data.getBoolean("pettingtamed").orElse(false);

            if (!isAlreadyCustomTamed) {
                // A. Set Name
                String ownerName = player.getDisplayName().getString();
                String entityName = entity.getType().getDescription().getString();
                entity.setCustomName(Component.literal(ownerName + "'s " + entityName));
                
                // CHANGED: Set to false so the name only shows when looking at the entity
                entity.setCustomNameVisible(false);

                // B. Set Logic Tags
                data.putString("ownerUUID", player.getStringUUID());
                data.putBoolean("pettingtamed", true);

                // C. Set AI Behavior
                data.putBoolean("attackifownerattacks", true);
                data.putBoolean("attackifownerattacked", true);
                data.putBoolean("attackifselfattacked", true);
                data.putBoolean("attackifownersetasattacktarget", false);
                data.putBoolean("attackifselfsetasattacktarget", false);
                data.putBoolean("damageOwner", false);
                data.putInt("followdistance", 10);
                data.putInt("teleportdistance", 20);
                data.putBoolean("sitstill", false);

                actionSuccessful = true;
            }
        }

        // 6. Common Success Code (Feedback)
        if (actionSuccessful) {
            Level world = entity.level();

            // A. Spawn Heart Particles
            if (world instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.HEART, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }

            // B. Play Sound
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            // C. Swing Hand
            player.swing(InteractionHand.MAIN_HAND, true);

            // D. Consume Item (only if not in Creative)
            if (!player.getAbilities().instabuild) {
                itemInHand.shrink(1);
            }
        }
    }
}