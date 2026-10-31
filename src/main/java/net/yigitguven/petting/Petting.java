package net.yigitguven.petting;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.yigitguven.petting.command.SummonPetCommand;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.event.PetEvents;
import net.yigitguven.petting.init.PettingDataComponents;
import net.yigitguven.petting.init.PettingModAttachments;
import net.yigitguven.petting.init.PettingModBlockEntities;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.network.PettingNetwork;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

public class Petting implements ModInitializer {
    public static final String MODID = "petting";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        PettingServerConfig.load();

        PettingModAttachments.register();
        PettingModBlocks.register();
        PettingModBlockEntities.register();
        PettingModItems.register();
        PettingDataComponents.register();
        PettingModMenus.register();
        PettingModTabs.register();

        PettingNetwork.registerPayloads();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SummonPetCommand.register(dispatcher, registryAccess)
        );

        PetEvents.register();
    }
}
