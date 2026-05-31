package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;

@EventBusSubscriber
public class OwnerRightclicksPetProcedure {
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL)
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.isCanceled())
            return;
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;
        
        Entity target = event.getTarget();
        // In NeoForge 1.21.1 PartEntity check might be different or needed
        if (target instanceof net.neoforged.neoforge.entity.PartEntity<?> part) {
            target = part.getParent();
        }
        
        execute(event, target, event.getEntity());
    }

    private static void execute(PlayerInteractEvent.EntityInteract event, Entity entity, Player player) {
        if (entity == null || player == null)
            return;
            
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;

        boolean isClient = entity.level().isClientSide();
        CompoundTag data = entity.getPersistentData();
        String petName = entity.hasCustomName() ? entity.getCustomName().getString() : entity.getType().getDescription().getString();

        if (isOwner(entity, player)) {
            net.minecraft.world.item.ItemStack heldItem = player.getMainHandItem();
            net.minecraft.world.item.Item item = heldItem.getItem();
            boolean isShift = player.isShiftKeyDown();
            
            if (heldItem.isEmpty()) {
                if (!isClient) {
                    if (isShift) {
                        if (!net.yigitguven.petting.util.PetInventoryUtil.isInventoryAllowed(entity)) return;

                        final Entity target = entity;
                        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                            (id, inv, p) -> new net.yigitguven.petting.world.inventory.PetInventoryMenu(id, inv, target),
                            target.getDisplayName()
                        ), buf -> buf.writeInt(target.getId()));
                    } else {
                        boolean isSitting = data.getBoolean("sitstill");
                        boolean isWaiting = data.getBoolean("waiting");
                        boolean isFreewander = data.getBoolean("freewander");
                        boolean hasSaddle = net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(entity);
                        boolean isRideable = net.yigitguven.petting.util.PetInventoryUtil.isRidingAllowed(entity);
                        
                        if (isSitting || isWaiting || isFreewander) {
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", false);
                            data.putBoolean("freewander", false);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(false);
                            }
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, petName + " is now following you.");
                            playStateChangeFeedback(entity, false);
                        } else if (hasSaddle && isRideable) {
                            player.startRiding(entity, true);
                            if (net.yigitguven.petting.config.PettingConfig.MOUNT_REQUIRE_SADDLE.get()) {
                                sendFeedback(player, "§6[Riding] §fYou are now riding " + petName + ". (Saddle Active)");
                            } else {
                                sendFeedback(player, "§6[Riding] §fYou are now riding " + petName + ".");
                            }
                        } else {
                            data.putBoolean("sitstill", true);
                            data.putBoolean("waiting", false);
                            data.putBoolean("freewander", false);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(true);
                            }
                            entity.setShiftKeyDown(true);
                            sendFeedback(player, petName + " is now sitting and relaxing.");
                            playStateChangeFeedback(entity, true);
                        }
                    }
                    player.swing(InteractionHand.MAIN_HAND, true);
                }
                cancelInteraction(event);
                return;
            }

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.STATUS_TOOL_ID.get(), net.minecraft.world.item.Items.STICK) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_STATUS.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    if (isShift) {
                        String mode = "§eFollowing";
                        if (data.getBoolean("sitstill")) mode = "§eSitting";
                        else if (data.getBoolean("waiting")) mode = "§eWaiting";
                        else if (data.getBoolean("freewander")) mode = "§eWandering (Free)";

                        player.sendSystemMessage(Component.literal("§6--- Pet Status: §f" + petName + " §6---"));
                        player.sendSystemMessage(Component.literal("§eAI Mode: " + mode));
                        player.sendSystemMessage(Component.literal("§eAggressive Mode: " + (data.getBoolean("attackifownerattacks") ? "§aON" : "§cOFF")));
                        player.sendSystemMessage(Component.literal("§eGuard Owner: " + (data.getBoolean("attackifownerattacked") ? "§aON" : "§cOFF")));
                        player.sendSystemMessage(Component.literal("§eRetaliate (Self): " + (data.getBoolean("attackifselfattacked") ? "§aON" : "§cOFF")));
                        player.sendSystemMessage(Component.literal("§eFollow Distance: §f" + data.getInt("followdistance")));
                        player.sendSystemMessage(Component.literal("§eTeleport Distance: §f" + data.getInt("teleportdistance")));
                        player.sendSystemMessage(Component.literal("§eWhistle Response: " + (data.getBoolean("ignoreWhistle") ? "§cIgnored" : "§aNormal")));
                    } else {
                        boolean isSitting = data.getBoolean("sitstill");
                        boolean isWaiting = data.getBoolean("waiting");
                        boolean isFreewander = data.getBoolean("freewander");
                        
                        if (!isSitting && !isWaiting && !isFreewander) { 
                            data.putBoolean("sitstill", true);
                            data.putBoolean("waiting", false);
                            data.putBoolean("freewander", false);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(true);
                            }
                            entity.setShiftKeyDown(true);
                            sendFeedback(player, "§6[Mode] §f" + petName + " is now §eSitting§f.");
                            playStateChangeFeedback(entity, true);
                        } else if (isSitting) { 
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", true);
                            data.putBoolean("freewander", false);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(false);
                            }
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, "§6[Mode] §f" + petName + " is now §eWaiting§f.");
                            playStateChangeFeedback(entity, true);
                        } else if (isWaiting) { 
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", false);
                            data.putBoolean("freewander", true);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(false);
                            }
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, "§6[Mode] §f" + petName + " is now §eWandering§f.");
                            playStateChangeFeedback(entity, false);
                        } else { 
                            data.putBoolean("sitstill", false);
                            data.putBoolean("waiting", false);
                            data.putBoolean("freewander", false);
                            if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                                tamable.setOrderedToSit(false);
                            }
                            entity.setShiftKeyDown(false);
                            sendFeedback(player, "§6[Mode] §f" + petName + " is now §eFollowing§f.");
                            playStateChangeFeedback(entity, false);
                        }
                    }
                }
                cancelInteraction(event);
                return;
            }

            net.minecraft.world.item.Item aggressionTool = net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.AGGRESSION_TOOL_ID.get(), net.minecraft.world.item.Items.IRON_SWORD);
            if ((item == aggressionTool || (net.yigitguven.petting.config.PettingConfig.AGGRESSION_TOOL_ID.get().equals("minecraft:iron_sword") && item instanceof net.minecraft.world.item.SwordItem)) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_AGGRESSION.get()) {
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

            net.minecraft.world.item.Item defenseTool = net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.DEFENSE_TOOL_ID.get(), net.minecraft.world.item.Items.SHIELD);
            if ((item == defenseTool || (net.yigitguven.petting.config.PettingConfig.DEFENSE_TOOL_ID.get().equals("minecraft:shield") && item instanceof net.minecraft.world.item.ShieldItem)) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_SELF_DEFENSE.get()) {
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

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.GUARD_TOOL_ID.get(), net.minecraft.world.item.Items.COOKIE) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_GUARD.get()) {
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

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.FOLLOW_DIST_TOOL_ID.get(), net.yigitguven.petting.init.PettingModItems.FOLLOW_WHISTLE.get()) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_FOLLOW_DIST.get()) {
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

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.TELEPORT_DIST_TOOL_ID.get(), net.yigitguven.petting.init.PettingModItems.TELEPORT_ORB.get()) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_TELEPORT_DIST.get()) {
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

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.WHISTLE_RESPONSE_TOOL_ID.get(), net.minecraft.world.item.Items.CLOCK) && net.yigitguven.petting.config.PettingConfig.ALLOW_PER_PET_WHISTLE_TOGGLE.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    boolean current = data.getBoolean("ignoreWhistle");
                    boolean newVal = !current;
                    // Use applyUpdate so the change is validated and synced to all clients
                    net.yigitguven.petting.network.EntitySettingsServer.applyUpdate((net.minecraft.server.level.ServerPlayer) player, entity.getId(), "ignoreWhistle", Boolean.toString(newVal));
                    sendFeedback(player, "§6[Whistle] §f" + petName + " will now " + (newVal ? "§cignore" : "§arespond to") + " §fwhistles.");
                    playStateChangeFeedback(entity, current);
                    playControlSound(entity, !current);
                }
                cancelInteraction(event);
                return;
            }

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.TETHER_TOOL_ID.get(), net.yigitguven.petting.init.PettingModItems.PET_TETHER.get()) && net.yigitguven.petting.config.PettingConfig.ALLOW_PET_TETHERING.get()) {
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

            net.minecraft.world.item.Item releaseTool = net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.RELEASE_TOOL_ID.get(), net.minecraft.world.item.Items.SHEARS);
            if ((item == releaseTool || (net.yigitguven.petting.config.PettingConfig.RELEASE_TOOL_ID.get().equals("minecraft:shears") && item instanceof net.minecraft.world.item.ShearsItem)) && player.isShiftKeyDown() && net.yigitguven.petting.config.PettingConfig.ALLOW_PET_RELEASING.get()) {
                if (!isClient) {
                    player.swing(InteractionHand.MAIN_HAND, true);
                    
                    if (data.getBoolean("isNameGenerated")) {
                        entity.setCustomName(null);
                    }

                    data.remove("pettingtamed");
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity, 
                        new net.yigitguven.petting.network.SyncPetStatusPayload(entity.getId(), false));
                        
                    data.remove("ownerUUID");
                    data.remove("isNameGenerated");
                    data.remove("sitstill");
                    data.remove("waiting");
                    data.remove("freewander");
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

            if (item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.TELEPORT_DIST_TOOL_ID.get(), net.yigitguven.petting.init.PettingModItems.TELEPORT_ORB.get()) ||
                item == net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(net.yigitguven.petting.config.PettingConfig.FOLLOW_DIST_TOOL_ID.get(), net.yigitguven.petting.init.PettingModItems.FOLLOW_WHISTLE.get())) {
                cancelInteraction(event);
            }
        }
    }

    private static boolean isOwner(Entity entity, Player player) {
        if (entity == null || player == null) return false;
        
        CompoundTag data = entity.getPersistentData();
        if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(player.getStringUUID())) {
            return true;
        }
        
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tamable) {
            if (tamable.isOwnedBy(player)) return true;
        }
        
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            String expectedPrefix = player.getDisplayName().getString() + "'s";
            if (name.startsWith(expectedPrefix)) return true;
        }
        
        return false;
    }

    private static void cancelInteraction(PlayerInteractEvent.EntityInteract event) {
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void sendFeedback(Player player, String message) {
        net.yigitguven.petting.config.PettingConfig.FeedbackStyle style = net.yigitguven.petting.config.PettingConfig.COMMAND_FEEDBACK_STYLE.get();
        if (style == net.yigitguven.petting.config.PettingConfig.FeedbackStyle.CHAT) {
            player.sendSystemMessage(Component.literal(message));
        } else if (style == net.yigitguven.petting.config.PettingConfig.FeedbackStyle.ACTION_BAR) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    private static void playControlSound(Entity entity, boolean positive) {
        net.minecraft.world.level.Level world = entity.level();
        world.playSound(null, entity.blockPosition(), 
            positive ? net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value() : net.minecraft.sounds.SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 
            net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, positive ? 1.5F : 0.8F);
    }

    private static void playStateChangeFeedback(Entity entity, boolean isStopping) {
        net.minecraft.world.level.Level world = entity.level();
        if (world instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (isStopping) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.NOTE, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    3, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_CHIME.value(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.2F);
            } else {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH, 
                    entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 
                    10, 0.2, 0.2, 0.2, 0.0);
                world.playSound(null, entity.blockPosition(), net.minecraft.sounds.SoundEvents.NOTE_BLOCK_SNARE.value(), 
                    net.minecraft.sounds.SoundSource.NEUTRAL, 0.7F, 1.5F);
            }
        }
    }
}
