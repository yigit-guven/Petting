package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;
import java.util.UUID;

public class PetData {
    public static final MapCodec<PetData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(data -> Optional.ofNullable(data.ownerUUID)),
                    Codec.BOOL.fieldOf("tamed").forGetter(PetData::isTamed),
                    Codec.STRING.xmap(PetOrder::valueOf, PetOrder::name).fieldOf("order").forGetter(PetData::getOrder),
                    Codec.STRING.xmap(CombatMode::valueOf, CombatMode::name).fieldOf("combat_mode").forGetter(PetData::getCombatMode)
            ).apply(instance, PetData::new)
    );

    public static final StreamCodec<ByteBuf, PetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC),
            data -> Optional.ofNullable(data.ownerUUID),
            ByteBufCodecs.BOOL,
            PetData::isTamed,
            ByteBufCodecs.stringUtf8(32).map(PetOrder::valueOf, PetOrder::name),
            PetData::getOrder,
            ByteBufCodecs.stringUtf8(32).map(CombatMode::valueOf, CombatMode::name),
            PetData::getCombatMode,
            PetData::new
    );

    private UUID ownerUUID;
    private boolean tamed;
    private PetOrder order;
    private CombatMode combatMode;

    public PetData() {
        this.ownerUUID = null;
        this.tamed = false;
        this.order = PetOrder.FOLLOW;
        this.combatMode = CombatMode.DEFENSIVE;
    }

    public PetData(Optional<UUID> ownerUUID, boolean tamed, PetOrder order, CombatMode combatMode) {
        this.ownerUUID = ownerUUID.orElse(null);
        this.tamed = tamed;
        this.order = order != null ? order : PetOrder.FOLLOW;
        this.combatMode = combatMode != null ? combatMode : CombatMode.DEFENSIVE;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public boolean isTamed() {
        return tamed;
    }

    public void setTamed(boolean tamed) {
        this.tamed = tamed;
    }

    public PetOrder getOrder() {
        return order;
    }

    public void setOrder(PetOrder order) {
        this.order = order != null ? order : PetOrder.FOLLOW;
    }

    public CombatMode getCombatMode() {
        return combatMode;
    }

    public void setCombatMode(CombatMode combatMode) {
        this.combatMode = combatMode != null ? combatMode : CombatMode.DEFENSIVE;
    }
}
