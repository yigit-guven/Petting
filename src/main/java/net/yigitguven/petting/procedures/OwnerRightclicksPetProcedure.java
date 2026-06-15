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
import net.minecraft.core.registries.BuiltInRegistries;

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
            if (data.getLong("pettingTamedTick") == entity.level().getGameTime()) {
                return InteractionResult.SUCCESS;
            }

            net.minecraft.world.item.ItemStack heldItem = player.getMainHandItem();
            net.minecraft.world.item.Item item = heldItem.getItem();

            // DETECTION for Name Tag usage to clear "automatic" flag
            if (item == net.minecraft.world.item.Items.NAME_TAG) {
                if (!isClient) data.remove("isNameGenerated");
            }
            
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            String itemStr = itemId != null ? itemId.toString() : "";

            // 1. STICK (Status Report)
            if (itemStr.equals(PettingConfig.statusTool) && PettingConfig.allowPerPetStatus) {
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
            if (itemStr.equals(PettingConfig.aggressionTool) && PettingConfig.allowPerPetAggression) {
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
            if (itemStr.equals(PettingConfig.defenseTool) && PettingConfig.allowPerPetSelfDefense) {
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
            if (itemStr.equals(PettingConfig.guardTool) && PettingConfig.allowPerPetGuard) {
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
            if (itemStr.equals(PettingConfig.followDistTool) && PettingConfig.allowPerPetFollowDist) {
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
            if (itemStr.equals(PettingConfig.teleportDistTool) && PettingConfig.allowPerPetTeleportDist) {
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
            if (itemStr.equals(PettingConfig.whistleTool) && PettingConfig.allowPerPetWhistleToggle) {
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
            if (itemStr.equals(PettingConfig.tetherTool) && PettingConfig.allowPetTethering) {
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
            if (itemStr.equals(PettingConfig.releaseTool) && player.isShiftKeyDown() && PettingConfig.allowPetReleasing) {
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

            // Custom Control Mappings
            boolean isShift = player.isShiftKeyDown();
            String mappingRules = isShift 
                ? (data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : (((IEntityData)player).getPersistentData().contains("petting_default_control_shift_right_click") ? ((IEntityData)player).getPersistentData().getString("petting_default_control_shift_right_click") : "CYCLE|NONE"))
                : (data.contains("control_right_click") ? data.getString("control_right_click") : (((IEntityData)player).getPersistentData().contains("petting_default_control_right_click") ? ((IEntityData)player).getPersistentData().getString("petting_default_control_right_click") : "SIT|NONE"));

            boolean handled = false;
            String[] rules = mappingRules.split(";");
            for (String rule : rules) {
                if (rule.isBlank()) continue;
                String[] parts = rule.split("\\|", 3);
                String action = parts[0];
                String condition = parts.length > 1 ? parts[1] : "NONE";
                String command = parts.length > 2 ? parts[2] : "";

                boolean conditionMet = false;
                switch (condition) {
                    case "NONE" -> conditionMet = true;
                    case "SADDLE" -> {
                        if (entity instanceof net.minecraft.world.entity.Saddleable saddleable) {
                            conditionMet = saddleable.isSaddled();
                        } else {
                            conditionMet = false; // Fallback
                        }
                    }
                    case "HEALTH_LT_50" -> {
                        if (entity instanceof LivingEntity le) {
                            conditionMet = (le.getHealth() / le.getMaxHealth() < 0.5f);
                        }
                    }
                    case "HOLD_ITEM" -> conditionMet = !item.equals(net.minecraft.world.item.Items.AIR);
                    case "SNEAK" -> conditionMet = isShift;
                }

                if (conditionMet) {
                    executeAction(action, command, entity, player, data, isClient, petName);
                    handled = true;
                    break;
                }
            }

            if (!handled && item.equals(net.minecraft.world.item.Items.AIR)) {
                // Default petting action (Hearts)
                if (!isClient) {
                    Level world = entity.level();
                    if (world instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight() + 0.5D, entity.getZ(), 3, 0.3, 0.3, 0.3, 0.1);
                        world.playSound(null, entity.blockPosition(), SoundEvents.WOLF_PANT, SoundSource.NEUTRAL, 1.0F, 1.0F);
                    }
                }
                player.swing(InteractionHand.MAIN_HAND, true);
                return InteractionResult.SUCCESS;
            }
            
            if (handled) {
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static void executeAction(String action, String command, Entity entity, Player player, CompoundTag data, boolean isClient, String petName) {
        if (!isClient) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }
        switch (action) {
            case "SIT":
                if (!isClient) {
                    boolean isSitting = data.getBoolean("sitstill");
                    data.putBoolean("sitstill", !isSitting);
                    data.putBoolean("waiting", false);
                    entity.setShiftKeyDown(!isSitting);
                    sendFeedback(player, petName + (!isSitting ? " is now sitting and relaxing." : " is now wandering."));
                    playStateChangeFeedback(entity, !isSitting);
                }
                break;
            case "TOGGLE_WAIT":
                if (!isClient) {
                    boolean isWaiting = data.getBoolean("waiting");
                    data.putBoolean("waiting", !isWaiting);
                    data.putBoolean("sitstill", false);
                    entity.setShiftKeyDown(false);
                    sendFeedback(player, petName + (!isWaiting ? " is now waiting." : " is now wandering."));
                    playStateChangeFeedback(entity, !isWaiting);
                }
                break;
            case "CYCLE":
                if (!isClient) {
                    int fd = data.getInt("followdistance");
                    if (fd == 5) {
                        data.putInt("followdistance", 10);
                        data.putInt("teleportdistance", 20);
                    } else if (fd == 10) {
                        data.putInt("followdistance", 20);
                        data.putInt("teleportdistance", 50);
                    } else {
                        data.putInt("followdistance", 5);
                        data.putInt("teleportdistance", 10);
                    }
                    sendFeedback(player, "§6[Distance] §f" + petName + " follow/teleport distance set to: §e" + data.getInt("followdistance") + "/" + data.getInt("teleportdistance"));
                    playControlSound(entity, true);
                }
                break;
            case "OPEN_SETTINGS":
                if (!isClient) {
                    net.yigitguven.petting.network.EntitySettingsServer.handleOpenRequest((net.minecraft.server.level.ServerPlayer)player, entity.getId());
                }
                break;
            case "OPEN_INV":
                if (!isClient) {
                    player.openMenu(net.yigitguven.petting.init.PettingModMenus.PET_INVENTORY, buf -> {
                        buf.writeInt(entity.getId());
                    });
                }
                break;
            case "RIDE":
                if (!isClient) {
                    player.startRiding(entity);
                }
                break;
            case "TOGGLE_FOLLOW_TELEPORT":
                if (!isClient) {
                    int fd = data.getInt("followdistance");
                    int td = data.getInt("teleportdistance");
                    data.putInt("followdistance", td);
                    data.putInt("teleportdistance", fd);
                    sendFeedback(player, "§6[Distance] §f" + petName + " follow/teleport distances swapped: §e" + data.getInt("followdistance") + "/" + data.getInt("teleportdistance"));
                    playControlSound(entity, true);
                }
                break;
            case "RUN_COMMAND":
                if (!isClient && command != null && !command.isBlank()) {
                    String cmd = command.replace("@pet", petName);
                    player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), cmd);
                }
                break;
            case "NONE":
            default:
                break;
        }
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
