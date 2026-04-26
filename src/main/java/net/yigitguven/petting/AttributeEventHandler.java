package net.yigitguven.petting;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.yigitguven.petting.init.PettingModAttributes;

@EventBusSubscriber
public class AttributeEventHandler {
    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C1);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C2);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C3);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C4);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C5);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C6);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C7);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C8);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C9);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C10);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C11);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C12);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C13);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C14);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C15);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C16);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C17);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C18);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C19);
        event.add(EntityType.PLAYER, PettingModAttributes.MAX_PETS_C20);
    }
}



