package net.yigitguven.petting.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.util.PetHelper;

@EventBusSubscriber(modid = Petting.MODID)
public class PetRidingEvents {

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.getFirstPassenger() instanceof Player player && PetHelper.isOwner(mob, player)) {
                PetRidingHandler.tickRiddenMob(mob, player);
            } else if (PetRidingHandler.isMarkedRiddenNoAi(mob)) {
                PetRidingHandler.onDismount(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.getFirstPassenger() instanceof Player player && PetHelper.isOwner(mob, player) && PetHelper.hasSaddle(mob)) {
                PetRidingHandler.lockRotations(mob, player);
                PetRidingHandler.suppressMobAi(mob, player);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (event.getEntityMounting() instanceof Player player && event.getEntityBeingMounted() instanceof Mob mob) {
            if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                if (event.isDismounting()) {
                    // 1. If forced dismount was triggered (e.g. via keybind, grounded sneak, or DismountPetPayload), allow immediately
                    if (PetRidingHandler.isForcedDismountAllowed(player)) {
                        PetRidingHandler.onDismount(mob);
                        return;
                    }

                    // 2. On client side: if this dismount is NOT from the player actively holding sneak,
                    // it is from a server packet (e.g. ClientboundSetPassengersPacket) or entity removal.
                    // NEVER cancel a server-ordered dismount on the client!
                    if (event.getLevel().isClientSide() && !player.isShiftKeyDown()) {
                        PetRidingHandler.onDismount(mob);
                        return;
                    }

                    // 3. Flying pets: if actively holding sneak and CAN descend in air, sneak descends instead of dismounting
                    if (PetHelper.isFlyingPet(mob)) {
                        if (player.isShiftKeyDown() && !PetRidingHandler.isGroundedOrCannotDescend(mob)) {
                            event.setCanceled(true);
                            return;
                        }
                    }

                    // 4. Swimming pets: if actively holding sneak and CAN descend in water, sneak dives instead of dismounting
                    if (PetHelper.isSwimmingPet(mob) && mob.isInWater()) {
                        if (player.isShiftKeyDown() && !PetRidingHandler.isGroundedOrCannotDescend(mob)) {
                            event.setCanceled(true);
                            return;
                        }
                    }

                    // Normal dismount reached ground / completed
                    PetRidingHandler.onDismount(mob);
                } else if (event.isMounting()) {
                    if (!(mob instanceof net.minecraft.world.entity.animal.equine.AbstractHorse) && !PettingServerConfig.isPetInventoryEnabled()) {
                        event.setCanceled(true);
                        return;
                    }
                    PetRidingHandler.onMount(mob, player);
                }
            }
        }
    }
}
