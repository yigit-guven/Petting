package net.yigitguven.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class OwnerRightclicksPetProcedure {
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;
        execute(event, event.getTarget(), event.getEntity());
    }

    public static void execute(Entity entity, Entity sourceentity) {
        execute(null, entity, sourceentity);
    }

    private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
            
        if (entity.level().isClientSide()) return;
        
        if (!(sourceentity instanceof Player player)) return;

        if ((entity.getPersistentData().getString("ownerUUID")).equals(player.getStringUUID())) {
            
            net.yigitguven.petting.config.PettingConfig.ControlScheme scheme = net.yigitguven.petting.config.PettingConfig.CONTROL_SCHEME.get();
            boolean isShift = player.isShiftKeyDown();
            
            boolean shouldHandle = false;
            boolean isCycleMode = false;
            boolean cycleSit = false;
            boolean cycleWait = false;
            
            if (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT) {
                shouldHandle = true;
                isCycleMode = false;
            } else if (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.RIGHT_CLICK_CYCLE) {
                if (!isShift) {
                    shouldHandle = true;
                    isCycleMode = true;
                }
            } else if (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.SHIFT_RIGHT_CLICK_CYCLE) {
                if (isShift) {
                    shouldHandle = true;
                    isCycleMode = true;
                }
            }

            if (shouldHandle) {
                player.swing(InteractionHand.MAIN_HAND, true);
                    
                boolean isSitting = entity.getPersistentData().getBoolean("sitstill");
                boolean isWaiting = entity.getPersistentData().getBoolean("waiting");
                String petName = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();
                
                if (!isCycleMode) {
                    // RIGHT_CLICK_SIT_SHIFT_WAIT
                    if (isShift) { // Toggles Wait
                        if (isSitting) {
                            entity.getPersistentData().putBoolean("sitstill", false);
                        }
                        if (isWaiting) {
                            entity.getPersistentData().putBoolean("waiting", false);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now wandering.");
                            playStateChangeFeedback(entity, false);
                        } else {
                            entity.getPersistentData().putBoolean("waiting", true);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now waiting.");
                            playStateChangeFeedback(entity, true);
                        }
                    } else { // Toggles Sit
                        if (isWaiting) {
                            entity.getPersistentData().putBoolean("waiting", false);
                        }
                        if (isSitting) {
                            entity.getPersistentData().putBoolean("sitstill", false);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now wandering.");
                            playStateChangeFeedback(entity, false);
                        } else {
                            entity.getPersistentData().putBoolean("sitstill", true);
                            entity.setShiftKeyDown(true);
                            sendFeedback(player, petName + " is now sitting and relaxing.");
                            playStateChangeFeedback(entity, true);
                        }
                    }
                } else {
                    // CYCLE MODE
                    if (!isSitting && !isWaiting) {
                        entity.getPersistentData().putBoolean("sitstill", true);
                        entity.getPersistentData().putBoolean("waiting", false);
                        entity.setShiftKeyDown(true); // Visual indicator
                        sendFeedback(player, petName + " is now sitting and relaxing.");
                        playStateChangeFeedback(entity, true);
                    } else if (isSitting) {
                        entity.getPersistentData().putBoolean("sitstill", false);
                        entity.getPersistentData().putBoolean("waiting", true);
                        entity.setShiftKeyDown(false);
                        sendFeedback(player, petName + " is now waiting.");
                        playStateChangeFeedback(entity, true);
                    } else {
                        entity.getPersistentData().putBoolean("sitstill", false);
                        entity.getPersistentData().putBoolean("waiting", false);
                        entity.setShiftKeyDown(false);
                        sendFeedback(player, petName + " is now wandering.");
                        playStateChangeFeedback(entity, false);
                    }
                }
                
                if (event != null && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    private static void sendFeedback(Player player, String message) {
        net.yigitguven.petting.config.PettingConfig.FeedbackStyle style = net.yigitguven.petting.config.PettingConfig.COMMAND_FEEDBACK_STYLE.get();
        if (style == net.yigitguven.petting.config.PettingConfig.FeedbackStyle.CHAT) {
            player.sendSystemMessage(Component.literal(message));
        } else if (style == net.yigitguven.petting.config.PettingConfig.FeedbackStyle.ACTION_BAR) {
            player.displayClientMessage(Component.literal(message), true);
        }
        // NONE does nothing, staying silent.
    }

    private static void playStateChangeFeedback(Entity entity, boolean isStopping) {
        net.minecraft.world.level.Level world = entity.level();
        if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (isStopping) {
                // Happy/Stopping feedback
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    3, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.get(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.2F);
            } else {
                // Resuming/Wandering feedback
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    10, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_SNARE.get(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.7F, 1.5F);
            }
        }
    }
}
