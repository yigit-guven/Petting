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

public class PetDefendOwnerTargetGoal extends TargetGoal {
    private final Mob mob;
    private LivingEntity attacker;
    private int ownerLastHurtTimestamp;
    private int mobLastHurtTimestamp;

    public PetDefendOwnerTargetGoal(Mob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    private boolean isValidAttacker(LivingEntity attacker, Player owner) {
        if (attacker == null || !attacker.isAlive() || attacker == owner) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets() && PetHelper.isRootOwner(attacker, owner)) {
            return false;
        }
        return this.canAttack(attacker, TargetingConditions.forCombat().ignoreLineOfSight());
    }

    @Override
    public boolean canUse() {
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        Player owner = PetHelper.getOwner(this.mob);
        if (owner == null) {
            return false;
        }

        LivingEntity ownerAttacker = owner.getLastHurtByMob();
        int ownerTime = owner.getLastHurtByMobTimestamp();
        if (ownerAttacker != null && ownerTime != this.ownerLastHurtTimestamp && isValidAttacker(ownerAttacker, owner)) {
            this.attacker = ownerAttacker;
            return true;
        }

        if (settings.retaliateWhenHurt()) {
            LivingEntity mobAttacker = this.mob.getLastHurtByMob();
            int mobTime = this.mob.getLastHurtByMobTimestamp();
            if (mobAttacker != null && mobTime != this.mobLastHurtTimestamp && isValidAttacker(mobAttacker, owner)) {
                this.attacker = mobAttacker;
                return true;
            }
        }

        if (settings.defendOtherPets()) {
            for (LivingEntity otherPet : this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(16.0D))) {
                if (otherPet != this.mob && PetHelper.isTamed(otherPet) && PetHelper.isOwner(otherPet, owner)) {
                    LivingEntity petAttacker = otherPet.getLastHurtByMob();
                    if (petAttacker != null && isValidAttacker(petAttacker, owner)) {
                        this.attacker = petAttacker;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        Player owner = PetHelper.getOwner(this.mob);
        if (owner != null && owner.getLastHurtByMob() == this.attacker) {
            this.ownerLastHurtTimestamp = owner.getLastHurtByMobTimestamp();
        }
        if (this.mob.getLastHurtByMob() == this.attacker) {
            this.mobLastHurtTimestamp = this.mob.getLastHurtByMobTimestamp();
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        net.yigitguven.petting.data.CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (!PetHelper.isTamed(this.mob) || PetHelper.getOrder(this.mob) == PetOrder.SIT || settings.combatMode() == CombatMode.PASSIVE) {
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
