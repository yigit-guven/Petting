package net.yigitguven.petting.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetSitGoal extends Goal {
    private final Mob mob;

    public PetSitGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return PetHelper.isTamed(this.mob) && PetHelper.getOrder(this.mob) == PetOrder.SIT;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
    }
}
