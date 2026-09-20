package net.yigitguven.petting.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.ai.PetAttackOwnerTargetGoal;
import net.yigitguven.petting.ai.PetDefendOwnerTargetGoal;
import net.yigitguven.petting.ai.PetFollowOwnerGoal;
import net.yigitguven.petting.ai.PetMeleeAttackGoal;
import net.yigitguven.petting.ai.PetSitGoal;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.init.PettingModAttachments;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PetHelper {

    public static boolean isTamed(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return false;
        }
        return entity.getData(PettingModAttachments.PET_DATA).isTamed();
    }

    public static Optional<UUID> getOwnerUUID(LivingEntity entity) {
        if (!isTamed(entity)) {
            return Optional.empty();
        }
        return Optional.ofNullable(entity.getData(PettingModAttachments.PET_DATA).getOwnerUUID());
    }

    @Nullable
    public static Player getOwner(LivingEntity entity) {
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        if (uuidOpt.isEmpty()) {
            return null;
        }
        return entity.level().getPlayerByUUID(uuidOpt.get());
    }

    public static boolean isOwner(LivingEntity entity, Player player) {
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        return uuidOpt.isPresent() && uuidOpt.get().equals(player.getUUID());
    }

    public static PetOrder getOrder(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return PetOrder.FOLLOW;
        }
        return entity.getData(PettingModAttachments.PET_DATA).getOrder();
    }

    public static void setOrder(LivingEntity entity, PetOrder order) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            entity.getData(PettingModAttachments.PET_DATA).setOrder(order);
        }
    }

    public static CombatMode getCombatMode(LivingEntity entity) {
        if (!entity.hasData(PettingModAttachments.PET_DATA)) {
            return CombatMode.DEFENSIVE;
        }
        return entity.getData(PettingModAttachments.PET_DATA).getCombatMode();
    }

    public static void setCombatMode(LivingEntity entity, CombatMode mode) {
        if (entity.hasData(PettingModAttachments.PET_DATA)) {
            entity.getData(PettingModAttachments.PET_DATA).setCombatMode(mode);
        }
    }

    public static void setTamed(Mob mob, Player owner) {
        PetData data = mob.getData(PettingModAttachments.PET_DATA);
        data.setTamed(true);
        data.setOwnerUUID(owner.getUUID());
        data.setOrder(PetOrder.FOLLOW);
        data.setCombatMode(CombatMode.DEFENSIVE);
        injectGoals(mob);
    }

    public static void setUntamed(Mob mob) {
        if (mob.hasData(PettingModAttachments.PET_DATA)) {
            PetData data = mob.getData(PettingModAttachments.PET_DATA);
            data.setTamed(false);
            data.setOwnerUUID(null);
            data.setOrder(PetOrder.FOLLOW);
            data.setCombatMode(CombatMode.DEFENSIVE);
        }
    }

    public static void injectGoals(Mob mob) {
        boolean hasGoals = mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetFollowOwnerGoal);
        if (hasGoals) {
            return;
        }

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        if (mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(2, new PetFollowOwnerGoal(pathfinderMob, 1.25D, 4.0F, 2.0F, 20.0F));
            mob.goalSelector.addGoal(3, new PetMeleeAttackGoal(pathfinderMob, 1.2D, true));
            mob.targetSelector.addGoal(1, new PetDefendOwnerTargetGoal(pathfinderMob));
            mob.targetSelector.addGoal(2, new PetAttackOwnerTargetGoal(pathfinderMob));
        }
    }
}
