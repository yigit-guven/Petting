package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.network.MountPetPayload;
import net.yigitguven.petting.network.OpenPetInventoryPayload;
import net.yigitguven.petting.network.PettingNetwork;
import net.yigitguven.petting.network.RiddenPetAttackPayload;
import net.yigitguven.petting.network.TogglePetSitPayload;
import net.yigitguven.petting.util.PetHelper;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Petting.MODID, value = Dist.CLIENT)
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
        if (target instanceof EnderDragonPart part && part.parentMob != null) {
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
            for (EnderDragon dragon : mc.level.getEntitiesOfClass(EnderDragon.class, searchBox)) {
                if (dragon == mc.player.getVehicle() || dragon.hasPassenger(mc.player)) {
                    continue;
                }
                if (dragon.getBoundingBox().inflate(1.5D).clip(eyePos, reachVec).isPresent()) {
                    return dragon;
                }
                for (EnderDragonPart part : dragon.getSubEntities()) {
                    if (part.getBoundingBox().inflate(1.0D).clip(eyePos, reachVec).isPresent()) {
                        return dragon;
                    }
                }
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.hasSaddle(mob)) {
            float yaw = (mob instanceof EnderDragon)
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
            if (mc.player != null && mc.level != null && mc.screen == null) {
                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob == null && mc.player.getVehicle() instanceof LivingEntity vehicle && PetHelper.isTamed(vehicle)) {
                    mob = vehicle;
                }
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PettingNetwork.sendToServer(new OpenPetInventoryPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.TOGGLE_PET_SIT.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.screen == null) {
                if (mc.player.isPassenger()) {
                    continue;
                }
                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && mob != mc.player.getVehicle() && !mob.hasPassenger(mc.player) && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    ItemStack mainStack = mc.player.getMainHandItem();
                    boolean isRightClick = PettingClient.TOGGLE_PET_SIT.getKey().getType() == com.mojang.blaze3d.platform.InputConstants.Type.MOUSE
                            && PettingClient.TOGGLE_PET_SIT.getKey().getValue() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT
                            && PettingClient.TOGGLE_PET_SIT.getKeyModifier() == KeyModifier.NONE;
                    if (isRightClick && PetHelper.isInteractionItem(mob, mainStack)) {
                        continue;
                    }
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    PettingNetwork.sendToServer(new TogglePetSitPayload(mob.getId()));
                }
            }
        }

        while (PettingClient.RIDE_PET.consumeClick()) {
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                continue;
            }
            if (mc.player != null && mc.level != null && mc.screen == null) {
                if (mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PetRidingHandler.allowForcedDismount(mc.player);
                    mc.player.stopRiding();
                    mc.player.setPose(Pose.STANDING);
                    PettingNetwork.sendToServer(new MountPetPayload(mob.getId()));
                    continue;
                }

                LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PettingNetwork.sendToServer(new MountPetPayload(mob.getId()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (mc.player.getVehicle() instanceof LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                if (PetHelper.isRideAttackOnClick(mob) && PettingServerConfig.isRiddenPetsAttackOnClick()) {
                    event.setSwingHand(false);
                    event.setCanceled(true);
                    PettingNetwork.sendToServer(new RiddenPetAttackPayload(mob.getId()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientChatReceived(ClientChatReceivedEvent event) {
        String key = getTranslatableKey(event.getMessage());
        if (key != null && (POSITIVE_KEYS.contains(key) || NEGATIVE_KEYS.contains(key) || DEATH_KEYS.contains(key))) {
            if (!PettingClientConfig.isFeedbackMessageEnabled(key)) {
                event.setCanceled(true);
            }
        }
    }


    private static String getTranslatableKey(Component component) {
        if (component == null) {
            return null;
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        for (Component sibling : component.getSiblings()) {
            String key = getTranslatableKey(sibling);
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            float drop = SittingPoseRenderer.getNameTagDrop(entity);
            if (drop != 0.0F) {
                event.getPoseStack().translate(0.0D, -drop, 0.0D);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity != null && SittingPoseRenderer.shouldSit(entity, event.getRenderer().getModel())) {
            entity.walkAnimation.setSpeed(0.0F);
        }
    }
}
