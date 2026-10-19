package net.yigitguven.petting.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.CollarRespawnPoint;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.data.PetSavedData;
import net.yigitguven.petting.util.PetHelper;

@Mod.EventBusSubscriber(modid = Petting.MODID)
public class PetGoalEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (PetHelper.getOrder(mob) == PetOrder.SIT) {
                if (!mob.isShiftKeyDown()) {
                    mob.setShiftKeyDown(true);
                }
                boolean hoverFlying = PettingServerConfig.isFlyingPetsHoverWhenSitting() && PetHelper.isFlyingPet(mob);
                boolean stillSwimming = PettingServerConfig.isSwimmingPetsStayStillWhenSitting() && PetHelper.isSwimmingPet(mob);

                if (hoverFlying || stillSwimming) {
                    mob.setNoGravity(true);
                    mob.setDeltaMovement(Vec3.ZERO);
                    mob.fallDistance = 0.0F;
                    mob.setPos(mob.xo, mob.yo, mob.zo);
                    if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                        wither.setAlternativeTarget(0, 0);
                        wither.setAlternativeTarget(1, 0);
                        wither.setAlternativeTarget(2, 0);
                    }
                } else {
                    Vec3 delta = mob.getDeltaMovement();
                    mob.setDeltaMovement(0.0, delta.y, 0.0);
                    mob.setPos(mob.xo, mob.getY(), mob.zo);
                }
            } else if (mob.isShiftKeyDown()) {
                mob.setShiftKeyDown(false);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            PetHelper.injectGoals(mob);
            PetHelper.syncEntityTameStatus(mob);
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                PetHelper.getOwnerUUID(mob).ifPresent(ownerUuid ->
                        PetSavedData.get(serverLevel).addPet(ownerUuid, mob.getUUID())
                );
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.level() instanceof ServerLevel serverLevel) {
                boolean respawnFailedNotified = false;
                if (PettingServerConfig.isPetInventoryEnabled() && PettingServerConfig.isCollarRespawnEnabled()) {
                    net.minecraft.world.item.ItemStack collar = PetHelper.getCollar(mob);
                    if (!collar.isEmpty() && collar.getTag() != null && collar.getTag().contains("petting:collar_respawn")) {
                        CollarRespawnPoint respawnPoint = CollarRespawnPoint.fromNbt(collar.getTag().getCompound("petting:collar_respawn"));
                        if (respawnPoint != null) {
                            ServerLevel targetLevel = serverLevel.getServer().getLevel(respawnPoint.pos().dimension());
                            if (targetLevel != null && PetHelper.isRespawnPointValid(targetLevel, respawnPoint)) {
                                if (PetHelper.respawnPetAtBoundPoint(mob, serverLevel, targetLevel, respawnPoint)) {
                                    event.setCanceled(true);
                                    return;
                                }
                            } else {
                                if (PettingServerConfig.isPetDeathMessagesEnabled()) {
                                    net.minecraft.world.entity.player.Player owner = PetHelper.getOwner(mob);
                                    if (owner != null) {
                                        net.minecraft.network.chat.Component targetType = net.minecraft.network.chat.Component.translatable(respawnPoint.isCushion() ? "petting.term.cushion" : "petting.term.bed");
                                        owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_respawn_failed", mob.getName(), targetType));
                                        respawnFailedNotified = true;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!respawnFailedNotified && PettingServerConfig.isPetDeathMessagesEnabled()) {
                    net.minecraft.world.entity.player.Player owner = PetHelper.getOwner(mob);
                    if (owner != null) {
                        owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_died", mob.getName()));
                    }
                }

                net.minecraft.world.item.ItemStack collar = PetHelper.getCollar(mob);
                if (!collar.isEmpty()) {
                    mob.spawnAtLocation(collar);
                    PetHelper.setCollar(mob, net.minecraft.world.item.ItemStack.EMPTY);
                }
                net.minecraft.world.item.ItemStack saddle = PetHelper.getSaddle(mob);
                if (!saddle.isEmpty()) {
                    mob.spawnAtLocation(saddle);
                    PetHelper.setSaddle(mob, net.minecraft.world.item.ItemStack.EMPTY);
                }
                PetSavedData.get(serverLevel).removePet(mob.getUUID());
                PetHelper.updatePetBossBarVisibility(mob, true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.isRemoved()) {
                Entity.RemovalReason reason = mob.getRemovalReason();
                if (reason != null && reason.shouldDestroy()) {
                    if (event.getLevel() instanceof ServerLevel serverLevel) {
                        PetSavedData.get(serverLevel).removePet(mob.getUUID());
                    }
                }
            }
        }
    }
}
