package net.yigitguven.petting.client;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;

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
            "petting.action.pet_standing"
    );

    private static final Set<String> NEGATIVE_KEYS = Set.of(
            "petting.action.taming_failed",
            "petting.action.too_healthy",
            "petting.action.already_vanilla_tamed",
            "petting.action.already_tamed_other",
            "petting.action.pet_full_health",
            "petting.action.pet_limit_reached"
    );

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof Mob mob && PetHelper.isTamed(mob) && PetHelper.hasSaddle(mob)) {
            mob.setYRot(mc.player.getYRot());
            mob.yRotO = mc.player.getYRot();
            mob.yBodyRot = mc.player.getYRot();
            mob.yBodyRotO = mc.player.getYRot();
            mob.setYHeadRot(mc.player.getYRot());
            mob.yHeadRotO = mc.player.getYRot();
            mob.setXRot(mc.player.getXRot());
            mob.xRotO = mc.player.getXRot();
        }

        while (PettingClient.OPEN_PET_INVENTORY.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                        (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
                if (target instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                        ClientPacketDistributor.sendToServer(new OpenPetInventoryPayload(mob.getId()));
                    }
                }
            }
        }

        while (PettingClient.TOGGLE_PET_SIT.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                        (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
                if (target instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
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
        }

        while (PettingClient.RIDE_PET.consumeClick()) {
            if (mc.player != null && mc.level != null && mc.gui.screen() == null) {
                if (mc.player.getVehicle() instanceof Mob mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PetRidingHandler.allowForcedDismount(mc.player);
                    mc.player.stopRiding();
                    mc.player.setPose(net.minecraft.world.entity.Pose.STANDING);
                    ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.MountPetPayload(mob.getId()));
                    continue;
                }

                Entity target = mc.crosshairPickEntity != null ? mc.crosshairPickEntity :
                        (mc.hitResult instanceof EntityHitResult ehr ? ehr.getEntity() : null);
                if (target instanceof Mob mob && mob.isAlive()) {
                    if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                        ClientPacketDistributor.sendToServer(new net.yigitguven.petting.network.MountPetPayload(mob.getId()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.isAttack()) {
            if (mc.player.getVehicle() instanceof Mob mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
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
        if (event.getMessage().getContents() instanceof TranslatableContents translatable) {
            String key = translatable.getKey();
            if (POSITIVE_KEYS.contains(key) && !PettingClientConfig.isPositiveFeedbackEnabled()) {
                event.setCanceled(true);
            } else if (NEGATIVE_KEYS.contains(key) && !PettingClientConfig.isNegativeFeedbackEnabled()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderNameTag(net.neoforged.neoforge.client.event.RenderNameTagEvent.CanRender event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
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
