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
            
        if (!(sourceentity instanceof Player player)) return;
        boolean isClient = entity.level().isClientSide();
        CompoundTag data = entity.getPersistentData();
        String petName = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();

        if (isOwner(entity, player)) {
            net.minecraft.world.item.ItemStack heldItem = player.getMainHandItem();
            net.minecraft.world.item.Item item = heldItem.getItem();
            
            // DETECTION for Name Tag usage to clear "automatic" flag
            if (item == net.minecraft.world.item.Items.NAME_TAG) {
                if (!isClient) data.remove("isNameGenerated");
                // Don't cancel here, let vanilla handle name tagging if they wish, 
                // OR cancel if we want to manage it. For now, let's just detect it.
            }

            // 1. STICK (Status Report)
            if (item == net.minecraft.world.item.Items.STICK && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_STATUS.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    player.sendSystemMessage(Component.literal("§6--- Pet Status: §f" + petName + " §6---"));
                    player.sendSystemMessage(Component.literal("§eAggressive Mode: " + (data.getBoolean("attackifownerattacks") ? "§aON" : "§cOFF")));
                    player.sendSystemMessage(Component.literal("§eGuard Owner: " + (data.getBoolean("attackifownerattacked") ? "§aON" : "§cOFF")));
                    player.sendSystemMessage(Component.literal("§eRetaliate (Self): " + (data.getBoolean("attackifselfattacked") ? "§aON" : "§cOFF")));
                    player.sendSystemMessage(Component.literal("§eFollow Distance: §f" + data.getInt("followdistance")));
                    player.sendSystemMessage(Component.literal("§eTeleport Distance: §f" + data.getInt("teleportdistance")));
                    player.sendSystemMessage(Component.literal("§eWhistle Response: " + (data.getBoolean("ignoreWhistle") ? "§cIgnored" : "§aNormal")));
                }
                cancelInteraction(event);
                return;
            }

            // 2. SWORD (Toggle Aggressive Mode)
            if (item instanceof net.minecraft.world.item.SwordItem && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_AGGRESSION.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifownerattacks");
                    data.putBoolean("attackifownerattacks", !current);
                    sendFeedback(player, "§6[Aggression] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fattack your targets.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                cancelInteraction(event);
                return;
            }

            // 3. SHIELD (Toggle Retaliation)
            if (item instanceof net.minecraft.world.item.ShieldItem && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_SELF_DEFENSE.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifselfattacked");
                    data.putBoolean("attackifselfattacked", !current);
                    sendFeedback(player, "§6[Retaliation] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fdefend itself.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                cancelInteraction(event);
                return;
            }

            // 4. COOKIE (Toggle Guard Owner)
            if (item == net.minecraft.world.item.Items.COOKIE && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_GUARD.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifownerattacked");
                    data.putBoolean("attackifownerattacked", !current);
                    sendFeedback(player, "§6[Guard] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fprotect you from attackers.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                cancelInteraction(event);
                return;
            }

            // 5. FOLLOW WHISTLE (Cycle Follow Distance)
            if (item == net.yigitguven.petting.init.PettingModItems.FOLLOW_WHISTLE.get() && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_FOLLOW_DIST.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    int current = data.getInt("followdistance");
                    if (current == 0) current = 5;
                    int next = switch (current) {
                        case 5 -> 10;
                        case 10 -> 20;
                        case 20 -> 50;
                        default -> 5;
                    };
                    data.putInt("followdistance", next);
                    sendFeedback(player, "§6[Follow] §f" + petName + " follow distance set to: §e" + next);
                    playStateChangeFeedback(entity, false);
                    playControlSound(entity, true);
                }
                cancelInteraction(event);
                return;
            }

            // 6. TELEPORT ORB (Cycle Teleport Distance)
            if (item == net.yigitguven.petting.init.PettingModItems.TELEPORT_ORB.get() && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_TELEPORT_DIST.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    int current = data.getInt("teleportdistance");
                    if (current == 0) current = 20;
                    int next = switch (current) {
                        case 10 -> 20;
                        case 20 -> 50;
                        case 50 -> 100;
                        default -> 10;
                    };
                    data.putInt("teleportdistance", next);
                    sendFeedback(player, "§6[Teleport] §f" + petName + " teleport distance set to: §e" + next);
                    playStateChangeFeedback(entity, false);
                    playControlSound(entity, true);
                }
                cancelInteraction(event);
                return;
            }

            // 7. CLOCK (Toggle Whistle Response)
            if (item == net.minecraft.world.item.Items.CLOCK && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_WHISTLE_TOGGLE.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("ignoreWhistle");
                    data.putBoolean("ignoreWhistle", !current);
                    sendFeedback(player, "§6[Whistle] §f" + petName + " will now " + (!current ? "§cignore" : "§arespond to") + " §fwhistles.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                cancelInteraction(event);
                return;
            }

            // 8. PET TETHER (Toggle Binding)
            if (item == net.yigitguven.petting.init.PettingModItems.PET_TETHER.get() && net.yigitguven.petting.config.PettingConfig.ALLOW_PET_TETHERING.get()) {
                if (!isClient) {
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
                }
                cancelInteraction(event);
                return;
            }

            // 9. SHEARS (Release Pet - Crouch REQUIRED)
            if (item instanceof net.minecraft.world.item.ShearsItem && player.isShiftKeyDown() && net.yigitguven.petting.config.PettingConfig.ALLOW_PET_RELEASING.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    
                    if (data.getBoolean("isNameGenerated")) {
                        entity.setCustomName(null);
                    }

                    data.remove("pettingtamed");
                    data.remove("ownerUUID");
                    data.remove("isNameGenerated");
                    data.remove("sitstill");
                    data.remove("waiting");
                    data.remove("pettingbound");
                    data.remove("boundX");
                    data.remove("boundY");
                    data.remove("boundZ");
                    data.remove("attackifownerattacks");
                    data.remove("attackifownerattacked");
                    data.remove("attackifselfattacked");
                    data.remove("ignoreWhistle");
                    
                    net.minecraft.world.level.Level world = entity.level();
                    world.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.SHEEP_SHEAR, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.5, entity.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                    }
                    sendFeedback(player, "§c[Released] §f" + petName + " is no longer your pet and has returned to the wild.");
                }
                cancelInteraction(event);
                return;
            }

            // HANDLE SIT/WAIT states
            net.yigitguven.petting.config.PettingConfig.ControlScheme scheme = net.yigitguven.petting.config.PettingConfig.CONTROL_SCHEME.get();
            boolean isShift = player.isShiftKeyDown();
            
            boolean shouldHandle = (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT)
                    || (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.RIGHT_CLICK_CYCLE && !isShift)
                    || (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.SHIFT_RIGHT_CLICK_CYCLE && isShift);

            if (shouldHandle) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean isSitting = data.getBoolean("sitstill");
                    boolean isWaiting = data.getBoolean("waiting");
                    
                    if (scheme == net.yigitguven.petting.config.PettingConfig.ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT) {
                        if (isShift) { // Stand up / Wait toggle
                            boolean targetWander = isSitting; // If sitting, standing up means wandering
                            if (isSitting) {
                                data.putBoolean("sitstill", false);
                                entity.setShiftKeyDown(false);
                                sendFeedback(player, petName + " is now wandering.");
                                playStateChangeFeedback(entity, false);
                            } else {
                                // Original Wait toggle
                                data.putBoolean("sitstill", false);
                                boolean targetWait = !isWaiting;
                                data.putBoolean("waiting", targetWait);
                                entity.setShiftKeyDown(false);
                                sendFeedback(player, petName + (targetWait ? " is now waiting." : " is now wandering."));
                                playStateChangeFeedback(entity, targetWait);
                            }
                        } else { // Sit / Ride logic
                            boolean hasSaddle = net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(entity);
                            boolean isRideable = net.yigitguven.petting.util.PetInventoryUtil.isRidingAllowed(entity);
                            boolean canRide;
                            
                            if (net.yigitguven.petting.config.PettingConfig.MOUNT_REQUIRE_SADDLE.get()) {
                                canRide = hasSaddle && isRideable;
                            } else {
                                canRide = (isSitting || hasSaddle) && isRideable;
                            }

                            if (canRide && heldItem.isEmpty()) {
                                // RIDE if sitting OR saddled, and hand is empty
                                player.startRiding(entity, true);
                                if (net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(entity)) {
                                    sendFeedback(player, "§6[Riding] §fYou are now riding " + petName + ". (Saddle Control Active)");
                                } else {
                                    sendFeedback(player, "§6[Riding] §fYou are now riding " + petName + ". (No Saddle)");
                                }
                            } else {
                                // Original Sit toggle
                                data.putBoolean("waiting", false);
                                boolean targetSit = !isSitting;
                                data.putBoolean("sitstill", targetSit);
                                entity.setShiftKeyDown(targetSit);
                                sendFeedback(player, petName + (targetSit ? " is now sitting and relaxing." : " is now wandering."));
                                playStateChangeFeedback(entity, targetSit);
                            }
                        }
                    } else { // CYCLE
                        if (!isSitting && !isWaiting) {
                            data.putBoolean("sitstill", true);
                            data.putBoolean("waiting", false);
                            entity.setShiftKeyDown(true);
                            sendFeedback(player, petName + " is now sitting and relaxing.");
                            playStateChangeFeedback(entity, true);
                        } else if (isSitting) {
                            boolean hasSaddle = net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(entity);
                            boolean isRideable = net.yigitguven.petting.util.PetInventoryUtil.isRidingAllowed(entity);
                            boolean canRide;
                            
                            if (net.yigitguven.petting.config.PettingConfig.MOUNT_REQUIRE_SADDLE.get()) {
                                canRide = hasSaddle && isRideable;
                            } else {
                                canRide = (isSitting || hasSaddle) && isRideable;
                            }

                            if (canRide && heldItem.isEmpty()) {
                                player.startRiding(entity, true);
                                sendFeedback(player, "§6[Riding] §fYou are now riding " + petName + ".");
                            } else {
                                data.putBoolean("sitstill", false);
                                data.putBoolean("waiting", true);
                                entity.setShiftKeyDown(false);
                                sendFeedback(player, petName + " is now waiting.");
                                playStateChangeFeedback(entity, true);
                            }
                        } else {
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", false);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now wandering.");
                            playStateChangeFeedback(entity, false);
                        }
                    }
                }
            
            // CATCH-ALL: If we are here and own the pet, and held item is something that would be used (like Ender Pearl),
            // we should probably cancel the interaction anyway if it's meant for petting controls.
            // Items like Ender Pearls, Goat Horns, etc. often trigger right-click actions.
                if (heldItem.isEdible() || item == net.minecraft.world.item.Items.ENDER_PEARL || item == net.yigitguven.petting.init.PettingModItems.TELEPORT_ORB.get() || item == net.yigitguven.petting.init.PettingModItems.FOLLOW_WHISTLE.get() || item instanceof net.minecraft.world.item.ChorusFruitItem) {
                    cancelInteraction(event);
                }
            }
        }
    }

    private static boolean isOwner(Entity entity, Player player) {
        if (entity == null || player == null) return false;
        
        // 1. Check NBT (Reliable on Server, potentially empty on Client depending on sync)
        CompoundTag data = entity.getPersistentData();
        if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(player.getStringUUID())) {
            return true;
        }
        
        // 2. Check TamableAnimal (Works on Client for Vanilla pets)
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }
        
        // 3. Fallback: Custom Name check (Client-side helper)
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            String expectedPrefix = player.getDisplayName().getString() + "'s";
            if (name.startsWith(expectedPrefix)) return true;
        }
        
        return false;
    }

    private static void cancelInteraction(@Nullable Event event) {
        if (event instanceof PlayerInteractEvent.EntityInteract interactEvent) {
            interactEvent.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            interactEvent.setCanceled(true);
        } else if (event != null && event.isCancelable()) {
            event.setCanceled(true);
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
