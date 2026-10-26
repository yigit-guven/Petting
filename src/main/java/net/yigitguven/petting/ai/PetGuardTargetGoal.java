package net.yigitguven.petting.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class PetGuardTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity target;

    public PetGuardTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isValidGuardTarget(LivingEntity candidate, Player owner, BlockPos homePos, double maxDistSqr) {
        if (candidate == null || !candidate.isAlive() || candidate == this.mob || candidate == owner) {
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
        return this.canAttack(candidate, TargetingConditions.forCombat().ignoreLineOfSight());
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() != CombatMode.GUARD) {
            return false;
        }
        if (!(this.mob instanceof Enemy) && !PettingServerConfig.isPassiveMobAttacksEnabled()) {
            return false;
        }
        if (!this.mob.hasHome()) {
            this.mob.setHomeTo(this.mob.blockPosition(), (int) PetHelper.getFollowDistance(this.mob));
        }

        BlockPos homePos = this.mob.getHomePosition();
        int radius = Math.max(2, this.mob.getHomeRadius());
        double radiusSqr = (double) radius * radius;

        Player owner = PetHelper.getOwner(this.mob);
        AABB patrolArea = new AABB(homePos).inflate(radius);

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
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() != CombatMode.GUARD) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (currentTarget == owner) {
            return false;
        }
        if (currentTarget.isAlliedTo(this.mob)) {
            return false;
        }
        if (PetHelper.isTamed(currentTarget)) {
            return false;
        }
        if (owner != null && !PettingServerConfig.canPetsAttackPets() && PetHelper.isOwner(currentTarget, owner)) {
            return false;
        }

        if (this.mob.hasHome()) {
            BlockPos homePos = this.mob.getHomePosition();
            double leashDist = this.mob.getHomeRadius() + 8.0D;
            if (currentTarget.distanceToSqr(Vec3.atBottomCenterOf(homePos)) > leashDist * leashDist) {
                return false;
            }
        }

        return super.canContinueToUse();
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.setTarget(null);
        super.stop();
    }
}
