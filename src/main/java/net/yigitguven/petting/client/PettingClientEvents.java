package net.yigitguven.petting.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.SystemMessageReceivedEvent;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.client.render.PetRenderStateData;
import net.yigitguven.petting.client.render.PettingClientRender;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
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
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
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
                    PettingNetwork.sendToServer(new OpenPetInventoryPayload(mob.getId()));
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
                            && PettingClient.TOGGLE_PET_SIT.getKeyModifier() == KeyModifier.NONE;
                    if (isRightClick && PetHelper.isInteractionItem(mob, mainStack)) {
                        continue;
                    }
                    mc.player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
                    PettingNetwork.sendToServer(new TogglePetSitPayload(mob.getId()));
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
                    PettingNetwork.sendToServer(new MountPetPayload(mob.getId()));
                    continue;
                }

                net.minecraft.world.entity.LivingEntity mob = getTargetLivingEntity(mc);
                if (mob != null && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                    PettingNetwork.sendToServer(new MountPetPayload(mob.getId()));
                }
            }
        }
    }

    @SubscribeEvent
    public static boolean onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered.Attack event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (mc.player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity mob && PetHelper.isTamed(mob) && PetHelper.isOwner(mob, mc.player)) {
                if (PetHelper.isRideAttackOnClick(mob) && PettingServerConfig.isRiddenPetsAttackOnClick()) {
                    event.setSwingHand(false);
                    PettingNetwork.sendToServer(new RiddenPetAttackPayload(mob.getId()));
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static boolean onClientChatReceived(ClientChatReceivedEvent event) {
        String key = getTranslatableKey(event.getMessage());
        if (key != null && (POSITIVE_KEYS.contains(key) || NEGATIVE_KEYS.contains(key) || DEATH_KEYS.contains(key))) {
            if (!PettingClientConfig.isFeedbackMessageEnabled(key)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static boolean onSystemMessageReceived(SystemMessageReceivedEvent event) {
        String key = getTranslatableKey(event.getMessage());
        if (key != null && (POSITIVE_KEYS.contains(key) || NEGATIVE_KEYS.contains(key) || DEATH_KEYS.contains(key))) {
            if (!PettingClientConfig.isFeedbackMessageEnabled(key)) {
                return true;
            }
        }
        return false;
    }

    private static String getTranslatableKey(net.minecraft.network.chat.Component component) {
        if (component == null) {
            return null;
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }
        for (net.minecraft.network.chat.Component sibling : component.getSiblings()) {
            String key = getTranslatableKey(sibling);
            if (key != null) {
                return key;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getState() instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState livingState) {
            PetRenderStateData data = PettingClientRender.getRenderData(livingState);
            if (data != null && data.sitGroundOffset() > 0.0F) {
                livingState.isDiscrete = true;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?, ?> event) {
        var state = event.getState();
        PetRenderStateData data = PettingClientRender.getRenderData(state);
        if (data != null) {
            if (data.isSittingPet()) {
                if (state instanceof HumanoidRenderState humanoidState) {
                    humanoidState.isPassenger = true;
                } else if (state instanceof IllagerRenderState illagerState) {
                    illagerState.isRiding = true;
                } else if (state instanceof FoxRenderState foxState) {
                    foxState.isSitting = true;
                } else if (state instanceof PandaRenderState pandaState) {
                    pandaState.isSitting = true;
                } else if (state instanceof WolfRenderState wolfState) {
                    wolfState.isSitting = true;
                } else if (state instanceof FelineRenderState felineState) {
                    felineState.isSitting = true;
                } else if (state instanceof ParrotRenderState parrotState) {
                    parrotState.pose = ParrotModel.Pose.SITTING;
                }

                state.walkAnimationPos = 0.0F;
                state.walkAnimationSpeed = 0.0F;
                state.isDiscrete = true;
            }

            if (data.sitGroundOffset() > 0.0F) {
                event.getPoseStack().translate(0.0D, -data.sitGroundOffset(), 0.0D);
            }
        }
    }
}
