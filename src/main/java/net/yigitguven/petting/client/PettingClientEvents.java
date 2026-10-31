package net.yigitguven.petting.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.network.MountPetPayload;
import net.yigitguven.petting.network.OpenPetInventoryPayload;
import net.yigitguven.petting.network.RiddenPetAttackPayload;
import net.yigitguven.petting.network.TogglePetSitPayload;
import net.yigitguven.petting.util.PetHelper;

import java.util.Set;

public class PettingClientEvents {

    private static final Set<String> POSITIVE_KEYS = Set.of(
            "petting.action.tamed_success",
            "petting.action.pet_healed",
            "petting.action.pet_sitting",
            "petting.action.pet_standing",
            "petting.action.collar_bound",
            "petting.action.mount_controlled",
            "petting.action.egg_converted_to_golden"
    );

    private static final Set<String> NEGATIVE_KEYS = Set.of(
            "petting.action.taming_failed",
            "petting.action.too_healthy",
            "petting.action.already_vanilla_tamed",
            "petting.action.already_tamed_other",
            "petting.action.pet_full_health",
            "petting.action.pet_limit_reached",
            "petting.action.saddle_required",
            "petting.action.mount_uncontrolled"
    );

    private static final Set<String> DEATH_KEYS = Set.of(
            "petting.action.pet_respawned",
            "petting.action.pet_respawn_failed",
            "petting.action.pet_died"
    );

    private static LivingEntity getTargetLivingEntity(Minecraft mc) {
        Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
        if (target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part && part.parentMob != null) {
            target = part.parentMob;
        }
        if (mc.player != null && (target == mc.player.getVehicle() || (target != null && target.hasPassenger(mc.player)))) {
            target = null;
        }
        if (target instanceof LivingEntity living && living.isAlive()) {
            return living;
        }
        if (mc.player != null && mc.level != null) {
            Vec3 eyePos = mc.player.getEyePosition();
            Vec3 viewVec = mc.player.getViewVector(1.0F);
            Vec3 reachVec = eyePos.add(viewVec.scale(16.0D));
            AABB searchBox = mc.player.getBoundingBox().inflate(16.0D);
            for (net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon : mc.level.getEntitiesOfClass(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, searchBox)) {
                if (dragon == mc.player.getVehicle() || dragon.hasPassenger(mc.player)) {
                    continue;
                }
                if (dragon.getBoundingBox().inflate(1.5D).clip(eyePos, reachVec).isPresent()) {
                    return dragon;
                }
                for (net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part : dragon.getSubEntities()) {
                    if (part.getBoundingBox().inflate(1.0D).clip(eyePos, reachVec).isPresent()) {
                        return dragon;
                    }
                }
            }
        }
        return null;
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.player != null && mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.hasSaddle(mob)) {
            float yaw = (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon)
                    ? net.minecraft.util.Mth.wrapDegrees(mc.player.getYRot() + 180.0F)
                    : mc.player.getYRot();
            float pitch = mc.player.getXRot();
            mob.setYRot(yaw);
            mob.yRotO = yaw;
            mob.yBodyRot = yaw;
            mob.yBodyRotO = yaw;
            mob.setYHeadRot(yaw);
            mob.yHeadRotO = yaw;
            mob.setXRot(pitch);
            mob.xRotO = pitch;
        }

        // Handle attack click when riding
        if (mc.player != null && mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
            if (PetHelper.isRideAttackOnClick(mob) && PettingServerConfig.isRiddenPetsAttackOnClick()) {
                while (mc.options.keyAttack.consumeClick()) {
                    ClientPlayNetworking.send(new RiddenPetAttackPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.OPEN_PET_INVENTORY.consumeClick()) {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                continue;
            }
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                // Must be holding shift for open pet inventory if bound to right click
                if (!mc.player.isShiftKeyDown()) {
                    continue;
                }
                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob == null && mc.player.getVehicle() instanceof LivingEntity vehicle && PetHelper.isTamed(vehicle)) {
                    mob = vehicle;
                }
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ClientPlayNetworking.send(new OpenPetInventoryPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.TOGGLE_PET_SIT.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                if (mc.player.isShiftKeyDown() || mc.player.isPassenger()) {
                    continue;
                }
                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && mob != mc.player.getVehicle() && !mob.hasPassenger(mc.player) && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ItemStack mainStack = mc.player.getMainHandItem();
                    InputConstants.Key boundKey = KeyMappingHelper.getBoundKeyOf(PettingClient.TOGGLE_PET_SIT);
                    boolean isRightClick = boundKey.getType() == InputConstants.Type.MOUSE
                            && boundKey.getValue() == InputConstants.MOUSE_BUTTON_RIGHT;
                    if (isRightClick && PetHelper.isInteractionItem(mob, mainStack)) {
                        continue;
                    }
                    mc.player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, false);
                    ClientPlayNetworking.send(new TogglePetSitPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.RIDE_PET.consumeClick()) {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                continue;
            }
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                if (mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PetRidingHandler.allowForcedDismount(mc.player);
                    mc.player.stopRiding();
                    mc.player.setPose(Pose.STANDING);
                    ClientPlayNetworking.send(new MountPetPayload(mob.getId()));
                    continue;
                }

                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ClientPlayNetworking.send(new MountPetPayload(mob.getId()));
                }
            }
        }
    }

    public static boolean onClientChatReceived(Component component) {
        String key = getTranslatableKey(component);
        if (key != null && (POSITIVE_KEYS.contains(key) || NEGATIVE_KEYS.contains(key) || DEATH_KEYS.contains(key))) {
            if (!PettingClientConfig.isFeedbackMessageEnabled(key)) {
                return false;
            }
        }
        return true;
    }

    private static String getTranslatableKey(Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        for (Component sibling : component.getSiblings()) {
            if (sibling.getContents() instanceof TranslatableContents translatable) {
                return translatable.getKey();
            }
        }
        return null;
    }
}
