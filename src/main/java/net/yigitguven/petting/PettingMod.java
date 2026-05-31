package net.yigitguven.petting;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.init.PettingModAttributes;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.yigitguven.petting.config.PettingConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.fml.util.thread.SidedThreadGroups;

import java.util.function.Supplier;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;
@Mod(PettingMod.MODID)
public class PettingMod {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "petting";

	public PettingMod(IEventBus modEventBus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.COMMON, PettingConfig.SPEC, "petting-common.toml");

		// Register the config screen factory
		if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
			net.yigitguven.petting.client.PettingClientRegistration.registerConfigScreen(modContainer);
		}

		PettingModItems.REGISTRY.register(modEventBus);
		PettingModBlocks.REGISTRY.register(modEventBus);
		net.yigitguven.petting.init.PettingModMenus.REGISTRY.register(modEventBus);
		net.yigitguven.petting.init.PettingModAttachments.REGISTRY.register(modEventBus);
		PettingModTabs.REGISTRY.register(modEventBus);
		PettingModAttributes.REGISTRY.register(modEventBus);

		modEventBus.addListener(this::registerPayloads);

		if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
			net.yigitguven.petting.client.PettingClientRegistration.register(modEventBus);
		}

		// Register to NeoForge event bus for ServerTickEvent
		net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
	}

	private void registerPayloads(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
		final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(PettingMod.MODID).versioned("1.0");
		
		registrar.playToClient(net.yigitguven.petting.network.SyncPetStatusPayload.TYPE, net.yigitguven.petting.network.SyncPetStatusPayload.STREAM_CODEC, net.yigitguven.petting.network.SyncPetStatusPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.OpenPetInventoryPayload.TYPE, net.yigitguven.petting.network.OpenPetInventoryPayload.STREAM_CODEC, net.yigitguven.petting.network.OpenPetInventoryPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.PetAttackPayload.TYPE, net.yigitguven.petting.network.PetAttackPayload.STREAM_CODEC, net.yigitguven.petting.network.PetAttackPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.CancelBindingPayload.TYPE, net.yigitguven.petting.network.CancelBindingPayload.STREAM_CODEC, net.yigitguven.petting.network.CancelBindingPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.OpenPetSettingsPayload.TYPE, net.yigitguven.petting.network.OpenPetSettingsPayload.STREAM_CODEC, net.yigitguven.petting.network.OpenPetSettingsPayload::handle);
		registrar.playToClient(net.yigitguven.petting.network.SendPetSettingsPayload.TYPE, net.yigitguven.petting.network.SendPetSettingsPayload.STREAM_CODEC, net.yigitguven.petting.network.SendPetSettingsPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.UpdatePetSettingPayload.TYPE, net.yigitguven.petting.network.UpdatePetSettingPayload.STREAM_CODEC, net.yigitguven.petting.network.UpdatePetSettingPayload::handle);
		registrar.playToServer(net.yigitguven.petting.network.SavePetControlDefaultsPayload.TYPE, net.yigitguven.petting.network.SavePetControlDefaultsPayload.STREAM_CODEC, net.yigitguven.petting.network.SavePetControlDefaultsPayload::handle);
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(ServerTickEvent.Post event) {
		List<AbstractMap.SimpleEntry<Runnable, Integer>> actions = new ArrayList<>();
		workQueue.forEach(work -> {
			work.setValue(work.getValue() - 1);
			if (work.getValue() == 0)
				actions.add(work);
		});
		actions.forEach(e -> e.getKey().run());
		workQueue.removeAll(actions);
	}
}
