package net.yigitguven.petting.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public class EntitySettingsServer {
    public static void handleOpenRequest(ServerPlayer player, int entityId) {
        Entity entity = null;
        
        if (entityId != -1) {
            entity = player.level().getEntity(entityId);
            if (entity != null && !isOwner(entity, player)) {
                entity = null; // Target is not owned by player, fall back to nearest
            }
        }

        if (entity == null) {
            // Find nearest owned pet within 10 blocks
            double closestDist = Double.MAX_VALUE;
            net.minecraft.world.phys.AABB searchBox = player.getBoundingBox().inflate(10.0);
            for (net.minecraft.world.entity.LivingEntity le : player.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, searchBox)) {
                if (isOwner(le, player)) {
                    double dist = player.distanceToSqr(le);
                    if (dist < closestDist) {
                        closestDist = dist;
                        entity = le;
                    }
                }
            }
        }

        if (entity == null) return;

        CompoundTag data = ((net.yigitguven.petting.IEntityData)entity).getPersistentData();
        boolean sitstill = data.getBoolean("sitstill");
        boolean waiting = data.getBoolean("waiting");
        boolean isTamed = data.getBoolean("pettingtamed");
        boolean attackIfOwnerAttacks = data.getBoolean("attackifownerattacks");
        boolean attackIfOwnerAttacked = data.getBoolean("attackifownerattacked");
        boolean attackIfSelfAttacked = data.getBoolean("attackifselfattacked");
        boolean damageOwner = data.getBoolean("damageOwner");
        boolean ignoreWhistle = data.getBoolean("ignoreWhistle");
        String defaultRight = ((net.yigitguven.petting.IEntityData)player).getPersistentData().contains("petting_default_control_right_click") ? ((net.yigitguven.petting.IEntityData)player).getPersistentData().getString("petting_default_control_right_click") : "RIDE|SADDLE;SIT|NONE";
        String defaultShift = ((net.yigitguven.petting.IEntityData)player).getPersistentData().contains("petting_default_control_shift_right_click") ? ((net.yigitguven.petting.IEntityData)player).getPersistentData().getString("petting_default_control_shift_right_click") : "OPEN_INV|NONE";
        String controlRightClick = data.contains("control_right_click") ? data.getString("control_right_click") : defaultRight;
        String controlShiftRightClick = data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : defaultShift;
        if (!data.contains("control_right_click")) data.putString("control_right_click", controlRightClick);
        if (!data.contains("control_shift_right_click")) data.putString("control_shift_right_click", controlShiftRightClick);
        int followDistance = data.contains("followdistance") ? data.getInt("followdistance") : (int)net.yigitguven.petting.config.PettingConfig.followDistance;
        int teleportDistance = data.contains("teleportdistance") ? data.getInt("teleportdistance") : (int)net.yigitguven.petting.config.PettingConfig.teleportDistance;

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        buf.writeBoolean(sitstill);
        buf.writeBoolean(waiting);
        buf.writeBoolean(isTamed);
        buf.writeBoolean(attackIfOwnerAttacks);
        buf.writeBoolean(attackIfOwnerAttacked);
        buf.writeBoolean(attackIfSelfAttacked);
        buf.writeBoolean(damageOwner);
        buf.writeBoolean(ignoreWhistle);
        buf.writeInt(followDistance);
        buf.writeInt(teleportDistance);
        buf.writeBoolean(true); // openScreen
        buf.writeUtf(controlRightClick);
        buf.writeUtf(controlShiftRightClick);

        ServerPlayNetworking.send(player, new net.minecraft.resources.ResourceLocation("petting", "send_pet_settings"), buf);
    }

    public static void saveControlDefaults(ServerPlayer player, String rightClick, String shiftRightClick) {
        if (player == null) return;
        CompoundTag data = ((net.yigitguven.petting.IEntityData)player).getPersistentData();
        if (rightClick != null && !rightClick.isBlank()) {
            String normalized = normalizeMapping(rightClick, "RIDE|SADDLE;SIT|NONE");
            if (normalized != null) data.putString("petting_default_control_right_click", normalized);
        }
        if (shiftRightClick != null && !shiftRightClick.isBlank()) {
            String normalized = normalizeMapping(shiftRightClick, "OPEN_INV|NONE");
            if (normalized != null) data.putString("petting_default_control_shift_right_click", normalized);
        }
    }

    public static void applyUpdate(ServerPlayer player, int entityId, String key, String value) {
        if ("default_control_right_click".equals(key) || "default_control_shift_right_click".equals(key)) {
            saveControlDefaults(player, "default_control_right_click".equals(key) ? value : null, "default_control_shift_right_click".equals(key) ? value : null);
            return;
        }
        Entity entity = player.level().getEntity(entityId);
        if (entity == null) return;
        if (!isOwner(entity, player)) return;
        CompoundTag data = ((net.yigitguven.petting.IEntityData)entity).getPersistentData();

        java.util.Set<String> booleanKeys = java.util.Set.of(
                "sitstill", "waiting", "attackifownerattacks", "attackifownerattacked",
                "attackifselfattacked", "damageOwner", "ignoreWhistle"
        );
        java.util.Set<String> intKeys = java.util.Set.of("followdistance", "teleportdistance");
        java.util.Set<String> controlKeys = java.util.Set.of("control_right_click", "control_shift_right_click");

        if (booleanKeys.contains(key)) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                data.putBoolean(key, Boolean.parseBoolean(value));
            } else {
                return;
            }
        } else if (intKeys.contains(key)) {
            try {
                int iv = Integer.parseInt(value);
                if (key.equals("followdistance")) {
                    int min = 1;
                    int max = 100;
                    if (iv < min) iv = min;
                    if (iv > max) iv = max;
                    data.putInt("followdistance", iv);
                } else if (key.equals("teleportdistance")) {
                    int min = 1;
                    int max = 100;
                    iv = Math.max(min, Math.min(max, iv));
                    data.putInt(key, iv);
                }
            } catch (NumberFormatException nfe) {
                return;
            }
        } else if (controlKeys.contains(key)) {
            String fallback = key.equals("control_right_click") ? "RIDE|SADDLE;SIT|NONE" : "OPEN_INV|NONE";
            String normalized = normalizeMapping(value, fallback);
            if (normalized == null) return;
            data.putString(key, normalized);
        } else {
            return;
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        buf.writeBoolean(data.getBoolean("sitstill"));
        buf.writeBoolean(data.getBoolean("waiting"));
        buf.writeBoolean(data.getBoolean("pettingtamed"));
        buf.writeBoolean(data.getBoolean("attackifownerattacks"));
        buf.writeBoolean(data.getBoolean("attackifownerattacked"));
        buf.writeBoolean(data.getBoolean("attackifselfattacked"));
        buf.writeBoolean(data.getBoolean("damageOwner"));
        buf.writeBoolean(data.getBoolean("ignoreWhistle"));
        buf.writeInt(data.contains("followdistance") ? data.getInt("followdistance") : (int)net.yigitguven.petting.config.PettingConfig.followDistance);
        buf.writeInt(data.contains("teleportdistance") ? data.getInt("teleportdistance") : (int)net.yigitguven.petting.config.PettingConfig.teleportDistance);
        buf.writeBoolean(false); // openScreen
        buf.writeUtf(data.contains("control_right_click") ? data.getString("control_right_click") : "RIDE|SADDLE;SIT|NONE");
        buf.writeUtf(data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : "OPEN_INV|NONE");

        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(trackingPlayer, new net.minecraft.resources.ResourceLocation("petting", "send_pet_settings"), buf);
        }
        if (!PlayerLookup.tracking(entity).contains(player)) {
            ServerPlayNetworking.send(player, new net.minecraft.resources.ResourceLocation("petting", "send_pet_settings"), buf);
        }
    }

    private static boolean isOwner(Entity entity, ServerPlayer player) {
        if (entity == null || player == null) return false;
        CompoundTag data = ((net.yigitguven.petting.IEntityData)entity).getPersistentData();
        if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(player.getStringUUID())) return true;
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }
        return false;
    }

    private static String normalizeMapping(String value, String fallbackAction) {
        if (value == null || value.isBlank()) return fallbackAction.contains("|") ? fallbackAction : (fallbackAction + "|NONE");
        java.util.Set<String> allowedActions = java.util.Set.of("SIT", "CYCLE", "RIDE", "OPEN_INV", "TOGGLE_WAIT", "TOGGLE_FOLLOW_TELEPORT", "OPEN_SETTINGS", "RUN_COMMAND", "NONE");
        java.util.Set<String> allowedConditions = java.util.Set.of("NONE", "SADDLE", "SNEAK", "HEALTH_LT_50", "HOLD_ITEM");
        java.util.List<String> out = new java.util.ArrayList<>();
        String[] rawRules = value.split(";");
        for (String rawRule : rawRules) {
            String rule = rawRule.trim();
            if (rule.isEmpty()) continue;
            String action;
            String condition = "NONE";
            String command = "";
            if (rule.contains("|")) {
                String[] parts = rule.split("\\|", 3);
                action = parts[0].trim();
                if (parts.length > 1 && !parts[1].isBlank()) condition = parts[1].trim();
                if (parts.length > 2) command = parts[2];
            } else {
                action = rule;
            }
            if (!allowedActions.contains(action) || !allowedConditions.contains(condition)) return null;
            String normalized = action + "|" + condition;
            if ("RUN_COMMAND".equals(action) && !command.isBlank()) {
                command = command.replaceAll("[\\p{Cntrl}]", "").trim();
                if (command.length() > 256) command = command.substring(0, 256);
                if (!command.isEmpty()) normalized += "|" + command;
            }
            out.add(normalized);
        }
        if (out.isEmpty()) out.add(fallbackAction.contains("|") ? fallbackAction : (fallbackAction + "|NONE"));
        return String.join(";", out);
    }
}
