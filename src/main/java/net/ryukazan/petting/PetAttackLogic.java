package net.ryukazan.petting;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingEvent; // <-- NEW/FIXED IMPORT

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class PetAttackLogicForgeBusEvents {
        
        @SubscribeEvent
        public static void serverLoad(ServerStartingEvent event) {
        }

        @SubscribeEvent
        // FIX: Use LivingTickEvent for all living entity tick logic
        public static void onEntityTick(LivingEvent.LivingTickEvent event) {
            // Note: This event fires once per tick per Living entity. 
            // In a simple conversion from NeoForge's EntityTickEvent.Post, this is generally sufficient.

            Entity entity = event.getEntity();

            if (entity.level().isClientSide() || !(entity instanceof Mob pet)) {
                return;
            }

            if (isCustomPet(pet)) {
                Player owner = getOwner(pet);
                if (owner == null) return;

                // --- LOGIC: IS COMBAT ACTIVE? ---
                boolean combatMode = false;
                LivingEntity forcedTarget = null;

                // 1. Check Owner's Target
                if (owner.getLastHurtMob() != null) {
                    combatMode = true;
                    forcedTarget = owner.getLastHurtMob();
                }
                // 2. Check Attacker of Owner
                else if (owner.getLastHurtByMob() != null) {
                    combatMode = true;
                    forcedTarget = owner.getLastHurtByMob();
                }
                // 3. Check Attacker of Pet
                else if (pet.getLastHurtByMob() != null) {
                    combatMode = true;
                    forcedTarget = pet.getLastHurtByMob();
                }

                // --- ATTRIBUTE MANIPULATION ---
                AttributeInstance followRange = pet.getAttribute(Attributes.FOLLOW_RANGE);
                if (followRange != null) {
                    if (combatMode) {
                        // COMBAT: Restore vision so it can fight
                        if (followRange.getBaseValue() < 64.0) {
                            followRange.setBaseValue(64.0);
                        }
                    } else {
                        // PEACE: Blind the pet so AI sees NOTHING
                        if (followRange.getBaseValue() > 0.0) {
                            followRange.setBaseValue(0.0);
                        }
                    }
                }

                // --- CLEANUP ---
                if (!combatMode) {
                    forceStopAttack(pet);
                    
                    // Wither Specific: Silence side heads
                    if (pet instanceof WitherBoss wither) {
                         wither.setAlternativeTarget(0, 0); 
                         wither.setAlternativeTarget(1, 0); 
                    }
                } 
                else if (forcedTarget != null && pet.getTarget() != forcedTarget) {
                    // Only switch if current target is invalid
                    if (pet.getTarget() == null || !isValidCombatTarget(pet, owner, pet.getTarget())) {
                         pet.setTarget(forcedTarget);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onProjectileSpawn(EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()) return;

            if (event.getEntity() instanceof WitherSkull skull) {
                Entity shooter = skull.getOwner();
                
                if (shooter instanceof Mob pet && isCustomPet(pet)) {
                    Player owner = getOwner(pet);
                    
                    if (owner != null) {
                        boolean validCombat = false;
                        
                        if (owner.getLastHurtMob() != null || owner.getLastHurtByMob() != null) {
                            validCombat = true;
                        }
                        if (pet.getTarget() != null && isValidCombatTarget(pet, owner, pet.getTarget())) {
                            validCombat = true;
                        }

                        // If not in a valid fight, delete the skull
                        if (!validCombat) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onIncomingDamage(LivingAttackEvent event) {
            Entity victim = event.getEntity();
            Entity source = event.getSource().getEntity(); 

            if (victim == null || source == null || victim.level().isClientSide()) return;

            // A. Prevent Pet from hurting Owner
            if (victim instanceof Player owner && isCustomPet(source)) {
                if (isOwnerOf(source, owner)) {
                    boolean allowDamage = source.getPersistentData().getBoolean("damageOwner");
                    if (!allowDamage) {
                        event.setCanceled(true); 
                    }
                }
            }
            
            // B. If Owner hits Pet: Cancel damage or wipe aggro
            if (isCustomPet(victim) && source instanceof Player owner) {
                if (isOwnerOf(victim, owner)) {
                   ((Mob) victim).setLastHurtByMob(null);
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamagePost(LivingDamageEvent event) {
            LivingEntity victim = event.getEntity();
            Entity sourceEntity = event.getSource().getEntity();

            if (victim == null || sourceEntity == null || victim.level().isClientSide()) return;
            if (!(sourceEntity instanceof LivingEntity attacker)) return;

            // SCENARIO: Self Defense
            if (isCustomPet(victim) && victim instanceof Mob petMob) {
                Player owner = getOwner(petMob);
                
                if (attacker instanceof Player atkPlayer && isOwnerOf(petMob, atkPlayer)) {
                    petMob.setTarget(null);
                    return; 
                }

                boolean attackSelf = true; // Default behavior
                if (petMob.getPersistentData().contains("attackifselfattacked")) {
                    attackSelf = petMob.getPersistentData().getBoolean("attackifselfattacked");
                }
                
                if (attackSelf) {
                    petMob.setTarget(attacker);
                }
            }
        }

        // --- Helper Methods ---

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            return entity.getPersistentData().getBoolean("pettingtamed");
        }

        private static boolean isValidCombatTarget(Mob pet, Player owner, LivingEntity potentialTarget) {
            if (potentialTarget == owner) return false;
            if (potentialTarget == pet) return false;
            if (isOwnerOf(potentialTarget, owner)) return false; 

            if (owner.getLastHurtMob() != null && owner.getLastHurtMob().is(potentialTarget)) return true;
            if (owner.getLastHurtByMob() != null && owner.getLastHurtByMob().is(potentialTarget)) return true;
            if (pet.getLastHurtByMob() != null && pet.getLastHurtByMob().is(potentialTarget)) return true;

            return false;
        }

        private static void forceStopAttack(Mob pet) {
            pet.setTarget(null);
            pet.setLastHurtByMob(null);
        }

        private static Player getOwner(Mob pet) {
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return null;
            try {
                UUID storedId = UUID.fromString(ownerUUIDStr);
                return pet.level().getPlayerByUUID(storedId);
            } catch (Exception e) {
                return null;
            }
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player)) return false;
            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return false;

            try {
                UUID storedId = UUID.fromString(ownerUUIDStr);
                return storedId.equals(potentialOwner.getUUID());
            } catch (Exception e) {
                return false;
            }
        }

        private static List<Mob> getPetsAround(Level level, double x, double y, double z, double radius) {
            AABB searchBox = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
            List<Mob> mobs = level.getEntitiesOfClass(Mob.class, searchBox);
            mobs.removeIf(mob -> !isCustomPet(mob));
            return mobs;
        }
    }
}