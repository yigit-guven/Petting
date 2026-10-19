package net.yigitguven.petting.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.yigitguven.petting.init.PettingDataComponents;
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("OwnerUUID", UUIDUtil.CODEC, this.ownerUUID);
        if (this.ownerName != null) {
            output.putString("OwnerName", this.ownerName);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ownerUUID = input.read("OwnerUUID", UUIDUtil.CODEC).orElse(null);
        this.ownerName = input.getStringOr("OwnerName", null);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        java.util.UUID owner = components.get(PettingDataComponents.DRAGON_EGG_OWNER.get());
        if (owner != null) {
            this.ownerUUID = owner;
        }
        String name = components.get(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get());
        if (name != null) {
            this.ownerName = name;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (this.ownerUUID != null) {
            builder.set(PettingDataComponents.DRAGON_EGG_OWNER.get(), this.ownerUUID);
        }
        if (this.ownerName != null) {
            builder.set(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get(), this.ownerName);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
