package net.yigitguven.petting.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.data.PetSavedData;
import net.yigitguven.petting.util.PetHelper;

public class PetGoalEvents {

    public static void onEntityTickPre(Mob mob) {
        if (PetHelper.isTamed(mob)) {
            if (PetHelper.getOrder(mob) == PetOrder.SIT) {
                boolean hoverFlying = PettingServerConfig.isFlyingPetsHoverWhenSitting() && PetHelper.isFlyingPet(mob);
                boolean stillSwimming = PettingServerConfig.isSwimmingPetsStayStillWhenSitting() && PetHelper.isSwimmingPet(mob);

                if (hoverFlying || stillSwimming) {
                    mob.setNoGravity(true);
                    mob.setDeltaMovement(Vec3.ZERO);
                    mob.fallDistance = 0.0F;
                    if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                        wither.setAlternativeTarget(0, 0);
                        wither.setAlternativeTarget(1, 0);
                        wither.setAlternativeTarget(2, 0);
                    }
                }
            }
        }
    }

    public static void onEntityTickPost(Mob mob) {
        if (PetHelper.isTamed(mob)) {
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

    public static void onEntityLoad(Entity entity, ServerLevel serverLevel) {
        if (entity instanceof Mob mob && PetHelper.isTamed(mob)) {
            PetHelper.injectGoals(mob);
            PetHelper.syncEntityTameStatus(mob);
            PetHelper.getOwnerUUID(mob).ifPresent(ownerUuid ->
                    PetSavedData.get(serverLevel).addPet(ownerUuid, mob.getUUID())
            );
        }
    }

    public static boolean allowDeath(LivingEntity entity, DamageSource damageSource, float amount) {
        if (entity instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.level() instanceof ServerLevel serverLevel) {
                boolean respawnFailedNotified = false;
                if (PettingServerConfig.isPetInventoryEnabled() && PettingServerConfig.isCollarRespawnEnabled()) {
                    net.minecraft.world.item.ItemStack collar = PetHelper.getCollar(mob);
                    if (!collar.isEmpty() && collar.has(net.yigitguven.petting.init.PettingDataComponents.COLLAR_RESPAWN_POINT)) {
                        net.yigitguven.petting.data.CollarRespawnPoint respawnPoint = collar.get(net.yigitguven.petting.init.PettingDataComponents.COLLAR_RESPAWN_POINT);
                        if (respawnPoint != null) {
                            ServerLevel targetLevel = serverLevel.getServer().getLevel(respawnPoint.pos().dimension());
                            if (targetLevel != null && PetHelper.isRespawnPointValid(targetLevel, respawnPoint)) {
                                if (PetHelper.respawnPetAtBoundPoint(mob, serverLevel, targetLevel, respawnPoint)) {
                                    return false; // Prevent death!
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
                    mob.spawnAtLocation(serverLevel, collar);
                    PetHelper.setCollar(mob, net.minecraft.world.item.ItemStack.EMPTY);
                }
                net.minecraft.world.item.ItemStack saddle = PetHelper.getSaddle(mob);
                if (!saddle.isEmpty()) {
                    mob.spawnAtLocation(serverLevel, saddle);
                    PetHelper.setSaddle(mob, net.minecraft.world.item.ItemStack.EMPTY);
                }
                PetSavedData.get(serverLevel).removePet(mob.getUUID());
                PetHelper.updatePetBossBarVisibility(mob, true);
            }
        }
        return true;
    }

    public static void onEntityUnload(Entity entity, ServerLevel serverLevel) {
        if (entity instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.isRemoved()) {
                Entity.RemovalReason reason = mob.getRemovalReason();
                if (reason != null && reason.shouldDestroy()) {
                    PetSavedData.get(serverLevel).removePet(mob.getUUID());
                }
            }
        }
    }
}
