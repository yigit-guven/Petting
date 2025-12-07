package net.ryukazan.petting.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;
import java.util.Comparator;

import net.ryukazan.petting.init.PettingModItems;

public class GoldenWheatItemInHandTickProcedure {

    public static void execute(LevelAccessor world, Entity entity) {
        if (entity == null) return;

        // Ensure we are processing a player
        if (entity instanceof Player player) {

            // 1. CHECK HANDS FOR CUSTOM GOLDEN WHEAT
            boolean isHoldingWheat = player.getMainHandItem().getItem() == PettingModItems.GOLDEN_WHEAT.get() 
                                  || player.getOffhandItem().getItem() == PettingModItems.GOLDEN_WHEAT.get();

            // OPTIMIZATION: Run logic only once every 5 ticks (0.25s) to prevent lag
            // In 1.19.4, use world instanceof Level to check game time
            if (world instanceof Level _lvl && _lvl.getGameTime() % 5 != 0) {
                return;
            }

            double searchRadius = 12.0;

            // Get nearby animals
            List<Animal> nearbyAnimals = world.getEntitiesOfClass(
                Animal.class, 
                player.getBoundingBox().inflate(searchRadius, 4.0, searchRadius), 
                mob -> {
                    boolean isAlreadyCustomTamed = mob.getPersistentData().getBoolean("pettingtamed");
                    
                    if (isAlreadyCustomTamed) return false;
                    if (mob instanceof TamableAnimal tamable && tamable.isTame()) return false;
                    if (mob instanceof AbstractHorse horse && horse.isTamed()) return false;
                    return true;
                }
            );

            // --- SCENARIO A: PLAYER IS HOLDING GOLDEN WHEAT (ATTRACT) ---
            if (isHoldingWheat) {
                
                // Sort animals so the closest ones react first
                nearbyAnimals.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));

                for (Animal mob : nearbyAnimals) {
                    // Only attract if they aren't busy attacking someone else
                    if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                        
                        // 1. Force Look at Player
                        mob.getLookControl().setLookAt(player, 10.0F, mob.getMaxHeadXRot());

                        // 2. Move to Player (Speed 1.25)
                        boolean success = mob.getNavigation().moveTo(player, 1.25);

                        // 3. TAG THE ANIMAL
                        mob.addTag("TemptedByGoldenWheat");

                        // 4. Particles (Visual feedback)
                        if (success && world instanceof ServerLevel serverLevel && Math.random() < 0.3) {
                             serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                                 mob.getX(), mob.getY() + mob.getBbHeight() + 0.5, mob.getZ(), 
                                 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }
            } 
            
            // --- SCENARIO B: PLAYER PUT THE ITEM AWAY (STOP THEM) ---
            else {
                for (Animal mob : nearbyAnimals) {
                    // Check if this animal has the tag we gave it earlier
                    if (mob.getTags().contains("TemptedByGoldenWheat")) {
                        
                        // 1. Stop Navigation Immediately
                        mob.getNavigation().stop();
                        
                        // 2. Remove the tag (so they can wander freely again)
                        mob.removeTag("TemptedByGoldenWheat");
                    }
                }
            }
        }
    }
}