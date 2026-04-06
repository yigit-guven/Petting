package net.yigitguven.petting;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.yigitguven.petting.init.PettingModTabs;
import net.yigitguven.petting.init.PettingModItems;

import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.network.OpenPetInventoryPacket;
import net.yigitguven.petting.init.PettingModMenus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;
import java.util.function.Function;
import java.util.function.BiConsumer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.AbstractMap;

@Mod("petting")
public class PettingMod {
	public static final Logger LOGGER = LogManager.getLogger(PettingMod.class);
	public static final String MODID = "petting";

	public PettingMod() {
		FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
		// Start of user code block mod constructor
// Inside PettingMod constructor
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PettingConfig.SPEC, "petting-common.toml");
		// End of user code block mod constructor
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = context.getModEventBus();
		PettingModItems.REGISTRY.register(bus);
		PettingModTabs.REGISTRY.register(bus);
		PettingModMenus.REGISTRY.register(bus);
		net.yigitguven.petting.init.PettingModAttributes.REGISTRY.register(bus);
		// Start of user code block mod init
        addNetworkMessage(OpenPetInventoryPacket.class, OpenPetInventoryPacket::toBytes, OpenPetInventoryPacket::new, OpenPetInventoryPacket::handle);
        addNetworkMessage(net.yigitguven.petting.network.PetAttackPacket.class, net.yigitguven.petting.network.PetAttackPacket::toBytes, net.yigitguven.petting.network.PetAttackPacket::new, net.yigitguven.petting.network.PetAttackPacket::handle);
		// End of user code block mod init
	}

	// Start of user code block mod methods
    @SubscribeEvent
    public void onAttachCapabilities(net.minecraftforge.event.AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        net.minecraft.world.entity.Entity entity = event.getObject();
        if (entity instanceof net.minecraft.world.entity.LivingEntity) {
            // Skip blacklisted mobs
            if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;

            event.addCapability(new net.minecraft.resources.ResourceLocation(MODID, "pet_inventory"), new net.yigitguven.petting.capability.PetInventoryProvider());
        }
    }
    @SubscribeEvent
    public void onEntityDeath(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        event.getEntity().getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                net.minecraft.world.item.ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), stack.copy()));
                }
            }
        });
    }
	// End of user code block mod methods
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int messageID = 0;

	public static <T> void addNetworkMessage(Class<T> messageType, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
		PACKET_HANDLER.registerMessage(messageID, messageType, encoder, decoder, messageConsumer);
		messageID++;
	}

	private static final Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue = new ConcurrentLinkedQueue<>();

	public static void queueServerWork(int tick, Runnable action) {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
			workQueue.add(new AbstractMap.SimpleEntry<>(action, tick));
	}

	@SubscribeEvent
	public void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
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
}
