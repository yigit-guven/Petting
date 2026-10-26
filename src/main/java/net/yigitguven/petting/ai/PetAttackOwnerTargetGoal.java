package net.yigitguven.petting.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PetAttackOwnerTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity target;

    public PetAttackOwnerTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isValidTarget(LivingEntity target, Player owner) {
        if (target == null || !target.isAlive() || target == this.mob) {
            return false;
        }
        if (owner != null && target == owner) {
            return false;
        }
        if (PetHelper.isTamed(target)) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isRootOwner(target, owner)) {
            return false;
        }
        return this.canAttack(target, TargetingConditions.forCombat().ignoreLineOfSight());
    }

    private boolean isWithinCombatRange(LivingEntity target, Player owner) {
        if (target == null) return false;
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
        if (!PetHelper.isTamed(this.mob) || settings.combatMode() != CombatMode.AGGRESSIVE) {
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
        if (currentTarget != null && currentTarget.isAlive() && isValidTarget(currentTarget, owner) && isWithinCombatRange(currentTarget, owner)) {
            return false;
        }

        LivingEntity ownerTarget = owner.getLastHurtMob();
        if (ownerTarget != null && ownerTarget.isAlive() && isValidTarget(ownerTarget, owner) && isWithinCombatRange(ownerTarget, owner)) {
            this.target = ownerTarget;
            return true;
        }

        double searchRadius = Math.max(16.0D, PetHelper.getFollowDistance(this.mob));
        AABB searchArea = this.mob.getBoundingBox().inflate(searchRadius);
        List<LivingEntity> enemies = this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                searchArea,
                e -> e instanceof net.minecraft.world.entity.monster.Enemy && isValidTarget(e, owner) && isWithinCombatRange(e, owner)
        );

        if (!enemies.isEmpty()) {
            enemies.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
            this.target = enemies.get(0);
            return true;
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        this.targetMob = this.target;
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT || settings.combatMode() != CombatMode.AGGRESSIVE) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (!isValidTarget(currentTarget, owner)) {
            return false;
        }
        return isWithinCombatRange(currentTarget, owner);
    }

    @Override
    public void stop() {
        LivingEntity currentTarget = this.mob.getTarget();
        Player owner = PetHelper.getOwner(this.mob);
        if (currentTarget == null || !currentTarget.isAlive() || !isValidTarget(currentTarget, owner)
                || !isWithinCombatRange(currentTarget, owner)
                || PetHelper.getOrder(this.mob) == PetOrder.SIT
                || PetHelper.getCombatMode(this.mob) != CombatMode.AGGRESSIVE) {
            this.mob.setTarget(null);
            this.target = null;
            this.targetMob = null;
        }
    }
}
