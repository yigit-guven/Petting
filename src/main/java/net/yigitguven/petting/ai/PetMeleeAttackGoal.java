package net.yigitguven.petting.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

public class PetMeleeAttackGoal extends MeleeAttackGoal {
    public PetMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob)) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        if (PetHelper.getCombatMode(this.mob) == CombatMode.PASSIVE) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT || PetHelper.getCombatMode(this.mob) == CombatMode.PASSIVE) {
            return false;
        }
        return super.canContinueToUse();
    }
}
