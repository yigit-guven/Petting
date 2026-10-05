package net.yigitguven.petting.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
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

        // Always: pets can never attack their owner
        if (victim instanceof Player player && attacker instanceof LivingEntity petMob && PetHelper.isOwner(petMob, player)) {
            event.setCanceled(true);
            return;
        }

        // Configurable: owner attacking their own pets
        if (!PettingServerConfig.canOwnerAttackPets() && attacker instanceof Player player && PetHelper.isOwner(victim, player)) {
            event.setCanceled(true);
            return;
        }

        // Configurable: friendly fire between pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets() && attacker instanceof LivingEntity attackerPet) {
            Optional<UUID> owner1 = PetHelper.getOwnerUUID(attackerPet);
            if (owner1.isPresent() && owner1.equals(PetHelper.getOwnerUUID(victim))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity pet = event.getEntity();
        if (!PetHelper.isTamed(pet)) {
            return;
        }

        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (newTarget == null) {
            return;
        }

        // Always: pets can never target their owner
        if (newTarget instanceof Player player && PetHelper.isOwner(pet, player)) {
            event.setCanceled(true);
            event.setNewAboutToBeSetTarget(null);
            if (pet instanceof NeutralMob neutralMob) {
                neutralMob.stopBeingAngry();
            }
            return;
        }

        // Configurable: pets targeting other pets of the same owner
        if (!PettingServerConfig.canPetsAttackPets()) {
            Optional<UUID> petOwner = PetHelper.getOwnerUUID(pet);
            if (petOwner.isPresent() && petOwner.equals(PetHelper.getOwnerUUID(newTarget))) {
                event.setCanceled(true);
                event.setNewAboutToBeSetTarget(null);
                if (pet instanceof NeutralMob neutralMob) {
                    neutralMob.stopBeingAngry();
                }
            }
        }
    }
}
