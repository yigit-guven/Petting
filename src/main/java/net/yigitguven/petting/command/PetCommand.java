package net.yigitguven.petting.command;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;

@EventBusSubscriber
public class PetCommand {

    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("pet")
            .requires(s -> s.hasPermission(4))
            .then(Commands.argument("mob", EntityArgument.entity())
                .then(Commands.argument("sourceentity", EntityArgument.player())
                    .executes(PetCommand::execute)
                )
            )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> arguments) {
        try {
            Entity mob = EntityArgument.getEntity(arguments, "mob");
            Entity source = EntityArgument.getEntity(arguments, "sourceentity");
            tameMob(mob, source);
            return 1; 
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            return 0; 
        }
    }

    private static void tameMob(Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null) return;
        if (entity.level().isClientSide()) return;
        if (!(sourceentity instanceof Player player)) return;

        boolean actionSuccessful = false;
        boolean needsRespawn = false;

        if (entity instanceof TamableAnimal tamable) {
            if (!tamable.isTame()) {
                tamable.tame(player);
                tamable.setTarget(null);
                actionSuccessful = true;
            }
        }
        else if (entity instanceof Mob oldMob) {
            CompoundTag data = oldMob.getPersistentData();
            boolean isAlreadyCustomTamed = data.getBooleanOr("pettingtamed", false);

            if (!isAlreadyCustomTamed) {
                actionSuccessful = true;
                needsRespawn = true;
            }
        }

        if (actionSuccessful) {
            Level world = entity.level();

            if (world instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.HEART,
                        entity.getX(), entity.getY() + 0.5, entity.getZ(),
                        7, 0.5, 0.5, 0.5, 0.1);
            }
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

            if (needsRespawn && world instanceof ServerLevel serverLevel) {
                // FIXED: Use simple create(world) for 1.20.1
                Entity newEntity = entity.getType().create(serverLevel, EntitySpawnReason.COMMAND);

                if (newEntity instanceof Mob newMob) {
                    newMob.setPos(entity.getX(), entity.getY(), entity.getZ());
                    newMob.setYRot(entity.getYRot());
                    newMob.setXRot(entity.getXRot());
                    newMob.yBodyRot = ((Mob) entity).yBodyRot;
                    newMob.yHeadRot = ((Mob) entity).yHeadRot;

                    CompoundTag newData = newMob.getPersistentData();
                    String ownerName = player.getDisplayName().getString();
                    String entityName = newMob.getType().getDescription().getString();

                    newMob.setCustomName(Component.literal(ownerName + "'s " + entityName));
                    newMob.setCustomNameVisible(false);

                    newData.putString("ownerUUID", player.getStringUUID());
                    newData.putBoolean("pettingtamed", true);
                    newData.putBoolean("attackifownerattacks", true);
                    newData.putBoolean("attackifownerattacked", true);
                    newData.putBoolean("attackifselfattacked", true);
                    newData.putBoolean("damageOwner", false);
                    newData.putBoolean("sitstill", false);
                    newData.putInt("followdistance", 10);
                    newData.putInt("teleportdistance", 20);

                    newMob.setTarget(null);
                    world.addFreshEntity(newMob);
                    entity.discard();
                }
            }
        }
    }
}



