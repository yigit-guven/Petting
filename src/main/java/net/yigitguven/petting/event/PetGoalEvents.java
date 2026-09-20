package net.yigitguven.petting.event;

import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;

@EventBusSubscriber(modid = Petting.MODID)
public class PetGoalEvents {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            PetHelper.injectGoals(mob);
        }
    }
}
