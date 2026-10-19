package net.yigitguven.petting.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class PetData {
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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(ownerUUID != null);
        if (ownerUUID != null) {
            buf.writeUUID(ownerUUID);
        }
        buf.writeBoolean(tamed);
        buf.writeUtf(order != null ? order.name() : PetOrder.FOLLOW.name());
        buf.writeUtf(combatMode != null ? combatMode.name() : CombatMode.DEFENSIVE.name());
        buf.writeItem(getCollar());
        buf.writeItem(getSaddle());
        buf.writeBoolean(rideAttackOnClick);
        buf.writeBoolean(rideAutoAttackHostiles);
    }

    public static PetData fromBytes(FriendlyByteBuf buf) {
        PetData data = new PetData();
        if (buf.readBoolean()) {
            data.ownerUUID = buf.readUUID();
        }
        data.tamed = buf.readBoolean();
        try {
            data.order = PetOrder.valueOf(buf.readUtf(32));
        } catch (Exception e) {
            data.order = PetOrder.FOLLOW;
        }
        try {
            data.combatMode = CombatMode.valueOf(buf.readUtf(32));
        } catch (Exception e) {
            data.combatMode = CombatMode.DEFENSIVE;
        }
        data.collar = buf.readItem();
        data.saddle = buf.readItem();
        data.rideAttackOnClick = buf.readBoolean();
        data.rideAutoAttackHostiles = buf.readBoolean();
        return data;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        if (ownerUUID != null) {
            tag.putUUID("owner", ownerUUID);
        }
        tag.putBoolean("tamed", tamed);
        tag.putString("order", order != null ? order.name() : PetOrder.FOLLOW.name());
        tag.putString("combat_mode", combatMode != null ? combatMode.name() : CombatMode.DEFENSIVE.name());
        if (collar != null && !collar.isEmpty()) {
            tag.put("collar", collar.save(new CompoundTag()));
        }
        if (saddle != null && !saddle.isEmpty()) {
            tag.put("saddle", saddle.save(new CompoundTag()));
        }
        tag.putBoolean("ride_attack_on_click", rideAttackOnClick);
        tag.putBoolean("ride_auto_attack_hostiles", rideAutoAttackHostiles);
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        if (tag == null) return;
        if (tag.hasUUID("owner")) {
            this.ownerUUID = tag.getUUID("owner");
        } else {
            this.ownerUUID = null;
        }
        this.tamed = tag.getBoolean("tamed");
        if (tag.contains("order")) {
            try {
                this.order = PetOrder.valueOf(tag.getString("order"));
            } catch (Exception e) {
                this.order = PetOrder.FOLLOW;
            }
        }
        if (tag.contains("combat_mode")) {
            try {
                this.combatMode = CombatMode.valueOf(tag.getString("combat_mode"));
            } catch (Exception e) {
                this.combatMode = CombatMode.DEFENSIVE;
            }
        }
        if (tag.contains("collar")) {
            this.collar = ItemStack.of(tag.getCompound("collar"));
        } else {
            this.collar = ItemStack.EMPTY;
        }
        if (tag.contains("saddle")) {
            this.saddle = ItemStack.of(tag.getCompound("saddle"));
        } else {
            this.saddle = ItemStack.EMPTY;
        }
        this.rideAttackOnClick = !tag.contains("ride_attack_on_click") || tag.getBoolean("ride_attack_on_click");
        this.rideAutoAttackHostiles = tag.getBoolean("ride_auto_attack_hostiles");
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

    public void copyFrom(PetData other) {
        if (other == null) {
            return;
        }
        this.ownerUUID = other.ownerUUID;
        this.tamed = other.tamed;
        this.order = other.order;
        this.combatMode = other.combatMode;
        this.collar = other.collar != null ? other.collar.copy() : ItemStack.EMPTY;
        this.saddle = other.saddle != null ? other.saddle.copy() : ItemStack.EMPTY;
        this.rideAttackOnClick = other.rideAttackOnClick;
        this.rideAutoAttackHostiles = other.rideAutoAttackHostiles;
    }
}
