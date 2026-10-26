package net.yigitguven.petting.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PetGuardTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity target;
    private int nextSearchDelay = 0;

    public PetGuardTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isValidGuardTarget(LivingEntity candidate, Player owner, BlockPos homePos, double maxDistSqr) {
        if (candidate == null || !candidate.isAlive() || candidate == this.mob) {
            return false;
        }
        if (owner != null && candidate == owner) {
            return false;
        }
        if (!(candidate instanceof Enemy)) {
            return false;
        }
        if (PetHelper.isTamed(candidate)) {
            return false;
        }
        if (candidate.isAlliedTo(this.mob)) {
            return false;
        }
        if (owner != null && !PettingServerConfig.canPetsAttackPets() && PetHelper.isOwner(candidate, owner)) {
            return false;
        }
        if (candidate.distanceToSqr(Vec3.atBottomCenterOf(homePos)) > maxDistSqr) {
            return false;
        }
        if (!candidate.canBeSeenAsEnemy()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() != CombatMode.GUARD) {
            return false;
        }
        if (!(this.mob instanceof Enemy) && !PettingServerConfig.isPassiveMobAttacksEnabled()) {
            return false;
        }

        if (!this.mob.hasRestriction()) {
            this.mob.restrictTo(this.mob.blockPosition(), (int) PetHelper.getFollowDistance(this.mob));
        }

        BlockPos homePos = this.mob.getRestrictCenter();
        double leashDist = this.mob.getRestrictRadius() + 10.0D;

        LivingEntity current = this.mob.getTarget();
        if (current != null && current.isAlive()) {
            if (current.distanceToSqr(Vec3.atBottomCenterOf(homePos)) <= leashDist * leashDist) {
                return false;
            }
        }

        if (this.nextSearchDelay > 0) {
            this.nextSearchDelay--;
            return false;
        }
        this.nextSearchDelay = 4 + this.mob.getRandom().nextInt(5);

        int radius = Math.max(2, (int) this.mob.getRestrictRadius());
        double radiusSqr = (double) radius * radius;

        Player owner = PetHelper.getOwner(this.mob);
        AABB patrolArea = new AABB(homePos).inflate(radius, 4.0D, radius);

        List<LivingEntity> enemies = this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                patrolArea,
                e -> isValidGuardTarget(e, owner, homePos, radiusSqr)
        );

        if (enemies.isEmpty()) {
            return false;
        }

        enemies.sort(Comparator.comparingDouble(this.mob::distanceToSqr));
        this.target = enemies.get(0);
        return true;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        this.targetMob = this.target;
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() != CombatMode.GUARD) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null && currentTarget == owner) {
            return false;
        }
        if (currentTarget.isAlliedTo(this.mob) || PetHelper.isTamed(currentTarget)) {
            return false;
        }
        if (owner != null && !PettingServerConfig.canPetsAttackPets() && PetHelper.isOwner(currentTarget, owner)) {
            return false;
        }

        if (this.mob.hasRestriction()) {
            BlockPos homePos = this.mob.getRestrictCenter();
            double leashDist = this.mob.getRestrictRadius() + 10.0D;
            if (currentTarget.distanceToSqr(Vec3.atBottomCenterOf(homePos)) > leashDist * leashDist) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void stop() {
        LivingEntity currentTarget = this.mob.getTarget();
        BlockPos homePos = this.mob.hasRestriction() ? this.mob.getRestrictCenter() : this.mob.blockPosition();
        double leashDist = (this.mob.hasRestriction() ? this.mob.getRestrictRadius() : 16.0F) + 10.0D;

        if (currentTarget == null || !currentTarget.isAlive()
                || currentTarget.distanceToSqr(Vec3.atBottomCenterOf(homePos)) > leashDist * leashDist
                || PetHelper.getOrder(this.mob) == PetOrder.SIT
                || PetHelper.getCombatMode(this.mob) != CombatMode.GUARD) {
            this.mob.setTarget(null);
            this.target = null;
            this.targetMob = null;
            this.mob.getNavigation().stop();
            if (this.mob.hasRestriction() && !this.mob.isWithinRestriction()) {
                this.mob.getNavigation().moveTo(homePos.getX() + 0.5D, homePos.getY(), homePos.getZ() + 0.5D, 1.0D);
            }
        }
    }
}
