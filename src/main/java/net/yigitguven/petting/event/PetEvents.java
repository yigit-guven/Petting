package net.yigitguven.petting.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

public class PetEvents {

    public static void register() {
        UseEntityCallback.EVENT.register(PetTamingEvents::onEntityInteract);
        UseBlockCallback.EVENT.register(PetTamingEvents::onRightClickBlock);

        ServerLivingEntityEvents.ALLOW_DAMAGE.register(PetSafetyEvents::allowDamage);
        ServerLivingEntityEvents.AFTER_DAMAGE.register(PetSafetyEvents::afterDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register(PetGoalEvents::allowDeath);

        ServerEntityEvents.ENTITY_LOAD.register((entity, serverLevel) -> {
            PetSafetyEvents.onEntityJoinLevel(entity);
            PetGoalEvents.onEntityLoad(entity, serverLevel);
        });

        ServerEntityEvents.ENTITY_UNLOAD.register(PetGoalEvents::onEntityUnload);
    }
}
