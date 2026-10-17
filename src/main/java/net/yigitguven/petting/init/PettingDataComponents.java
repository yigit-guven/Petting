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

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<net.yigitguven.petting.data.CollarRespawnPoint>> COLLAR_RESPAWN_POINT =
            DATA_COMPONENTS.registerComponentType("collar_respawn_point",
                    builder -> builder.persistent(net.yigitguven.petting.data.CollarRespawnPoint.CODEC).networkSynchronized(net.yigitguven.petting.data.CollarRespawnPoint.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.UUID>> DRAGON_EGG_OWNER =
            DATA_COMPONENTS.registerComponentType("dragon_egg_owner",
                    builder -> builder.persistent(net.minecraft.core.UUIDUtil.CODEC).networkSynchronized(net.minecraft.core.UUIDUtil.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DRAGON_EGG_OWNER_NAME =
            DATA_COMPONENTS.registerComponentType("dragon_egg_owner_name",
                    builder -> builder.persistent(com.mojang.serialization.Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8));
}
