package net.yigitguven.petting;

import net.fabricmc.api.ModInitializer;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModAttributes;
import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.network.PettingServerNetworking;
import net.yigitguven.petting.procedures.GoldenWheatItemInHandTickProcedure;
import net.yigitguven.petting.procedures.PetWhistleProcedure;
import net.yigitguven.petting.procedures.PetDeathHandlerProcedure;
import net.yigitguven.petting.procedures.OwnerRightclicksPetProcedure;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PettingMod implements ModInitializer {
    public static final String MODID = "petting";
    public static final Logger LOGGER = LogManager.getLogger("Petting");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Petting Mod for Fabric...");
        
        // Initialize Config
        PettingConfig.load();
        
        // Register Registries
        PettingModBlocks.register();
        PettingModItems.register();
        PettingModAttributes.register();
        PettingModTabs.register();

        PettingServerNetworking.registerReceivers();

        // Register Procedures/Events
        GoldenWheatItemInHandTickProcedure.register();
        PetWhistleProcedure.register();
        PetDeathHandlerProcedure.register();
        PetBedBindingHandler.register();
        OwnerRightclicksPetProcedure.register();

        ItemGroupEvents.modifyEntriesEvent(PettingModTabs.PETTING_TAB_KEY).register(content -> {
            content.accept(PettingModItems.GOLDEN_WHEAT);
            content.accept(PettingModItems.PET_TETHER);
            content.accept(PettingModItems.FOLLOW_WHISTLE);
            content.accept(PettingModItems.TELEPORT_ORB);
            content.accept(PettingModItems.PET_BED);
        });
    }
}
