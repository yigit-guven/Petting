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

        // Case 1: Owner attacks their own pet
        if (attacker instanceof Player player && PetHelper.isTamed(victim)) {
            if (PetHelper.isOwner(victim, player)) {
                event.setCanceled(true);
                return;
            }
        }

        // Case 2: Pet attacks their owner
        if (victim instanceof Player player && attacker instanceof LivingEntity petMob && PetHelper.isTamed(petMob)) {
            if (PetHelper.isOwner(petMob, player)) {
                event.setCanceled(true);
                return;
            }
        }

        // Case 3: Two pets belonging to the same owner attack each other
        if (attacker instanceof LivingEntity attackerPet && PetHelper.isTamed(attackerPet) && PetHelper.isTamed(victim)) {
            Optional<UUID> owner1 = PetHelper.getOwnerUUID(attackerPet);
            Optional<UUID> owner2 = PetHelper.getOwnerUUID(victim);
            if (owner1.isPresent() && owner1.equals(owner2)) {
                event.setCanceled(true);
            }
        }
    }
}
