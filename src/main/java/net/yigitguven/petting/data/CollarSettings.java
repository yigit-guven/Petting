package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.yigitguven.petting.config.PettingServerConfig;

public record CollarSettings(
        CombatMode combatMode,
        int followDistance,
        int teleportDistance,
        boolean teleportEnabled,
        boolean preferRangedAttack,
        boolean defendOtherPets,
        boolean retaliateWhenHurt
) {
    public static final Codec<CollarSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(CombatMode::valueOf, CombatMode::name).fieldOf("combat_mode").forGetter(CollarSettings::combatMode),
                    Codec.INT.fieldOf("follow_distance").forGetter(CollarSettings::followDistance),
                    Codec.INT.fieldOf("teleport_distance").forGetter(CollarSettings::teleportDistance),
                    Codec.BOOL.fieldOf("teleport_enabled").forGetter(CollarSettings::teleportEnabled),
                    Codec.BOOL.fieldOf("prefer_ranged_attack").forGetter(CollarSettings::preferRangedAttack),
                    Codec.BOOL.fieldOf("defend_other_pets").forGetter(CollarSettings::defendOtherPets),
                    Codec.BOOL.fieldOf("retaliate_when_hurt").forGetter(CollarSettings::retaliateWhenHurt)
            ).apply(instance, CollarSettings::new)
    );

    public static CollarSettings fromConfig() {
        return new CollarSettings(
                PettingServerConfig.getDefaultCombatMode(),
                PettingServerConfig.getDefaultFollowDistance(),
                PettingServerConfig.getDefaultTeleportDistance(),
                PettingServerConfig.isDefaultTeleportEnabled(),
                PettingServerConfig.isDefaultPreferRangedAttack(),
                PettingServerConfig.isDefaultDefendOtherPets(),
                PettingServerConfig.isDefaultRetaliateWhenHurt()
        );
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(combatMode.name());
        buf.writeVarInt(followDistance);
        buf.writeVarInt(teleportDistance);
        buf.writeBoolean(teleportEnabled);
        buf.writeBoolean(preferRangedAttack);
        buf.writeBoolean(defendOtherPets);
        buf.writeBoolean(retaliateWhenHurt);
    }

    public static CollarSettings fromBytes(FriendlyByteBuf buf) {
        CombatMode mode;
        try {
            mode = CombatMode.valueOf(buf.readUtf(32));
        } catch (Exception e) {
            mode = CombatMode.DEFENSIVE;
        }
        int followDist = buf.readVarInt();
        int teleDist = buf.readVarInt();
        boolean teleEnabled = buf.readBoolean();
        boolean preferRanged = buf.readBoolean();
        boolean defendOther = buf.readBoolean();
        boolean retaliate = buf.readBoolean();
        return new CollarSettings(mode, followDist, teleDist, teleEnabled, preferRanged, defendOther, retaliate);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("combat_mode", combatMode.name());
        tag.putInt("follow_distance", followDistance);
        tag.putInt("teleport_distance", teleportDistance);
        tag.putBoolean("teleport_enabled", teleportEnabled);
        tag.putBoolean("prefer_ranged_attack", preferRangedAttack);
        tag.putBoolean("defend_other_pets", defendOtherPets);
        tag.putBoolean("retaliate_when_hurt", retaliateWhenHurt);
        return tag;
    }

    public static CollarSettings fromNbt(CompoundTag tag) {
        if (tag == null) return fromConfig();
        CombatMode mode = CombatMode.DEFENSIVE;
        if (tag.contains("combat_mode")) {
            try {
                mode = CombatMode.valueOf(tag.getString("combat_mode"));
            } catch (Exception ignored) {}
        }
        int followDist = tag.contains("follow_distance") ? tag.getInt("follow_distance") : 3;
        int teleDist = tag.contains("teleport_distance") ? tag.getInt("teleport_distance") : 12;
        boolean teleEnabled = !tag.contains("teleport_enabled") || tag.getBoolean("teleport_enabled");
        boolean preferRanged = tag.getBoolean("prefer_ranged_attack");
        boolean defendOther = !tag.contains("defend_other_pets") || tag.getBoolean("defend_other_pets");
        boolean retaliate = !tag.contains("retaliate_when_hurt") || tag.getBoolean("retaliate_when_hurt");
        return new CollarSettings(mode, followDist, teleDist, teleEnabled, preferRanged, defendOther, retaliate);
    }

    public CollarSettings withCombatMode(CombatMode mode) {
        return new CollarSettings(mode, followDistance, teleportDistance, teleportEnabled, preferRangedAttack, defendOtherPets, retaliateWhenHurt);
    }

    public CollarSettings withFollowDistance(int dist) {
        return new CollarSettings(combatMode, dist, teleportDistance, teleportEnabled, preferRangedAttack, defendOtherPets, retaliateWhenHurt);
    }

    public CollarSettings withTeleportDistance(int dist) {
        return new CollarSettings(combatMode, followDistance, dist, teleportEnabled, preferRangedAttack, defendOtherPets, retaliateWhenHurt);
    }

    public CollarSettings withTeleportEnabled(boolean enabled) {
        return new CollarSettings(combatMode, followDistance, teleportDistance, enabled, preferRangedAttack, defendOtherPets, retaliateWhenHurt);
    }

    public CollarSettings withPreferRangedAttack(boolean prefer) {
        return new CollarSettings(combatMode, followDistance, teleportDistance, teleportEnabled, prefer, defendOtherPets, retaliateWhenHurt);
    }

    public CollarSettings withDefendOtherPets(boolean defend) {
        return new CollarSettings(combatMode, followDistance, teleportDistance, teleportEnabled, preferRangedAttack, defend, retaliateWhenHurt);
    }

    public CollarSettings withRetaliateWhenHurt(boolean retaliate) {
        return new CollarSettings(combatMode, followDistance, teleportDistance, teleportEnabled, preferRangedAttack, defendOtherPets, retaliate);
    }
}
