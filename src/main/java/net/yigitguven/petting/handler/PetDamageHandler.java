package net.yigitguven.petting.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingConfig;

@EventBusSubscriber
public class PetDamageHandler {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;
        
        if (player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            if (target.equals(vehicle) && !PettingConfig.ALLOW_OWNER_TO_HURT_PETS.get()) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingDamageEvent event) {
        if (!PettingConfig.PREVENT_PET_TO_OWNER_DAMAGE.get()) return;

        LivingEntity target = event.getEntity();
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // 1. Pet to owner damage prevention
        if (attacker != null && attacker.getPersistentData().getBoolean("pettingtamed")) {
            String ownerUUID = attacker.getPersistentData().getString("ownerUUID");
            if (ownerUUID.equals(target.getStringUUID())) {
                event.setCanceled(true);
                return;
            }
        }

        // 2. Projectile protection
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
}
