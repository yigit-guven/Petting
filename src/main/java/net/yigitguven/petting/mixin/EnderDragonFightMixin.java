package net.yigitguven.petting.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.yigitguven.petting.block.entity.GoldenDragonEggBlockEntity;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Shadow
    private ServerLevel level;

    @Shadow
    private @Nullable BlockPos exitPortalLocation;

    @Shadow
    private @Nullable UUID dragonUUID;

    @Shadow
    private net.minecraft.server.level.ServerBossEvent dragonEvent;

    @Inject(method = "createNewDragon", at = @At("RETURN"))
    private void petting$onDragonCreated(CallbackInfoReturnable<EnderDragon> cir) {
        EnderDragon dragon = cir.getReturnValue();
        if (dragon == null || this.level == null) {
            return;
        }

        int portalY = this.exitPortalLocation != null ? this.exitPortalLocation.getY() : 65;

        // Search for Golden Dragon Egg on or near the exit portal structure
        BlockPos foundEggPos = null;
        UUID ownerUUID = null;
        String ownerName = null;

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int y = portalY - 1; y <= portalY + 8; y++) {
            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    mpos.set(x, y, z);
                    if (this.level.getBlockState(mpos).is(PettingModBlocks.GOLDEN_DRAGON_EGG.get())) {
                        foundEggPos = mpos.immutable();
                        if (this.level.getBlockEntity(foundEggPos) instanceof GoldenDragonEggBlockEntity be) {
                            ownerUUID = be.getOwnerUUID();
                            ownerName = be.getOwnerName();
                        }
                        break;
                    }
                }
                if (foundEggPos != null) break;
            }
            if (foundEggPos != null) break;
        }

        if (foundEggPos != null) {
            // Hatch the egg safely and peacefully
            this.level.destroyBlock(foundEggPos, false);
            PetHelper.recordGoldenDragonSummon();

            // Hatching particles and audio (peaceful magic: totem, end rod, amethyst)
            this.level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, foundEggPos.getX() + 0.5, foundEggPos.getY() + 0.5, foundEggPos.getZ() + 0.5, 100, 0.6, 0.6, 0.6, 0.3);
            this.level.sendParticles(ParticleTypes.END_ROD, foundEggPos.getX() + 0.5, foundEggPos.getY() + 0.5, foundEggPos.getZ() + 0.5, 60, 0.4, 0.4, 0.4, 0.15);
            this.level.playSound(null, foundEggPos, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.5F, 1.0F);
            this.level.playSound(null, foundEggPos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.5F, 1.2F);

            // Position the dragon descending right above the portal at reachable height
            float yaw = this.level.getRandom().nextFloat() * 360.0F;
            dragon.setPos(0.0, portalY + 2.5, 0.0);
            dragon.setYRot(yaw);
            dragon.setXRot(0.0F);
            dragon.yBodyRot = yaw;
            dragon.yHeadRot = yaw;

            // Resolve the owner player
            Player ownerPlayer = null;
            if (ownerUUID != null) {
                ownerPlayer = this.level.getServer().getPlayerList().getPlayer(ownerUUID);
            }
            if (ownerPlayer == null && ownerUUID == null) {
                ownerPlayer = this.level.getNearestPlayer(0.0, portalY, 0.0, 64.0, false);
                if (ownerPlayer != null) {
                    ownerUUID = ownerPlayer.getUUID();
                    ownerName = ownerPlayer.getName().getString();
                }
            }

            // Tame the dragon
            PetHelper.setTamed(dragon, ownerUUID, ownerPlayer);

            // Clear EnderDragonFight boss management so dragon doesn't circle crystals or act as arena boss
            this.dragonUUID = null;
            if (this.dragonEvent != null) {
                this.dragonEvent.setVisible(false);
                this.dragonEvent.removeAllPlayers();
            }

            // Set dragon phase directly to hovering near owner
            dragon.getPhaseManager().setPhase(net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase.HOVERING);

            // Send notification
            if (ownerPlayer != null) {
                ownerPlayer.sendSystemMessage(Component.translatable("petting.action.golden_dragon_hatched"));
            }
            for (ServerPlayer sp : this.level.players()) {
                if (sp != ownerPlayer) {
                    sp.sendSystemMessage(Component.translatable("petting.action.golden_dragon_summoned", ownerName != null ? ownerName : "A player"));
                }
            }
        }
    }
}
