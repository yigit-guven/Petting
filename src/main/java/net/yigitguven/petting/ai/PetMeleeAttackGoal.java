package net.yigitguven.petting.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.Optional;
import java.util.UUID;

public class PetMeleeAttackGoal extends MeleeAttackGoal {
    public PetMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    private boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            return PetHelper.isValidPetTarget(this.mob, target);
        }
        if (target instanceof Player player && PetHelper.isOwner(this.mob, player)) {
            return false;
        }
        if (!PettingServerConfig.canPetsAttackPets()) {
            Optional<UUID> owner = PetHelper.getOwnerUUID(this.mob);
            if (owner.isPresent() && owner.equals(PetHelper.getOwnerUUID(target))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (!PetHelper.isTamed(this.mob)) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (settings.preferRangedAttack() && this.mob.isHolding(stack -> stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem)) {
            return false;
        }
        if (!isValidTarget(this.mob.getTarget())) {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT || settings.combatMode() == CombatMode.PASSIVE) {
            return false;
        }
        if (settings.preferRangedAttack() && this.mob.isHolding(stack -> stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem)) {
            return false;
        }
        if (!isValidTarget(this.mob.getTarget())) {
            return false;
        }
        return super.canContinueToUse();
    }
}
