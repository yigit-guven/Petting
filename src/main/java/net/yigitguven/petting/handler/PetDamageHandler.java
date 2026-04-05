package net.yigitguven.petting.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.config.PettingConfig;

@Mod.EventBusSubscriber
public class PetDamageHandler {
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        
        if (player.getVehicle() != null) {
            Entity vehicle = player.getVehicle();
            // Cancel if attacking the vehicle or its parts
            if (target.equals(vehicle)) {
                event.setCanceled(true);
            } else if (target instanceof net.minecraftforge.entity.PartEntity<?> part && part.getParent().equals(vehicle)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!PettingConfig.PREVENT_PET_TO_OWNER_DAMAGE.get()) return;

        LivingEntity target = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // 1. Pet to owner damage prevention (Direct and indirect)
        if (attacker != null && attacker.getPersistentData().getBoolean("pettingtamed")) {
            String ownerUUID = attacker.getPersistentData().getString("ownerUUID");
            if (ownerUUID.equals(target.getStringUUID())) {
                event.setCanceled(true);
                return;
            }
        }

        // 2. Projectile protection for pet and owner (especially for fireballs/skulls)
        if (directEntity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
            Entity shooter = projectile.getOwner();
            if (shooter != null && shooter.getPersistentData().getBoolean("pettingtamed")) {
                String ownerUUID = shooter.getPersistentData().getString("ownerUUID");
                
                // Protect the pet from its own projectile
                if (target.equals(shooter)) {
                    event.setCanceled(true);
                    return;
                }
                
                // Protect the owner from the pet's projectile
                if (ownerUUID.equals(target.getStringUUID())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
