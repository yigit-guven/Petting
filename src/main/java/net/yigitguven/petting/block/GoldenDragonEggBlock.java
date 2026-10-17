package net.yigitguven.petting.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yigitguven.petting.block.entity.GoldenDragonEggBlockEntity;
import net.yigitguven.petting.init.PettingDataComponents;

import javax.annotation.Nullable;
import java.util.UUID;

public class GoldenDragonEggBlock extends FallingBlock implements EntityBlock {
    public static final com.mojang.serialization.MapCodec<GoldenDragonEggBlock> CODEC = simpleCodec(GoldenDragonEggBlock::new);
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    public GoldenDragonEggBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldenDragonEggBlockEntity(pos, state);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        this.teleport(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (level.getBlockEntity(pos) instanceof GoldenDragonEggBlockEntity be) {
                    String ownerName = be.getOwnerName();
                    if (ownerName != null && !ownerName.isEmpty()) {
                        player.sendSystemMessage(Component.translatable("petting.action.golden_egg_info_owner", ownerName));
                    } else {
                        player.sendSystemMessage(Component.translatable("petting.action.golden_egg_info_unbound"));
                    }
                }
                if (level instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.2, 0.2, 0.2, 0.05);
                }
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.3F);
            }
            return InteractionResult.SUCCESS;
        }

        this.teleport(state, level, pos);
        return InteractionResult.SUCCESS;
    }

    private void teleport(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        WorldBorder border = level.getWorldBorder();
        RandomSource random = level.getRandom();

        UUID ownerUUID = null;
        String ownerName = null;
        if (level.getBlockEntity(pos) instanceof GoldenDragonEggBlockEntity be) {
            ownerUUID = be.getOwnerUUID();
            ownerName = be.getOwnerName();
        }

        for (int i = 0; i < 1000; i++) {
            BlockPos targetPos = pos.offset(
                    random.nextInt(16) - random.nextInt(16),
                    random.nextInt(8) - random.nextInt(8),
                    random.nextInt(16) - random.nextInt(16)
            );
            if (level.getBlockState(targetPos).isAir()
                    && !level.getBlockState(targetPos.below()).isAir()
                    && border.isWithinBounds(targetPos)
                    && level.isInWorldBounds(targetPos)) {

                level.levelEvent(2003, pos, 0);
                level.setBlock(targetPos, state, 2);
                if (level.getBlockEntity(targetPos) instanceof GoldenDragonEggBlockEntity newBe) {
                    newBe.setOwnerUUID(ownerUUID);
                    newBe.setOwnerName(ownerName);
                    newBe.setChanged();
                }
                level.removeBlock(pos, false);
                return;
            }
        }
    }

    @Override
    protected int getDelayAfterPlace() {
        return 5;
    }

    @Override
    protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return 0xFFD700;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            UUID ownerUUID = null;
            String ownerName = null;
            if (level.getBlockEntity(pos) instanceof GoldenDragonEggBlockEntity be) {
                ownerUUID = be.getOwnerUUID();
                ownerName = be.getOwnerName();
            }

            FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);

            CompoundTag tag = new CompoundTag();
            if (ownerUUID != null) {
                tag.putUUID("OwnerUUID", ownerUUID);
            }
            if (ownerName != null) {
                tag.putString("OwnerName", ownerName);
            }
            tag.putString("id", "petting:golden_dragon_egg");
            fallingBlock.blockData = tag;
        }
    }

    @Override
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity entity) {
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof GoldenDragonEggBlockEntity be) {
            if (stack.has(PettingDataComponents.DRAGON_EGG_OWNER.get())) {
                be.setOwnerUUID(stack.get(PettingDataComponents.DRAGON_EGG_OWNER.get()));
                if (stack.has(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get())) {
                    be.setOwnerName(stack.get(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get()));
                }
            } else if (placer instanceof Player player) {
                be.setOwnerUUID(player.getUUID());
                be.setOwnerName(player.getName().getString());
            }
            be.setChanged();
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        return super.playerWillDestroy(level, pos, state, player);
    }
}
