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
import net.minecraft.world.item.DyeColor;
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
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (PetHelper.isCushionEntity(event.getTarget())) {
            if (handleCushionBinding(player, event.getTarget(), stack, event.getLevel(), event.getHand())) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
        }

        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }

        boolean isClient = event.getLevel().isClientSide();

        if (!(event.getTarget() instanceof Mob mob)) {
            return;
        }

        if (PetHelper.isTamed(mob)) {
            if (PetHelper.isOwner(mob, player)) {
                if (PettingServerConfig.isPetInventoryEnabled()) {
                    if (PettingServerConfig.isCollarSlotEnabled() && stack.getItem() instanceof net.yigitguven.petting.item.CollarItem) {
                        if (PetHelper.getCollar(mob).isEmpty()) {
                            if (!isClient) {
                                PetHelper.setCollar(mob, stack.copyWithCount(1));
                                if (!player.getAbilities().instabuild) {
                                    stack.shrink(1);
                                }
                                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 1.0F);
                            }
                            event.setCancellationResult(InteractionResult.SUCCESS);
                            event.setCanceled(true);
                            return;
                        }
                    }

                    DyeColor dyeColor = DyeColor.getColor(stack);
                    if (dyeColor != null) {
                        ItemStack currentCollar = PetHelper.getCollar(mob);
                        if (!currentCollar.isEmpty() && currentCollar.getItem() instanceof net.yigitguven.petting.item.CollarItem currentItem) {
                            if (currentItem.getColor() != dyeColor) {
                                if (!isClient) {
                                    net.minecraft.world.item.Item newCollarItem = net.yigitguven.petting.init.PettingModItems.getCollar(dyeColor).get();
                                    ItemStack newCollar = new ItemStack(newCollarItem);
                                    newCollar.applyComponents(currentCollar.getComponents());
                                    PetHelper.setCollar(mob, newCollar);
                                    if (!player.getAbilities().instabuild) {
                                        stack.shrink(1);
                                    }
                                    mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                                }
                                event.setCancellationResult(InteractionResult.SUCCESS);
                                event.setCanceled(true);
                                return;
                            }
                        }
                    }
                }

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
                if (PetHelper.isInteractionItem(mob, stack)) {
                    PetHelper.syncEntityTameStatus(mob);
                    return;
                }
                PetHelper.syncEntityTameStatus(mob);
                if (PettingServerConfig.isPetSittingEnabled()) {
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
                return;
            } else {
                if (!isClient) {
                    player.sendSystemMessage(Component.translatable("petting.action.already_tamed_other"));
                }
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
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

        if (!isClient && mob.level() instanceof ServerLevel serverLevel) {
            if (PetHelper.hasReachedPetLimit(player, serverLevel)) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(), 8, 0.4D, 0.4D, 0.4D, 0.0D);
                mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.sendSystemMessage(Component.translatable("petting.action.pet_limit_reached", PettingServerConfig.getMaxPetCount()));
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }
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

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!PettingServerConfig.isCollarRespawnEnabled() || !PettingServerConfig.isPetInventoryEnabled()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof net.yigitguven.petting.item.CollarItem)) {
            return;
        }

        net.minecraft.core.BlockPos pos = event.getPos();
        net.minecraft.world.level.Level level = event.getLevel();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);

        boolean isBed = PetHelper.isBedBlock(state);
        boolean isCushion = PetHelper.isCushionBlock(state);

        if (!isBed && !isCushion) {
            return;
        }

        Player player = event.getEntity();
        if (!level.isClientSide()) {
            net.minecraft.core.GlobalPos globalPos = net.minecraft.core.GlobalPos.of(level.dimension(), pos);
            net.yigitguven.petting.data.CollarRespawnPoint point = new net.yigitguven.petting.data.CollarRespawnPoint(globalPos, isCushion);
            stack.set(net.yigitguven.petting.init.PettingDataComponents.COLLAR_RESPAWN_POINT, point);
            stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.2);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.2F);

            Component targetType = Component.translatable(isCushion ? "petting.term.cushion" : "petting.term.bed");
            player.sendSystemMessage(Component.translatable("petting.action.collar_bound", targetType, pos.getX(), pos.getY(), pos.getZ()));
        }

        player.swing(event.getHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean handleCushionBinding(Player player, net.minecraft.world.entity.Entity target, ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.InteractionHand hand) {
        if (!PettingServerConfig.isCollarRespawnEnabled() || !PettingServerConfig.isPetInventoryEnabled()) {
            return false;
        }
        if (!(stack.getItem() instanceof net.yigitguven.petting.item.CollarItem)) {
            return false;
        }

        net.minecraft.core.BlockPos pos = target.blockPosition();
        if (!level.isClientSide()) {
            net.minecraft.core.GlobalPos globalPos = net.minecraft.core.GlobalPos.of(level.dimension(), pos);
            net.yigitguven.petting.data.CollarRespawnPoint point = new net.yigitguven.petting.data.CollarRespawnPoint(globalPos, true);
            stack.set(net.yigitguven.petting.init.PettingDataComponents.COLLAR_RESPAWN_POINT, point);
            stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.2);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.0);
            level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.2F);

            Component targetType = Component.translatable("petting.term.cushion");
            player.sendSystemMessage(Component.translatable("petting.action.collar_bound", targetType, pos.getX(), pos.getY(), pos.getZ()));
        }

        player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return true;
    }
}
