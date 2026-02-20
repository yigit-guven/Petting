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

    public static void execute(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null) return;
        if (entity.level().isClientSide()) return;
        if (!(sourceentity instanceof Player player)) return;

        ItemStack itemInHand = player.getMainHandItem();
        ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(itemInHand.getItem());
        if (itemID == null) return;
        
        ResourceLocation entityKey = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityKey == null) return;
        
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
            if (!customItemMatch) return; // Entity strictly requires the mapped item
        } else {
            if (!itemName.equals("petting:golden_wheat")) return; // Must be holding Wheat
            if (!PettingConfig.ALLOW_GOLDEN_WHEAT.get()) return; // Wheat must be globally enabled
        }

        // --- NEW CONFIG CHECKS ---
        long currentTime = entity.level().getGameTime();
        if (entity.getPersistentData().contains("pettingLastInteracted")) {
            long lastInteracted = entity.getPersistentData().getLong("pettingLastInteracted");
            if (currentTime - lastInteracted < PettingConfig.INTERACTION_COOLDOWN.get()) {
                return; // Cooldown active
            }
        }
        
        // 1. Check Blacklist First
        if (PettingConfig.BLACKLIST_ENABLED.get()) {
            List<? extends String> blacklist = PettingConfig.TAMING_BLACKLIST.get();
            if (blacklist.contains(entityName)) {
                return; // Explicitly forbidden
            }
        }

        // 2. Check Whitelist
        if (PettingConfig.WHITELIST_ONLY.get()) {
            List<? extends String> whitelist = PettingConfig.TAMING_WHITELIST.get();
            if (!whitelist.contains(entityName)) {
                return; // Not in whitelist
            }
        }
        
        if (PettingConfig.REQUIRE_KILL_TO_TAME.get()) {
            if (player instanceof ServerPlayer serverPlayer) {
                int kills = serverPlayer.getStats().getValue(Stats.ENTITY_KILLED.get(entity.getType()));
                if (kills <= 0) {
                    return; // Must kill at least one first
                }
            }
        }
        
        // 4. Check Max Pets Limit
        int maxPets = PettingConfig.MAX_PETS_PER_PLAYER.get();
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
            if (currentPets >= maxPets) {
                player.displayClientMessage(Component.literal("§cYou cannot tame any more custom pets! (Limit: " + maxPets + ")"), true);
                return; // Exceeded limit
            }
        }

        // 5. Check Health Threshold
        double hpThreshold = PettingConfig.TAME_HEALTH_THRESHOLD.get();
        if (hpThreshold > 0.0 && entity instanceof net.minecraft.world.entity.LivingEntity minion) {
            float maxHp = minion.getMaxHealth();
            float currentHp = minion.getHealth();
            double missingPercentage = (maxHp - currentHp) / maxHp;
            
            if (missingPercentage < hpThreshold) {
                player.displayClientMessage(Component.literal("§cThis entity is too strong to be tamed right now. Weaken it first! (Requires at least " + (int)(hpThreshold * 100) + "% missing health)"), true);
                return;
            }
        }
        
        entity.getPersistentData().putLong("pettingLastInteracted", currentTime);
        // --- END CONFIG CHECKS ---

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
            actualChance = baseChance + (missingPercentage * (1.0 - baseChance)); // Scale up to 100% chance based on missing health
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
                    
                    // HYBRIDIZATION: Inject Petting logic into Vanilla Wolves/Cats
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

                    // Wipe Vanilla Hostile Goals
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
            // RNG Failed
            Level world = entity.level();
            if (world instanceof ServerLevel _level && PettingConfig.ENABLE_PARTICLES.get()) {
                _level.sendParticles(ParticleTypes.SMOKE, 
                    entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                    7, 0.5, 0.5, 0.5, 0.1);
            }
            player.swing(InteractionHand.MAIN_HAND, true);
        }
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
    }

    private static boolean isCustomPet(Mob entity) {
        return entity.getPersistentData().getBoolean("pettingtamed");
    }
}
