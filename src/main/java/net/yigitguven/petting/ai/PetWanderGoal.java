package net.yigitguven.petting.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetWanderGoal extends Goal {
    private final Mob mob;
    private final double speedModifier;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private int cooldown = 0;
    private boolean wasLeashed = false;
    private boolean wasVehicle = false;

    public PetWanderGoal(Mob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob)) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        if (this.mob.isPassenger()) {
            return false;
        }
        if (this.mob.isVehicle()) {
            this.wasVehicle = true;
            return false;
        }
        if (this.wasVehicle) {
            this.wasVehicle = false;
            int radius = (int) PetHelper.getFollowDistance(this.mob);
            this.mob.restrictTo(this.mob.blockPosition(), radius);
        }

        CombatMode mode = PetHelper.getCombatMode(this.mob);
        if (mode != CombatMode.WANDER && mode != CombatMode.GUARD) {
            return false;
        }

        if (this.mob.isLeashed()) {
            this.wasLeashed = true;
            return false;
        }
        if (this.wasLeashed) {
            this.wasLeashed = false;
            int radius = (int) PetHelper.getFollowDistance(this.mob);
            this.mob.restrictTo(this.mob.blockPosition(), radius);
        }

        if (!this.mob.hasRestriction()) {
            int radius = (int) PetHelper.getFollowDistance(this.mob);
            this.mob.restrictTo(this.mob.blockPosition(), radius);
        }

        // If mob has an active target, let combat goals handle it
        if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) {
            return false;
        }

        if (--this.cooldown > 0) {
            return false;
        }

        // If outside restriction radius, return back towards restriction center
        if (!this.mob.isWithinRestriction()) {
            BlockPos home = this.mob.getRestrictCenter();
            if (this.mob.distanceToSqr(Vec3.atBottomCenterOf(home)) > 64.0D * 64.0D) {
                this.mob.moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, this.mob.getYRot(), this.mob.getXRot());
                this.mob.getNavigation().stop();
                this.cooldown = 20;
                return false;
            }
            this.wantedX = home.getX() + 0.5D;
            this.wantedY = home.getY();
            this.wantedZ = home.getZ() + 0.5D;
            return true;
        }

        // Inside restriction, randomly wander around restriction center
        Vec3 pos = findWanderPosition();
        if (pos == null) {
            this.cooldown = 40;
            return false;
        }
        this.wantedX = pos.x;
        this.wantedY = pos.y;
        this.wantedZ = pos.z;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        if (this.mob.isVehicle() || this.mob.isPassenger() || this.mob.isLeashed()) {
            return false;
        }
        CombatMode mode = PetHelper.getCombatMode(this.mob);
        if (mode != CombatMode.WANDER && mode != CombatMode.GUARD) {
            return false;
        }
        if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) {
            return false;
        }
        return !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        if (PetHelper.isFlyingPet(this.mob)) {
            this.mob.getMoveControl().setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.cooldown = 40 + this.mob.getRandom().nextInt(60);
    }

    private Vec3 findWanderPosition() {
        BlockPos home = this.mob.getRestrictCenter();
        int radius = Math.max(2, (int) this.mob.getRestrictRadius());

        if (this.mob instanceof PathfinderMob pathfinderMob && !PetHelper.isFlyingPet(this.mob)) {
            Vec3 pos = LandRandomPos.getPos(pathfinderMob, Math.min(radius, 10), 4);
            if (pos != null && this.mob.isWithinRestriction(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z))) {
                return pos;
            }
            Vec3 towardHome = DefaultRandomPos.getPosTowards(pathfinderMob, Math.min(radius, 10), 4, Vec3.atBottomCenterOf(home), (float) (Math.PI / 2));
            if (towardHome != null && this.mob.isWithinRestriction(new BlockPos((int) towardHome.x, (int) towardHome.y, (int) towardHome.z))) {
                return towardHome;
            }
        }

        // Flying mobs or fallback
        for (int i = 0; i < 10; i++) {
            double rx = (this.mob.getRandom().nextDouble() * 2 - 1) * radius;
            double rz = (this.mob.getRandom().nextDouble() * 2 - 1) * radius;
            double ry = (this.mob.getRandom().nextDouble() * 2 - 1) * Math.min(4, radius);
            Vec3 target = new Vec3(home.getX() + 0.5D + rx, home.getY() + ry, home.getZ() + 0.5D + rz);
            if (this.mob.isWithinRestriction(new BlockPos((int) target.x, (int) target.y, (int) target.z))) {
                return target;
            }
        }
        return null;
    }
}
