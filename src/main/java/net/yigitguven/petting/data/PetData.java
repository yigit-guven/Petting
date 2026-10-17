package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class PetData {
    public static final MapCodec<PetData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(data -> Optional.ofNullable(data.ownerUUID)),
                    Codec.BOOL.fieldOf("tamed").forGetter(PetData::isTamed),
                    Codec.STRING.xmap(PetOrder::valueOf, PetOrder::name).fieldOf("order").forGetter(PetData::getOrder),
                    Codec.STRING.xmap(CombatMode::valueOf, CombatMode::name).fieldOf("combat_mode").forGetter(PetData::getCombatMode),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("collar", ItemStack.EMPTY).forGetter(PetData::getCollar),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("saddle", ItemStack.EMPTY).forGetter(PetData::getSaddle),
                    Codec.BOOL.optionalFieldOf("ride_attack_on_click", true).forGetter(PetData::isRideAttackOnClick),
                    Codec.BOOL.optionalFieldOf("ride_auto_attack_hostiles", false).forGetter(PetData::isRideAutoAttackHostiles)
            ).apply(instance, PetData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PetData> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, Optional.ofNullable(val.ownerUUID));
                buf.writeBoolean(val.isTamed());
                buf.writeUtf(val.getOrder().name());
                buf.writeUtf(val.getCombatMode().name());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, val.getCollar());
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, val.getSaddle());
                buf.writeBoolean(val.isRideAttackOnClick());
                buf.writeBoolean(val.isRideAutoAttackHostiles());
            },
            buf -> new PetData(
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf),
                    buf.readBoolean(),
                    PetOrder.valueOf(buf.readUtf(32)),
                    CombatMode.valueOf(buf.readUtf(32)),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    private UUID ownerUUID;
    private boolean tamed;
    private PetOrder order;
    private CombatMode combatMode;
    private ItemStack collar;
    private ItemStack saddle;
    private boolean rideAttackOnClick;
    private boolean rideAutoAttackHostiles;

    public PetData() {
        this.ownerUUID = null;
        this.tamed = false;
        this.order = PetOrder.FOLLOW;
        this.combatMode = net.yigitguven.petting.config.PettingServerConfig.getDefaultCombatMode();
        this.collar = ItemStack.EMPTY;
        this.saddle = ItemStack.EMPTY;
        this.rideAttackOnClick = true;
        this.rideAutoAttackHostiles = false;
    }

    public PetData(Optional<UUID> ownerUUID, boolean tamed, PetOrder order, CombatMode combatMode, ItemStack collar, ItemStack saddle, boolean rideAttackOnClick, boolean rideAutoAttackHostiles) {
        this.ownerUUID = ownerUUID.orElse(null);
        this.tamed = tamed;
        this.order = order != null ? order : PetOrder.FOLLOW;
        this.combatMode = combatMode != null ? combatMode : net.yigitguven.petting.config.PettingServerConfig.getDefaultCombatMode();
        this.collar = collar != null ? collar : ItemStack.EMPTY;
        this.saddle = saddle != null ? saddle : ItemStack.EMPTY;
        this.rideAttackOnClick = rideAttackOnClick;
        this.rideAutoAttackHostiles = rideAutoAttackHostiles;
    }

    public ItemStack getCollar() {
        return collar != null ? collar : ItemStack.EMPTY;
    }

    public void setCollar(ItemStack collar) {
        this.collar = collar != null ? collar : ItemStack.EMPTY;
    }

    public ItemStack getSaddle() {
        return saddle != null ? saddle : ItemStack.EMPTY;
    }

    public void setSaddle(ItemStack saddle) {
        this.saddle = saddle != null ? saddle : ItemStack.EMPTY;
    }

    public boolean isRideAttackOnClick() {
        return rideAttackOnClick;
    }

    public void setRideAttackOnClick(boolean rideAttackOnClick) {
        this.rideAttackOnClick = rideAttackOnClick;
    }

    public boolean isRideAutoAttackHostiles() {
        return rideAutoAttackHostiles;
    }

    public void setRideAutoAttackHostiles(boolean rideAutoAttackHostiles) {
        this.rideAutoAttackHostiles = rideAutoAttackHostiles;
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
