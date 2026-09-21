package net.yigitguven.petting;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.yigitguven.petting.client.PettingClient;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.init.PettingModAttachments;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModTabs;

@Mod(Petting.MODID)
public class Petting {
    public static final String MODID = "petting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Petting(IEventBus modEventBus, ModContainer modContainer) {
        PettingModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        PettingModItems.ITEMS.register(modEventBus);
        PettingModTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, PettingServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, PettingClientConfig.SPEC);

        if (FMLEnvironment.getDist().isClient()) {
            PettingClient.registerClientExtensions(modContainer, modEventBus);
        }
    }
}
