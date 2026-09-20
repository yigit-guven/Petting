package net.yigitguven.petting.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;

@EventBusSubscriber(modid = Petting.MODID)
public class SummonPetCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_A_MOB =
            new SimpleCommandExceptionType(Component.literal("Target entity is not a mob and cannot be tamed as a pet."));

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher(), event.getBuildContext());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("summon")
                        .then(Commands.literal("pet")
                                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .then(Commands.argument("entity", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                                                .suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES))
                                                .executes(ctx -> spawnPet(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "owner"),
                                                        ResourceArgument.getResource(ctx, "entity", Registries.ENTITY_TYPE),
                                                        ctx.getSource().getPosition(),
                                                        new CompoundTag()))
                                                .then(Commands.argument("pos", Vec3Argument.vec3())
                                                        .executes(ctx -> spawnPet(ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "owner"),
                                                                ResourceArgument.getResource(ctx, "entity", Registries.ENTITY_TYPE),
                                                                Vec3Argument.getVec3(ctx, "pos"),
                                                                new CompoundTag()))
                                                        .then(Commands.argument("nbt", CompoundTagArgument.compoundTag())
                                                                .executes(ctx -> spawnPet(ctx.getSource(),
                                                                        EntityArgument.getPlayer(ctx, "owner"),
                                                                        ResourceArgument.getResource(ctx, "entity", Registries.ENTITY_TYPE),
                                                                        Vec3Argument.getVec3(ctx, "pos"),
                                                                        CompoundTagArgument.getCompoundTag(ctx, "nbt")))))))));
    }

    private static int spawnPet(CommandSourceStack source, ServerPlayer owner, Holder.Reference<EntityType<?>> entityTypeHolder, Vec3 pos, CompoundTag nbt) throws CommandSyntaxException {
        Entity entity = SummonCommand.createEntity(source, entityTypeHolder, pos, nbt, true);

        if (!(entity instanceof Mob mob)) {
            entity.discard();
            throw ERROR_NOT_A_MOB.create();
        }

        PetHelper.setTamed(mob, owner);

        source.sendSuccess(() -> Component.literal("Summoned " + mob.getDisplayName().getString() + " as a pet for " + owner.getScoreboardName()), true);
        return 1;
    }
}
