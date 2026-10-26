package net.yigitguven.petting.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetDefendOwnerTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity attacker;

    public PetDefendOwnerTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isValidAttacker(LivingEntity attacker, Player owner) {
        if (attacker == null || !attacker.isAlive() || attacker == this.mob) {
            return false;
        }
        if (owner != null && attacker == owner) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isRootOwner(attacker, owner)) {
            return false;
        }
        return this.canAttack(attacker, TargetingConditions.forCombat().ignoreLineOfSight());
    }

    private boolean isWithinCombatRange(LivingEntity target) {
        if (target == null) return false;
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if ((settings.combatMode() == CombatMode.GUARD || settings.combatMode() == CombatMode.WANDER) && this.mob.hasRestriction()) {
            double leashDist = this.mob.getRestrictRadius() + 10.0D;
            return target.distanceToSqr(Vec3.atBottomCenterOf(this.mob.getRestrictCenter())) <= leashDist * leashDist;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null) {
            double maxDist = Math.max(32.0D, PetHelper.getFollowDistance(this.mob) * 2.0D);
            if (this.mob.distanceToSqr(owner) > maxDist * maxDist && target.distanceToSqr(owner) > maxDist * maxDist) {
                return false;
            }
        }
        return this.mob.distanceToSqr(target) <= 32.0D * 32.0D;
    }

    @Override
    public boolean canUse() {
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (!(this.mob instanceof net.minecraft.world.entity.monster.Enemy) && !PettingServerConfig.isPassiveMobAttacksEnabled()) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner == null) {
            return false;
        }

        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget != null && currentTarget.isAlive() && isValidAttacker(currentTarget, owner) && isWithinCombatRange(currentTarget)) {
            return false;
        }

        LivingEntity ownerAttacker = owner.getLastHurtByMob();
        if (ownerAttacker != null && ownerAttacker.isAlive() && isValidAttacker(ownerAttacker, owner) && isWithinCombatRange(ownerAttacker)) {
            this.attacker = ownerAttacker;
            return true;
        }

        if (settings.retaliateWhenHurt()) {
            LivingEntity mobAttacker = this.mob.getLastHurtByMob();
            if (mobAttacker != null && mobAttacker.isAlive() && isValidAttacker(mobAttacker, owner) && isWithinCombatRange(mobAttacker)) {
                this.attacker = mobAttacker;
                return true;
            }
        }

        if (settings.defendOtherPets()) {
            for (LivingEntity otherPet : this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(16.0D))) {
                if (otherPet != this.mob && PetHelper.isTamed(otherPet) && PetHelper.isOwner(otherPet, owner)) {
                    LivingEntity petAttacker = otherPet.getLastHurtByMob();
                    if (petAttacker != null && petAttacker.isAlive() && isValidAttacker(petAttacker, owner) && isWithinCombatRange(petAttacker)) {
                        this.attacker = petAttacker;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        this.targetMob = this.attacker;
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT || settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (!isValidAttacker(currentTarget, owner)) {
            return false;
        }
        return isWithinCombatRange(currentTarget);
    }

    @Override
    public void stop() {
        LivingEntity currentTarget = this.mob.getTarget();
        Player owner = PetHelper.getOwner(this.mob);
        if (currentTarget == null || !currentTarget.isAlive() || !isValidAttacker(currentTarget, owner)
                || !isWithinCombatRange(currentTarget)
                || PetHelper.getOrder(this.mob) == PetOrder.SIT
                || PetHelper.getCombatMode(this.mob) == CombatMode.PASSIVE) {
            this.mob.setTarget(null);
            this.attacker = null;
            this.targetMob = null;
        }
    }
}
