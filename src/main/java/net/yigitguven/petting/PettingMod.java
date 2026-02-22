package net.yigitguven.petting;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModAttributes;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.yigitguven.petting.config.PettingConfig;
@Mod(PettingMod.MODID)
public class PettingMod {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "petting";

	public PettingMod(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, PettingConfig.SPEC, "petting-common.toml");

		// Register the config screen factory
		modContainer.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class, 
			(container, screen) -> new net.yigitguven.petting.client.ConfigGuiScreen(screen));

		PettingModItems.REGISTRY.register(modEventBus);
		PettingModTabs.REGISTRY.register(modEventBus);
		PettingModAttributes.REGISTRY.register(modEventBus);
	}
}
