package net.yigitguven.petting.procedures;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

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

import java.util.UUID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class PetDeathHandlerProcedure {

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event == null || event.getEntity() == null) return;

        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        
        if (!(entity instanceof Mob mob)) return;

        CompoundTag data = mob.getPersistentData();
        
        if (!data.getBoolean("pettingtamed")) {
            return;
        }

        String ownerUUIDStr = data.getString("ownerUUID");
        if (ownerUUIDStr.isEmpty()) return;

        // 3. CHECK FOR PET BED
        if (data.contains("pet_bed_loc_x") && data.contains("pet_bed_loc_y") && data.contains("pet_bed_loc_z")) {
            
            double bedX = data.getDouble("pet_bed_loc_x");
            double bedY = data.getDouble("pet_bed_loc_y");
            double bedZ = data.getDouble("pet_bed_loc_z");

            BlockPos bedPos = new BlockPos((int)bedX, (int)bedY, (int)bedZ);
            BlockPos respawnPos = findSafeRespawnLocation(entity.level(), bedPos);

            if (respawnPos != null) {
                // A. Cancel Death
                event.setCanceled(true);

                // B. Heal Fully
                mob.setHealth(mob.getMaxHealth());
                mob.removeAllEffects();

                // Reset bee stinger state via NBT so it doesn't re-enter the death loop.
                // setHasStung() is private, but vanilla reads "HasStung" from NBT via
                // readAdditionalSaveData, so we patch that and reload just that tag.
                if (mob instanceof net.minecraft.world.entity.animal.Bee) {
                    net.minecraft.nbt.CompoundTag beeNbt = new net.minecraft.nbt.CompoundTag();
                    mob.saveWithoutId(beeNbt);
                    beeNbt.putBoolean("HasStung", false);
                    mob.readAdditionalSaveData(beeNbt);
                }

                // C. Teleport to Bed
                mob.teleportTo(respawnPos.getX() + 0.5, respawnPos.getY(), respawnPos.getZ() + 0.5);
                
                // D. Force Sit
                data.putBoolean("sitstill", true);
                mob.getNavigation().stop();
                mob.setDeltaMovement(0, 0, 0);

                // E. Notify Owner
                notifyOwner(entity.level(), ownerUUIDStr, 
                    Component.literal("§a[Petting] §fYour pet " + mob.getDisplayName().getString() + " was saved by its bed!"));

                // F. Effects at Bed
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, 
                        respawnPos.getX() + 0.5, respawnPos.getY() + 0.5, respawnPos.getZ() + 0.5, 
                        15, 0.3, 0.3, 0.3, 0.05);
                    serverLevel.playSound(null, respawnPos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.NEUTRAL, 1.0f, 1.0f);
                }

                return; 
            } else {
                notifyOwner(entity.level(), ownerUUIDStr, 
                    Component.literal("§c[Petting] Your pet tried to respawn at its bed, but the location was blocked!"));
            }
        }

        // 4. STANDARD DEATH (No Bed / Blocked Bed)
        try {
            Component deathMessage = entity.getCombatTracker().getDeathMessage();
            notifyOwner(entity.level(), ownerUUIDStr, deathMessage);
        } catch (Exception e) {
            System.err.println("Petting: Invalid Owner UUID");
        }
    }

    private static void notifyOwner(Level level, String uuidStr, Component message) {
        if (level instanceof ServerLevel serverLevel) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (owner != null) {
                    owner.sendSystemMessage(message);
                }
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
                    if (isSafePos(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null; 
    }

    /**
     * UPDATED FOR 1.20.1: Removed getMaterial() checks
     */
    private static boolean isSafePos(Level level, BlockPos pos) {
        // 1. Ground Check
        // Must be solid OR a liquid (so aquatic pets don't die instantly, though logic is imperfect for fish)
        BlockState ground = level.getBlockState(pos.below());
        boolean isGroundSolid = ground.isSolid(); // Basic check
        boolean isLiquid = !ground.getFluidState().isEmpty();
        
        if (!isGroundSolid && !isLiquid) return false; 

        // 2. Legs Space Check
        // Must NOT have a collision shape (so air, grass, flowers are fine)
        BlockState legs = level.getBlockState(pos);
        if (!legs.getCollisionShape(level, pos).isEmpty()) return false;

        // 3. Head Space Check
        BlockState head = level.getBlockState(pos.above());
        if (!head.getCollisionShape(level, pos.above()).isEmpty()) return false;

        return true;
    }
}



