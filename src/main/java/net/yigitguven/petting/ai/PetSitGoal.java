package net.yigitguven.petting.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetSitGoal extends Goal {
    private final Mob mob;
    private boolean originalNoGravity;
    private boolean isAlteredGravity;

    public PetSitGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return PettingServerConfig.isPetSittingEnabled()
                && PetHelper.isTamed(this.mob)
                && PetHelper.getOrder(this.mob) == PetOrder.SIT
                && !this.mob.isVehicle();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        if (this.mob instanceof TamableAnimal tamable) {
            tamable.setInSittingPose(true);
        }
        this.mob.setShiftKeyDown(false);
        if (PettingServerConfig.shouldForgiveAttacksOnSit()) {
            PetHelper.forgiveAttacksAndClearTarget(this.mob);
        }

        this.originalNoGravity = this.mob.isNoGravity();
        this.isAlteredGravity = false;
        applySittingPhysics();
    }

    @Override
    public void stop() {
        if (this.mob instanceof TamableAnimal tamable) {
            tamable.setInSittingPose(false);
        }
        this.mob.setShiftKeyDown(false);
        if (this.isAlteredGravity) {
            this.mob.setNoGravity(this.originalNoGravity);
            this.isAlteredGravity = false;
        }
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        applySittingPhysics();
    }

    private void applySittingPhysics() {
        this.mob.getNavigation().stop();
        if (this.mob.isShiftKeyDown()) {
            this.mob.setShiftKeyDown(false);
        }

        boolean hoverFlying = PettingServerConfig.isFlyingPetsHoverWhenSitting() && PetHelper.isFlyingPet(this.mob);
        boolean stillSwimming = PettingServerConfig.isSwimmingPetsStayStillWhenSitting() && PetHelper.isSwimmingPet(this.mob);

        if (hoverFlying || stillSwimming) {
            if (!this.mob.isNoGravity()) {
                this.mob.setNoGravity(true);
                this.isAlteredGravity = true;
            }
            this.mob.setDeltaMovement(Vec3.ZERO);
            this.mob.fallDistance = 0.0F;
            if (this.mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                wither.setAlternativeTarget(0, 0);
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
            }
        } else {
            if (this.isAlteredGravity) {
                this.mob.setNoGravity(this.originalNoGravity);
                this.isAlteredGravity = false;
            }
            Vec3 delta = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(0.0, delta.y, 0.0);
        }
    }
}
