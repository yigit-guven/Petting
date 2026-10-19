package net.yigitguven.petting.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.yigitguven.petting.data.PetData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PetDataProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PetData> PET_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final PetData petData = new PetData();
    private final LazyOptional<PetData> optional = LazyOptional.of(() -> petData);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PET_DATA_CAPABILITY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return petData.toNbt();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        petData.fromNbt(nbt);
    }
}
