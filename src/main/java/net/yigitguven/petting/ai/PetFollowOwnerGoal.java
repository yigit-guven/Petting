package net.yigitguven.petting.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetFollowOwnerGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final float startDistanceSqr;
    private final float stopDistanceSqr;
    private final float teleportDistanceSqr;
    private final PathNavigation navigation;
    private Player owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public PetFollowOwnerGoal(PathfinderMob mob, double speedModifier, float startDistance, float stopDistance, float teleportDistance) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.startDistanceSqr = startDistance * startDistance;
        this.stopDistanceSqr = stopDistance * stopDistance;
        this.teleportDistanceSqr = teleportDistance * teleportDistance;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private net.yigitguven.petting.data.CollarSettings getSettings() {
        return PetHelper.getCollarSettings(this.mob);
    }

    private double getStartDistanceSqr() {
        double dist = PetHelper.getFollowDistance(this.mob);
        return dist * dist;
    }

    private double getStopDistanceSqr() {
        double dist = Math.max(1.5D, PetHelper.getFollowDistance(this.mob) * 0.5D);
        return dist * dist;
    }

    private double getTeleportDistanceSqr() {
        net.yigitguven.petting.data.CollarSettings settings = getSettings();
        if (!settings.teleportEnabled()) {
            return Double.MAX_VALUE;
        }
        double dist = PetHelper.getTeleportDistance(this.mob);
        return dist * dist;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (!PetHelper.isTamed(this.mob)) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) != PetOrder.FOLLOW) {
            return false;
        }
        CombatMode mode = getSettings().combatMode();
        if (mode == CombatMode.WANDER || mode == CombatMode.GUARD) {
            return false;
        }
        this.owner = PetHelper.getOwner(this.mob);
        if (this.owner == null || this.owner.isSpectator()) {
            return false;
        }
        double distSqr = this.mob.distanceToSqr(this.owner);
        net.minecraft.world.entity.LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && distSqr < getTeleportDistanceSqr()) {
            return false;
        }
        return distSqr >= getStartDistanceSqr();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) != PetOrder.FOLLOW) {
            return false;
        }
        CombatMode mode = getSettings().combatMode();
        if (mode == CombatMode.WANDER || mode == CombatMode.GUARD) {
            return false;
        }
        if (this.owner == null || this.owner.isSpectator()) {
            return false;
        }
        double distSqr = this.mob.distanceToSqr(this.owner);
        net.minecraft.world.entity.LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive() && distSqr < getTeleportDistanceSqr()) {
            return false;
        }
        return distSqr > getStopDistanceSqr();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.mob.setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        if (this.owner == null) {
            return;
        }

        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            double distSqr = this.mob.distanceToSqr(this.owner);

            if (distSqr >= getTeleportDistanceSqr()) {
                tryTeleportToOwner();
            } else {
                boolean hasPath = this.navigation.moveTo(this.owner, this.speedModifier);
                if (!hasPath && distSqr > 144.0D) {
                    tryTeleportToOwner();
                } else if (!hasPath) {
                    this.timeToRecalcPath = 5;
                }
            }
        }
    }

    private void tryTeleportToOwner() {
        BlockPos ownerPos = this.owner.blockPosition();

        for (int i = 0; i < 10; i++) {
            int dx = this.mob.getRandom().nextInt(7) - 3;
            int dy = this.mob.getRandom().nextInt(3) - 1;
            int dz = this.mob.getRandom().nextInt(7) - 3;

            BlockPos targetPos = ownerPos.offset(dx, dy, dz);
            if (isTeleportSafe(targetPos)) {
                this.mob.moveTo(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, this.mob.getYRot(), this.mob.getXRot());
                this.navigation.stop();
                return;
            }
        }
    }

    private boolean isTeleportSafe(BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = this.mob.level().getBlockState(below);

        if (!belowState.isFaceSturdy(this.mob.level(), below, Direction.UP)) {
            return false;
        }
        BlockState state = this.mob.level().getBlockState(pos);
        if (!state.isAir() && !state.canBeReplaced()) {
            return false;
        }
        AABB box = this.mob.getBoundingBox().move(Vec3.atBottomCenterOf(pos).subtract(this.mob.position()));
        return this.mob.level().noCollision(this.mob, box);
    }
}
