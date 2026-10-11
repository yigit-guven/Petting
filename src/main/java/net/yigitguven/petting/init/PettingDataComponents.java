package net.yigitguven.petting.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.CollarSettings;

public class PettingDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Petting.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CollarSettings>> COLLAR_SETTINGS =
            DATA_COMPONENTS.registerComponentType("collar_settings",
                    builder -> builder.persistent(CollarSettings.CODEC).networkSynchronized(CollarSettings.STREAM_CODEC));
}
