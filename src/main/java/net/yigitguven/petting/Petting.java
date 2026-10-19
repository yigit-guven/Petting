package net.yigitguven.petting;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.yigitguven.petting.client.PettingClient;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.init.PettingModBlockEntities;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.network.PettingNetwork;
import org.slf4j.Logger;

@Mod(Petting.MODID)
public class Petting {
    public static final String MODID = "petting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Petting() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        PettingModBlocks.BLOCKS.register(modEventBus);
        PettingModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        PettingModItems.ITEMS.register(modEventBus);
        PettingModMenus.MENUS.register(modEventBus);
        PettingModTabs.CREATIVE_MODE_TABS.register(modEventBus);

        PettingNetwork.init();

        modEventBus.addListener(Petting::onEntityAttributeModification);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PettingServerConfig.SPEC, "petting-synced.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PettingClientConfig.SPEC, "petting-client.toml");

        if (FMLEnvironment.dist.isClient()) {
            PettingClient.registerClientExtensions(modEventBus);
        }
    }

    private static void onEntityAttributeModification(net.minecraftforge.event.entity.EntityAttributeModificationEvent event) {
        for (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
                event.add(entityType, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 2.0D);
            }
        }
    }
}
