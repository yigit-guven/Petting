package net.yigitguven.petting.procedures;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.PetAttackLogic;

import java.util.UUID;

public class PetDeathHandlerProcedure {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity.level().isClientSide() || !(entity instanceof Mob mob)) return true;
            if (!PetAttackLogic.isCustomPet(mob)) return true;

            CompoundTag data = ((IEntityData) mob).getPersistentData();
            String ownerUUIDStr = data.getString("ownerUUID");
            if (ownerUUIDStr.isEmpty()) return true;

            // CHECK FOR PET BED
            if (data.contains("pet_bed_loc_x") && data.contains("pet_bed_loc_y") && data.contains("pet_bed_loc_z")) {
                double bedX = data.getDouble("pet_bed_loc_x");
                double bedY = data.getDouble("pet_bed_loc_y");
                double bedZ = data.getDouble("pet_bed_loc_z");
                String bedDimStr = data.contains("pet_bed_loc_dim") ? data.getString("pet_bed_loc_dim") : entity.level().dimension().location().toString();

                ServerLevel targetLevel = entity.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(bedDimStr)));
                if (targetLevel == null) targetLevel = (ServerLevel)entity.level();

                BlockPos bedPos = new BlockPos((int)bedX, (int)bedY, (int)bedZ);
                BlockPos respawnPos = findSafeRespawnLocation(targetLevel, bedPos);

                if (respawnPos != null) {
                    // Heal and Teleport
                    mob.setHealth(mob.getMaxHealth());
                    mob.removeAllEffects(); 
                    if (targetLevel != entity.level()) {
                        FabricDimensions.teleport(mob, targetLevel, new PortalInfo(new Vec3(respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5), Vec3.ZERO, mob.getYRot(), mob.getXRot()));
                    } else {
                        mob.teleportTo(respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5);
                    }
                    data.putBoolean("sitstill", true);
                    mob.getNavigation().stop();
                    mob.setDeltaMovement(0, 0, 0);

                    notifyOwner(targetLevel, ownerUUIDStr, 
                        Component.literal("§a[Petting] §fYour pet " + mob.getDisplayName().getString() + " was saved by its bed!"));

                    targetLevel.sendParticles(ParticleTypes.POOF, 
                        respawnPos.getX() + 0.5, respawnPos.getY() + 0.5, respawnPos.getZ() + 0.5, 
                        15, 0.3, 0.3, 0.3, 0.05);
                    targetLevel.playSound(null, respawnPos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                    
                    return false; // Cancel death
                } else {
                    notifyOwner(entity.level(), ownerUUIDStr, 
                        Component.literal("§c[Petting] Your pet tried to respawn at its bed, but the location was blocked!"));
                }
            }

            // STANDARD DEATH notification
            notifyOwner(entity.level(), ownerUUIDStr, entity.getCombatTracker().getDeathMessage());
            return true;
        });
    }

    private static void notifyOwner(Level level, String uuidStr, Component message) {
        if (level instanceof ServerLevel serverLevel) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (owner != null) owner.sendSystemMessage(message);
            } catch (Exception ignored) {}
        }
    }

    private static BlockPos findSafeRespawnLocation(Level level, BlockPos center) {
        if (isSafePos(level, center)) return center;
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) { 
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos candidate = center.offset(x, y, z);
                    if (isSafePos(level, candidate)) return candidate;
                }
            }
        }
        return null; 
    }

    private static boolean isSafePos(Level level, BlockPos pos) {
        // 1. Ground Check
        // Must be solid OR a liquid (so aquatic pets don't die instantly)
        BlockState ground = level.getBlockState(pos.below());
        boolean isGroundSolid = ground.isSolid(); 
        boolean isLiquid = !ground.getFluidState().isEmpty();
        
        if (!isGroundSolid && !isLiquid) return false; 

        // 2. Legs Space Check
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) return false;

        // 3. Head Space Check
        if (!level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()) return false;

        return true;
    }
}
