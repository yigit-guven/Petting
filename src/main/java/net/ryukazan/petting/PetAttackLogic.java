package net.ryukazan.petting;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber
public class PetAttackLogic {
    public PetAttackLogic() {
    }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) {
        new PetAttackLogic();
    }

    @SubscribeEvent
    public static void clientLoad(FMLClientSetupEvent event) {
    }

    @EventBusSubscriber
    private static class PetAttackLogicForgeBusEvents {
        
        @SubscribeEvent
        public static void serverLoad(ServerStartingEvent event) {
        }

        /**
         * EVENT 0: TARGET MONITOR
         * Keeps the pet from attacking the owner.
         */
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Pre event) {
            Entity entity = event.getEntity();

            // Check if server-side and is a mob
            if (entity.level().isClientSide() || !(entity instanceof Mob pet)) {
                return;
            }

            if (isCustomPet(pet)) {
                LivingEntity currentTarget = pet.getTarget();

                // Only interfere if the target IS the owner
                if (currentTarget instanceof Player owner && isOwnerOf(pet, owner)) {
                    // 1. Clear the target
                    pet.setTarget(null);
                    
                    // 2. Wipe memory of the attack to prevent immediate re-targeting
                    pet.setLastHurtByMob(null); 
                    // FIXED: Using 2 arguments for 1.21
                    pet.setLastHurtByPlayer((Player)null, 0); 
                    
                    // 3. Optional: Stop pathfinding so it doesn't walk to the owner aggressively
                    pet.getNavigation().stop();
                }
            }
        }

        /**
         * EVENT 1: INCOMING DAMAGE (Friendly Fire Prevention)
         */
        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            Entity victim = event.getEntity();
            Entity source = event.getSource().getEntity(); 

            if (victim == null || source == null || victim.level().isClientSide()) return;

            // A. Prevent Pet from hurting Owner
            if (victim instanceof Player owner && isCustomPet(source)) {
                if (isOwnerOf(source, owner)) {
                    // FIXED: Added .orElse(false) because return type is Optional
                    boolean allowDamage = source.getPersistentData().getBoolean("damageOwner").orElse(false);
                    if (!allowDamage) {
                        event.setCanceled(true); 
                    }
                }
            }
            
            // B. If Owner hits Pet: Cancel damage or just wipe aggro
            if (isCustomPet(victim) && source instanceof Player owner) {
                if (isOwnerOf(victim, owner)) {
                   ((Mob) victim).setLastHurtByMob(null);
                   // FIXED: Using 2 arguments for 1.21
                   ((Mob) victim).setLastHurtByPlayer((Player)null, 0);
                }
            }
        }

        /**
         * EVENT 2: POST-DAMAGE (Aggro Logic)
         * Triggers when damage is actually dealt.
         */
        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            LivingEntity victim = event.getEntity();
            Entity sourceEntity = event.getSource().getEntity();

            if (victim == null || sourceEntity == null || victim.level().isClientSide()) return;
            if (!(sourceEntity instanceof LivingEntity attacker)) return;

            Level level = victim.level();

            // SCENARIO A: Pet was attacked (Self Defense)
            if (isCustomPet(victim) && victim instanceof Mob petMob) {
                if (attacker instanceof Player owner && isOwnerOf(petMob, owner)) {
                    petMob.setTarget(null);
                    return; 
                }

                // FIXED: Added .orElse(true) so pets defend themselves by default
                boolean attackSelf = petMob.getPersistentData().getBoolean("attackifselfattacked").orElse(true);
                
                if (attackSelf) {
                    setTarget(petMob, attacker);
                }
            }

            // SCENARIO B: Owner was attacked (Defend Owner)
            if (victim instanceof Player owner) {
                List<Mob> nearbyPets = getPetsAround(level, owner.getX(), owner.getY(), owner.getZ(), 20.0);
                
                for (Mob pet : nearbyPets) {
                    if (isOwnerOf(pet, owner)) {
                        // FIXED: Added .orElse(true)
                        boolean shouldAttack = pet.getPersistentData().getBoolean("attackifownerattacked").orElse(true);
                        // FIXED: Added .orElse(false)
                        boolean isSitting = pet.getPersistentData().getBoolean("sitstill").orElse(false);

                        if (shouldAttack && !isSitting) {
                            setTarget(pet, attacker);
                        }
                    }
                }
            }

            // SCENARIO C: Owner attacked something (Assist Owner)
            if (sourceEntity instanceof Player owner) {
                List<Mob> nearbyPets = getPetsAround(level, owner.getX(), owner.getY(), owner.getZ(), 20.0);
                
                for (Mob pet : nearbyPets) {
                    if (isOwnerOf(pet, owner)) {
                        // FIXED: Added .orElse(true)
                        boolean shouldAttack = pet.getPersistentData().getBoolean("attackifownerattacks").orElse(true);
                        // FIXED: Added .orElse(false)
                        boolean isSitting = pet.getPersistentData().getBoolean("sitstill").orElse(false);

                        if (shouldAttack && !isSitting && victim != pet && !isOwnerOf(victim, owner)) {
                            setTarget(pet, victim);
                        }
                    }
                }
            }
        }

        // --- Helper Methods ---

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            // FIXED: Added .orElse(false)
            return entity.getPersistentData().getBoolean("pettingtamed").orElse(false);
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player)) return false;
            
            // FIXED: Added .orElse("")
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID").orElse("");
            
            if (ownerUUIDStr.isEmpty()) return false;

            try {
                UUID storedId = UUID.fromString(ownerUUIDStr);
                return storedId.equals(potentialOwner.getUUID());
            } catch (Exception e) {
                return false;
            }
        }

        private static void setTarget(Mob pet, LivingEntity target) {
            if (target instanceof Player owner && isOwnerOf(pet, owner)) return;
            if (target == pet) return;

            pet.setTarget(target);
            pet.setLastHurtByMob(null); 
        }

        private static List<Mob> getPetsAround(Level level, double x, double y, double z, double radius) {
            AABB searchBox = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, searchBox);
            mobs.removeIf(mob -> !isCustomPet(mob));
            return mobs;
        }
    }
}