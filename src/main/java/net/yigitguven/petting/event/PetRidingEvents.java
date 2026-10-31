package net.yigitguven.petting.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

public class PetRidingEvents {

    public static void onEntityTickPre(LivingEntity mob) {
        if (PetHelper.isTamed(mob)) {
            if (mob.getFirstPassenger() instanceof Player player && PetHelper.isOwner(mob, player)) {
                PetRidingHandler.tickRiddenMob(mob, player);
            } else if (PetRidingHandler.isMarkedRiddenNoAi(mob)) {
                PetRidingHandler.onDismount(mob);
            }
        }
    }

    public static void onEntityTickPost(LivingEntity mob) {
        if (PetHelper.isTamed(mob)) {
            if (mob.getFirstPassenger() instanceof Player player && PetHelper.isOwner(mob, player) && PetHelper.hasSaddle(mob)) {
                PetRidingHandler.lockRotations(mob, player);
                PetRidingHandler.suppressMobAi(mob, player);
            }
        }
    }

    public static boolean canMount(LivingEntity mob, Player player) {
        if (!(mob instanceof net.minecraft.world.entity.animal.equine.AbstractHorse)) {
            if (!PettingServerConfig.isPetInventoryEnabled()
                    || !PettingServerConfig.isPetRidingEnabled()
                    || !PetHelper.isPetRidingEnabledFor(mob)
                    || (!PetHelper.hasSaddle(mob) && !PettingServerConfig.isAllowRidingWithoutSaddle())) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldCancelDismount(LivingEntity mob, Player player) {
        // 1. If forced dismount was triggered (e.g. via keybind, grounded sneak, or DismountPetPayload), allow immediately
        if (PetRidingHandler.isForcedDismountAllowed(player)) {
            return false;
        }

        // 2. On client side: if this dismount is NOT from the player actively holding sneak,
        // it is from a server packet (e.g. ClientboundSetPassengersPacket) or entity removal.
        // NEVER cancel a server-ordered dismount on the client!
        if (mob.level().isClientSide() && !player.isShiftKeyDown()) {
            return false;
        }

        // 3. Flying pets: if actively holding sneak and CAN descend in air, sneak descends instead of dismounting
        if (PetHelper.isFlyingPet(mob)) {
            if (player.isShiftKeyDown() && !PetRidingHandler.isGroundedOrCannotDescend(mob)) {
                return true;
            }
        }

        // 4. Swimming pets: if actively holding sneak and CAN descend in water, sneak dives instead of dismounting
        if (PetHelper.isSwimmingPet(mob) && mob.isInWater()) {
            if (player.isShiftKeyDown() && !PetRidingHandler.isGroundedOrCannotDescend(mob)) {
                return true;
            }
        }

        return false;
    }
}
