package net.yigitguven.petting.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PetInventoryCapability {
    public interface IPetInventory extends IItemHandler {}

    public static class PetInventoryHandler extends ItemStackHandler implements IPetInventory {
        public PetInventoryHandler(int size) {
            super(size);
        }
    }

    public static final Capability<IPetInventory> PET_INVENTORY = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPetInventory.class);
    }
}
