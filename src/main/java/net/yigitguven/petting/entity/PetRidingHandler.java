package net.yigitguven.petting.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.network.DismountPetPayload;
import net.yigitguven.petting.network.PettingNetwork;
import net.yigitguven.petting.util.PetHelper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PetRidingHandler {
    private static final Map<UUID, Long> FORCED_DISMOUNT_TICKS = new ConcurrentHashMap<>();
    private static final Set<UUID> RIDDEN_PETS_NO_AI = Collections.synchronizedSet(new HashSet<>());
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    private static final Field JUMPING_FIELD = ObfuscationReflectionHelper.findField(LivingEntity.class, "f_20898_");
    static {
        JUMPING_FIELD.setAccessible(true);
    }

    public static boolean isJumping(LivingEntity entity) {
        try {
            return JUMPING_FIELD.getBoolean(entity);
        } catch (Exception e) {
            return false;
        }
    }

    public static void allowForcedDismount(Player player) {
        FORCED_DISMOUNT_TICKS.put(player.getUUID(), System.currentTimeMillis() + 1500L);
    }

    public static boolean isForcedDismountAllowed(Player player) {
        Long expiry = FORCED_DISMOUNT_TICKS.get(player.getUUID());
        if (expiry != null) {
            if (System.currentTimeMillis() <= expiry) {
                return true;
            } else {
                FORCED_DISMOUNT_TICKS.remove(player.getUUID());
            }
        }
        return false;
    }

    public static boolean consumeForcedDismount(Player player) {
        return isForcedDismountAllowed(player);
    }

    public static void clearForcedDismount(Player player) {
        FORCED_DISMOUNT_TICKS.remove(player.getUUID());
    }

    public static boolean isMarkedRiddenNoAi(LivingEntity mob) {
        return RIDDEN_PETS_NO_AI.contains(mob.getUUID());
    }

    public static void onMount(LivingEntity mob, Player player) {
        if (PetHelper.hasSaddle(mob)) {
            RIDDEN_PETS_NO_AI.add(mob.getUUID());
            if (mob instanceof Mob m && !(mob instanceof EnderDragon)) {
                m.setNoAi(true);
            }
            suppressMobAi(mob, player);
            lockRotations(mob, player);
        }
    }

    public static void onDismount(LivingEntity mob) {
        if (RIDDEN_PETS_NO_AI.remove(mob.getUUID())) {
            if (mob instanceof Mob m && !(mob instanceof EnderDragon)) {
                m.setNoAi(false);
            }
        }
        restoreMobAi(mob);
        if (!PetHelper.isSitting(mob)) {
            if (!(mob instanceof Ghast || mob instanceof EnderDragon)) {
                mob.setNoGravity(false);
            }
        }
    }

    public static void suppressMobAi(LivingEntity mob, Player player) {
        if (mob instanceof Mob m) {
            m.getNavigation().stop();
            m.setTarget(null);
            m.setLastHurtByMob(null);
            m.goalSelector.disableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE);
            m.goalSelector.disableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.JUMP);
            m.goalSelector.disableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK);
            m.targetSelector.disableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET);
            m.getMoveControl().strafe(0.0F, 0.0F);
        }
        mob.xxa = 0.0F;
        mob.yya = 0.0F;
        mob.zza = 0.0F;
        mob.setSpeed(0.0F);
        if (mob instanceof WitherBoss wither) {
            wither.setAlternativeTarget(0, 0);
            wither.setAlternativeTarget(1, 0);
        }
    }

    public static void restoreMobAi(LivingEntity mob) {
        if (mob instanceof Mob m) {
            m.goalSelector.enableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE);
            m.goalSelector.enableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.JUMP);
            m.goalSelector.enableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK);
            m.targetSelector.enableControlFlag(net.minecraft.world.entity.ai.goal.Goal.Flag.TARGET);
        }
    }

    public static void lockRotations(LivingEntity mob, Player player) {
        float yaw = (mob instanceof EnderDragon)
                ? net.minecraft.util.Mth.wrapDegrees(player.getYRot() + 180.0F)
                : player.getYRot();
        float pitch = player.getXRot();
        mob.setYRot(yaw);
        mob.yRotO = yaw;
        mob.yBodyRot = yaw;
        mob.yBodyRotO = yaw;
        mob.setYHeadRot(yaw);
        mob.yHeadRotO = yaw;
        mob.setXRot(pitch);
        mob.xRotO = pitch;
    }

    public static boolean isGroundedOrCannotDescend(LivingEntity mob) {
        if (mob.onGround() || mob.verticalCollision || mob.verticalCollisionBelow) {
            return true;
        }
        AABB box = mob.getBoundingBox();
        if (!mob.level().noCollision(mob, box.move(0, -0.2D, 0))) {
            return true;
        }
        AABB groundBox = new AABB(box.minX, box.minY - 2.5D, box.minZ, box.maxX, box.minY, box.maxZ);
        if (!mob.level().noCollision(mob, groundBox)) {
            return true;
        }
        int minX = net.minecraft.util.Mth.floor(box.minX);
        int maxX = net.minecraft.util.Mth.floor(box.maxX);
        int minZ = net.minecraft.util.Mth.floor(box.minZ);
        int maxZ = net.minecraft.util.Mth.floor(box.maxZ);
        int minY = net.minecraft.util.Mth.floor(box.minY - 2.5D);
        int maxY = net.minecraft.util.Mth.floor(box.minY);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    BlockState state = mob.level().getBlockState(pos);
                    if (!state.isAir() && (state.blocksMotion() || state.entityCanStandOn(mob.level(), pos, mob) || !state.getCollisionShape(mob.level(), pos).isEmpty())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isDirectlyAboveGround(LivingEntity mob) {
        return isGroundedOrCannotDescend(mob);
    }

    public static Vec3 findSafeDismountPosition(Player player, LivingEntity mob) {
        Level level = mob.level();
        double[][] candidateOffsets = {
                {1.5D, 0.0D},
                {-1.5D, 0.0D},
                {0.0D, 1.5D},
                {0.0D, -1.5D},
                {1.2D, 1.2D},
                {-1.2D, 1.2D},
                {1.2D, -1.2D},
                {-1.2D, -1.2D},
                {0.0D, 0.0D}
        };

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int baseBlockY = net.minecraft.util.Mth.floor(mob.getY());

        for (double[] offset : candidateOffsets) {
            double cx = mob.getX() + offset[0];
            double cz = mob.getZ() + offset[1];
            int blockX = net.minecraft.util.Mth.floor(cx);
            int blockZ = net.minecraft.util.Mth.floor(cz);

            for (int dy = 1; dy >= -5; dy--) {
                int testY = baseBlockY + dy;
                pos.set(blockX, testY, blockZ);
                BlockState groundState = level.getBlockState(pos);

                if (!groundState.isAir() && (groundState.blocksMotion() || groundState.entityCanStandOn(level, pos, player) || !groundState.getCollisionShape(level, pos).isEmpty())) {
                    BlockPos above1 = pos.above();
                    BlockPos above2 = above1.above();
                    BlockState state1 = level.getBlockState(above1);
                    BlockState state2 = level.getBlockState(above2);

                    if (state1.getCollisionShape(level, above1).isEmpty() && state2.getCollisionShape(level, above2).isEmpty()) {
                        double standY = testY + (groundState.getCollisionShape(level, pos).isEmpty() ? 1.0D : groundState.getCollisionShape(level, pos).max(Direction.Axis.Y));
                        return new Vec3(cx, standY, cz);
                    }
                }
            }
        }

        return new Vec3(mob.getX(), mob.getY(), mob.getZ());
    }

    public static void dismountPlayer(ServerPlayer player, LivingEntity mob) {
        allowForcedDismount(player);
        PettingNetwork.sendToPlayer(player, new DismountPetPayload(mob.getId()));

        player.stopRiding();
        mob.ejectPassengers();
        onDismount(mob);
        player.connection.send(new ClientboundSetPassengersPacket(mob));

        Vec3 safePos = findSafeDismountPosition(player, mob);
        player.teleportTo(safePos.x, safePos.y, safePos.z);
        player.setPose(Pose.STANDING);
        player.resetFallDistance();
    }

    public static void tickRiddenMob(LivingEntity mob, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (PetHelper.getOrder(mob) == net.yigitguven.petting.data.PetOrder.SIT) {
            PetHelper.setOrder(mob, net.yigitguven.petting.data.PetOrder.FOLLOW);
        }

        if (!PetHelper.hasSaddle(mob)) {
            if (RIDDEN_PETS_NO_AI.remove(mob.getUUID())) {
                if (mob instanceof Mob m && !(mob instanceof EnderDragon)) {
                    m.setNoAi(false);
                }
                restoreMobAi(mob);
            }
            if (player.isShiftKeyDown()) {
                dismountPlayer(serverPlayer, mob);
            }
            return;
        }

        RIDDEN_PETS_NO_AI.add(mob.getUUID());
        if (mob instanceof Mob m && !m.isNoAi() && !(mob instanceof EnderDragon)) {
            m.setNoAi(true);
        }
        suppressMobAi(mob, player);

        lockRotations(mob, player);

        float forward = player.zza;
        float strafe = -player.xxa;
        boolean jump = isJumping(player);
        boolean shift = player.isShiftKeyDown();
        double sprintMul = player.isSprinting() ? 1.35D : 1.0D;

        Vec3 look = Vec3.directionFromRotation(0.0F, player.getYRot());
        Vec3 right = Vec3.directionFromRotation(0.0F, player.getYRot() + 90.0F);
        Vec3 wishDir = look.scale(forward).add(right.scale(strafe));
        if (wishDir.lengthSqr() > 1e-4) {
            wishDir = wishDir.normalize();
        }

        if (PetHelper.isRideAutoAttackHostiles(mob) && mob.tickCount % 20 == 0) {
            checkAndPerformAutoAttack(serverPlayer, mob);
        }

        if (PetHelper.isFlyingPet(mob)) {
            if (shift) {
                if (isGroundedOrCannotDescend(mob)) {
                    dismountPlayer(serverPlayer, mob);
                    return;
                }
            }

            double baseSpeed = (mob instanceof EnderDragon ? 0.85D : Math.max(0.28D, mob.getAttributeValue(Attributes.MOVEMENT_SPEED)))
                    * PettingServerConfig.getFlyingPetSpeedMultiplier() * sprintMul;
            double dy = 0.0D;
            if (jump) {
                dy = (mob instanceof EnderDragon ? 0.6D : PettingServerConfig.getFlyingPetAscentSpeed());
            } else if (shift) {
                dy = -(mob instanceof EnderDragon ? 0.6D : PettingServerConfig.getFlyingPetDescentSpeed());
            }

            Vec3 horiz = wishDir.scale(baseSpeed);
            mob.setDeltaMovement(new Vec3(horiz.x, dy, horiz.z));
            mob.move(MoverType.SELF, mob.getDeltaMovement());
            mob.resetFallDistance();
            player.resetFallDistance();
            mob.setNoGravity(true);

            if (shift && isGroundedOrCannotDescend(mob)) {
                dismountPlayer(serverPlayer, mob);
                return;
            }
        } else if (PetHelper.isSwimmingPet(mob)) {
            if (mob.isInWater()) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, false, false, true));
                if (shift) {
                    if (isGroundedOrCannotDescend(mob)) {
                        dismountPlayer(serverPlayer, mob);
                        return;
                    }
                }

                double baseSpeed = Math.max(0.26D, mob.getAttributeValue(Attributes.MOVEMENT_SPEED))
                        * PettingServerConfig.getSwimmingPetSpeedMultiplier() * sprintMul;
                double dy = 0.0D;
                if (jump) {
                    dy = 0.30D;
                } else if (shift) {
                    dy = -0.30D;
                }

                Vec3 horiz = wishDir.scale(baseSpeed);
                mob.setDeltaMovement(new Vec3(horiz.x, dy, horiz.z));
                mob.move(MoverType.SELF, mob.getDeltaMovement());
                mob.resetFallDistance();
                player.resetFallDistance();

                if (shift && isGroundedOrCannotDescend(mob)) {
                    dismountPlayer(serverPlayer, mob);
                    return;
                }
            } else {
                if (shift) {
                    dismountPlayer(serverPlayer, mob);
                    return;
                }

                double baseSpeed = Math.max(0.12D, mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.6D);
                double my = mob.getDeltaMovement().y;
                if (mob.onGround()) {
                    if (jump) {
                        my = 0.42D;
                    } else {
                        my = -0.0784D;
                    }
                } else {
                    my = (my - 0.08D) * 0.98D;
                }
                Vec3 horiz = wishDir.scale(baseSpeed);
                mob.setDeltaMovement(new Vec3(horiz.x, my, horiz.z));
                mob.move(MoverType.SELF, mob.getDeltaMovement());
            }
        } else {
            if (shift) {
                dismountPlayer(serverPlayer, mob);
                return;
            }

            double baseSpeed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * sprintMul;
            double my = mob.getDeltaMovement().y;
            if (mob.onGround()) {
                if (jump) {
                    my = 0.50D;
                } else {
                    my = -0.0784D;
                }
            } else {
                my = (my - 0.08D) * 0.98D;
                if (my < -3.9D) {
                    my = -3.9D;
                }
            }

            mob.setMaxUpStep(1.06F);

            Vec3 horiz = wishDir.scale(baseSpeed);
            mob.setDeltaMovement(new Vec3(horiz.x, my, horiz.z));
            mob.move(MoverType.SELF, mob.getDeltaMovement());
        }
    }

    public static void performRiddenAttack(ServerPlayer player, LivingEntity mob) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long now = serverLevel.getGameTime();
        Long last = COOLDOWNS.get(mob.getUUID());
        if (last != null && now - last < PettingServerConfig.getRiddenAttackCooldownTicks()) {
            return;
        }
        COOLDOWNS.put(mob.getUUID(), now);

        Vec3 look = player.getLookAngle().normalize();

        if (mob instanceof EnderDragon) {
            DragonFireball fireball = new DragonFireball(serverLevel, mob, look.x, look.y, look.z);
            fireball.setPos(mob.getX() + look.x * 2.5D, mob.getEyeY(), mob.getZ() + look.z * 2.5D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof Ghast) {
            LargeFireball fireball = new LargeFireball(serverLevel, mob, look.x, look.y, look.z, 1);
            fireball.setPos(mob.getX() + look.x * 2.0D, mob.getEyeY(), mob.getZ() + look.z * 2.0D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof Blaze) {
            SmallFireball fireball = new SmallFireball(serverLevel, mob, look.x, look.y, look.z);
            fireball.setPos(mob.getX() + look.x * 1.5D, mob.getEyeY(), mob.getZ() + look.z * 1.5D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof WitherBoss) {
            WitherSkull skull = new WitherSkull(serverLevel, mob, look.x, look.y, look.z);
            skull.setPos(mob.getX() + look.x * 1.5D, mob.getEyeY(), mob.getZ() + look.z * 1.5D);
            serverLevel.addFreshEntity(skull);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof AbstractSkeleton
                || mob.getMainHandItem().is(Items.BOW)
                || mob.getMainHandItem().is(Items.CROSSBOW)
                || PetHelper.getCollarSettings(mob).preferRangedAttack()) {
            Arrow arrow = new Arrow(serverLevel, mob);
            arrow.setPos(mob.getX() + look.x * 1.2D, mob.getEyeY(), mob.getZ() + look.z * 1.2D);
            arrow.shoot(look.x, look.y, look.z, 2.5F, 1.0F);
            serverLevel.addFreshEntity(arrow);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            mob.swing(InteractionHand.MAIN_HAND);
            AABB attackBox = mob.getBoundingBox().inflate(2.5D).expandTowards(look.scale(2.0D));
            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, attackBox,
                    e -> e != mob && e != player && !PetHelper.isOwner(e, player) && e.isAlive());
            for (LivingEntity target : targets) {
                if (mob instanceof Mob m) {
                    PetHelper.doPetHurtTarget(m, target);
                } else {
                    target.hurt(mob.damageSources().mobAttack(mob), (float) net.yigitguven.petting.config.PettingServerConfig.getPassiveMobAttackDamage());
                }
                break;
            }
        }
    }

    private static void checkAndPerformAutoAttack(ServerPlayer player, LivingEntity mob) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB box = mob.getBoundingBox().inflate(12.0D);
        List<Monster> hostiles = serverLevel.getEntitiesOfClass(Monster.class, box,
                m -> m.isAlive() && !PetHelper.isTamed(m) && m.hasLineOfSight(mob));
        if (hostiles.isEmpty()) {
            return;
        }

        Monster target = hostiles.get(0);
        Vec3 dir = target.getEyePosition().subtract(mob.getEyePosition()).normalize();

        if (mob instanceof EnderDragon) {
            DragonFireball fireball = new DragonFireball(serverLevel, mob, dir.x, dir.y, dir.z);
            fireball.setPos(mob.getX() + dir.x * 2.5D, mob.getEyeY(), mob.getZ() + dir.z * 2.5D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof Ghast) {
            LargeFireball fireball = new LargeFireball(serverLevel, mob, dir.x, dir.y, dir.z, 1);
            fireball.setPos(mob.getX() + dir.x * 2.0D, mob.getEyeY(), mob.getZ() + dir.z * 2.0D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.GHAST_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof Blaze) {
            SmallFireball fireball = new SmallFireball(serverLevel, mob, dir.x, dir.y, dir.z);
            fireball.setPos(mob.getX() + dir.x * 1.5D, mob.getEyeY(), mob.getZ() + dir.z * 1.5D);
            serverLevel.addFreshEntity(fireball);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof WitherBoss) {
            WitherSkull skull = new WitherSkull(serverLevel, mob, dir.x, dir.y, dir.z);
            skull.setPos(mob.getX() + dir.x * 1.5D, mob.getEyeY(), mob.getZ() + dir.z * 1.5D);
            serverLevel.addFreshEntity(skull);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob instanceof AbstractSkeleton
                || mob.getMainHandItem().is(Items.BOW)
                || mob.getMainHandItem().is(Items.CROSSBOW)
                || PetHelper.getCollarSettings(mob).preferRangedAttack()) {
            Arrow arrow = new Arrow(serverLevel, mob);
            arrow.setPos(mob.getX() + dir.x * 1.2D, mob.getEyeY(), mob.getZ() + dir.z * 1.2D);
            arrow.shoot(dir.x, dir.y, dir.z, 2.5F, 1.0F);
            serverLevel.addFreshEntity(arrow);
            serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (mob.distanceTo(target) <= 3.5D) {
            mob.swing(InteractionHand.MAIN_HAND);
            if (mob instanceof Mob m) {
                PetHelper.doPetHurtTarget(m, target);
            } else {
                target.hurt(mob.damageSources().mobAttack(mob), (float) net.yigitguven.petting.config.PettingServerConfig.getPassiveMobAttackDamage());
            }
        }
    }
}
