package net.yigitguven.petting.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingConfig;

@EventBusSubscriber
public class PetDamageHandler {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        if (target == null || net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;
        
        if (!PettingConfig.ALLOW_OWNER_TO_HURT_PETS.get()) {
            if (player.getVehicle() != null && target.equals(player.getVehicle())) {
                event.setCanceled(true);
                return;
            }
            if (isPetOf(target, player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target == null || net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // 1. Prevent owner from hurting their own pets (melee or projectile)
        if (!PettingConfig.ALLOW_OWNER_TO_HURT_PETS.get()) {
            if (attacker != null && isPetOf(target, attacker)) {
                event.setCanceled(true);
                return;
            }
            if (directEntity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
                Entity shooter = projectile.getOwner();
                if (shooter != null && isPetOf(target, shooter)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (!PettingConfig.PREVENT_PET_TO_OWNER_DAMAGE.get()) return;

        // 2. Pet to owner damage prevention
        if (attacker != null && attacker.getPersistentData().getBoolean("pettingtamed")) {
            String ownerUUID = attacker.getPersistentData().getString("ownerUUID");
            if (ownerUUID.equals(target.getStringUUID())) {
                event.setCanceled(true);
                return;
            }
        }

        // 3. Projectile protection from pet
        if (directEntity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
            Entity shooter = projectile.getOwner();
            if (shooter != null && shooter.getPersistentData().getBoolean("pettingtamed")) {
                String ownerUUID = shooter.getPersistentData().getString("ownerUUID");
                
                if (target.equals(shooter)) {
                    event.setCanceled(true);
                    return;
                }
                
                if (ownerUUID.equals(target.getStringUUID())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    private static boolean isPetOf(Entity pet, Entity owner) {
        if (pet == null || owner == null) return false;
        if (pet.getPersistentData().getBoolean("pettingtamed")) {
            String ownerUUID = pet.getPersistentData().getString("ownerUUID");
            if (!ownerUUID.isEmpty() && ownerUUID.equals(owner.getStringUUID())) {
                return true;
            }
        }
        if (pet instanceof TamableAnimal tamable && owner instanceof Player player) {
            if (tamable.isOwnedBy(player)) return true;
        }
        return false;
    }
}
