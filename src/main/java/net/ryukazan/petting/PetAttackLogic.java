package net.ryukazan.petting;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent; 
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

@EventBusSubscriber
public class PetAttackLogic {
    public PetAttackLogic() { }

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) { new PetAttackLogic(); }

    @EventBusSubscriber
    private static class PetAttackLogicForgeBusEvents {

        @SubscribeEvent
        public static void onTargetChange(LivingChangeTargetEvent event) {
            if (!(event.getEntity() instanceof Mob pet) || !isCustomPet(pet)) return;

            LivingEntity newTarget = event.getNewAboutToBeSetTarget();
            if (newTarget == null) return;

            Player owner = getOwner(pet);

            if (newTarget == pet) {
                event.setCanceled(true); 
                return;
            }
            if (owner != null && newTarget == owner) {
                event.setCanceled(true); 
                return;
            }
            if (isOwnerOf(newTarget, owner)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onEntityTick(EntityTickEvent.Post event) {
            Entity entity = event.getEntity();
            if (entity.level().isClientSide() || !(entity instanceof Mob pet)) return;

            if (isCustomPet(pet)) {
                Player owner = getOwner(pet);
                if (owner == null) return;

                LivingEntity currentTarget = pet.getTarget();
                if (currentTarget != null) {
                    if (currentTarget == pet || currentTarget == owner || isOwnerOf(currentTarget, owner)) {
                        pet.setTarget(null);
                        return;
                    }
                    if (!currentTarget.isAlive()) {
                        pet.setTarget(null);
                        return;
                    }
                }

                LivingEntity forcedTarget = null;

                if (owner.getLastHurtMob() != null) {
                    forcedTarget = owner.getLastHurtMob();
                }
                else if (owner.getLastHurtByMob() != null) {
                    forcedTarget = owner.getLastHurtByMob();
                }
                else if (pet.getLastHurtByMob() != null) {
                    forcedTarget = pet.getLastHurtByMob();
                }

                if (forcedTarget != null && isValidCombatTarget(pet, owner, forcedTarget)) {
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

            if (victim == sourceEntity) {
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                return; 
            }

            if (!(sourceEntity instanceof LivingEntity attacker)) return;

            Player owner = getOwner(petMob);
            if (attacker == owner) { 
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                return; 
            }

            // FIX: Removed .orElse(true)
            boolean attackSelf = true;
            if (petMob.getPersistentData().contains("attackifselfattacked")) {
                attackSelf = petMob.getPersistentData().getBoolean("attackifselfattacked");
            }
            
            if (attackSelf) {
                if (isValidCombatTarget(petMob, owner, attacker)) {
                    petMob.setTarget(attacker);
                }
            }
        }

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            // FIX: Removed .orElse(false)
            return entity.getPersistentData().getBoolean("pettingtamed");
        }

        private static boolean isValidCombatTarget(Mob pet, Player owner, LivingEntity potentialTarget) {
            if (potentialTarget == null || potentialTarget == pet) return false;
            if (potentialTarget == owner) return false;
            if (isOwnerOf(potentialTarget, owner)) return false; 
            return true;
        }

        private static Player getOwner(Mob pet) {
            // FIX: Removed .orElse("")
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return null;
            try {
                return pet.level().getPlayerByUUID(UUID.fromString(ownerUUIDStr));
            } catch (Exception e) { return null; }
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player) || pet == null) return false;
            // FIX: Removed .orElse("")
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return false;
            try {
                return UUID.fromString(ownerUUIDStr).equals(potentialOwner.getUUID());
            } catch (Exception e) { return false; }
        }
    }
}