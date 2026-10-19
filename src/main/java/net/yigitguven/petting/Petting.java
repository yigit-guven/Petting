package net.yigitguven.petting;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.yigitguven.petting.client.PettingClient;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.init.PettingDataComponents;
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

    public Petting(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        PettingModBlocks.BLOCKS.register(modBusGroup);
        PettingModBlockEntities.BLOCK_ENTITY_TYPES.register(modBusGroup);
        PettingModItems.ITEMS.register(modBusGroup);
        PettingDataComponents.DATA_COMPONENTS.register(modBusGroup);
        PettingModMenus.MENUS.register(modBusGroup);
        PettingModTabs.CREATIVE_MODE_TABS.register(modBusGroup);

        PettingNetwork.init();

        EntityAttributeModificationEvent.BUS.addListener(Petting::onEntityAttributeModification);

        context.registerConfig(ModConfig.Type.COMMON, PettingServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, PettingClientConfig.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            PettingClient.registerClientExtensions(context);
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
