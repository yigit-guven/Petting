package net.yigitguven.petting.capability;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.yigitguven.petting.Petting;
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
    public CompoundTag serializeNBT(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        PetData.MAP_CODEC.codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, lookup), petData)
                .resultOrPartial(err -> Petting.LOGGER.error("Failed to encode PetData: {}", err))
                .ifPresent(nbt -> {
                    if (nbt instanceof CompoundTag ct) {
                        tag.merge(ct);
                    }
                });
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag nbt) {
        PetData.MAP_CODEC.codec().parse(RegistryOps.create(NbtOps.INSTANCE, lookup), nbt)
                .resultOrPartial(err -> Petting.LOGGER.error("Failed to decode PetData: {}", err))
                .ifPresent(petData::copyFrom);
    }
}
