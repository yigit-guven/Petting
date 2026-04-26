package net.yigitguven.petting.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

import net.yigitguven.petting.config.PettingConfig;

public class GoldenWheatRightclickedProcedure {

    public static boolean execute(Entity entity, Player player) {
        if (entity == null || player == null) return false;

        long currentTime = entity.level().getGameTime();
        long lastInteracted = entity.getPersistentData().getLongOr("pettingLastInteracted", 0L);
        if (currentTime - lastInteracted < PettingConfig.INTERACTION_COOLDOWN.get()) {
            return false;
        }

        net.minecraft.world.item.ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        // 1. Tool check (Specific Golden Wheat or Configured Tool)
        String targetTamingItem = PettingConfig.TAMING_ITEM_ID.get();
        boolean toolMatches = false;
        
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(itemInHand.getItem());
        if (itemKey != null && itemKey.toString().equals(targetTamingItem)) {
            toolMatches = true;
        }

        if (!toolMatches && PettingConfig.ALLOW_GOLDEN_WHEAT.get()) {
             if (itemInHand.getItem() == net.yigitguven.petting.init.PettingModItems.GOLDEN_WHEAT.get()) {
                 toolMatches = true;
             }
        }

        if (!toolMatches) return false;

        // 2. Blacklist Check
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) {
            player.displayClientMessage(Component.literal("§cThis entity cannot be tamed."), true);
            return true;
        }

        // 3. Whitelist Check
        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityName = (entityKey != null) ? entityKey.toString() : "";
        
        if (PettingConfig.WHITELIST_ONLY.get()) {
            java.util.List<? extends String> whitelist = PettingConfig.TAMING_WHITELIST.get();
            if (!whitelist.contains(entityName)) {
                player.displayClientMessage(Component.literal("§cOnly specific mobs can be tamed in this modpack."), true);
                return true;
            }
        }

        // 4. Kill Requirement
        if (PettingConfig.REQUIRE_KILL_TO_TAME.get() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
             int killCount = serverPlayer.getStats().getValue(net.minecraft.stats.Stats.ENTITY_KILLED.get(entity.getType()));
             if (killCount <= 0) {
                 player.displayClientMessage(Component.literal("§cYou must kill at least one " + entity.getType().getDescription().getString() + " before you can tame it!"), true);
                 return true;
             }
        }
        
        // 5. Global Pet Limit
        double maxPets = player.getAttributeValue(net.yigitguven.petting.init.PettingModAttributes.MAX_PETS);
        
        if (maxPets != -1 && entity.level() instanceof ServerLevel serverLevel) {
            int currentPets = 0;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof Mob m && isCustomPet(m)) {
                    String ownerStr = m.getPersistentData().getStringOr("ownerUUID", "");
                    if (ownerStr.equals(player.getStringUUID())) {
                        currentPets++;
                    }
                }
            }
            if (currentPets >= (int)maxPets) {
                player.displayClientMessage(Component.literal("§cYou have reached your global pet limit (" + (int)maxPets + ")!"), true);
                PleasantryHelper.sendParticles(serverLevel, entity, ParticleTypes.SMOKE);
                return true;
            }
        }

        // 6. Category Limits
        java.util.List<? extends String> categoryConfigs = PettingConfig.PET_CATEGORIES.get();
        for (String catConfig : categoryConfigs) {
            String[] parts = catConfig.split("\\|");
            if (parts.length >= 4) {
                String catName = parts[1].trim();
                java.util.List<String> catMobs = java.util.Arrays.asList(parts[2].trim().split(","));
                
                if (catMobs.contains(entityName)) {
                    try {
                        int slot = Integer.parseInt(parts[0].trim());
                        double catMax = player.getAttributeValue(getCategoryAttribute(slot));
                        
                        if (catMax <= 0) {
                            player.displayClientMessage(Component.literal("§cYou cannot tame mobs in the " + catName + " category!"), true);
                            return true;
                        }

                        if (entity.level() instanceof ServerLevel serverLevel) {
                            int currentCatPets = 0;
                            for (Entity e : serverLevel.getAllEntities()) {
                                if (e instanceof Mob m && isCustomPet(m)) {
                                    String ownerStr = m.getPersistentData().getStringOr("ownerUUID", "");
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
                                PleasantryHelper.sendParticles(serverLevel, entity, ParticleTypes.SMOKE);
                                return true;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }

        // 7. Health Threshold
        double hpThreshold = PettingConfig.TAME_HEALTH_THRESHOLD.get();
        if (hpThreshold > 0.0 && entity instanceof net.minecraft.world.entity.LivingEntity minion) {
            float maxHp = minion.getMaxHealth();
            float currentHp = minion.getHealth();
            double missingPercentage = (maxHp - currentHp) / maxHp;
            
            if (missingPercentage < hpThreshold) {
                player.displayClientMessage(Component.literal("§cThis entity is too strong to be tamed right now. Weaken it first! (Requires at least " + (int)(hpThreshold * 100) + "% missing health)"), true);
                return true;
            }
        }
        
        entity.getPersistentData().putLong("pettingLastInteracted", currentTime);

        if (!player.getAbilities().instabuild) {
            itemInHand.shrink(1);
        }

        // 8. RNG Taming
        double baseChance = PettingConfig.TAME_CHANCE.get();
        double actualChance = baseChance;
        
        if (PettingConfig.HEALTH_SCALES_TAMING_CHANCE.get() && entity instanceof net.minecraft.world.entity.LivingEntity minion) {
            float maxHp = minion.getMaxHealth();
            float currentHp = minion.getHealth();
            double missingPercentage = (maxHp - currentHp) / maxHp;
            actualChance = baseChance + (missingPercentage * (1.0 - baseChance)); 
        }

        boolean rngPass = Math.random() < actualChance;
        boolean actionSuccessful = false;
        boolean needsRespawn = false;

        CompoundTag data = entity.getPersistentData();
        boolean isAlreadyCustomTamed = data.getBooleanOr("pettingtamed", false);

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
                if (!PettingConfig.DISABLE_RESPAWN_ON_TAME.get()) {
                    needsRespawn = true;
                } else {
                    injectPettingTags(entity, player, data);
                    oldMob.setTarget(null);
                }
            }
        }

        if (actionSuccessful) {
            Level world = entity.level();
            if (world instanceof ServerLevel _level && PettingConfig.ENABLE_PARTICLES.get()) {
                _level.sendParticles(ParticleTypes.HEART, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.swing(InteractionHand.MAIN_HAND, true);

            if (needsRespawn && world instanceof ServerLevel serverLevel) {
                Entity newEntity = entity.getType().create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (newEntity instanceof Mob newMob) {
                    newMob.setPos(entity.getX(), entity.getY(), entity.getZ());
                    newMob.setYRot(entity.getYRot());
                    newMob.setXRot(entity.getXRot());
                    newMob.yBodyRot = ((Mob)entity).yBodyRot;
                    newMob.yHeadRot = ((Mob)entity).yHeadRot;
                    CompoundTag newData = newMob.getPersistentData();
                    injectPettingTags(newMob, player, newData);
                    newMob.setTarget(null);
                    world.addFreshEntity(newMob); 
                    entity.discard(); 
                }
            }
        } else if (!isAlreadyCustomTamed) {
            Level world = entity.level();
            if (world instanceof ServerLevel _level && PettingConfig.ENABLE_PARTICLES.get()) {
                _level.sendParticles(ParticleTypes.SMOKE, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            player.swing(InteractionHand.MAIN_HAND, true);
        }
        return true;
    }

    private static void injectPettingTags(Entity entity, Player player, CompoundTag data) {
        String ownerName = player.getDisplayName().getString();
        String entityNameStr = entity.getType().getDescription().getString();
        entity.setCustomName(Component.literal(ownerName + "'s " + entityNameStr));
        entity.setCustomNameVisible(false);

        data.putString("ownerUUID", player.getStringUUID());
        data.putBoolean("pettingtamed", true);
        data.putBoolean("isNameGenerated", true);
        data.putBoolean("attackifownerattacks", true);
        data.putBoolean("attackifownerattacked", true);
        data.putBoolean("attackifselfattacked", true);
        data.putBoolean("damageOwner", false);
        data.putBoolean("sitstill", false);
        data.putInt("followdistance", 10);
        data.putInt("teleportdistance", 20);

        if (entity.level() instanceof ServerLevel serverLevel) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(entity, 
                new net.yigitguven.petting.network.SyncPetStatusPayload(entity.getId(), true));
        }
    }

    private static Holder<Attribute> getCategoryAttribute(int slot) {
        return switch (slot) {
            case 1 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C1;
            case 2 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C2;
            case 3 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C3;
            case 4 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C4;
            case 5 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C5;
            case 6 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C6;
            case 7 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C7;
            case 8 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C8;
            case 9 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C9;
            case 10 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C10;
            case 11 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C11;
            case 12 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C12;
            case 13 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C13;
            case 14 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C14;
            case 15 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C15;
            case 16 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C16;
            case 17 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C17;
            case 18 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C18;
            case 19 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C19;
            case 20 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C20;
            default -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS;
        };
    }

    private static boolean isCustomPet(Mob entity) {
        return entity.getPersistentData().getBooleanOr("pettingtamed", false);
    }
    
    private static class PleasantryHelper {
        public static void sendParticles(ServerLevel level, Entity entity, net.minecraft.core.particles.ParticleOptions type) {
            level.sendParticles(type, entity.getX(), entity.getY() + 0.5, entity.getZ(), 7, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
