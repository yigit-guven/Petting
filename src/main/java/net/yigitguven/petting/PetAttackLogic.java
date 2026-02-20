package net.yigitguven.petting;

import net.minecraftforge.event.entity.living.LivingEvent; // NEW IMPORT
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerBossEvent;
import net.yigitguven.petting.config.PettingConfig;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PetAttackLogic {
    public PetAttackLogic() {}

    @SubscribeEvent
    public static void init(FMLCommonSetupEvent event) { new PetAttackLogic(); }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class PetAttackLogicForgeBusEvents {

        @SubscribeEvent
        public static void onTargetChange(LivingChangeTargetEvent event) {
            if (!(event.getEntity() instanceof Mob attacker)) return;
            
            LivingEntity newTarget = event.getNewTarget();
            if (newTarget == null) return;
            
            Player attackerOwner = getOwner(attacker);
            Player targetOwner = null;
            if (newTarget instanceof Mob targetMob) {
                targetOwner = getOwner(targetMob);
            }

            // Prevent pets of the SAME owner from targeting each other
            if (attackerOwner != null && attackerOwner == targetOwner) {
                event.setCanceled(true);
                return;
            }
            
            if (newTarget == attackerOwner) {
                event.setCanceled(true);
                return;
            }

            // Only run remaining logic (forced targets/retaliation) for actual custom pets
            if (!isCustomPet(attacker)) return;
            
            if (newTarget == attacker) { event.setCanceled(true); return; }
            if (isOwnerOf(newTarget, attackerOwner)) { event.setCanceled(true); }
        }

        @SubscribeEvent
        public static void onEntityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
            if (event.getLevel().isClientSide()) return;
            Entity spawned = event.getEntity();
            
            // --- IDLE WITHER PROJECTILE INTERCEPT ---
            if (spawned instanceof net.minecraft.world.entity.projectile.WitherSkull skull) {
                Entity shooter = skull.getOwner();
                if (shooter instanceof Mob shooterMob && isCustomPet(shooterMob)) {
                    // We forcefully destroy ALL skulls fired by tamed Withers to completely neuter
                    // the erratic side-head firing behavior. They use melee solely.
                    skull.kill();
                    skull.remove(Entity.RemovalReason.KILLED);
                    return;
                }
            }

            if (spawned instanceof Mob mob) {
                if (isCustomPet(mob)) {
                    // Wipe vanilla hostile targeting goals naturally
                    mob.targetSelector.removeAllGoals(goal -> true);
                }
            }
        }

        @SubscribeEvent
        public static void onEntityTick(LivingEvent.LivingTickEvent event) {
            Entity entity = event.getEntity();
            
            if (entity.level().isClientSide() || !(entity instanceof Mob pet)) return;

            if (isCustomPet(pet)) {
                Player owner = getOwner(pet);
                if (owner == null) return;

                LivingEntity currentTarget = pet.getTarget();
                if (currentTarget != null) {
                    if (currentTarget == pet || currentTarget == owner || isOwnerOf(currentTarget, owner)) {
                        pet.setTarget(null);
                    }
                }

                // VANILLA PACIFISM FREEZE:
                // Stop native wolves, cats, or other vanilla AI from growling/hunting tamed custom pets
                // by sweeping nearby entities and forcefully wiping their target if it matches our owner.
                List<Mob> nearbyHostiles = pet.level().getEntitiesOfClass(Mob.class, pet.getBoundingBox().inflate(16.0D), 
                    mob -> mob.getTarget() == pet);
                
                for (Mob aggressor : nearbyHostiles) {
                    if (isOwnerOf(aggressor, owner)) {
                        aggressor.setTarget(null); // Forcefully blind the native vanilla AI
                    }
                }

                // Evaluate forced combat (owner instructed explicitly by being attacked, or attacking)
                LivingEntity forcedTarget = null;
                if (owner.getLastHurtMob() != null) {
                    forcedTarget = owner.getLastHurtMob();
                } else if (owner.getLastHurtByMob() != null) {
                    forcedTarget = owner.getLastHurtByMob();
                } else if (pet.getLastHurtByMob() != null) {
                    forcedTarget = pet.getLastHurtByMob();
                }

                if (forcedTarget != null && isValidCombatTarget(pet, owner, forcedTarget)) {
                    if (pet.getTarget() != forcedTarget) {
                        pet.setTarget(forcedTarget);
                    }
                } else {
                    // TRUE PACIFISM OVERRIDE: 
                    // If no valid commanded target exists, forcefully zero out targets EVERY tick 
                    // This paralyzes standard hostile AI loops (like Withers scanning for players)
                    pet.setTarget(null);

                    // --- WITHER BOSS PATCH ---
                    if (pet instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                        // Blast side-heads back to 0 (no target) actively
                        wither.setAlternativeTarget(1, 0);
                        wither.setAlternativeTarget(2, 0);
                        
                        // BOSSBAR DELETION (Reflection)
                        if (PettingConfig.HIDE_TAMED_BOSSBARS.get()) {
                            try {
                                Field bossEventField = net.minecraft.world.entity.boss.wither.WitherBoss.class.getDeclaredField("f_31427_"); // SRG name for 'bossEvent'
                                bossEventField.setAccessible(true);
                                ServerBossEvent bossEvent = (ServerBossEvent) bossEventField.get(wither);
                                if (bossEvent != null) {
                                    bossEvent.removeAllPlayers();
                                    bossEvent.setVisible(false);
                                }
                            } catch (Exception ignored) {
                                // Fallback reflection using standard Obfuscation naming just in case
                                try {
                                    Field bossEventField = net.minecraft.world.entity.boss.wither.WitherBoss.class.getDeclaredField("bossEvent");
                                    bossEventField.setAccessible(true);
                                    ServerBossEvent bossEvent = (ServerBossEvent) bossEventField.get(wither);
                                    if (bossEvent != null) {
                                        bossEvent.removeAllPlayers();
                                        bossEvent.setVisible(false);
                                    }
                                } catch (Exception ignored2) {}
                            }
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            LivingEntity victim = event.getEntity();

            if (victim == null || victim.level().isClientSide()) return;

            if (victim instanceof Mob petMob && isCustomPet(petMob)) {
                // QUALITY OF LIFE INVULNERABILITIES
                // Prevent tamed pets from taking silly environmental damage to preserve them better
                if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL) || 
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) ||
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.ON_FIRE) ||
                    event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LAVA)) {
                    event.setCanceled(true);
                    return;
                }
            }

            Entity sourceEntity = event.getSource().getEntity();
            if (sourceEntity == null) return;
            
            // Check mutual friendly fire first between ANY owned mobs
            if (victim instanceof Mob victimMob && sourceEntity instanceof Mob attackerMob) {
                Player victimOwner = getOwner(victimMob);
                Player attackerOwner = getOwner(attackerMob);
                if (victimOwner != null && victimOwner == attackerOwner) {
                    event.setCanceled(true); // Neutralize damage completely
                    victimMob.setTarget(null);
                    attackerMob.setTarget(null);
                    return;
                }
            }

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
                event.setCanceled(true); // Also cancel owner damaging their own pet
                return; 
            }

            boolean attackSelf = petMob.getPersistentData().getBoolean("attackifselfattacked");
            if (!petMob.getPersistentData().contains("attackifselfattacked")) attackSelf = true;

            if (attackSelf) {
                if (isValidCombatTarget(petMob, owner, attacker)) {
                    petMob.setTarget(attacker);
                }
            }
        }

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            return entity.getPersistentData().getBoolean("pettingtamed");
        }

        private static boolean isValidCombatTarget(Mob pet, Player owner, LivingEntity potentialTarget) {
            if (potentialTarget == null || potentialTarget == pet) return false;
            if (potentialTarget == owner) return false;
            if (isOwnerOf(potentialTarget, owner)) return false; 
            return true;
        }

        private static Player getOwner(Mob pet) {
            if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                LivingEntity owner = tamable.getOwner();
                if (owner instanceof Player p) {
                    return p;
                }
            }

            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return null;
            try {
                return pet.level().getPlayerByUUID(UUID.fromString(ownerUUIDStr));
            } catch (Exception e) { return null; }
        }

        private static boolean isOwnerOf(Entity pet, Entity potentialOwner) {
            if (!(potentialOwner instanceof Player player) || pet == null) return false;
            
            if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                if (tamable.isOwnedBy(player)) {
                    return true;
                }
            }

            String ownerUUIDStr = pet.getPersistentData().getString("ownerUUID");
            if (!ownerUUIDStr.isEmpty()) {
                try {
                    return UUID.fromString(ownerUUIDStr).equals(potentialOwner.getUUID());
                } catch (Exception e) { return false; }
            }
            return false;
        }
    }
}
