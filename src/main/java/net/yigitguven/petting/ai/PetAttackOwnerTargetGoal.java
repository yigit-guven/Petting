package net.yigitguven.petting.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetAttackOwnerTargetGoal extends TargetGoal {
    private final PathfinderMob mob;
    private LivingEntity target;
    private int timestamp;

    public PetAttackOwnerTargetGoal(PathfinderMob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getCombatMode(this.mob) != CombatMode.AGGRESSIVE) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner == null) {
            return false;
        }

        this.target = owner.getLastHurtMob();
        int lastHurtTime = owner.getLastHurtMobTimestamp();
        if (lastHurtTime == this.timestamp || this.target == null) {
            return false;
        }

        if (this.target == owner) {
            return false;
        }
        if (this.target instanceof Mob otherMob && PetHelper.isOwner(otherMob, owner)) {
            return false;
        }

        return this.canAttack(this.target, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
