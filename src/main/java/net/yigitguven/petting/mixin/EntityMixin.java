package net.yigitguven.petting.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements net.yigitguven.petting.IEntityData {
    @Unique
    private CompoundTag persistentData;

    @Override
    @Unique
    public CompoundTag getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new CompoundTag();
        }
        return this.persistentData;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void onSaveWithoutId(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (this.persistentData != null && !this.persistentData.isEmpty()) {
            cir.getReturnValue().put("PettingPersistentData", this.persistentData.copy());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("PettingPersistentData", 10)) {
            if (this.persistentData == null) {
                this.persistentData = new CompoundTag();
            }
            this.persistentData.merge(nbt.getCompound("PettingPersistentData"));
        }
    }
}
