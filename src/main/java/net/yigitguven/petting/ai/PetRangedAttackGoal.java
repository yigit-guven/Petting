package net.yigitguven.petting.ai;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class PetRangedAttackGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final int attackIntervalMin;
    private final float attackRadiusSqr;
    private final PathNavigation navigation;

    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public PetRangedAttackGoal(PathfinderMob mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackIntervalMin;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isHoldingRangedWeapon() {
        return this.mob.isHolding(stack -> stack.getItem() instanceof ProjectileWeaponItem);
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
        if (!settings.preferRangedAttack()) {
            return false;
        }
        if (!isHoldingRangedWeapon()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (!isValidTarget(target)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isVehicle()) {
            return false;
        }
        if (PetHelper.getOrder(this.mob) == PetOrder.SIT) {
            return false;
        }
        CollarSettings settings = PetHelper.getCollarSettings(this.mob);
        if (settings.combatMode() == CombatMode.PASSIVE || !settings.preferRangedAttack()) {
            return false;
        }
        if (!isHoldingRangedWeapon()) {
            return false;
        }
        LivingEntity target = this.mob.getTarget();
        if (!isValidTarget(target)) {
            return false;
        }
        return (this.canUse() || !this.navigation.isDone());
    }

    @Override
    public void start() {
        super.start();
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        this.mob.stopUsingItem();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }

        double distSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);

        if (canSee) {
            this.seeTime++;
        } else {
            this.seeTime = 0;
        }

        // Positioning & Strafing AI
        if (distSqr <= (double) this.attackRadiusSqr && this.seeTime >= 20) {
            this.navigation.stop();
            this.strafingTime++;
        } else {
            this.navigation.moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3F) {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (this.mob.getRandom().nextFloat() < 0.3F) {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distSqr > (double) (this.attackRadiusSqr * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distSqr < (double) (this.attackRadiusSqr * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.mob.getMoveControl().strafe(
                    this.strafingBackwards ? -0.5F : 0.5F,
                    this.strafingClockwise ? 0.5F : -0.5F
            );
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        ItemStack weapon = this.mob.getMainHandItem();
        InteractionHand hand = InteractionHand.MAIN_HAND;
        if (!(weapon.getItem() instanceof ProjectileWeaponItem)) {
            weapon = this.mob.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
        }

        if (weapon.getItem() instanceof CrossbowItem crossbow) {
            tickCrossbow(crossbow, weapon, hand, target, canSee);
        } else if (weapon.getItem() instanceof BowItem) {
            tickBow(weapon, hand, target, canSee);
        }
    }

    private void tickBow(ItemStack weapon, InteractionHand hand, LivingEntity target, boolean canSee) {
        if (this.mob.isUsingItem()) {
            if (!canSee && this.seeTime < -60) {
                this.mob.stopUsingItem();
            } else if (canSee) {
                int useTicks = this.mob.getTicksUsingItem();
                if (useTicks >= 20) {
                    this.mob.stopUsingItem();
                    performBowAttack(weapon, target, BowItem.getPowerForTime(useTicks));
                    this.attackTime = this.attackIntervalMin;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            this.mob.startUsingItem(hand);
        }
    }

    private void performBowAttack(ItemStack weapon, LivingEntity target, float power) {
        if (this.mob instanceof RangedAttackMob rangedMob) {
            rangedMob.performRangedAttack(target, power);
        } else {
            ItemStack ammo = this.mob.getProjectile(weapon);
            if (ammo.isEmpty()) {
                ammo = new ItemStack(Items.ARROW);
            }
            AbstractArrow arrow = ProjectileUtil.getMobArrow(this.mob, ammo, power, weapon);
            double dx = target.getX() - this.mob.getX();
            double dy = target.getY(0.3333333333333333D) - arrow.getY();
            double dz = target.getZ() - this.mob.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + dist * 0.20000000298023224D, dz, 1.6F, 1.0F);
            this.mob.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
            this.mob.level().addFreshEntity(arrow);
            weapon.hurtAndBreak(1, this.mob, EquipmentSlot.MAINHAND);
        }
    }

    private void tickCrossbow(CrossbowItem crossbow, ItemStack weapon, InteractionHand hand, LivingEntity target, boolean canSee) {
        if (CrossbowItem.isCharged(weapon)) {
            if (canSee && --this.attackTime <= 0) {
                crossbow.performShooting(this.mob.level(), this.mob, hand, weapon, 1.6F, 1.0F, target);
                this.attackTime = this.attackIntervalMin;
            }
        } else if (this.mob.isUsingItem()) {
            int duration = CrossbowItem.getChargeDuration(weapon, this.mob);
            if (this.mob.getTicksUsingItem() >= duration) {
                this.mob.releaseUsingItem();
            }
        } else if (--this.attackTime <= 0 && canSee) {
            this.mob.startUsingItem(hand);
        }
    }
}
