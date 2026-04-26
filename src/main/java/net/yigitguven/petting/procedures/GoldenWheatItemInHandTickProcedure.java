package net.yigitguven.petting.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;
import java.util.Comparator;

import net.yigitguven.petting.init.PettingModItems;

public class GoldenWheatItemInHandTickProcedure {

    public static void execute(LevelAccessor world, Entity entity) {
        if (entity == null) return;

        if (entity instanceof Player player) {
            boolean isHoldingWheat = player.getMainHandItem().getItem() == PettingModItems.GOLDEN_WHEAT.get() 
                                  || player.getOffhandItem().getItem() == PettingModItems.GOLDEN_WHEAT.get();

            if (world instanceof Level _lvl && _lvl.getGameTime() % 5 != 0) {
                return;
            }

            double searchRadius = 12.0;

            List<Animal> nearbyAnimals = world.getEntitiesOfClass(
                Animal.class, 
                player.getBoundingBox().inflate(searchRadius, 4.0, searchRadius), 
                mob -> {
                    boolean isAlreadyCustomTamed = mob.getPersistentData().getBoolean("pettingtamed");
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
            } 
            else {
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
