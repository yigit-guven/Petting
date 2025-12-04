package net.ryukazan.petting;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
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
         * EVENT 1: INCOMING DAMAGE
         * Used for Damage Prevention (Friendly Fire).
         * Fires before armor/absorption. This is the correct place to cancel attacks.
         */
        @SubscribeEvent
        public static void onIncomingDamage(LivingIncomingDamageEvent event) {
            Entity victim = event.getEntity();
            Entity attacker = event.getSource().getEntity(); // getEntity() covers indirect sources (arrows/potions)

            // 1. Safety Checks
            if (victim == null || attacker == null || victim.level().isClientSide()) {
                return;
            }

            // 2. Prevent Pet from damaging Owner (Friendly Fire Protection)
            if (victim instanceof Player owner && isCustomPet(attacker)) {
                if (isOwnerOf(attacker, owner)) {
                    // Check if config allows damage (default false)
                    boolean allowDamage = attacker.getPersistentData().getBoolean("damageOwner").orElse(false);
                    if (!allowDamage) {
                        event.setCanceled(true); // BLOCK DAMAGE
                    }
                }
            }
        }

        /**
         * EVENT 2: POST-DAMAGE
         * Used for Aggro/Revenge Logic.
         * Fires after damage is applied.
         */
        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            LivingEntity victim = event.getEntity();
            Entity sourceEntity = event.getSource().getEntity();

            if (victim == null || sourceEntity == null || victim.level().isClientSide()) {
                return;
            }
            
            // We need the attacker to be a LivingEntity to be targeted
            if (!(sourceEntity instanceof LivingEntity attacker)) {
                return;
            }

            Level level = victim.level();

            // SCENARIO A: Pet was attacked (Self Defense)
            if (isCustomPet(victim) && victim instanceof Mob petMob) {
                boolean attackIfSelfAttacked = petMob.getPersistentData().getBoolean("attackifselfattacked").orElse(true);
                // Don't target owner even if owner attacked (handled by Incoming event anyway, but safe check)
                if (attackIfSelfAttacked && !isOwnerOf(petMob, attacker)) {
                    setTarget(petMob, attacker);
                }
            }

            // SCENARIO B: Owner was attacked (Defend Owner)
            if (victim instanceof Player owner) {
                // Find all pets nearby
                List<Mob> nearbyPets = getPetsAround(level, owner.getX(), owner.getY(), owner.getZ(), 20.0); // 20 block radius
                
                for (Mob pet : nearbyPets) {
                    // Check if this specific pet belongs to this owner
                    if (isOwnerOf(pet, owner)) {
                        boolean shouldAttack = pet.getPersistentData().getBoolean("attackifownerattacked").orElse(true);
                        boolean isSitting = pet.getPersistentData().getBoolean("sitstill").orElse(false);

                        // Only attack if enabled and NOT sitting (unless you want sitting dogs to just stare aggressively)
                        if (shouldAttack && !isSitting) {
                            setTarget(pet, attacker);
                        }
                    }
                }
            }

            // SCENARIO C: Owner attacked something (Assist Owner)
            if (sourceEntity instanceof Player owner) {
                // Find all pets nearby
                List<Mob> nearbyPets = getPetsAround(level, owner.getX(), owner.getY(), owner.getZ(), 20.0);
                
                for (Mob pet : nearbyPets) {
                    if (isOwnerOf(pet, owner)) {
                        boolean shouldAttack = pet.getPersistentData().getBoolean("attackifownerattacks").orElse(true);
                        boolean isSitting = pet.getPersistentData().getBoolean("sitstill").orElse(false);

                        // Don't attack if the victim is the pet itself (handled by friendly fire, but logical check)
                        if (shouldAttack && !isSitting && victim != pet) {
                            setTarget(pet, victim);
                        }
                    }
                }
            }
        }

        // --- Helper Methods ---

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            return entity.getPersistentData().getBoolean("pettingtamed").orElse(false);
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player)) return false;
            
            CompoundTag data = pet.getPersistentData();
            String ownerUUIDStr = data.getString("ownerUUID").orElse("");
            
            if (ownerUUIDStr.isEmpty()) return false;

            try {
                UUID storedId = UUID.fromString(ownerUUIDStr);
                return storedId.equals(potentialOwner.getUUID());
            } catch (Exception e) {
                return false;
            }
        }

        private static void setTarget(Mob pet, LivingEntity target) {
            // Logic to prevent targeting teammates could go here if needed
            pet.setTarget(target);
        }

        private static List<Mob> getPetsAround(Level level, double x, double y, double z, double radius) {
            AABB searchBox = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            
            // Get all Mobs in the box
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, searchBox);
            
            // Filter list to only include our custom pets
            mobs.removeIf(mob -> !isCustomPet(mob));
            
            return mobs;
        }
    }
}