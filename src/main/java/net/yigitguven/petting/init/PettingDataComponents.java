package net.yigitguven.petting.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.CollarRespawnPoint;
import net.yigitguven.petting.data.CollarSettings;

import java.util.UUID;

public class PettingDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Petting.MODID);

    public static final RegistryObject<DataComponentType<CollarSettings>> COLLAR_SETTINGS =
            DATA_COMPONENTS.register("collar_settings", () ->
                    DataComponentType.<CollarSettings>builder()
                            .persistent(CollarSettings.CODEC)
                            .networkSynchronized(CollarSettings.STREAM_CODEC)
                            .build());

    public static final RegistryObject<DataComponentType<CollarRespawnPoint>> COLLAR_RESPAWN_POINT =
            DATA_COMPONENTS.register("collar_respawn_point", () ->
                    DataComponentType.<CollarRespawnPoint>builder()
                            .persistent(CollarRespawnPoint.CODEC)
                            .networkSynchronized(CollarRespawnPoint.STREAM_CODEC)
                            .build());

    public static final RegistryObject<DataComponentType<UUID>> DRAGON_EGG_OWNER =
            DATA_COMPONENTS.register("dragon_egg_owner", () ->
                    DataComponentType.<UUID>builder()
                            .persistent(UUIDUtil.CODEC)
                            .networkSynchronized(UUIDUtil.STREAM_CODEC)
                            .build());

    public static final RegistryObject<DataComponentType<String>> DRAGON_EGG_OWNER_NAME =
            DATA_COMPONENTS.register("dragon_egg_owner_name", () ->
                    DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                            .build());
}
