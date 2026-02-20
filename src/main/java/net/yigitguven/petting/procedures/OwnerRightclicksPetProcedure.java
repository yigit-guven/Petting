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
import net.minecraft.nbt.CompoundTag;

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
            net.minecraft.world.item.ItemStack heldItem = player.getMainHandItem();
            net.minecraft.world.item.Item item = heldItem.getItem();
            CompoundTag data = entity.getPersistentData();
            String petName = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();

            // 1. STICK (Status Report)
            if (item == net.minecraft.world.item.Items.STICK && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_STATUS.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                player.sendSystemMessage(Component.literal("§6--- Pet Status: §f" + petName + " §6---"));
                player.sendSystemMessage(Component.literal("§eAggressive Mode: " + (data.getBoolean("attackifownerattacks") ? "§aON" : "§cOFF")));
                player.sendSystemMessage(Component.literal("§eGuard Owner: " + (data.getBoolean("attackifownerattacked") ? "§aON" : "§cOFF")));
                player.sendSystemMessage(Component.literal("§eRetaliate (Self): " + (data.getBoolean("attackifselfattacked") ? "§aON" : "§cOFF")));
                player.sendSystemMessage(Component.literal("§eFollow Distance: §f" + data.getInt("followdistance")));
                player.sendSystemMessage(Component.literal("§eTeleport Distance: §f" + data.getInt("teleportdistance")));
                player.sendSystemMessage(Component.literal("§eWhistle Response: " + (data.getBoolean("ignoreWhistle") ? "§cIgnored" : "§aNormal")));
                if (event != null && event.isCancelable()) event.setCanceled(true);
                return;
            }

            // 2. SWORD (Toggle Aggressive Mode)
            if (item instanceof net.minecraft.world.item.SwordItem && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_AGGRESSION.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean current = data.getBoolean("attackifownerattacks");
                data.putBoolean("attackifownerattacks", !current);
                sendFeedback(player, "§6[Aggression] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fattack your targets.");
                playStateChangeFeedback(entity, current);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, !current);
                return;
            }

            // 3. SHIELD (Toggle Retaliation)
            if (item instanceof net.minecraft.world.item.ShieldItem && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_SELF_DEFENSE.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean current = data.getBoolean("attackifselfattacked");
                data.putBoolean("attackifselfattacked", !current);
                sendFeedback(player, "§6[Retaliation] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fdefend itself.");
                playStateChangeFeedback(entity, current);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, !current);
                return;
            }

            // 4. COOKIE (Toggle Guard Owner)
            if (item == net.minecraft.world.item.Items.COOKIE && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_GUARD.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean current = data.getBoolean("attackifownerattacked");
                data.putBoolean("attackifownerattacked", !current);
                sendFeedback(player, "§6[Guard] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fprotect you from attackers.");
                playStateChangeFeedback(entity, current);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, !current);
                return;
            }

            // 5. LEAD (Cycle Follow Distance)
            if (item == net.minecraft.world.item.Items.LEAD && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_FOLLOW_DIST.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                int current = data.getInt("followdistance");
                if (current == 0) current = 10; // Default fallback
                int next = switch (current) {
                    case 5 -> 10;
                    case 10 -> 20;
                    case 20 -> 50;
                    default -> 5;
                };
                data.putInt("followdistance", next);
                sendFeedback(player, "§6[Follow] §f" + petName + " follow distance set to: §e" + next);
                playStateChangeFeedback(entity, false);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, true);
                return;
            }

            // 6. ENDER PEARL (Cycle Teleport Distance)
            if (item == net.minecraft.world.item.Items.ENDER_PEARL && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_TELEPORT_DIST.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                int current = data.getInt("teleportdistance");
                if (current == 0) current = 20; // Default fallback
                int next = switch (current) {
                    case 10 -> 20;
                    case 20 -> 50;
                    case 50 -> 100;
                    default -> 10;
                };
                data.putInt("teleportdistance", next);
                sendFeedback(player, "§6[Teleport] §f" + petName + " teleport distance set to: §e" + next);
                playStateChangeFeedback(entity, false);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, true);
                return;
            }

            // 7. CLOCK (Toggle Whistle Response)
            if (item == net.minecraft.world.item.Items.CLOCK && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_WHISTLE_TOGGLE.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean current = data.getBoolean("ignoreWhistle");
                data.putBoolean("ignoreWhistle", !current);
                sendFeedback(player, "§6[Whistle] §f" + petName + " will now " + (!current ? "§cignore" : "§arespond to") + " §fwhistles.");
                playStateChangeFeedback(entity, current);
                if (event != null && event.isCancelable()) event.setCanceled(true);
                playControlSound(entity, !current);
                return;
            }

            // 8. PET TETHER (Toggle Binding)
            if (item == net.yigitguven.petting.init.PettingModItems.PET_TETHER.get() && net.yigitguven.petting.config.PettingConfig.ALLOW_PET_TETHERING.get()) {
                player.swing(InteractionHand.MAIN_HAND, true);
                boolean isBound = data.getBoolean("pettingbound");
                if (!isBound) {
                    data.putBoolean("pettingbound", true);
                    data.putDouble("boundX", entity.getX());
                    data.putDouble("boundY", entity.getY());
                    data.putDouble("boundZ", entity.getZ());
                    sendFeedback(player, "§6[Bound] §f" + petName + " is now bound to this area.");
                    playStateChangeFeedback(entity, true);
                } else {
                    data.putBoolean("pettingbound", false);
                    sendFeedback(player, "§6[Bound] §f" + petName + " is no longer bound.");
                    playStateChangeFeedback(entity, false);
                }
                if (event != null && event.isCancelable()) event.setCanceled(true);
                return;
            }
        }

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

    private static void playControlSound(Entity entity, boolean positive) {
        net.minecraft.world.level.Level world = entity.level();
        world.playSound(null, entity.blockPosition(), 
            positive ? net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.get() : net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), 
            net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, positive ? 1.5F : 0.8F);
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
