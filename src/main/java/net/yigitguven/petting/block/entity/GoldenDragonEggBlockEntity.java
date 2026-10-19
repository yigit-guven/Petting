package net.yigitguven.petting.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yigitguven.petting.init.PettingModBlockEntities;

import javax.annotation.Nullable;
import java.util.UUID;

public class GoldenDragonEggBlockEntity extends BlockEntity {
    private @Nullable UUID ownerUUID;
    private @Nullable String ownerName;

    public GoldenDragonEggBlockEntity(BlockPos pos, BlockState state) {
        super(PettingModBlockEntities.GOLDEN_DRAGON_EGG.get(), pos, state);
    }

    public @Nullable UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        setChanged();
    }

    public @Nullable String getOwnerName() {
        return this.ownerName;
    }

    public void setOwnerName(@Nullable String ownerName) {
        this.ownerName = ownerName;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        if (this.ownerName != null) {
            tag.putString("OwnerName", this.ownerName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        } else {
            this.ownerUUID = null;
        }
        if (tag.contains("OwnerName")) {
            this.ownerName = tag.getString("OwnerName");
        } else {
            this.ownerName = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
