package net.yigitguven.petting.procedures;

import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.config.PettingConfig;

public class OwnerRightclicksPetProcedure {

    public static void register() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND) {
                return InteractionResult.PASS;
            }
            
            InteractionResult result = execute(entity, player);
            if (result != InteractionResult.PASS) return result;
            
            return GoldenWheatRightclickedProcedure.execute(entity, player);
        });
    }

    public static InteractionResult execute(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return InteractionResult.PASS;
            
        if (!(sourceentity instanceof Player player)) return InteractionResult.PASS;
        boolean isClient = entity.level().isClientSide();
        CompoundTag data = ((IEntityData) entity).getPersistentData();
        String petName = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();

        if ((data.getString("ownerUUID")).equals(player.getStringUUID())) {
            net.minecraft.world.item.ItemStack heldItem = player.getMainHandItem();
            net.minecraft.world.item.Item item = heldItem.getItem();

            // DETECTION for Name Tag usage to clear "automatic" flag
            if (item == net.minecraft.world.item.Items.NAME_TAG) {
                if (!isClient) data.remove("isNameGenerated");
            }

            // 1. STICK (Status Report)
            if (item == net.minecraft.world.item.Items.STICK && PettingConfig.allowPerPetStatus) {
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
                return InteractionResult.SUCCESS;
            }

            // 2. SWORD (Toggle Aggressive Mode)
            if (item instanceof net.minecraft.world.item.SwordItem && PettingConfig.allowPerPetAggression) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifownerattacks");
                    data.putBoolean("attackifownerattacks", !current);
                    sendFeedback(player, "§6[Aggression] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fattack your targets.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                return InteractionResult.SUCCESS;
            }

            // 3. SHIELD (Toggle Retaliation)
            if (item instanceof net.minecraft.world.item.ShieldItem && PettingConfig.allowPerPetSelfDefense) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifselfattacked");
                    data.putBoolean("attackifselfattacked", !current);
                    sendFeedback(player, "§6[Retaliation] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fdefend itself.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                return InteractionResult.SUCCESS;
            }

            // 4. COOKIE (Toggle Guard Owner)
            if (item == net.minecraft.world.item.Items.COOKIE && PettingConfig.allowPerPetGuard) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("attackifownerattacked");
                    data.putBoolean("attackifownerattacked", !current);
                    sendFeedback(player, "§6[Guard] §f" + petName + " will " + (!current ? "§anow" : "§cno longer") + " §fprotect you from attackers.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                return InteractionResult.SUCCESS;
            }

            // 5. LEAD (Cycle Follow Distance)
            if (item == net.minecraft.world.item.Items.LEAD && PettingConfig.allowPerPetFollowDist) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    int current = data.getInt("followdistance");
                    if (current == 0) current = 10;
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
                return InteractionResult.SUCCESS;
            }

            // 6. ENDER PEARL (Cycle Teleport Distance)
            if (item == net.minecraft.world.item.Items.ENDER_PEARL && PettingConfig.allowPerPetTeleportDist) {
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
                return InteractionResult.SUCCESS;
            }

            // 7. CLOCK (Toggle Whistle Response)
            if (item == net.minecraft.world.item.Items.CLOCK && PettingConfig.allowPerPetWhistleToggle) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("ignoreWhistle");
                    data.putBoolean("ignoreWhistle", !current);
                    sendFeedback(player, "§6[Whistle] §f" + petName + " will now " + (!current ? "§cignore" : "§arespond to") + " §fwhistles.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                return InteractionResult.SUCCESS;
            }

            // 8. PET TETHER (Toggle Binding)
            if (item == net.yigitguven.petting.init.PettingModItems.PET_TETHER && PettingConfig.allowPetTethering) {
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
                return InteractionResult.SUCCESS;
            }

            // 9. SHEARS (Release Pet - Crouch REQUIRED)
            if (item instanceof net.minecraft.world.item.ShearsItem && player.isShiftKeyDown() && PettingConfig.allowPetReleasing) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    data.remove("pettingtamed");
                    data.remove("ownerUUID");
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
                    
                    Level world = entity.level();
                    world.playSound(null, entity.blockPosition(), SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.5, entity.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
                    }
                    sendFeedback(player, "§c[Released] §f" + petName + " is no longer your pet and has returned to the wild.");
                }
                return InteractionResult.SUCCESS;
            }

            // HANDLE SIT/WAIT states
            PettingConfig.ControlScheme scheme = PettingConfig.controlScheme;
            boolean isShift = player.isShiftKeyDown();
            
            boolean shouldHandle = (scheme == PettingConfig.ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT)
                    || (scheme == PettingConfig.ControlScheme.RIGHT_CLICK_CYCLE && !isShift)
                    || (scheme == PettingConfig.ControlScheme.SHIFT_RIGHT_CLICK_CYCLE && isShift);

            if (shouldHandle) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean isSitting = data.getBoolean("sitstill");
                    boolean isWaiting = data.getBoolean("waiting");
                    
                    if (scheme == PettingConfig.ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT) {
                        if (isShift) { // Wait
                            data.putBoolean("sitstill", false);
                            boolean targetWait = !isWaiting;
                            data.putBoolean("waiting", targetWait);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + (targetWait ? " is now waiting." : " is now wandering."));
                            playStateChangeFeedback(entity, targetWait);
                        } else { // Sit
                            data.putBoolean("waiting", false);
                            boolean targetSit = !isSitting;
                            data.putBoolean("sitstill", targetSit);
                            entity.setShiftKeyDown(targetSit);
                            sendFeedback(player, petName + (targetSit ? " is now sitting and relaxing." : " is now wandering."));
                            playStateChangeFeedback(entity, targetSit);
                        }
                    } else { // CYCLE
                        if (!isSitting && !isWaiting) {
                            data.putBoolean("sitstill", true);
                            data.putBoolean("waiting", false);
                            entity.setShiftKeyDown(true);
                            sendFeedback(player, petName + " is now sitting and relaxing.");
                            playStateChangeFeedback(entity, true);
                        } else if (isSitting) {
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", true);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now waiting.");
                            playStateChangeFeedback(entity, true);
                        } else {
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", false);
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now wandering.");
                            playStateChangeFeedback(entity, false);
                        }
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static void sendFeedback(Player player, String message) {
        PettingConfig.FeedbackStyle style = PettingConfig.commandFeedbackStyle;
        if (style == PettingConfig.FeedbackStyle.CHAT) {
            player.sendSystemMessage(Component.literal(message));
        } else if (style == PettingConfig.FeedbackStyle.ACTION_BAR) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    private static void playControlSound(Entity entity, boolean positive) {
        Level world = entity.level();
        world.playSound(null, entity.blockPosition(), 
            positive ? SoundEvents.UI_BUTTON_CLICK.value() : SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 
            SoundSource.PLAYERS, 0.5F, positive ? 1.5F : 0.8F);
    }

    private static void playStateChangeFeedback(Entity entity, boolean isStopping) {
        Level world = entity.level();
        if (world instanceof ServerLevel serverLevel) {
            if (isStopping) {
                serverLevel.sendParticles(ParticleTypes.NOTE, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    3, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), 
                    SoundSource.NEUTRAL, 1.0F, 1.2F);
            } else {
                serverLevel.sendParticles(ParticleTypes.ASH, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    10, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE.value(), 
                    SoundSource.NEUTRAL, 0.7F, 1.5F);
            }
        }
    }
}
