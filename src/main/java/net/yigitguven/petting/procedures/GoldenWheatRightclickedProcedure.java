package net.yigitguven.petting.procedures;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.yigitguven.petting.config.PettingConfig;

import java.util.List;

public class GoldenWheatRightclickedProcedure {

    public static void execute(Entity sourceentity) {
        execute(null, sourceentity);
    }

    public static boolean execute(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null) return false;
        if (entity.level().isClientSide()) return false;
        if (!(sourceentity instanceof Player player)) return false;

        ItemStack itemInHand = player.getMainHandItem();
        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(itemInHand.getItem());
        if (itemID == null) return false;
        
        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityKey == null) return false;
        
        String entityName = entityKey.toString();
        String itemName = itemID.toString();

        // 0. Check Custom Taming Items Config First
        boolean hasCustomItem = false;
        boolean customItemMatch = false;

        List<? extends String> customItemsMap = PettingConfig.CUSTOM_TAMING_ITEMS.get();
        for (String mapping : customItemsMap) {
            String[] parts = mapping.split("\\|");
            if (parts.length == 2 && parts[0].trim().equals(entityName)) {
                hasCustomItem = true;
                if (parts[1].trim().equals(itemName)) {
                    customItemMatch = true;
                }
                break; // Found the entity mapping, no need to keep searching
            }
        }

        // Evaluate Taming Eligibility
        if (hasCustomItem) {
            if (!customItemMatch) return false; // Entity strictly requires the mapped item
        } else {
            if (!itemName.equals("petting:golden_wheat")) return false; // Must be holding Wheat
            if (!PettingConfig.ALLOW_GOLDEN_WHEAT.get()) return false; // Wheat must be globally enabled
        }

        // Interaction handled beyond this point
        
        // --- NEW CONFIG CHECKS ---
        long currentTime = entity.level().getGameTime();
        if (entity.getPersistentData().contains("pettingLastInteracted")) {
            long lastInteracted = entity.getPersistentData().getLong("pettingLastInteracted");
            if (currentTime - lastInteracted < PettingConfig.INTERACTION_COOLDOWN.get()) {
                return true; // Cooldown active, but still "handled"
            }
        }
        
        // 1. Check Blacklist First
        if (PettingConfig.BLACKLIST_ENABLED.get()) {
            List<? extends String> blacklist = PettingConfig.TAMING_BLACKLIST.get();
            if (blacklist.contains(entityName)) {
                return true;
            }
        }

        // 2. Check Whitelist
        if (PettingConfig.WHITELIST_ONLY.get()) {
            List<? extends String> whitelist = PettingConfig.TAMING_WHITELIST.get();
            if (!whitelist.contains(entityName)) {
                return true; 
            }
        }
        
        if (PettingConfig.REQUIRE_KILL_TO_TAME.get()) {
            if (player instanceof ServerPlayer serverPlayer) {
                int kills = serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(entity.getType()));
                if (kills <= 0) {
                    return true;
                }
            }
        }
        
        // 4. Check Max Pets Limit
        double maxPets = player.getAttributeValue(net.yigitguven.petting.init.PettingModAttributes.MAX_PETS.get());
        
        if (maxPets != -1 && entity.level() instanceof ServerLevel serverLevel) {
            int currentPets = 0;
            for (Entity e : serverLevel.getAllEntities()) {
                if (e instanceof Mob m && isCustomPet(m)) {
                    String ownerStr = m.getPersistentData().getString("ownerUUID");
                    if (ownerStr.equals(player.getStringUUID())) {
                        currentPets++;
                    }
                }
            }
            if (currentPets >= (int)maxPets) {
                player.displayClientMessage(Component.literal("§cYou cannot tame any more custom pets! (Limit: " + (int)maxPets + ")"), true);
                return true;
            }
        }

        // 5. Check Category Limits
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
                                    String ownerStr = m.getPersistentData().getString("ownerUUID");
                                    if (ownerStr.equals(player.getStringUUID())) {
                                        ResourceLocation eKey = ForgeRegistries.ENTITY_TYPES.getKey(m.getType());
                                        if (eKey != null && catMobs.contains(eKey.toString())) {
                                            currentCatPets++;
                                        }
                                    }
                                }
                            }
                            if (currentCatPets >= (int)catMax) {
                                player.displayClientMessage(Component.literal("§cYou have reached your " + catName + " pet limit (" + (int)catMax + ")!"), true);
                                return true;
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                    break;
                }
            }
        }

        // 6. Check Health Threshold
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

        // We consume the item since an attempt was made (unless in creative)
        if (!player.getAbilities().instabuild) {
            itemInHand.shrink(1);
        }

        // Calculate RNG Chance
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
        boolean isAlreadyCustomTamed = data.getBoolean("pettingtamed");

        if (!isAlreadyCustomTamed && rngPass) {
            if (entity instanceof TamableAnimal tamable) {
                if (!tamable.isTame()) {
                    tamable.tame(player);
                    tamable.setTarget(null);
                    actionSuccessful = true;
                    injectPettingTags(entity, player, data);
                    tamable.targetSelector.removeAllGoals(goal -> true);
                }
            }
            else if (entity instanceof Mob oldMob) { 
                actionSuccessful = true;
                if (!PettingConfig.DISABLE_RESPAWN_ON_TAME.get()) {
                    needsRespawn = true;
                } else {
                    injectPettingTags(entity, player, data);
                    oldMob.targetSelector.removeAllGoals(goal -> true);
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
                Entity newEntity = entity.getType().create(world);
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
    }

    private static net.minecraft.world.entity.ai.attributes.Attribute getCategoryAttribute(int slot) {
        return switch (slot) {
            case 1 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C1.get();
            case 2 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C2.get();
            case 3 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C3.get();
            case 4 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C4.get();
            case 5 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C5.get();
            case 6 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C6.get();
            case 7 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C7.get();
            case 8 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C8.get();
            case 9 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C9.get();
            case 10 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C10.get();
            case 11 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C11.get();
            case 12 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C12.get();
            case 13 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C13.get();
            case 14 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C14.get();
            case 15 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C15.get();
            case 16 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C16.get();
            case 17 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C17.get();
            case 18 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C18.get();
            case 19 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C19.get();
            case 20 -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS_C20.get();
            default -> net.yigitguven.petting.init.PettingModAttributes.MAX_PETS.get();
        };
    }

    private static boolean isCustomPet(Mob entity) {
        return entity.getPersistentData().getBoolean("pettingtamed");
    }
}
