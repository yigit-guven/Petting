package net.yigitguven.petting.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;

public class EntitySettingsServer {
    public static void handleOpenRequest(ServerPlayer player, int entityId) {
        Entity entity = player.level().getEntity(entityId);
        if (entity == null) return;

        if (!isOwner(entity, player)) return;

        CompoundTag data = entity.getPersistentData();
        boolean sitstill = data.getBoolean("sitstill");
        boolean waiting = data.getBoolean("waiting");
        boolean isTamed = data.getBoolean("pettingtamed");
        boolean attackIfOwnerAttacks = data.getBoolean("attackifownerattacks");
        boolean attackIfOwnerAttacked = data.getBoolean("attackifownerattacked");
        boolean attackIfSelfAttacked = data.getBoolean("attackifselfattacked");
        boolean damageOwner = data.getBoolean("damageOwner");
        boolean ignoreWhistle = data.getBoolean("ignoreWhistle");
        String defaultRight = player.getPersistentData().contains("petting_default_control_right_click") ? player.getPersistentData().getString("petting_default_control_right_click") : "SIT";
        String defaultShift = player.getPersistentData().contains("petting_default_control_shift_right_click") ? player.getPersistentData().getString("petting_default_control_shift_right_click") : "CYCLE";
        String controlRightClick = data.contains("control_right_click") ? data.getString("control_right_click") : defaultRight;
        String controlShiftRightClick = data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : defaultShift;
        if (!data.contains("control_right_click")) data.putString("control_right_click", controlRightClick);
        if (!data.contains("control_shift_right_click")) data.putString("control_shift_right_click", controlShiftRightClick);
        int followDistance = data.contains("followdistance") ? data.getInt("followdistance") : (int)net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE.get().doubleValue();
        int teleportDistance = data.contains("teleportdistance") ? data.getInt("teleportdistance") : (int)net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE.get().doubleValue();

        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
            new SendPetSettingsPayload(entityId, sitstill, waiting, isTamed, attackIfOwnerAttacks, attackIfOwnerAttacked, attackIfSelfAttacked, damageOwner, ignoreWhistle, followDistance, teleportDistance, true, controlRightClick, controlShiftRightClick));
    }

    public static void saveControlDefaults(ServerPlayer player, String rightClick, String shiftRightClick) {
        if (player == null) return;
        CompoundTag data = player.getPersistentData();
        if (rightClick != null && !rightClick.isBlank()) {
            String normalized = normalizeMapping(rightClick, "SIT");
            if (normalized != null) data.putString("petting_default_control_right_click", normalized);
        }
        if (shiftRightClick != null && !shiftRightClick.isBlank()) {
            String normalized = normalizeMapping(shiftRightClick, "CYCLE");
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
        CompoundTag data = entity.getPersistentData();

        // Allow-list of editable per-pet settings
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
                return; // invalid boolean value
            }
        } else if (intKeys.contains(key)) {
            try {
                int iv = Integer.parseInt(value);
                // validate ranges using config defaults/ranges
                if (key.equals("followdistance")) {
                    int min = net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE_MIN;
                    int max = net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE_MAX;
                    iv = Math.max(min, Math.min(max, iv));
                    data.putInt(key, iv);
                } else if (key.equals("teleportdistance")) {
                    int min = net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE_MIN;
                    int max = net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE_MAX;
                    iv = Math.max(min, Math.min(max, iv));
                    data.putInt(key, iv);
                }
            } catch (NumberFormatException nfe) {
                return; // invalid int
            }
        } else if (controlKeys.contains(key)) {
            String fallback = key.equals("control_right_click") ? "SIT" : "CYCLE";
            String normalized = normalizeMapping(value, fallback);
            if (normalized == null) return;
            data.putString(key, normalized);
        } else {
            // not an allowed key — ignore
            return;
        }

        // Sync to tracking players
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity,
            new SendPetSettingsPayload(entityId,
                data.getBoolean("sitstill"),
                data.getBoolean("waiting"),
                data.getBoolean("pettingtamed"),
                data.getBoolean("attackifownerattacks"),
                data.getBoolean("attackifownerattacked"),
                data.getBoolean("attackifselfattacked"),
                data.getBoolean("damageOwner"),
                data.getBoolean("ignoreWhistle"),
                data.contains("followdistance") ? data.getInt("followdistance") : (int)net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE.get().doubleValue(),
                data.contains("teleportdistance") ? data.getInt("teleportdistance") : (int)net.yigitguven.petting.config.PettingConfig.TELEPORT_DISTANCE.get().doubleValue(),
                false,
                data.contains("control_right_click") ? data.getString("control_right_click") : "SIT",
                data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : "CYCLE"
            ));
    }

    private static boolean isOwner(Entity entity, ServerPlayer player) {
        if (entity == null || player == null) return false;
        CompoundTag data = entity.getPersistentData();
        if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(player.getStringUUID())) return true;
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }
        return false;
    }

    private static String normalizeMapping(String value, String fallbackAction) {
        if (value == null || value.isBlank()) return fallbackAction + "|NONE";
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
                // Sanitize: strip control characters, cap length
                command = command.replaceAll("[\\p{Cntrl}]", "").trim();
                if (command.length() > 256) command = command.substring(0, 256);
                if (!command.isEmpty()) normalized += "|" + command;
            }
            out.add(normalized);
        }
        if (out.isEmpty()) out.add(fallbackAction + "|NONE");
        return String.join(";", out);
    }
}
