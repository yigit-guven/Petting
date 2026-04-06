package net.yigitguven.petting;

import net.minecraftforge.event.entity.living.LivingEvent; // NEW IMPORT
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerBossEvent;
import net.yigitguven.petting.config.PettingConfig;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.monster.warden.Warden;

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
            Entity attackerEntity = event.getEntity();
            if (!(attackerEntity instanceof Mob attacker)) return;
            
            // --- GLOBAL BLACKLIST CHECK ---
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(attackerEntity)) return;
            
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
            
            // --- GLOBAL BLACKLIST CHECK ---
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(spawned)) return;
            
            // --- IDLE WITHER PROJECTILE INTERCEPT ---
            if (spawned instanceof net.minecraft.world.entity.projectile.WitherSkull skull) {
                Entity shooter = skull.getOwner();
                if (shooter instanceof Mob shooterMob && isCustomPet(shooterMob)) {
                    // Only cancel projectiles if the Wither is NOT actively targeting something.
                    // This allows legit attacks while still neutering erratic side-head firing into nothingness.
                    if (shooterMob.getTarget() == null) {
                        event.setCanceled(true);
                    }
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

            // --- GLOBAL BLACKLIST CHECK ---
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;

            if (isCustomPet(pet)) {
                Player owner = getOwner(pet);
                if (owner == null) return;
                
                // ALWAYS enforce owner peace on Wardens globally
                if (pet instanceof Warden warden) {
                    warden.clearAnger(owner);
                }

                LivingEntity currentTarget = pet.getTarget();
                if (currentTarget != null) {
                    if (currentTarget == pet || currentTarget == owner || isOwnerOf(currentTarget, owner)) {
                        pet.setTarget(null);
                        pet.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                        
                        if (pet instanceof Warden warden) {
                            warden.clearAnger(currentTarget);
                            warden.clearAnger(owner);
                        }
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
                boolean attackWithOwner = pet.getPersistentData().getBoolean("attackifownerattacks");
                boolean defendOwner = pet.getPersistentData().getBoolean("attackifownerattacked");

                if (attackWithOwner && owner.getLastHurtMob() != null) {
                    forcedTarget = owner.getLastHurtMob();
                } else if (defendOwner && owner.getLastHurtByMob() != null) {
                    forcedTarget = owner.getLastHurtByMob();
                } else if (pet.getLastHurtByMob() != null) {
                    // Self-defense is checked in onLivingDamage, but this acts as a backup/persistence
                    forcedTarget = pet.getLastHurtByMob();
                }

                if (forcedTarget != null && isValidCombatTarget(pet, owner, forcedTarget)) {
                    // Always ensure anger and activity is maintained every tick for the Warden
                    if (pet instanceof Warden warden) {
                        warden.increaseAngerAt(forcedTarget, 100, true);
                        warden.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
                    }
                    
                    if (pet.getTarget() != forcedTarget) {
                        pet.setTarget(forcedTarget);
                        pet.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, forcedTarget);
                        if (!(pet instanceof Warden)) {
                            pet.getBrain().setMemory(MemoryModuleType.ANGRY_AT, forcedTarget.getUUID());
                        }
                    }
                } else {
                    // TRUE PACIFISM OVERRIDE: 
                    // If no valid commanded target exists, forcefully zero out targets EVERY tick 
                    // This paralyzes standard hostile AI loops (like Withers scanning for players)
                    pet.setTarget(null);
                    
                    // Clear modern Brain targeting
                    pet.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    pet.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                    
                    if (pet instanceof Warden warden) {
                        pet.getBrain().eraseMemory(MemoryModuleType.ROAR_TARGET);
                        pet.getBrain().eraseMemory(MemoryModuleType.DISTURBANCE_LOCATION);
                        if (currentTarget != null) {
                            warden.clearAnger(currentTarget);
                        }
                        // removed owner clear
                    }
                }
                
                // ALWAYS enforce owner peace on Wardens globally
                if (pet instanceof Warden warden) {
                    warden.clearAnger(owner);
                }

                // --- BOSSBAR DELETION ---
                if (PettingConfig.HIDE_TAMED_BOSSBARS.get()) {
                    // 1. WITHER BOSS
                    if (pet instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                        // Blast side-heads back to 0 (no target) actively
                        wither.setAlternativeTarget(1, 0);
                        wither.setAlternativeTarget(2, 0);
                        
                        try {
                            Field bossEventField = ObfuscationReflectionHelper.findField(net.minecraft.world.entity.boss.wither.WitherBoss.class, "f_31427_"); // official name: bossEvent
                            bossEventField.setAccessible(true);
                            ServerBossEvent bossEvent = (ServerBossEvent) bossEventField.get(wither);
                            if (bossEvent != null) {
                                bossEvent.setVisible(false);
                                bossEvent.removeAllPlayers();
                            }
                        } catch (Exception ignored) {}
                    }
                    // 2. ENDER DRAGON
                    else if (pet instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
                        if (dragon.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            net.minecraft.world.level.dimension.end.EndDragonFight fight = serverLevel.getDragonFight();
                            if (fight != null) {
                                try {
                                    Field fightBossEventField = ObfuscationReflectionHelper.findField(net.minecraft.world.level.dimension.end.EndDragonFight.class, "f_64287_"); // official name: bossEvent
                                    fightBossEventField.setAccessible(true);
                                    ServerBossEvent bossEvent = (ServerBossEvent) fightBossEventField.get(fight);
                                    if (bossEvent != null) {
                                        bossEvent.setVisible(false);
                                        bossEvent.removeAllPlayers();
                                    }
                                } catch (Exception ignored) {}
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

            // --- GLOBAL BLACKLIST CHECK ---
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(victim)) return;

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
                    petMob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);
                    
                    if (petMob instanceof Warden warden) {
                        warden.increaseAngerAt(attacker, 100, true);
                        warden.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
                    } else {
                        petMob.getBrain().setMemory(MemoryModuleType.ANGRY_AT, attacker.getUUID());
                    }
                }
            }
        }

        private static boolean isCustomPet(Entity entity) {
            if (!(entity instanceof Mob)) return false;
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return false; // ADDED BLACKLIST CHECK
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
