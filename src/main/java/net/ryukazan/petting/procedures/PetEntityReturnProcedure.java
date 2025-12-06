package net.ryukazan.petting.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.UUID;
import java.util.List;
import java.util.Comparator;

public class PetEntityReturnProcedure {

    /**
     * GUI Entry Point: Called by the GUI Screen (Client Side)
     */
    public static Entity execute() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return execute(Minecraft.getInstance().level, Minecraft.getInstance().player);
        }
        return null;
    }

    /**
     * Main Logic
     */
    public static Entity execute(LevelAccessor world, Entity entity) {
        if (world == null || entity == null)
            return null;

        // =============================================================
        // STRATEGY A: CLIENT SIDE (The GUI Context)
        // Since NBT doesn't sync, we look for what the player is pointing at.
        // =============================================================
        if (world.isClientSide()) {
            
            // 1. Raytrace Check: Is the crosshair directly on an entity?
            HitResult result = Minecraft.getInstance().hitResult;
            if (result instanceof EntityHitResult entityHit) {
                // We found the entity directly under the mouse cursor
                return entityHit.getEntity();
            }

            // 2. Proximity Fallback: If crosshair missed slightly, find closest entity within 4 blocks
            // (Excluding the player themselves)
            AABB searchArea = entity.getBoundingBox().inflate(4.0);
            List<Entity> nearbyEntities = world.getEntitiesOfClass(Entity.class, searchArea);
            
            Entity closestEntity = null;
            double closestDist = Double.MAX_VALUE;

            for (Entity e : nearbyEntities) {
                if (e == entity) continue; // Skip the player
                
                double d = e.distanceToSqr(entity);
                if (d < closestDist) {
                    closestDist = d;
                    closestEntity = e;
                }
            }

            return closestEntity; // Returns the closest pet, or null if nothing nearby
        }

        // =============================================================
        // STRATEGY B: SERVER SIDE (Logic Context)
        // If this is called by server procedures, NBT *does* work here.
        // =============================================================
        if (world instanceof ServerLevel _level) {
            String configUUID = entity.getPersistentData().getStringOr("configUUID", "");
            if (!configUUID.isEmpty()) {
                try {
                    UUID id = UUID.fromString(configUUID);
                    return _level.getEntity(id);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        return null;
    }
}