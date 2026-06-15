package net.yigitguven.petting.procedures;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.PetAttackLogic;
import net.yigitguven.petting.config.PettingConfig;

import java.util.List;

public class GoldenWheatRightclickedProcedure {

    public static InteractionResult execute(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null) return InteractionResult.PASS;
        if (entity.level().isClientSide()) return InteractionResult.PASS;
        if (!(sourceentity instanceof Player player)) return InteractionResult.PASS;

        ItemStack itemInHand = player.getMainHandItem();
        ResourceLocation itemID = BuiltInRegistries.ITEM.getKey(itemInHand.getItem());
        if (itemID == null) return InteractionResult.PASS;
        
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityKey == null) return InteractionResult.PASS;
        
        String entityName = entityKey.toString();
        String itemName = itemID.toString();

        // 0. Check Custom Taming Items Config First
        boolean hasCustomItem = false;
        boolean customItemMatch = false;

        List<String> customItemsMap = PettingConfig.customTamingItems;
        for (String mapping : customItemsMap) {
            String[] parts = mapping.split("\\|");
            if (parts.length == 2 && parts[0].trim().equals(entityName)) {
                hasCustomItem = true;
                if (parts[1].trim().equals(itemName)) {
                    customItemMatch = true;
                }
                break;
            }
        }

        // Evaluate Taming Eligibility
        if (hasCustomItem) {
            if (!customItemMatch) return InteractionResult.PASS;
        } else {
            if (PettingConfig.tamingItem != null && !PettingConfig.tamingItem.isEmpty()) {
                if (!itemName.equals(PettingConfig.tamingItem)) return InteractionResult.PASS;
            } else {
                if (!itemName.equals("petting:golden_wheat")) return InteractionResult.PASS;
                if (!PettingConfig.allowGoldenWheat) return InteractionResult.PASS;
            }
        }

        long currentTime = entity.level().getGameTime();
        CompoundTag persistentData = ((IEntityData) entity).getPersistentData();
        if (persistentData.contains("pettingLastInteracted")) {
            long lastInteracted = persistentData.getLong("pettingLastInteracted");
            if (currentTime - lastInteracted < PettingConfig.interactionCooldown) {
                return InteractionResult.SUCCESS;
            }
        }
        
        if (PettingConfig.blacklistEnabled) {
            List<String> blacklist = PettingConfig.tamingBlacklist;
            if (blacklist.contains(entityName)) {
                return InteractionResult.PASS;
            }
        }

        if (PettingConfig.whitelistOnly) {
            List<String> whitelist = PettingConfig.tamingWhitelist;
            if (!whitelist.contains(entityName)) {
                return InteractionResult.PASS;
            }
        }
        
        if (PettingConfig.requireKillToTame) {
            if (player instanceof ServerPlayer serverPlayer) {
                int kills = serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(entity.getType()));
                if (kills <= 0) {
                    return InteractionResult.PASS;
                }
            }
        }
        
        double maxPets = player.getAttributeValue(net.yigitguven.petting.init.PettingModAttributes.MAX_PETS);
        
        if (maxPets != -1 && entity.level() instanceof ServerLevel serverLevel) {
            int currentPets = 0;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof Mob m && PetAttackLogic.isCustomPet(m)) {
                    String ownerStr = ((IEntityData) m).getPersistentData().getString("ownerUUID");
                    if (ownerStr.equals(player.getStringUUID())) {
                        currentPets++;
                    }
                }
            }
            if (currentPets >= (int)maxPets) {
                player.displayClientMessage(Component.literal("§cYou cannot tame any more custom pets! (Limit: " + (int)maxPets + ")"), true);
                return InteractionResult.SUCCESS;
            }
        }

        List<String> categoryConfigs = PettingConfig.petCategories;
        for (String catConfig : categoryConfigs) {
            String[] parts = catConfig.split("\\|");
            if (parts.length >= 4) {
                String catName = parts[1].trim();
                List<String> catMobs = java.util.Arrays.asList(parts[2].trim().split(","));
                
                if (catMobs.contains(entityName)) {
                    try {
                        int slot = Integer.parseInt(parts[0].trim());
                        double catMax = player.getAttributeValue(getCategoryAttribute(slot));
                        
                        if (catMax <= 0) {
                            player.displayClientMessage(Component.literal("§cYou cannot tame mobs in the " + catName + " category!"), true);
                            return InteractionResult.SUCCESS;
                        }

                        if (entity.level() instanceof ServerLevel serverLevel) {
                            int currentCatPets = 0;
                            for (Entity e : serverLevel.getAllEntities()) {
                                if (e instanceof Mob m && PetAttackLogic.isCustomPet(m)) {
                                    String ownerStr = ((IEntityData) m).getPersistentData().getString("ownerUUID");
                                    if (ownerStr.equals(player.getStringUUID())) {
                                        ResourceLocation eKey = BuiltInRegistries.ENTITY_TYPE.getKey(m.getType());
                                        if (eKey != null && catMobs.contains(eKey.toString())) {
                                            currentCatPets++;
                                        }
                                    }
                                }
                            }
                            if (currentCatPets >= (int)catMax) {
                                player.displayClientMessage(Component.literal("§cYou have reached your " + catName + " pet limit (" + (int)catMax + ")!"), true);
                                return InteractionResult.SUCCESS;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }

        double hpThreshold = PettingConfig.tameHealthThreshold;
        if (hpThreshold > 0.0 && entity instanceof LivingEntity minion) {
            float maxHp = minion.getMaxHealth();
            float currentHp = minion.getHealth();
            double missingPercentage = (maxHp - currentHp) / maxHp;
            
            if (missingPercentage < hpThreshold) {
                player.displayClientMessage(Component.literal("§cThis entity is too strong to be tamed right now. Weaken it first! (Requires at least " + (int)(hpThreshold * 100) + "% missing health)"), true);
                return InteractionResult.SUCCESS;
            }
        }
        
        persistentData.putLong("pettingLastInteracted", currentTime);

        if (!player.getAbilities().instabuild) {
            itemInHand.shrink(1);
        }

        double baseChance = PettingConfig.tameChance;
        double actualChance = baseChance;
        
        if (PettingConfig.healthScalesTamingChance && entity instanceof LivingEntity minion) {
            float maxHp = minion.getMaxHealth();
            float currentHp = minion.getHealth();
            double missingPercentage = (maxHp - currentHp) / maxHp;
            actualChance = baseChance + (missingPercentage * (1.0 - baseChance));
        }

        boolean rngPass = Math.random() < actualChance;
        boolean actionSuccessful = false;
        boolean needsRespawn = false;

        CompoundTag data = ((IEntityData) entity).getPersistentData();
        boolean isAlreadyCustomTamed = data.getBoolean("pettingtamed");

        if (!isAlreadyCustomTamed && rngPass) {
            if (entity instanceof TamableAnimal tamable) {
                if (!tamable.isTame()) {
                    tamable.tame(player);
                    tamable.setTarget(null);
                    actionSuccessful = true;
                    injectPettingTags(entity, player, data);
                }
            }
            else if (entity instanceof Mob oldMob) { 
                actionSuccessful = true;
                if (!PettingConfig.disableRespawnOnTame) {
                    needsRespawn = true;
                } else {
                    injectPettingTags(entity, player, data);
                    oldMob.setTarget(null);
                }
            }
        }

        if (actionSuccessful) {
            Level world = entity.level();

            if (world instanceof ServerLevel _level && PettingConfig.enableParticles) {
                _level.sendParticles(ParticleTypes.HEART, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(InteractionHand.MAIN_HAND, true);

            if (needsRespawn && world instanceof ServerLevel serverLevel) {
                Entity newEntity = entity.getType().create(world);
                
                if (newEntity instanceof Mob newMob) {
                    newMob.setPos(entity.getX(), entity.getY(), entity.getZ());
                    newMob.setYRot(entity.getYRot());
                    newMob.setXRot(entity.getXRot());
                    newMob.yBodyRot = ((Mob)entity).yBodyRot;
                    newMob.yHeadRot = ((Mob)entity).yHeadRot;

                    CompoundTag newData = ((IEntityData) newMob).getPersistentData();
                    injectPettingTags(newMob, player, newData);

                    newMob.setTarget(null);
                    world.addFreshEntity(newMob); 
                    entity.discard(); 
                }
            }
            return InteractionResult.SUCCESS;
        } else if (!isAlreadyCustomTamed) {
            Level world = entity.level();
            if (world instanceof ServerLevel _level && PettingConfig.enableParticles) {
                _level.sendParticles(ParticleTypes.SMOKE, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            player.swing(InteractionHand.MAIN_HAND, true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static void injectPettingTags(Entity entity, Player player, CompoundTag data) {
        String ownerName = player.getDisplayName().getString();
        String entityNameStr = entity.getType().getDescription().getString();
        entity.setCustomName(Component.literal(ownerName + "'s " + entityNameStr));
        entity.setCustomNameVisible(false);

        data.putString("ownerUUID", player.getStringUUID());
        data.putBoolean("pettingtamed", true);
        data.putBoolean("attackifownerattacks", true);
        data.putBoolean("attackifownerattacked", true);
        data.putBoolean("attackifselfattacked", true);
        data.putBoolean("damageOwner", false);
        data.putBoolean("sitstill", false);
        data.putInt("followdistance", 10);
        data.putInt("teleportdistance", 20);
        data.putLong("pettingTamedTick", entity.level().getGameTime());
    }

    private static net.minecraft.world.entity.ai.attributes.Attribute getCategoryAttribute(int slot) {
        return switch (slot) {
            case 1 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[0];
            case 2 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[1];
            case 3 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[2];
            case 4 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[3];
            case 5 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[4];
            case 6 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[5];
            case 7 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[6];
            case 8 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[7];
            case 9 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[8];
            case 10 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[9];
            case 11 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[10];
            case 12 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[11];
            case 13 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[12];
            case 14 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[13];
            case 15 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[14];
            case 16 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[15];
            case 17 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[16];
            case 18 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[17];
            case 19 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[18];
            case 20 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_CATEGORIES[19];
            default -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS;
        };
    }
}
