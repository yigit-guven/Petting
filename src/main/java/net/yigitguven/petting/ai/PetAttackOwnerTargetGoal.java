package net.yigitguven.petting.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;

public class PetAttackOwnerTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity target;
    private int timestamp;

    public PetAttackOwnerTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
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

        this.target = owner.getLastHurtMob();
        int lastHurtTime = owner.getLastHurtMobTimestamp();
        if (lastHurtTime == this.timestamp || this.target == null || !this.target.isAlive()) {
            return false;
        }

        if (this.target == owner) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isRootOwner(this.target, owner)) {
            return false;
        }

        return this.canAttack(this.target, TargetingConditions.forCombat().ignoreLineOfSight());
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

    @Override
    public boolean canContinueToUse() {
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT || settings.combatMode() != CombatMode.AGGRESSIVE) {
            return false;
        }
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null) {
            if (currentTarget == owner) {
                return false;
            }
            if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isOwner(currentTarget, owner)) {
                return false;
            }
        }
        return super.canContinueToUse();
    }
}
