package net.yigitguven.petting.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.ai.PetAttackOwnerTargetGoal;
import net.yigitguven.petting.ai.PetDefendOwnerTargetGoal;
import net.yigitguven.petting.ai.PetFollowOwnerGoal;
import net.yigitguven.petting.ai.PetGuardTargetGoal;
import net.yigitguven.petting.ai.PetMeleeAttackGoal;
import net.yigitguven.petting.ai.PetRangedAttackGoal;
import net.yigitguven.petting.ai.PetSitGoal;
import net.yigitguven.petting.ai.PetWanderGoal;
import net.yigitguven.petting.data.CollarSettings;
import net.yigitguven.petting.data.CombatMode;
import net.yigitguven.petting.data.PetData;
import net.yigitguven.petting.data.PetOrder;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.capability.PetDataProvider;
import net.yigitguven.petting.init.PettingDataComponents;
import net.yigitguven.petting.network.PettingNetwork;
import net.yigitguven.petting.network.SyncPetDataPayload;

import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetSavedData;

public class PetHelper {

    public static @Nullable PetData getPetData(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        return entity.getCapability(PetDataProvider.PET_DATA_CAPABILITY).orElse(null);
    }

    public static boolean hasPetData(@Nullable net.minecraft.world.entity.Entity entity) {
        PetData data = getPetData(entity);
        return data != null && data.isTamed();
    }

    public static void setPetData(LivingEntity entity, PetData data) {
        if (entity == null) return;
        PetData current = getPetData(entity);
        if (current != null && data != null && current != data) {
            current.copyFrom(data);
        }
        syncPetData(entity);
    }

    public static void syncPetData(LivingEntity entity) {
        if (entity != null && !entity.level().isClientSide()) {
            PetData data = getPetData(entity);
            if (data != null) {
                PettingNetwork.sendToTrackingAndSelf(entity, new SyncPetDataPayload(entity.getId(), data));
            }
        }
    }

    public static boolean isVanillaTamed(LivingEntity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }
        if (entity instanceof AbstractHorse horse && horse.isTamed()) {
            return true;
        }
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                return true;
            }
            if (ownable.getOwner() != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTamed(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data != null && data.isTamed()) {
            return true;
        }
        return isVanillaTamed(entity);
    }

    private static final String[] MODDED_OWNER_METHOD_NAMES = new String[]{
        "getOwner", "getSummoner", "getCaster", "getMaster", "getLeader", "getOwnerEntity", "getCreator"
    };

    private static @Nullable net.minecraft.world.entity.Entity findModdedOwner(net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        Class<?> clazz = entity.getClass();
        for (String methodName : MODDED_OWNER_METHOD_NAMES) {
            try {
                Method method = clazz.getMethod(methodName);
                if (net.minecraft.world.entity.Entity.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0) {
                    Object result = method.invoke(entity);
                    if (result instanceof net.minecraft.world.entity.Entity owner && owner != entity) {
                        return owner;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static @Nullable LivingEntity getRootOwner(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof Player player) {
            return player;
        }

        java.util.Set<net.minecraft.world.entity.Entity> visited = new java.util.HashSet<>();
        net.minecraft.world.entity.Entity current = entity;

        while (current != null && visited.add(current)) {
            if (current instanceof Player player) {
                return player;
            }

            // 1. Direct Petting Data Capability
            PetData data = getPetData(current);
            if (data != null && data.isTamed() && data.getOwnerUUID() != null) {
                Player player = current.level().getPlayerByUUID(data.getOwnerUUID());
                if (player != null) {
                    return player;
                }
            }

            // 2. OwnableEntity (Vanilla tamables, Vex, EvokerFangs, Modded ownables)
            if (current instanceof OwnableEntity ownable) {
                net.minecraft.world.entity.Entity owner = ownable.getOwner();
                if (owner != null && owner != current) {
                    current = owner;
                    continue;
                }
                if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                    UUID ownerUuid = ownable.getOwnerReference().getUUID();
                    Player player = current.level().getPlayerByUUID(ownerUuid);
                    if (player != null) {
                        return player;
                    }
                }
            }

            // 3. TraceableEntity (Projectiles, AreaEffectCloud, PrimedTnt, Vex, Fangs)
            if (current instanceof net.minecraft.world.entity.TraceableEntity traceable) {
                net.minecraft.world.entity.Entity owner = traceable.getOwner();
                if (owner != null && owner != current) {
                    current = owner;
                    continue;
                }
            }

            // 4. Common modded summon/minion getter methods via reflection
            net.minecraft.world.entity.Entity moddedOwner = findModdedOwner(current);
            if (moddedOwner != null && moddedOwner != current) {
                current = moddedOwner;
                continue;
            }

            break;
        }

        return null;
    }

    public static Optional<UUID> getRootOwnerUUID(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        LivingEntity root = getRootOwner(entity);
        if (root instanceof Player player) {
            return Optional.of(player.getUUID());
        }
        if (entity instanceof LivingEntity living) {
            return getOwnerUUID(living);
        }
        return Optional.empty();
    }

    public static boolean isRootOwner(@Nullable net.minecraft.world.entity.Entity entity, @Nullable Player player) {
        if (entity == null || player == null) {
            return false;
        }
        LivingEntity root = getRootOwner(entity);
        if (root == player) {
            return true;
        }
        return getRootOwnerUUID(entity).map(uuid -> uuid.equals(player.getUUID())).orElse(false);
    }

    public static boolean areEntitiesAllied(@Nullable net.minecraft.world.entity.Entity a, @Nullable net.minecraft.world.entity.Entity b) {
        if (a == null || b == null) {
            return false;
        }
        if (a == b) {
            return true;
        }
        // Vehicle / passenger relationship
        if (a.isPassengerOfSameVehicle(b) || a.hasPassenger(b) || b.hasPassenger(a)) {
            return true;
        }
        // One is the root owner of the other
        if (a instanceof Player playerA && isRootOwner(b, playerA)) {
            return true;
        }
        if (b instanceof Player playerB && isRootOwner(a, playerB)) {
            return true;
        }
        // Both share the same root owner player
        Optional<UUID> ownerA = getRootOwnerUUID(a);
        Optional<UUID> ownerB = getRootOwnerUUID(b);
        if (ownerA.isPresent() && ownerB.isPresent() && ownerA.get().equals(ownerB.get())) {
            return true;
        }
        return false;
    }

    public static boolean arePetsOfSameOwner(@Nullable net.minecraft.world.entity.Entity a, @Nullable net.minecraft.world.entity.Entity b) {
        if (a == null || b == null || a == b) {
            return false;
        }
        if (a instanceof Player || b instanceof Player) {
            return false;
        }
        Optional<UUID> ownerA = getRootOwnerUUID(a);
        Optional<UUID> ownerB = getRootOwnerUUID(b);
        return ownerA.isPresent() && ownerB.isPresent() && ownerA.get().equals(ownerB.get());
    }

    public static Optional<UUID> getOwnerUUID(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data != null && data.isTamed() && data.getOwnerUUID() != null) {
            return Optional.of(data.getOwnerUUID());
        }
        if (entity instanceof OwnableEntity ownable) {
            if (ownable.getOwnerReference() != null && ownable.getOwnerReference().getUUID() != null) {
                return Optional.of(ownable.getOwnerReference().getUUID());
            }
            if (ownable.getOwner() != null) {
                if (ownable.getOwner() instanceof Player) {
                    return Optional.of(ownable.getOwner().getUUID());
                }
                // If owned by another entity, resolve root owner
                LivingEntity root = getRootOwner(entity);
                if (root instanceof Player) {
                    return Optional.of(root.getUUID());
                }
                return Optional.of(ownable.getOwner().getUUID());
            }
        }
        return Optional.empty();
    }

    @Nullable
    public static Player getOwner(LivingEntity entity) {
        LivingEntity root = getRootOwner(entity);
        if (root instanceof Player player) {
            return player;
        }
        Optional<UUID> uuidOpt = getOwnerUUID(entity);
        if (uuidOpt.isEmpty()) {
            return null;
        }
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel sLevel) {
            return sLevel.getServer().getPlayerList().getPlayer(uuidOpt.get());
        }
        return entity.level().getPlayerByUUID(uuidOpt.get());
    }

    public static boolean isOwner(net.minecraft.world.entity.Entity entity, Player player) {
        return isRootOwner(entity, player);
    }

    public static boolean isOwner(LivingEntity entity, Player player) {
        return isRootOwner(entity, player);
    }

    public static PetOrder getOrder(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data == null) {
            return PetOrder.FOLLOW;
        }
        return data.getOrder();
    }

    public static boolean isSitting(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data != null && data.getOrder() == PetOrder.SIT) {
            return true;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isInSittingPose()) {
            return true;
        }
        return false;
    }

    public static void setOrder(LivingEntity entity, PetOrder order) {
        PetData data = getPetData(entity);
        if (data != null) {
            data.setOrder(order);
            syncPetData(entity);
        }
        if (entity instanceof TamableAnimal tamable) {
            boolean sit = (order == PetOrder.SIT);
            tamable.setOrderedToSit(sit);
            tamable.setInSittingPose(sit);
        }
        if (entity instanceof Mob mob) {
            mob.setShiftKeyDown(order == PetOrder.SIT);
            if (order != PetOrder.SIT && isFlyingPet(mob)) {
                mob.setNoGravity(false);
            }
        }
        syncModdedSit(entity, order == PetOrder.SIT);
    }

    public static CombatMode getCombatMode(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return PettingServerConfig.getDefaultCombatMode();
        }
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty() && collar.has(PettingDataComponents.COLLAR_SETTINGS.get())) {
            return collar.get(PettingDataComponents.COLLAR_SETTINGS.get()).combatMode();
        }
        PetData data = getPetData(entity);
        if (data == null) {
            return PettingServerConfig.getDefaultCombatMode();
        }
        return data.getCombatMode();
    }

    public static void setCombatMode(LivingEntity entity, CombatMode mode) {
        PetData data = getPetData(entity);
        if (data != null) {
            data.setCombatMode(mode);
            syncPetData(entity);
        }
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            CollarSettings current = getCollarSettings(entity);
            if (current.combatMode() != mode) {
                collar.set(PettingDataComponents.COLLAR_SETTINGS.get(), current.withCombatMode(mode));
                setCollar(entity, collar);
            }
        }
        syncHomeRestrictions(entity, getCollarSettings(entity));
    }

    public static ItemStack getCollar(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return ItemStack.EMPTY;
        }
        PetData data = getPetData(entity);
        if (data == null) {
            return ItemStack.EMPTY;
        }
        return data.getCollar();
    }

    public static void setCollar(LivingEntity entity, ItemStack collar) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        PetData data = getPetData(entity);
        if (data != null) {
            data.setCollar(collar != null ? collar : ItemStack.EMPTY);
            if (collar != null && !collar.isEmpty() && collar.has(PettingDataComponents.COLLAR_SETTINGS.get())) {
                data.setCombatMode(collar.get(PettingDataComponents.COLLAR_SETTINGS.get()).combatMode());
            }
            syncPetData(entity);
        }
    }

    public static ItemStack getSaddle(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return ItemStack.EMPTY;
        }
        if (entity instanceof AbstractHorse horse) {
            ItemStack horseSaddle = horse.getItemBySlot(EquipmentSlot.SADDLE);
            if (!horseSaddle.isEmpty()) {
                return horseSaddle;
            }
        }
        PetData data = getPetData(entity);
        if (data != null) {
            return data.getSaddle();
        }
        return ItemStack.EMPTY;
    }

    public static void setSaddle(LivingEntity entity, ItemStack saddle) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        ItemStack item = saddle != null ? saddle : ItemStack.EMPTY;
        if (entity instanceof AbstractHorse horse) {
            horse.setItemSlot(EquipmentSlot.SADDLE, item);
            horse.setDropChance(EquipmentSlot.SADDLE, 1.0F);
        }
        PetData data = getPetData(entity);
        if (data != null) {
            data.setSaddle(item);
            syncPetData(entity);
        }
    }

    public static boolean hasSaddle(LivingEntity entity) {
        return !getSaddle(entity).isEmpty();
    }

    public static boolean isRideAttackOnClick(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data == null) {
            return true;
        }
        return data.isRideAttackOnClick();
    }

    public static void setRideAttackOnClick(LivingEntity entity, boolean enabled) {
        PetData data = getPetData(entity);
        if (data != null) {
            data.setRideAttackOnClick(enabled);
            syncPetData(entity);
        }
    }

    public static boolean isRideAutoAttackHostiles(LivingEntity entity) {
        PetData data = getPetData(entity);
        if (data == null) {
            return false;
        }
        return data.isRideAutoAttackHostiles();
    }

    public static void setRideAutoAttackHostiles(LivingEntity entity, boolean enabled) {
        PetData data = getPetData(entity);
        if (data != null) {
            data.setRideAutoAttackHostiles(enabled);
            syncPetData(entity);
        }
    }

    public static CollarSettings getCollarSettings(LivingEntity entity) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return CollarSettings.fromConfig();
        }
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            if (collar.has(PettingDataComponents.COLLAR_SETTINGS.get())) {
                return collar.get(PettingDataComponents.COLLAR_SETTINGS.get());
            }
            CollarSettings defaults = CollarSettings.fromConfig();
            PetData data = getPetData(entity);
            if (data != null) {
                defaults = defaults.withCombatMode(data.getCombatMode());
            }
            collar.set(PettingDataComponents.COLLAR_SETTINGS.get(), defaults);
            setCollar(entity, collar);
            return defaults;
        }
        return CollarSettings.fromConfig();
    }

    public static void setCollarSettings(LivingEntity entity, CollarSettings settings) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }
        ItemStack collar = getCollar(entity);
        if (!collar.isEmpty()) {
            collar.set(PettingDataComponents.COLLAR_SETTINGS.get(), settings);
            setCollar(entity, collar);
        }
        setCombatMode(entity, settings.combatMode());
        syncHomeRestrictions(entity, settings);
    }

    public static double getFollowDistance(LivingEntity entity) {
        return getCollarSettings(entity).followDistance();
    }

    public static double getTeleportDistance(LivingEntity entity) {
        return getCollarSettings(entity).teleportDistance();
    }

    public static void syncHomeRestrictions(LivingEntity entity, CollarSettings settings) {
        if (entity instanceof Mob mob) {
            if (settings.combatMode() == CombatMode.WANDER || settings.combatMode() == CombatMode.GUARD) {
                int radius = Math.max(2, (int) getFollowDistance(entity));
                if (!mob.hasHome()) {
                    mob.setHomeTo(mob.blockPosition(), radius);
                } else {
                    mob.setHomeTo(mob.getHomePosition(), radius);
                }
            } else {
                if (mob.hasHome()) {
                    mob.clearHome();
                }
            }
        }
    }

    public static boolean isPetRidingEnabledFor(@Nullable LivingEntity pet) {
        if (!PettingServerConfig.isPetInventoryEnabled()) {
            return false;
        }
        if (pet == null) {
            return false;
        }
        if (pet instanceof net.minecraft.world.entity.animal.equine.AbstractHorse) {
            return false;
        }
        if (pet instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            return true;
        }
        return PettingServerConfig.isPetRidingAllowed(pet.getType());
    }

    public static void setTamed(LivingEntity entity, Player owner) {
        setTamed(entity, owner.getUUID(), owner);
    }

    public static void setTamed(LivingEntity entity, UUID ownerUUID, @Nullable Player owner) {
        PetData data = getPetData(entity);
        if (data != null) {
            data.setTamed(true);
            data.setOwnerUUID(ownerUUID);
            data.setOrder(PetOrder.FOLLOW);
            data.setCombatMode(PettingServerConfig.getDefaultCombatMode());
            if (!PettingServerConfig.isPetInventoryEnabled()) {
                data.setCollar(ItemStack.EMPTY);
                data.setSaddle(ItemStack.EMPTY);
            }
            syncPetData(entity);
        }

        if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (mob instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            mob.targetSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);

            syncEntityTameStatus(mob);
            injectGoals(mob);
        } else if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
            dragon.getPhaseManager().setPhase(net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOVERING);
        }

        if (entity instanceof Mob mob) {
            updatePetBossBarVisibility(mob, !PettingServerConfig.shouldHideTamedBossBars());
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            PetSavedData.get(serverLevel).addPet(ownerUUID, entity.getUUID());
        }
    }

    public static void syncEntityTameStatus(Mob mob) {
        if (mob == null || mob.level().isClientSide()) {
            return;
        }
        if (!isTamed(mob)) {
            return;
        }

        UUID ownerUUID = getOwnerUUID(mob).orElse(null);
        Player owner = (ownerUUID != null && mob.level() instanceof ServerLevel sl) ? sl.getPlayerByUUID(ownerUUID) : null;

        if (mob instanceof AbstractHorse horse) {
            if (!horse.isTamed()) {
                horse.setTamed(true);
            }
            if (owner != null) {
                horse.setOwner(owner);
            }
            horse.setTemper(horse.getMaxTemper());
            horse.goalSelector.getAvailableGoals().removeIf(wrapped -> wrapped.getGoal() instanceof RunAroundLikeCrazyGoal);
        }

        if (mob instanceof TamableAnimal tamable) {
            if (!tamable.isTame()) {
                tamable.setTame(true, false);
            }
            if (owner != null) {
                tamable.setOwner(owner);
            }
            boolean sit = (getOrder(mob) == PetOrder.SIT);
            tamable.setOrderedToSit(sit);
            tamable.setInSittingPose(sit);
        }

        syncModdedTame(mob, ownerUUID, owner);
        syncModdedSit(mob, getOrder(mob) == PetOrder.SIT);
    }

    public static void syncModdedSit(LivingEntity entity, boolean sit) {
        Class<?> clazz = entity.getClass();
        try {
            for (Method m : clazz.getMethods()) {
                String name = m.getName();
                if ((name.equals("setOrderedToSit") || name.equals("setInSittingPose") || name.equals("setSitting") || name.equals("sit"))
                        && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                    m.invoke(entity, sit);
                } else if (sit && name.equals("sitDown") && m.getParameterCount() == 0) {
                    m.invoke(entity);
                } else if (!sit && name.equals("standUp") && m.getParameterCount() == 0) {
                    m.invoke(entity);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void syncModdedTame(Mob mob, @Nullable UUID ownerUUID, @Nullable Player owner) {
        Class<?> clazz = mob.getClass();
        try {
            for (Method m : clazz.getMethods()) {
                String name = m.getName();
                if ((name.equals("setTamed") || name.equals("setTame")) && m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                    m.invoke(mob, true);
                } else if (name.equals("setOwner") && m.getParameterCount() == 1 && LivingEntity.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    if (owner != null) {
                        m.invoke(mob, owner);
                    }
                } else if (name.equals("setOwnerUUID") && m.getParameterCount() == 1 && m.getParameterTypes()[0] == UUID.class) {
                    if (ownerUUID != null) {
                        m.invoke(mob, ownerUUID);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void setUntamed(Mob mob) {
        if (mob.hasHome()) {
            mob.clearHome();
        }
        PetData data = getPetData(mob);
        if (data != null) {
            ItemStack collar = data.getCollar();
            if (!collar.isEmpty()) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    mob.spawnAtLocation(serverLevel, collar);
                }
                data.setCollar(ItemStack.EMPTY);
            }
            ItemStack saddle = data.getSaddle();
            if (!saddle.isEmpty()) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    mob.spawnAtLocation(serverLevel, saddle);
                }
                data.setSaddle(ItemStack.EMPTY);
            }
            data.setTamed(false);
            data.setOwnerUUID(null);
            data.setOrder(PetOrder.FOLLOW);
            data.setCombatMode(PettingServerConfig.getDefaultCombatMode());
            syncPetData(mob);
        }
        if (mob instanceof AbstractHorse horse) {
            horse.setTamed(false);
            horse.setOwner(null);
        }
        if (mob instanceof TamableAnimal tamable) {
            tamable.setTame(false, false);
            tamable.setOwner(null);
        }
        if (mob.level() instanceof ServerLevel serverLevel) {
            PetSavedData.get(serverLevel).removePet(mob.getUUID());
        }
    }

    public static int getPetCount(Player player, ServerLevel serverLevel) {
        return PetSavedData.get(serverLevel).getPetCount(player.getUUID());
    }

    public static boolean hasReachedPetLimit(Player player, ServerLevel serverLevel) {
        int maxPets = PettingServerConfig.getMaxPetCount();
        if (maxPets < 0) {
            return false;
        }
        return getPetCount(player, serverLevel) >= maxPets;
    }

    public static void purgeBuiltInTargetGoals(Mob mob) {
        if (!isTamed(mob)) {
            return;
        }
        if (PettingServerConfig.shouldDisableBuiltInHostility()) {
            mob.targetSelector.removeAllGoals(goal -> !(goal instanceof PetDefendOwnerTargetGoal)
                    && !(goal instanceof PetAttackOwnerTargetGoal)
                    && !(goal instanceof PetGuardTargetGoal));
        }
    }

    public static boolean isValidPetTarget(LivingEntity pet, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (getOrder(pet) == PetOrder.SIT || getCombatMode(pet) == CombatMode.PASSIVE) {
            return false;
        }
        Player owner = getOwner(pet);
        if (owner == null) {
            return false;
        }

        // Rule: Pets can never target their owner
        if (target == owner) {
            return false;
        }

        // Rule: Pets cannot target fellow pets of the same owner unless friendly fire is enabled
        if (!PettingServerConfig.canPetsAttackPets() && isRootOwner(target, owner)) {
            return false;
        }

        // If the pet already has this target and is alive
        if (pet instanceof Mob mob) {
            if (mob.getTarget() == target) {
                CollarSettings cs = getCollarSettings(pet);
                if (cs.combatMode() == CombatMode.GUARD && mob.hasHome()) {
                    double leashDist = mob.getHomeRadius() + 10.0D;
                    if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                        return false;
                    }
                }
                return true;
            }
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                    && mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == target) {
                CollarSettings cs = getCollarSettings(pet);
                if (cs.combatMode() == CombatMode.GUARD && mob.hasHome()) {
                    double leashDist = mob.getHomeRadius() + 10.0D;
                    if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                        return false;
                    }
                }
                return true;
            }
        }

        CollarSettings settings = getCollarSettings(pet);

        // 1. Protect owner: target attacked the owner
        if (target == owner.getLastHurtByMob()) {
            return true;
        }

        // 2. Attack for owner: owner attacked the target (Aggressive mode only)
        if (settings.combatMode() == CombatMode.AGGRESSIVE && target == owner.getLastHurtMob()) {
            return true;
        }

        // 3. Self-defense: target attacked the pet
        if (settings.retaliateWhenHurt() && target == pet.getLastHurtByMob()) {
            return true;
        }

        // 4. Defend fellow pets
        if (settings.defendOtherPets()) {
            for (LivingEntity otherPet : pet.level().getEntitiesOfClass(LivingEntity.class, pet.getBoundingBox().inflate(16.0D))) {
                if (otherPet != pet && isTamed(otherPet) && isOwner(otherPet, owner) && target == otherPet.getLastHurtByMob()) {
                    return true;
                }
            }
        }

        // 5. Guard mode: untamed hostile monsters within patrol/leash area
        if (settings.combatMode() == CombatMode.GUARD && target instanceof net.minecraft.world.entity.monster.Enemy && !isTamed(target)) {
            if (pet instanceof Mob mob && mob.hasHome()) {
                double leashDist = mob.getHomeRadius() + 10.0D;
                if (target.distanceToSqr(Vec3.atBottomCenterOf(mob.getHomePosition())) > leashDist * leashDist) {
                    return false;
                }
            }
            return true;
        }

        // 6. Aggressive mode: untamed hostile monsters
        if (settings.combatMode() == CombatMode.AGGRESSIVE && target instanceof net.minecraft.world.entity.monster.Enemy && !isTamed(target)) {
            return true;
        }

        return false;
    }

    public static boolean isFlyingPet(LivingEntity entity) {
        if (entity.isNoGravity()) {
            return true;
        }
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon) {
            return true;
        }
        if (entity instanceof Mob mob) {
            if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.FlyingPathNavigation) {
                return true;
            }
            if (mob.getMoveControl() instanceof net.minecraft.world.entity.ai.control.FlyingMoveControl) {
                return true;
            }
        }
        if (entity instanceof net.minecraft.world.entity.monster.Ghast
                || entity instanceof net.minecraft.world.entity.monster.Phantom
                || entity instanceof net.minecraft.world.entity.boss.wither.WitherBoss
                || entity instanceof net.minecraft.world.entity.monster.Blaze
                || entity instanceof net.minecraft.world.entity.monster.Vex
                || entity instanceof net.minecraft.world.entity.ambient.Bat
                || entity instanceof net.minecraft.world.entity.animal.allay.Allay
                || entity instanceof net.minecraft.world.entity.animal.bee.Bee
                || entity instanceof net.minecraft.world.entity.animal.parrot.Parrot) {
            return true;
        }
        String name = entity.getClass().getSimpleName().toLowerCase();
        return name.contains("fly") || name.contains("bird") || name.contains("dragon") || name.contains("drake");
    }

    public static boolean isSwimmingPet(LivingEntity entity) {
        if (entity instanceof net.minecraft.world.entity.animal.fish.WaterAnimal) {
            return true;
        }
        if (entity instanceof net.minecraft.world.entity.monster.Guardian) {
            return true;
        }
        if (entity instanceof Mob mob && mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation) {
            return true;
        }
        return entity.isInWater();
    }

    public static void forgiveAttacksAndClearTarget(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            mob.setLastHurtMob(null);
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
            }
            if (mob.getBrain().hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                mob.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            }
            if (mob instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            if (mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                wither.setAlternativeTarget(0, 0);
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
            }
            mob.targetSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
            mob.goalSelector.getAvailableGoals().stream()
                    .filter(w -> w.getGoal() instanceof PetMeleeAttackGoal || w.getGoal() instanceof PetRangedAttackGoal)
                    .filter(WrappedGoal::isRunning)
                    .forEach(WrappedGoal::stop);
        }
    }

    public static void injectGoals(Mob mob) {
        mob.goalSelector.getAvailableGoals().removeIf(wrappedGoal -> wrappedGoal.getGoal() instanceof RunAroundLikeCrazyGoal
                || wrappedGoal.getGoal() instanceof PetSitGoal
                || wrappedGoal.getGoal() instanceof net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal
                || wrappedGoal.getGoal() instanceof net.minecraft.world.entity.ai.goal.FollowOwnerGoal
                || wrappedGoal.getGoal() instanceof PetRangedAttackGoal
                || wrappedGoal.getGoal() instanceof PetMeleeAttackGoal
                || wrappedGoal.getGoal() instanceof PetFollowOwnerGoal
                || wrappedGoal.getGoal() instanceof PetWanderGoal);
        purgeBuiltInTargetGoals(mob);

        mob.goalSelector.addGoal(1, new PetSitGoal(mob));
        if (mob instanceof PathfinderMob pathfinderMob) {
            mob.goalSelector.addGoal(2, new PetRangedAttackGoal(pathfinderMob, 1.25D, 20, 16.0F));
            mob.goalSelector.addGoal(3, new PetMeleeAttackGoal(pathfinderMob, 1.2D, true));
            mob.goalSelector.addGoal(4, new PetFollowOwnerGoal(pathfinderMob, 1.25D, 4.0F, 2.0F, 20.0F));
            mob.goalSelector.addGoal(5, new PetWanderGoal(pathfinderMob, 1.0D));
        } else {
            mob.goalSelector.addGoal(5, new PetWanderGoal(mob, 1.0D));
        }

        boolean hasDefendGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetDefendOwnerTargetGoal);
        if (!hasDefendGoal) {
            mob.targetSelector.addGoal(1, new PetDefendOwnerTargetGoal(mob));
        }

        boolean hasAttackGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetAttackOwnerTargetGoal);
        if (!hasAttackGoal) {
            mob.targetSelector.addGoal(2, new PetAttackOwnerTargetGoal(mob));
        }

        boolean hasGuardGoal = mob.targetSelector.getAvailableGoals().stream()
                .anyMatch(wrappedGoal -> wrappedGoal.getGoal() instanceof PetGuardTargetGoal);
        if (!hasGuardGoal) {
            mob.targetSelector.addGoal(3, new PetGuardTargetGoal(mob));
        }
    }

    public static boolean isInteractionItem(LivingEntity mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (PettingServerConfig.isPetInventoryEnabled()) {
            if (stack.getItem() instanceof net.yigitguven.petting.item.CollarItem && getCollar(mob).isEmpty()) {
                return true;
            }
            if (net.minecraft.world.item.DyeColor.getColor(stack) != null) {
                ItemStack currentCollar = getCollar(mob);
                if (!currentCollar.isEmpty() && currentCollar.getItem() instanceof net.yigitguven.petting.item.CollarItem currentItem) {
                    if (currentItem.getColor() != net.minecraft.world.item.DyeColor.getColor(stack)) {
                        return true;
                    }
                }
            }
        }
        if (PetEggHelper.canTame(mob, stack.getItem())) {
            return true;
        }
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon && stack.is(net.yigitguven.petting.init.PettingModItems.GOLDEN_STAR.get())) {
            return true;
        }
        if (PettingServerConfig.isPetRidingEnabled() && stack.is(net.minecraft.world.item.Items.SADDLE) && !hasSaddle(mob)) {
            return true;
        }
        if (stack.is(net.minecraft.world.item.Items.NAME_TAG)
                || stack.is(net.minecraft.world.item.Items.LEAD)
                || stack.is(net.minecraft.world.item.Items.SHEARS)) {
            return true;
        }
        return false;
    }

    private static final java.util.Map<Class<?>, java.util.List<java.lang.reflect.Field>> BOSS_EVENT_FIELDS_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static java.lang.reflect.Field endDragonFightEventField = null;
    private static boolean endDragonFightFieldFailed = false;

    public static void updatePetBossBarVisibility(Mob mob, boolean visible) {
        if (mob == null || mob.level().isClientSide()) {
            return;
        }

        // 1. Check EnderDragon via EnderDragonFight
        if (mob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
            net.minecraft.world.level.dimension.end.EnderDragonFight fight = dragon.getDragonFight();
            if (fight != null) {
                setFightBossEventVisible(fight, visible);
            }
        }

        // 2. Generic field lookup for WitherBoss and any modded boss mob
        java.util.List<java.lang.reflect.Field> fields = BOSS_EVENT_FIELDS_CACHE.computeIfAbsent(mob.getClass(), PetHelper::findBossEventFields);
        for (java.lang.reflect.Field field : fields) {
            try {
                Object obj = field.get(mob);
                if (obj instanceof net.minecraft.server.level.ServerBossEvent bossEvent) {
                    if (bossEvent.isVisible() != visible) {
                        bossEvent.setVisible(visible);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static java.util.List<java.lang.reflect.Field> findBossEventFields(Class<?> clazz) {
        java.util.List<java.lang.reflect.Field> list = new java.util.ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Mob.class && current != Object.class) {
            for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                if (net.minecraft.server.level.ServerBossEvent.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    list.add(f);
                }
            }
            current = current.getSuperclass();
        }
        return list;
    }

    private static void setFightBossEventVisible(net.minecraft.world.level.dimension.end.EnderDragonFight fight, boolean visible) {
        if (endDragonFightFieldFailed) {
            return;
        }
        try {
            if (endDragonFightEventField == null) {
                for (java.lang.reflect.Field f : net.minecraft.world.level.dimension.end.EnderDragonFight.class.getDeclaredFields()) {
                    if (net.minecraft.server.level.ServerBossEvent.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        endDragonFightEventField = f;
                        break;
                    }
                }
            }
            if (endDragonFightEventField != null) {
                Object obj = endDragonFightEventField.get(fight);
                if (obj instanceof net.minecraft.server.level.ServerBossEvent bossEvent) {
                    if (bossEvent.isVisible() != visible) {
                        bossEvent.setVisible(visible);
                    }
                }
            }
        } catch (Exception e) {
            endDragonFightFieldFailed = true;
        }
    }

    public static boolean isBedBlock(net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) {
            return false;
        }
        return state.is(net.minecraft.tags.BlockTags.BEDS)
                || state.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                || state.getBlock().getDescriptionId().contains("bed");
    }

    public static boolean isCushionBlock(net.minecraft.world.level.block.state.BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock().getDescriptionId().contains("cushion")
                || state.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "cushions")));
    }

    public static boolean isCushionEntity(@Nullable net.minecraft.world.entity.Entity entity) {
        if (entity == null) {
            return false;
        }
        return entity instanceof net.minecraft.world.entity.decoration.Cushion
                || entity.getType().getDescriptionId().contains("cushion")
                || entity.getClass().getSimpleName().toLowerCase().contains("cushion");
    }

    public static boolean isRespawnPointValid(@Nullable ServerLevel level, @Nullable net.yigitguven.petting.data.CollarRespawnPoint respawnPoint) {
        if (level == null || respawnPoint == null) {
            return false;
        }
        net.minecraft.core.BlockPos pos = respawnPoint.pos().pos();
        if (!level.hasChunkAt(pos)) {
            level.getChunk(pos);
        }

        if (respawnPoint.isCushion()) {
            net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(pos).inflate(1.5);
            boolean hasCushionEntity = !level.getEntitiesOfClass(net.minecraft.world.entity.decoration.Cushion.class, searchBox, c -> !c.isRemoved() && c.isAlive()).isEmpty();
            if (hasCushionEntity) {
                return true;
            }
            boolean hasModdedCushion = !level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, searchBox,
                    e -> !e.isRemoved() && e.isAlive() && isCushionEntity(e)).isEmpty();
            if (hasModdedCushion) {
                return true;
            }
            return isCushionBlock(level.getBlockState(pos));
        } else {
            return isBedBlock(level.getBlockState(pos));
        }
    }

    public static boolean respawnPetAtBoundPoint(Mob mob, ServerLevel currentLevel, ServerLevel targetLevel, net.yigitguven.petting.data.CollarRespawnPoint respawnPoint) {
        net.minecraft.core.BlockPos pos = respawnPoint.pos().pos();
        Vec3 respawnPos = new Vec3(pos.getX() + 0.5, pos.getY() + (respawnPoint.isCushion() ? 0.25 : 0.6), pos.getZ() + 0.5);

        if (mob.isVehicle()) {
            mob.ejectPassengers();
        }
        if (mob.isPassenger()) {
            mob.stopRiding();
        }

        mob.setHealth(mob.getMaxHealth());
        mob.removeAllEffects();
        mob.clearFire();
        mob.fallDistance = 0.0F;
        mob.deathTime = 0;
        mob.hurtTime = 0;
        mob.setDeltaMovement(Vec3.ZERO);
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        mob.setInvulnerableTime(60);
        mob.getNavigation().stop();
        mob.syncVelocity = false;

        net.minecraft.world.entity.Entity respawnedEntity;
        if (targetLevel != currentLevel) {
            respawnedEntity = mob.teleport(new net.minecraft.world.level.portal.TeleportTransition(
                    targetLevel, respawnPos, Vec3.ZERO, mob.getYRot(), 0.0F,
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
            ));
        } else {
            currentLevel.getChunkSource().removeEntity(mob);
            respawnedEntity = mob.teleport(new net.minecraft.world.level.portal.TeleportTransition(
                    targetLevel, respawnPos, Vec3.ZERO, mob.getYRot(), 0.0F,
                    net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
            ));
            currentLevel.getChunkSource().addEntity(respawnedEntity != null ? respawnedEntity : mob);
        }

        if (respawnedEntity == null) {
            return false;
        }

        if (respawnedEntity instanceof Mob respawnedMob) {
            respawnedMob.getNavigation().stop();
            respawnedMob.setDeltaMovement(Vec3.ZERO);
            respawnedMob.syncVelocity = false;
            setOrder(respawnedMob, PetOrder.SIT);
            if (respawnedMob instanceof net.minecraft.world.entity.boss.wither.WitherBoss wither) {
                wither.setAlternativeTarget(0, 0);
                wither.setAlternativeTarget(1, 0);
                wither.setAlternativeTarget(2, 0);
            }
        }

        final java.util.UUID newUuid = respawnedEntity.getUUID();
        if (!newUuid.equals(mob.getUUID())) {
            getOwnerUUID(mob).ifPresent(ownerUuid -> {
                PetSavedData.get(currentLevel).removePet(mob.getUUID());
                PetSavedData.get(currentLevel).addPet(ownerUuid, newUuid);
            });
        }

        targetLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING, respawnPos.x, respawnPos.y + 0.5, respawnPos.z, 25, 0.4, 0.4, 0.4, 0.1);
        targetLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, respawnPos.x, respawnPos.y + 0.8, respawnPos.z, 8, 0.3, 0.3, 0.3, 0.0);
        targetLevel.playSound(null, respawnPos.x, respawnPos.y, respawnPos.z, net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);

        if (PettingServerConfig.isPetDeathMessagesEnabled()) {
            Player owner = getOwner(mob);
            if (owner != null) {
                net.minecraft.network.chat.Component targetType = net.minecraft.network.chat.Component.translatable(respawnPoint.isCushion() ? "petting.term.cushion" : "petting.term.bed");
                owner.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_respawned", mob.getName(), targetType));
            }
        }

        final net.minecraft.world.entity.Entity finalEntity = respawnedEntity;
        targetLevel.getServer().schedule(new net.minecraft.server.TickTask(targetLevel.getServer().getTickCount(), () -> {
            if (finalEntity.isAlive()) {
                finalEntity.setDeltaMovement(Vec3.ZERO);
                finalEntity.syncVelocity = false;
                if (finalEntity instanceof Mob m) {
                    m.getNavigation().stop();
                }
            }
        }));

        return true;
    }

    private static volatile long lastGoldenDragonSummonTime = 0L;

    public static void recordGoldenDragonSummon() {
        lastGoldenDragonSummonTime = System.currentTimeMillis();
    }

    public static boolean isGoldenDragonRitualActive(net.minecraft.world.level.Level level) {
        if (level.dimension() != net.minecraft.world.level.Level.END) {
            return false;
        }
        if (System.currentTimeMillis() - lastGoldenDragonSummonTime < 25000L) {
            return true;
        }
        net.minecraft.core.BlockPos.MutableBlockPos mpos = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = 60; y <= 75; y++) {
            for (int x = -6; x <= 6; x++) {
                for (int z = -6; z <= 6; z++) {
                    mpos.set(x, y, z);
                    if (level.getBlockState(mpos).is(net.yigitguven.petting.init.PettingModBlocks.GOLDEN_DRAGON_EGG.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static @Nullable Vec3 getTamedDragonFlyTarget(net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
        if (dragon.level().isClientSide()) {
            return null;
        }

        // When ridden:
        if (dragon.isVehicle()) {
            if (hasSaddle(dragon)) {
                // Saddled controlled flight: player steers via PetRidingHandler, AI waypoint is null
                return null;
            } else {
                // Unsaddled uncontrolled flight: cruise smoothly forward in direction dragon's head is facing
                Vec3 forward = Vec3.directionFromRotation(0.0F, dragon.getYRot() + 180.0F);
                return dragon.position().add(forward.scale(24.0D)).add(0.0D, 0.5D, 0.0D);
            }
        }

        PetOrder order = getOrder(dragon);
        if (order == PetOrder.SIT) {
            return null;
        }

        CollarSettings settings = getCollarSettings(dragon);
        LivingEntity combatTarget = dragon.getTarget();

        // Active combat target: fly towards or circle target
        if (combatTarget != null && combatTarget.isAlive()) {
            if (settings.preferRangedAttack()) {
                double angle = dragon.tickCount * 0.05D;
                return combatTarget.position().add(Math.cos(angle) * 14.0D, 7.0D, Math.sin(angle) * 14.0D);
            } else {
                return combatTarget.position().add(0.0D, 1.0D, 0.0D);
            }
        }

        CombatMode mode = settings.combatMode();

        // Guard mode: patrol orbit around home position
        if (mode == CombatMode.GUARD) {
            net.minecraft.core.BlockPos home = dragon.hasHome() ? dragon.getHomePosition() : dragon.blockPosition();
            double radius = dragon.hasHome() ? Math.min(24.0D, Math.max(8.0D, dragon.getHomeRadius())) : 14.0D;
            double angle = dragon.tickCount * 0.025D;
            double gx = home.getX() + 0.5D + Math.cos(angle) * radius;
            double gy = home.getY() + 7.0D + Math.sin(angle * 0.5D) * 2.0D;
            double gz = home.getZ() + 0.5D + Math.sin(angle) * radius;
            return new Vec3(gx, gy, gz);
        }

        // Wander mode: smooth wandering orbit around anchor point
        if (mode == CombatMode.WANDER || order == PetOrder.WANDER) {
            net.minecraft.core.BlockPos anchor = dragon.hasHome() ? dragon.getHomePosition() : dragon.blockPosition();
            double time = dragon.tickCount * 0.015D;
            double wanderRadius = 24.0D;
            double wx = anchor.getX() + 0.5D + Math.sin(time) * wanderRadius + Math.cos(time * 0.5D) * 12.0D;
            double wy = anchor.getY() + 8.0D + Math.sin(time * 0.7D) * 4.0D;
            double wz = anchor.getZ() + 0.5D + Math.cos(time) * wanderRadius + Math.sin(time * 0.3D) * 12.0D;
            return new Vec3(wx, wy, wz);
        }

        // Default follow owner mode
        Player owner = getOwner(dragon);
        if (owner == null || owner.isSpectator() || owner.level() != dragon.level()) {
            return null;
        }

        double dist = dragon.distanceTo(owner);
        double tpDist = getTeleportDistance(dragon);

        if (settings.teleportEnabled() && dist >= tpDist) {
            double tpX = owner.getX();
            double tpY = owner.getY() + 3.0D;
            double tpZ = owner.getZ();
            dragon.teleportTo(tpX, tpY, tpZ);
            dragon.setDeltaMovement(Vec3.ZERO);
            dragon.level().playSound(null, tpX, tpY, tpZ, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 2.0F, 1.0F);
            if (dragon.level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, tpX, tpY, tpZ, 60, 2.0D, 2.0D, 2.0D, 0.2D);
            }
            return new Vec3(tpX, tpY, tpZ);
        }

        // Orbit gently around the owner at reachable height
        double followRadius = getFollowDistance(dragon);
        double time = dragon.tickCount * 0.035D;
        double targetX = owner.getX() + Math.cos(time) * followRadius;
        double targetY = owner.getY() + 3.0D + Math.sin(time * 0.5D) * 1.5D;
        double targetZ = owner.getZ() + Math.sin(time) * followRadius;

        return new Vec3(targetX, targetY, targetZ);
    }

    public static void tickTamedDragon(net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon) {
        if (dragon.level().isClientSide() || !(dragon.level() instanceof ServerLevel sl)) {
            return;
        }

        // Sitting or ridden: clear combat target
        if (dragon.isVehicle() || getOrder(dragon) == PetOrder.SIT) {
            if (dragon.getTarget() != null) {
                dragon.setTarget(null);
            }
            return;
        }

        CollarSettings settings = getCollarSettings(dragon);
        CombatMode combatMode = settings.combatMode();
        Player owner = getOwner(dragon);

        // 1. Validate current target
        LivingEntity target = dragon.getTarget();
        if (target != null) {
            if (!target.isAlive() || target.isRemoved() || !isValidPetTarget(dragon, target)) {
                dragon.setTarget(null);
                target = null;
            } else if (dragon.distanceToSqr(target) > 64.0D * 64.0D) {
                dragon.setTarget(null);
                target = null;
            } else if (combatMode == CombatMode.GUARD && dragon.hasHome()) {
                double leash = dragon.getHomeRadius() + 16.0D;
                if (target.distanceToSqr(Vec3.atBottomCenterOf(dragon.getHomePosition())) > leash * leash) {
                    dragon.setTarget(null);
                    target = null;
                }
            }
        }

        // 2. Select target if none exists and not passive
        if (target == null && combatMode != CombatMode.PASSIVE) {
            // Retaliate if dragon was hurt
            if (settings.retaliateWhenHurt()) {
                LivingEntity lastHurtBy = dragon.getLastHurtByMob();
                if (lastHurtBy != null && lastHurtBy.isAlive() && isValidPetTarget(dragon, lastHurtBy)) {
                    dragon.setTarget(lastHurtBy);
                    target = lastHurtBy;
                }
            }

            // Defend owner if owner was hurt
            if (target == null && owner != null) {
                LivingEntity ownerHurtBy = owner.getLastHurtByMob();
                if (ownerHurtBy != null && ownerHurtBy.isAlive() && isValidPetTarget(dragon, ownerHurtBy)) {
                    dragon.setTarget(ownerHurtBy);
                    target = ownerHurtBy;
                }
            }

            // Aggressive mode: target entity owner attacked
            if (target == null && owner != null && combatMode == CombatMode.AGGRESSIVE) {
                LivingEntity ownerTarget = owner.getLastHurtMob();
                if (ownerTarget != null && ownerTarget.isAlive() && isValidPetTarget(dragon, ownerTarget)) {
                    dragon.setTarget(ownerTarget);
                    target = ownerTarget;
                }
            }

            // Guard mode: untamed hostiles within guard radius
            if (target == null && combatMode == CombatMode.GUARD) {
                net.minecraft.core.BlockPos home = dragon.hasHome() ? dragon.getHomePosition() : dragon.blockPosition();
                int radius = dragon.hasHome() ? dragon.getHomeRadius() : 16;
                net.minecraft.world.phys.AABB guardArea = new net.minecraft.world.phys.AABB(home).inflate(radius);
                java.util.List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, guardArea,
                        e -> e.isAlive() && e instanceof net.minecraft.world.entity.monster.Enemy && isValidPetTarget(dragon, e));
                if (!enemies.isEmpty()) {
                    enemies.sort(java.util.Comparator.comparingDouble(dragon::distanceToSqr));
                    dragon.setTarget(enemies.get(0));
                    target = enemies.get(0);
                }
            }

            // Aggressive mode: untamed hostiles within 32 blocks
            if (target == null && combatMode == CombatMode.AGGRESSIVE) {
                net.minecraft.world.phys.AABB searchArea = dragon.getBoundingBox().inflate(32.0D);
                java.util.List<LivingEntity> enemies = sl.getEntitiesOfClass(LivingEntity.class, searchArea,
                        e -> e.isAlive() && e instanceof net.minecraft.world.entity.monster.Enemy && isValidPetTarget(dragon, e));
                if (!enemies.isEmpty()) {
                    enemies.sort(java.util.Comparator.comparingDouble(dragon::distanceToSqr));
                    dragon.setTarget(enemies.get(0));
                    target = enemies.get(0);
                }
            }
        }

        // 3. Combat actions when target is active
        if (target != null) {
            double dist = dragon.distanceTo(target);

            // Ranged fireball attack: every 40 ticks when ranged preferred or dist > 8.0D
            if ((settings.preferRangedAttack() || dist > 8.0D) && dist <= 48.0D) {
                if (dragon.tickCount % 40 == 0) {
                    Vec3 headPos = dragon.head.position();
                    Vec3 aim = target.getEyePosition().subtract(headPos).normalize();
                    net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball fireball =
                            new net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball(sl, dragon, aim);
                    fireball.setPos(headPos.x + aim.x * 2.5D, headPos.y + aim.y * 2.5D, headPos.z + aim.z * 2.5D);
                    sl.addFreshEntity(fireball);
                    sl.playSound(null, headPos.x, headPos.y, headPos.z, net.minecraft.sounds.SoundEvents.ENDER_DRAGON_SHOOT, net.minecraft.sounds.SoundSource.HOSTILE, 2.0F, 1.0F);
                }
            }

            // Melee bite attack: every 20 ticks when in melee reach
            if (dist <= 8.5D) {
                if (dragon.tickCount % 20 == 0) {
                    target.hurt(sl.damageSources().mobAttack(dragon), 14.0F);
                    Vec3 knock = target.position().subtract(dragon.position()).normalize().scale(1.2D).add(0.0D, 0.35D, 0.0D);
                    target.setDeltaMovement(target.getDeltaMovement().add(knock));
                    sl.playSound(null, target.getX(), target.getY(), target.getZ(), net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 1.0F);
                }
            }
        }
    }

    public static void alertTamedDragonsOfThreat(Player owner, LivingEntity threat) {
        if (owner.level().isClientSide() || threat == null || !threat.isAlive()) return;
        net.minecraft.world.phys.AABB box = owner.getBoundingBox().inflate(64.0D);
        for (net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon : owner.level().getEntitiesOfClass(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, box)) {
            if (isTamed(dragon) && isOwner(dragon, owner)) {
                CollarSettings cs = getCollarSettings(dragon);
                if (cs.combatMode() == CombatMode.DEFENSIVE || cs.combatMode() == CombatMode.AGGRESSIVE || cs.combatMode() == CombatMode.GUARD) {
                    if (isValidPetTarget(dragon, threat)) {
                        dragon.setTarget(threat);
                    }
                }
            }
        }
    }

    public static void alertTamedDragonsOfTarget(Player owner, LivingEntity target) {
        if (owner.level().isClientSide() || target == null || !target.isAlive()) return;
        net.minecraft.world.phys.AABB box = owner.getBoundingBox().inflate(64.0D);
        for (net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon : owner.level().getEntitiesOfClass(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class, box)) {
            if (isTamed(dragon) && isOwner(dragon, owner)) {
                CollarSettings cs = getCollarSettings(dragon);
                if (cs.combatMode() == CombatMode.AGGRESSIVE) {
                    if (isValidPetTarget(dragon, target)) {
                        dragon.setTarget(target);
                    }
                }
            }
        }
    }

    public static net.minecraft.world.InteractionResult handleTamedDragonInteract(
            net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon,
            Player player,
            net.minecraft.world.InteractionHand hand
    ) {
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        if (!isOwner(dragon, player)) {
            if (!player.level().isClientSide()) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.already_tamed_other"));
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);

        // Shift-Right Click: Open Pet Inventory
        if (player.isShiftKeyDown()) {
            if (!player.level().isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                net.yigitguven.petting.inventory.PetInventoryMenu.open(serverPlayer, dragon);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // Equip Collar
        if (PettingServerConfig.isPetInventoryEnabled() && PettingServerConfig.isCollarSlotEnabled() && stack.getItem() instanceof net.yigitguven.petting.item.CollarItem) {
            if (getCollar(dragon).isEmpty()) {
                if (!player.level().isClientSide()) {
                    setCollar(dragon, stack.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    dragon.level().playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(), net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_LEATHER, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        // Dye Collar
        net.minecraft.world.item.DyeColor dyeColor = net.minecraft.world.item.DyeColor.getColor(stack);
        if (dyeColor != null) {
            ItemStack currentCollar = getCollar(dragon);
            if (!currentCollar.isEmpty() && currentCollar.getItem() instanceof net.yigitguven.petting.item.CollarItem currentItem) {
                if (currentItem.getColor() != dyeColor) {
                    if (!player.level().isClientSide()) {
                        net.minecraft.world.item.Item newCollarItem = net.yigitguven.petting.init.PettingModItems.getCollar(dyeColor).get();
                        ItemStack newCollar = new ItemStack(newCollarItem);
                        newCollar.applyComponents(currentCollar.getComponents());
                        setCollar(dragon, newCollar);
                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }
                        dragon.level().playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(), net.minecraft.sounds.SoundEvents.DYE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        // Heal with Golden Star or Treats
        if (PetEggHelper.canTame(dragon, stack.getItem()) || stack.is(net.yigitguven.petting.init.PettingModItems.GOLDEN_STAR.get())) {
            if (dragon.getHealth() < dragon.getMaxHealth()) {
                if (!player.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    dragon.heal(dragon.getMaxHealth() * 0.25F);
                    if (dragon.level() instanceof ServerLevel sl) {
                        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, dragon.getX(), dragon.getY() + 3.0D, dragon.getZ(), 15, 1.5D, 1.0D, 1.5D, 0.1D);
                    }
                    dragon.level().playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_healed", dragon.getName()));
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            } else {
                if (!player.level().isClientSide()) {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_full_health", dragon.getName()));
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        // Equip Saddle
        if (stack.is(net.minecraft.world.item.Items.SADDLE) && !hasSaddle(dragon)) {
            if (!player.level().isClientSide()) {
                setSaddle(dragon, stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                dragon.level().playSound(null, dragon.getX(), dragon.getY(), dragon.getZ(), net.minecraft.sounds.SoundEvents.HORSE_SADDLE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        // If player is actively riding this dragon, right-click must never toggle sit
        if (dragon.hasPassenger(player) || player.getVehicle() == dragon) {
            return net.minecraft.world.InteractionResult.PASS;
        }

        // Toggle Sit / Follow (Riding is strictly performed via Keybind R / MountPetPayload)
        if (PettingServerConfig.isPetSittingEnabled()) {
            if (!player.level().isClientSide()) {
                toggleSit(dragon, player);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }

        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    private static final java.util.Map<UUID, Long> SIT_TOGGLE_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean toggleSit(LivingEntity mob, Player player) {
        if (!PettingServerConfig.isPetSittingEnabled()) {
            return false;
        }
        if (mob.isVehicle() || mob.hasPassenger(player) || player.getVehicle() == mob || mob.isPassenger()) {
            return false;
        }
        long now = System.currentTimeMillis();
        Long last = SIT_TOGGLE_COOLDOWNS.get(mob.getUUID());
        if (last != null && now - last < 350L) {
            return false;
        }
        SIT_TOGGLE_COOLDOWNS.put(mob.getUUID(), now);

        PetOrder currentOrder = getOrder(mob);
        PetOrder newOrder = (currentOrder == PetOrder.SIT) ? PetOrder.FOLLOW : PetOrder.SIT;
        setOrder(mob, newOrder);

        if (newOrder == PetOrder.SIT) {
            if (PettingServerConfig.shouldForgiveAttacksOnSit()) {
                forgiveAttacksAndClearTarget(mob);
            }
            boolean hoverFlying = PettingServerConfig.isFlyingPetsHoverWhenSitting() && isFlyingPet(mob);
            boolean stillSwimming = PettingServerConfig.isSwimmingPetsStayStillWhenSitting() && isSwimmingPet(mob);
            if (hoverFlying || stillSwimming) {
                mob.setNoGravity(true);
                mob.setDeltaMovement(Vec3.ZERO);
                mob.fallDistance = 0.0F;
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_sitting", mob.getName()));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("petting.action.pet_standing", mob.getName()));
        }
        return true;
    }

    public static boolean doPetHurtTarget(Mob mob, ServerLevel level, net.minecraft.world.entity.Entity target) {
        if (mob.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
            return mob.doHurtTarget(level, target);
        }
        DamageSource damageSource = mob.damageSources().mobAttack(mob);
        boolean wasHurt = target.hurtServer(level, damageSource, 2.0F);
        if (wasHurt) {
            mob.setLastHurtMob(target);
        }
        return wasHurt;
    }
}
