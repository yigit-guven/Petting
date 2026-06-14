package net.yigitguven.petting;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.yigitguven.petting.init.PettingModAttributes;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AttributeEventHandler {
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C1.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C2.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C3.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C4.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C5.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C6.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C7.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C8.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C9.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C10.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C11.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C12.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C13.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C14.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C15.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C16.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C17.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C18.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C19.get());
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C20.get());
    }
}



