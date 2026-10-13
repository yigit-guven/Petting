package net.yigitguven.petting.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.network.OpenPetInventoryPayload;
import net.yigitguven.petting.util.PetHelper;

import java.util.Set;

@EventBusSubscriber(modid = Petting.MODID, value = Dist.CLIENT)
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

    private static net.minecraft.world.entity.LivingEntity getTargetLivingEntity(Minecraft mc) {
        Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
        if (target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part && part.parentMob != null) {
            target = part.parentMob;
        }
        if (mc.player != null && (target == mc.player.getVehicle() || (target != null && target.hasPassenger(mc.player)))) {
            target = null;
        }
        if (target instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
            return living;
        }
        if (mc.player != null && mc.level != null) {
            net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition();
            net.minecraft.world.phys.Vec3 viewVec = mc.player.getViewVector(1.0F);
            net.minecraft.world.phys.Vec3 reachVec = eyePos.add(viewVec.scale(16.0D));
            net.minecraft.world.phys.AABB searchBox = mc.player.getBoundingBox().inflate(16.0D);
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

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.hasSaddle(mob)) {
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

        while (PettingClient.OPEN_PET_INVENTORY.consumeClick()) {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                continue;
            }
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                net.minecraft.world.entity.LivingEntity mob = getTargetLivingEntity(mc);
                if (mob == null && mc.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity vehicle && PetHelper.isTamed(vehicle)) {
                    mob = vehicle;
                }
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ClientPacketDistributor.sendToServer(new OpenPetInventoryPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.TOGGLE_PET_SIT.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                if (mc.player.isPassenger()) {
                    continue;
                }
                net.minecraft.world.entity.LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && mob != mc.player.getVehicle() && !mob.hasPassenger(mc.player) && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    net.minecraft.world.item.ItemStack mainStack = mc.player.getMainHandItem();
                    boolean isRightClick = PettingClient.TOGGLE_PET_SIT.getKey().getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE
                            && PettingClient.TOGGLE_PET_SIT.getKey().getValue() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT
                            && PettingClient.TOGGLE_PET_SIT.getKeyModifier() == net.neoforged.neoforge.client.settings.KeyModifier.NONE;
                    if (isRightClick && PetHelper.isInteractionItem(mob, mainStack)) {
                        continue;
                    }
                    mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                    ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.TogglePetSitPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.RIDE_PET.consumeClick()) {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                continue;
            }
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                if (mc.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PetRidingHandler.allowForcedDismount(mc.player);
                    mc.player.stopRiding();
                    mc.player.setPose(net.minecraft.world.entity.Pose.STANDING);
                    ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.MountPetPayload(mob.getId()));
                    continue;
                }

                net.minecraft.world.entity.LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.MountPetPayload(mob.getId()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.isAttack()) {
            if (mc.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                if (PetHelper.isRideAttackOnClick(mob) && net.yigitguven.petting.config.PettingServerConfig.isRiddenPetsAttackOnClick()) {
                    event.setCanceled(true);
                    event.setSwingHand(false);
                    ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.RiddenPetAttackPayload(mob.getId()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientChatReceived(ClientChatReceivedEvent.System event) {
        String key = getTranslatableKey(event.getMessage());
        if (key != null && (POSITIVE_KEYS.contains(key) || NEGATIVE_KEYS.contains(key) || DEATH_KEYS.contains(key))) {
            if (!PettingClientConfig.isFeedbackMessageEnabled(key)) {
                event.setCanceled(true);
            }
        }
    }

    private static String getTranslatableKey(net.minecraft.network.chat.Component component) {
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        for (net.minecraft.network.chat.Component sibling : component.getSiblings()) {
            if (sibling.getContents() instanceof TranslatableContents translatable) {
                return translatable.getKey();
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderNameTag(net.neoforged.neoforge.client.event.RenderNameTagEvent.CanRender event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.LivingEntity mob && PetHelper.isTamed(mob)) {
            if (PetHelper.isSitting(mob)) {
                event.getEntityRenderState().isDiscrete = true;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(net.neoforged.neoforge.client.event.RenderLivingEvent.Pre<?, ?, ?> event) {
        Float offset = event.getRenderState().getRenderData(net.yigitguven.petting.client.render.PettingClientRender.PET_SIT_GROUND_OFFSET_KEY);
        if (offset != null && offset > 0.0F) {
            event.getPoseStack().translate(0.0D, -offset.doubleValue(), 0.0D);
        }
    }
}
