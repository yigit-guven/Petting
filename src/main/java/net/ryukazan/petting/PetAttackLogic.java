package net.ryukazan.petting;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent; 
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
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
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;

import java.util.UUID;

@EventBusSubscriber
public class PetAttackLogic {
    public PetAttackLogic() { }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) { new PetAttackLogic(); }

    @EventBusSubscriber
    private static class PetAttackLogicForgeBusEvents {

        /**
         * EVENT 4: THE FIREWALL
         * Prevents selecting "Illegal" targets (Owner, Self, Friends).
         * Allows everything else (Monsters, etc.) to pass through.
         */
        @SubscribeEvent
        public static void onTargetChange(LivingChangeTargetEvent event) {
            if (!(event.getEntity() instanceof Mob pet) || !isCustomPet(pet)) return;

            // Use the safe getter for NeoForge
            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (newTarget == null) return;

            Player owner = getOwner(pet);

            // BLOCK: Self
            if (newTarget == pet) {
                event.setCanceled(true); 
                return;
            }
            // BLOCK: Owner
            if (owner != null && newTarget == owner) {
                event.setCanceled(true); 
                return;
            }
            // BLOCK: Friendly Fire
            if (isOwnerOf(newTarget, owner)) {
                event.setCanceled(true);
            }
        }

        /**
         * EVENT 0: TICK LOGIC (Target Setter)
         * Forces the pet to attack what the owner attacks.
         */
        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            Entity entity = event.getEntity();
            if (entity.level().isClientSide() || !(entity instanceof Mob pet)) return;

            if (isCustomPet(pet)) {
                Player owner = getOwner(pet);
                if (owner == null) return;

                // --- PRIORITY 1: CLEANUP EXISTING BAD TARGETS ---
                LivingEntity currentTarget = pet.getTarget();
                if (currentTarget != null) {
                    // If target is Self, Owner, or Friend -> STOP IMMEDIATELY
                    if (currentTarget == pet || currentTarget == owner || isOwnerOf(currentTarget, owner)) {
                        pet.setTarget(null);
                        return;
                    }
                    // If target is dead -> Stop
                    if (!currentTarget.isAlive()) {
                        pet.setTarget(null);
                        return;
                    }
                }

                // --- PRIORITY 2: ACQUIRE NEW TARGET (Wolf Logic) ---
                // Only switch targets if we are idle, or if the Owner specifically just hit something new.
                
                LivingEntity forcedTarget = null;

                // A. Owner is attacking someone
                if (owner.getLastHurtMob() != null) {
                    forcedTarget = owner.getLastHurtMob();
                }
                // B. Owner is being attacked
                else if (owner.getLastHurtByMob() != null) {
                    forcedTarget = owner.getLastHurtByMob();
                }
                // C. Pet is being attacked (Self Defense)
                else if (pet.getLastHurtByMob() != null) {
                    forcedTarget = pet.getLastHurtByMob();
                }

                // Apply the target if it is valid
                if (forcedTarget != null && isValidCombatTarget(pet, owner, forcedTarget)) {
                    // Only switch if we aren't already attacking it
                    if (pet.getTarget() != forcedTarget) {
                        pet.setTarget(forcedTarget);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent.Post event) {
            LivingEntity victim = event.getEntity();
            Entity sourceEntity = event.getSource().getEntity();

            if (victim == null || sourceEntity == null || victim.level().isClientSide()) return;
            if (!(victim instanceof Mob petMob) || !isCustomPet(petMob)) return;

            // Scenario: Self Harm -> Forget it
            if (victim == sourceEntity) {
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                return; 
            }

            if (!(sourceEntity instanceof LivingEntity attacker)) return;

            // Scenario: Owner Hurt Pet -> Forgive
            Player owner = getOwner(petMob);
            if (attacker == owner) { 
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                return; 
            }

            // Scenario: Stranger Hurt Pet -> Revenge
            boolean attackSelf = petMob.getPersistentData().getBoolean("attackifselfattacked").orElse(true);
            if (attackSelf) {
                if (isValidCombatTarget(petMob, owner, attacker)) {
                    petMob.setTarget(attacker);
                }
            }
        }

        // --- Helper Methods ---
        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            return entity.getPersistentData().getBoolean("pettingtamed").orElse(false);
        }

        private static boolean isValidCombatTarget(Mob pet, Player owner, LivingEntity potentialTarget) {
            if (potentialTarget == null || potentialTarget == pet) return false;
            if (potentialTarget == owner) return false;
            if (isOwnerOf(potentialTarget, owner)) return false; 
            return true;
        }

        private static Player getOwner(Mob pet) {
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID").orElse("");
            if (ownerUUIDStr.isEmpty()) return null;
            try {
                return pet.level().getPlayerByUUID(UUID.fromString(ownerUUIDStr));
            } catch (Exception e) { return null; }
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player) || pet == null) return false;
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID").orElse("");
            if (ownerUUIDStr.isEmpty()) return false;
            try {
                return UUID.fromString(ownerUUIDStr).equals(potentialOwner.getUUID());
            } catch (Exception e) { return false; }
        }
    }
}