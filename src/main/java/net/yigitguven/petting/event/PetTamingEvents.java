package net.yigitguven.petting.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetEggHelper;
import net.yigitguven.petting.util.PetHelper;

@EventBusSubscriber(modid = Petting.MODID)
public class PetTamingEvents {

    @SubscribeEvent
    public static void onAnimalTame(AnimalTameEvent event) {
        if (PetHelper.isTamed(event.getAnimal())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Mob mob)) {
            return;
        }

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        boolean isClient = event.getLevel().isClientSide();

        if (PetHelper.isTamed(mob)) {
            if (PetHelper.isOwner(mob, player)) {
                if (PetEggHelper.canTame(mob, stack.getItem())) {
                    if (mob.getHealth() < mob.getMaxHealth()) {
                        if (!isClient) {
                            if (!player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }
                            mob.heal(mob.getMaxHealth() * 0.5F);
                            ServerLevel serverLevel = (ServerLevel) mob.level();
                            serverLevel.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 7, 0.3D, 0.3D, 0.3D, 0.1D);
                            mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
                            player.sendSystemMessage(Component.translatable("petting.action.pet_healed", mob.getName()));
                        }
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    } else {
                        if (!isClient) {
                            player.sendSystemMessage(Component.translatable("petting.action.pet_full_health", mob.getName()));
                        }
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    }
                }
            } else if (PetEggHelper.canTame(mob, stack.getItem())) {
                if (!isClient) {
                    player.sendSystemMessage(Component.translatable("petting.action.already_tamed_other"));
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
            return;
        }

        if (!PetEggHelper.canTame(mob, stack.getItem())) {
            return;
        }

        if (PetHelper.isVanillaTamed(mob)) {
            if (!isClient) {
                ServerLevel serverLevel = (ServerLevel) mob.level();
                serverLevel.sendParticles(ParticleTypes.SMOKE, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("petting.action.already_vanilla_tamed"));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        boolean requiresWeakening = !PettingServerConfig.isHostileMobsOnly() || mob instanceof Enemy;
        double maxAllowedHealth = mob.getMaxHealth() * PettingServerConfig.getMaxHealthRatio();
        if (requiresWeakening && mob.getHealth() > maxAllowedHealth) {
            if (!isClient) {
                ServerLevel serverLevel = (ServerLevel) mob.level();
                serverLevel.sendParticles(ParticleTypes.SMOKE, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("petting.action.too_healthy", (int) Math.round(PettingServerConfig.getMaxHealthPercentage())));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!isClient) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            ServerLevel serverLevel = (ServerLevel) mob.level();
            double roll = mob.getRandom().nextDouble();
            boolean failed = roll < PettingServerConfig.getUnsuccessfulTamingRatio();

            if (failed) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("petting.action.taming_failed"));
            } else {
                PetHelper.setTamed(mob, player);
                serverLevel.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 10, 0.4D, 0.4D, 0.4D, 0.1D);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
                player.sendSystemMessage(Component.translatable("petting.action.tamed_success", mob.getName()));
            }
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
