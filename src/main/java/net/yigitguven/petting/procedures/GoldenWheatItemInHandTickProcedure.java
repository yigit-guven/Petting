package net.yigitguven.petting.procedures;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.init.PettingModItems;

import java.util.Comparator;
import java.util.List;

public class GoldenWheatItemInHandTickProcedure {

    public static void register() {
        // In 1.20.1 Fabric API, START_WORLD_TICK or START_SERVER_TICK are common.
        // We'll use START_SERVER_TICK and iterate players or just use a Mixin if this fails.
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (Player player : server.getPlayerList().getPlayers()) {
                execute(player.level(), player);
            }
        });
    }

    public static void execute(Level world, Entity entity) {
        if (entity == null || world.isClientSide()) return;

        if (entity instanceof Player player) {
            if (world.getGameTime() % 5 != 0) {
                return;
            }

            boolean isHoldingWheat = player.getMainHandItem().getItem() == PettingModItems.GOLDEN_WHEAT 
                                   || player.getOffhandItem().getItem() == PettingModItems.GOLDEN_WHEAT;

            double searchRadius = 12.0;
            List<Animal> nearbyAnimals = world.getEntitiesOfClass(
                Animal.class, 
                player.getBoundingBox().inflate(searchRadius, 4.0, searchRadius), 
                mob -> {
                    boolean isAlreadyCustomTamed = ((IEntityData) mob).getPersistentData().getBoolean("pettingtamed");
                    if (isAlreadyCustomTamed) return false;
                    if (mob instanceof TamableAnimal tamable && tamable.isTame()) return false;
                    if (mob instanceof AbstractHorse horse && horse.isTamed()) return false;
                    return true;
                }
            );

            if (isHoldingWheat) {
                nearbyAnimals.sort(Comparator.comparingDouble(mob -> mob.distanceToSqr(player)));

                for (Animal mob : nearbyAnimals) {
                    if (mob.getTarget() == null || !mob.getTarget().isAlive()) {
                        mob.getLookControl().setLookAt(player, 10.0F, mob.getMaxHeadXRot());
                        boolean success = mob.getNavigation().moveTo(player, 1.25);
                        mob.addTag("TemptedByGoldenWheat");

                        if (success && world instanceof ServerLevel serverLevel && Math.random() < 0.3) {
                             serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                                 mob.getX(), mob.getY() + mob.getBbHeight() + 0.5, mob.getZ(), 
                                 1, 0.1, 0.1, 0.1, 0);
                        }
                    }
                }
            } else {
                for (Animal mob : nearbyAnimals) {
                    if (mob.getTags().contains("TemptedByGoldenWheat")) {
                        mob.getNavigation().stop();
                        mob.removeTag("TemptedByGoldenWheat");
                    }
                }
            }
        }
    }
}
