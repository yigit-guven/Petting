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

public class PetDefendOwnerTargetGoal extends TargetGoal {
    private final PathfinderMob mob;
    private LivingEntity attacker;
    private int timestamp;

    public PetDefendOwnerTargetGoal(PathfinderMob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!PetHelper.isTamed(this.mob) || PetHelper.getCombatMode(this.mob) == CombatMode.PASSIVE) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner == null) {
            return false;
        }

        this.attacker = owner.getLastHurtByMob();
        int lastHurtTime = owner.getLastHurtByMobTimestamp();
        if (lastHurtTime == this.timestamp || this.attacker == null) {
            this.attacker = this.mob.getLastHurtByMob();
            lastHurtTime = this.mob.getLastHurtByMobTimestamp();
            if (lastHurtTime == this.timestamp || this.attacker == null) {
                return false;
            }
        }

        if (this.attacker == owner) {
            return false;
        }
        if (this.attacker instanceof Mob otherMob && PetHelper.isOwner(otherMob, owner)) {
            return false;
        }

        return this.canAttack(this.attacker, TargetingConditions.DEFAULT);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null && owner.getLastHurtByMob() == this.attacker) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        } else {
            this.timestamp = this.mob.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
