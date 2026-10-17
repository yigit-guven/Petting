package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
    public static final MapCodec<CollarSettings> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
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

    public static final Codec<CollarSettings> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, CollarSettings> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeUtf(val.combatMode.name());
                buf.writeVarInt(val.followDistance);
                buf.writeVarInt(val.teleportDistance);
                buf.writeBoolean(val.teleportEnabled);
                buf.writeBoolean(val.preferRangedAttack);
                buf.writeBoolean(val.defendOtherPets);
                buf.writeBoolean(val.retaliateWhenHurt);
            },
            buf -> new CollarSettings(
                    CombatMode.valueOf(buf.readUtf(32)),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
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
