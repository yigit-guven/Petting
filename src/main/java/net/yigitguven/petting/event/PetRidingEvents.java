package net.yigitguven.petting.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

@Mod.EventBusSubscriber(modid = Petting.MODID)
public class PetRidingEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof LivingEntity mob && PetHelper.isTamed(mob)) {
            if (mob.getFirstPassenger() instanceof Player player && PetHelper.isOwner(mob, player)) {
                PetRidingHandler.tickRiddenMob(mob, player);
                if (PetHelper.hasSaddle(mob)) {
                    PetRidingHandler.lockRotations(mob, player);
                    PetRidingHandler.suppressMobAi(mob, player);
                }
            } else if (PetRidingHandler.isMarkedRiddenNoAi(mob)) {
                PetRidingHandler.onDismount(mob);
            }
        }
    }

    @SubscribeEvent
    public static boolean onEntityMount(EntityMountEvent event) {
        if (event.getEntityMounting() instanceof Player player && event.getEntityBeingMounted() instanceof LivingEntity mob) {
            if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                if (event.isDismounting()) {
                    // 1. If forced dismount was triggered (e.g. via keybind, grounded sneak, or DismountPetPayload), allow immediately
                    if (PetRidingHandler.isForcedDismountAllowed(player)) {
                        PetRidingHandler.onDismount(mob);
                        return false;
                    }

                    // 2. On client side: if this dismount is NOT from the player actively holding sneak,
                    // it is from a server packet (e.g. ClientboundSetPassengersPacket) or entity removal.
                    // NEVER cancel a server-ordered dismount on the client!
                    if (event.getLevel().isClientSide() && !player.isShiftKeyDown()) {
                        PetRidingHandler.onDismount(mob);
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

                    // Normal dismount reached ground / completed
                    PetRidingHandler.onDismount(mob);
                } else if (event.isMounting()) {
                    if (!(mob instanceof net.minecraft.world.entity.animal.equine.AbstractHorse)) {
                        if (!PettingServerConfig.isPetInventoryEnabled()
                                || !PettingServerConfig.isPetRidingEnabled()
                                || !PetHelper.isPetRidingEnabledFor(mob)
                                || (!PetHelper.hasSaddle(mob) && !PettingServerConfig.isAllowRidingWithoutSaddle())) {
                            return true;
                        }
                    }
                    PetRidingHandler.onMount(mob, player);
                }
            }
        }
        return false;
    }
}
