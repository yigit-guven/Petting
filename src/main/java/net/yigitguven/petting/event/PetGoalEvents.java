package net.yigitguven.petting.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.PetSavedData;
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
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                PetHelper.getOwnerUUID(mob).ifPresent(ownerUuid ->
                        PetSavedData.get(serverLevel).addPet(ownerUuid, mob.getUUID())
                );
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.level() instanceof ServerLevel serverLevel) {
                PetSavedData.get(serverLevel).removePet(mob.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() instanceof Mob mob && PetHelper.isTamed(mob)) {
            if (mob.isRemoved()) {
                Entity.RemovalReason reason = mob.getRemovalReason();
                if (reason != null && reason.shouldDestroy()) {
                    if (event.getLevel() instanceof ServerLevel serverLevel) {
                        PetSavedData.get(serverLevel).removePet(mob.getUUID());
                    }
                }
            }
        }
    }
}
