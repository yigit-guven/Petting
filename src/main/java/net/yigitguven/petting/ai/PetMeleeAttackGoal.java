package net.yigitguven.petting.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetMeleeAttackGoal extends Goal {
    protected final PathfinderMob mob;
    protected final double speedModifier;
    protected final boolean followingTargetEvenIfNotSeen;

    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;

    public PetMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isHoldingRangedWeapon() {
        return this.mob.isHolding(stack -> stack.getItem() instanceof ProjectileWeaponItem);
    }

    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            return PetHelper.isValidPetTarget(this.mob, target);
        }
        if (target instanceof Player player && PetHelper.isOwner(this.mob, player)) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isRootOwner(target, PetHelper.getOwner(this.mob))) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if ((settings.combatMode() == CombatMode.GUARD || settings.combatMode() == CombatMode.WANDER) && this.mob.hasRestriction()) {
            double leashDist = this.mob.getRestrictRadius() + 10.0D;
            if (target.distanceToSqr(Vec3.atBottomCenterOf(this.mob.getRestrictCenter())) > leashDist * leashDist) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle() || this.mob.isPassenger()) {
            return false;
        }
        if (!PetHelper.isTamed(this.mob)) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (!(this.mob instanceof Enemy) && !PettingServerConfig.isPassiveMobAttacksEnabled()) {
            return false;
        }
        if (settings.preferRangedAttack() && isHoldingRangedWeapon()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !isValidTarget(target)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isVehicle() || this.mob.isPassenger()) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (!(this.mob instanceof Enemy) && !PettingServerConfig.isPassiveMobAttacksEnabled()) {
            return false;
        }
        if (settings.preferRangedAttack() && isHoldingRangedWeapon()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (target == null || !isValidTarget(target)) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        LivingEntity target = this.mob.getTarget();
        if (target != null && (!target.isAlive() || !isValidTarget(target))) {
            this.mob.setTarget(null);
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double distSqr = this.mob.distanceToSqr(target);

        this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
        if (this.ticksUntilNextPathRecalculation <= 0) {
            boolean targetMoved = target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D;
            if (targetMoved || this.mob.getNavigation().isDone() || this.mob.getRandom().nextFloat() < 0.1F) {
                this.pathedTargetX = target.getX();
                this.pathedTargetY = target.getY();
                this.pathedTargetZ = target.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(5);
                this.mob.getNavigation().moveTo(target, this.speedModifier);
            }
        }

        if (PetHelper.isFlyingPet(this.mob)) {
            this.mob.getMoveControl().setWantedPosition(target.getX(), target.getY() + target.getEyeHeight() * 0.5D, target.getZ(), this.speedModifier);
        }

        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        checkAndPerformAttack(target, distSqr);
    }

    protected void checkAndPerformAttack(LivingEntity target, double distToEnemySqr) {
        double attackReach = this.getAttackReachSqr(target);
        if (distToEnemySqr <= attackReach && this.ticksUntilNextAttack <= 0) {
            this.ticksUntilNextAttack = 20;
            this.mob.swing(InteractionHand.MAIN_HAND);
            PetHelper.doPetHurtTarget(this.mob, target);
        }
    }

    protected double getAttackReachSqr(LivingEntity target) {
        double w = this.mob.getBbWidth() * 2.0F;
        double reach = w * w + target.getBbWidth();
        return Math.max(4.0D, reach);
    }
}
