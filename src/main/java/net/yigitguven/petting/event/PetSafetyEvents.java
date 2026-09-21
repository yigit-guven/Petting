package net.yigitguven.petting.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Petting.MODID)
public class PetSafetyEvents {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        if (attacker == null) {
            return;
        }

        if (attacker instanceof Player player && PetHelper.isOwner(victim, player)) {
            event.setCanceled(true);
            return;
        }

        if (victim instanceof Player player && attacker instanceof LivingEntity petMob && PetHelper.isOwner(petMob, player)) {
            event.setCanceled(true);
            return;
        }

        if (attacker instanceof LivingEntity attackerPet) {
            Optional<UUID> owner1 = PetHelper.getOwnerUUID(attackerPet);
            if (owner1.isPresent() && owner1.equals(PetHelper.getOwnerUUID(victim))) {
                event.setCanceled(true);
            }
        }
    }
}
