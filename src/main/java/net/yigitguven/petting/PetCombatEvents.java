package net.yigitguven.petting;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.nbt.CompoundTag;

public class PetCombatEvents {

    public static void register() {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof net.minecraft.world.entity.projectile.WitherSkull skull) {
                net.minecraft.world.entity.Entity shooter = skull.getOwner();
                if (shooter instanceof Mob shooterMob && PetAttackLogic.isCustomPet(shooterMob)) {
                    // Refined fix: Only cancel if NOT targeting
                    if (shooterMob.getTarget() == null) {
                        skull.discard(); // Fabric equivalent of event cancellation for loading
                    }
                }
            }
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((victim, source, amount) -> {
            if (victim.level().isClientSide()) return true;

            if (victim instanceof Mob petMob && PetAttackLogic.isCustomPet(petMob)) {
                // QUALITY OF LIFE INVULNERABILITIES
                if (source.is(DamageTypes.FALL) || 
                    source.is(DamageTypes.IN_FIRE) ||
                    source.is(DamageTypes.ON_FIRE) ||
                    source.is(DamageTypes.LAVA)) {
                    return false;
                }
            }

            net.minecraft.world.entity.Entity sourceEntity = source.getEntity();
            if (sourceEntity == null) return true;
            
            // Mutual friendly fire
            if (victim instanceof Mob victimMob && sourceEntity instanceof Mob attackerMob) {
                Player victimOwner = PetAttackLogic.getOwner(victimMob);
                Player attackerOwner = PetAttackLogic.getOwner(attackerMob);
                if (victimOwner != null && victimOwner == attackerOwner) {
                    victimMob.setTarget(null);
                    attackerMob.setTarget(null);
                    return false;
                }
            }

            if (sourceEntity instanceof Mob attackerPet && PetAttackLogic.isCustomPet(attackerPet)) {
                Player petOwner = PetAttackLogic.getOwner(attackerPet);
                if (victim == petOwner && net.yigitguven.petting.config.PettingConfig.preventPetToOwnerDamage) {
                    attackerPet.setTarget(null);
                    return false;
                }
            }

            if (!(victim instanceof Mob petMob) || !PetAttackLogic.isCustomPet(petMob)) return true;

            if (victim == sourceEntity) {
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                return false; 
            }

            if (!(sourceEntity instanceof LivingEntity attacker)) return true;

            Player owner = PetAttackLogic.getOwner(petMob);
            if (attacker == owner) { 
                petMob.setTarget(null);
                petMob.setLastHurtByMob(null);
                if (!net.yigitguven.petting.config.PettingConfig.allowOwnerToHurtPets) {
                    return false; 
                }
            }

            CompoundTag data = ((IEntityData) petMob).getPersistentData();
            boolean attackSelf = data.getBoolean("attackifselfattacked");
            if (!data.contains("attackifselfattacked")) attackSelf = true;

            if (attackSelf) {
                if (PetAttackLogic.isValidCombatTarget(petMob, owner, attacker)) {
                    petMob.setTarget(attacker);
                }
            }
            return true;
        });
    }
}
