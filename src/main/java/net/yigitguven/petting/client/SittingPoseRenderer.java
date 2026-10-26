package net.yigitguven.petting.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SittingPoseRenderer {

    private static final float SIT_LEG_X_ROT = -1.4137167F;
    private static final float SIT_LEG_Y_ROT = 0.31415927F;
    private static final float SIT_LEG_Z_ROT = 0.07853982F;

    private static final float OFFSET_HUMANOID = 0.5F;
    private static final float OFFSET_ROBED = 0.25F;
    private static final float OFFSET_GOLEM = 0.6F;

    private static final Map<Integer, Float> WORLD_DROP = new ConcurrentHashMap<>();

    private SittingPoseRenderer() {
    }

    public static boolean isSupportedModel(EntityModel<?> model) {
        return model instanceof HumanoidModel<?>
                || model instanceof IllagerModel<?>
                || model instanceof VillagerModel<?>
                || model instanceof IronGolemModel<?>;
    }

    public static boolean shouldSit(LivingEntity entity, EntityModel<?> model) {
        if (entity == null || model == null || !isSupportedModel(model)) {
            return false;
        }
        if (entity.isPassenger() || !entity.isAlive() || entity.hasPose(Pose.SLEEPING)) {
            return false;
        }
        return PetHelper.isTamed(entity) && PetHelper.getOrder(entity) == PetOrder.SIT;
    }

    private static float getModelOffset(LivingEntity entity, EntityModel<?> model) {
        float offset;
        if (model instanceof HumanoidModel<?>) {
            offset = OFFSET_HUMANOID;
        } else if (model instanceof IronGolemModel<?>) {
            offset = OFFSET_GOLEM;
        } else {
            offset = OFFSET_ROBED;
        }
        return entity.isBaby() ? offset * 0.5F : offset;
    }

    public static void prepare(LivingEntity entity, EntityModel<?> model, PoseStack poseStack) {
        if (!shouldSit(entity, model)) {
            WORLD_DROP.remove(entity.getId());
            return;
        }

        model.riding = true;
        if (model instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.crouching = false;
        }

        float modelOffset = getModelOffset(entity, model);
        poseStack.translate(0.0F, modelOffset, 0.0F);

        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        poseStack.last().pose().transformDirection(up);
        WORLD_DROP.put(entity.getId(), Math.abs(up.y()) * modelOffset);
    }

    public static void applyPoseAfterAnim(LivingEntity entity, EntityModel<?> model) {
        if (!shouldSit(entity, model)) {
            return;
        }
        if (model instanceof HumanoidModel<?> || model instanceof IllagerModel<?>) {
            return;
        }
        if (model instanceof HierarchicalModel<?> hierarchical) {
            ModelPart root = hierarchical.root();
            if (model instanceof IronGolemModel<?>) {
                setLegs(root, -1.5707964F, 0.0F, 0.0F);
            } else if (model instanceof VillagerModel<?>) {
                setLegs(root, SIT_LEG_X_ROT, SIT_LEG_Y_ROT, SIT_LEG_Z_ROT);
            }
        }
    }

    private static void setLegs(ModelPart root, float xRot, float yRot, float zRot) {
        if (root.hasChild("right_leg")) {
            ModelPart right = root.getChild("right_leg");
            right.xRot = xRot;
            right.yRot = yRot;
            right.zRot = zRot;
        }
        if (root.hasChild("left_leg")) {
            ModelPart left = root.getChild("left_leg");
            left.xRot = xRot;
            left.yRot = -yRot;
            left.zRot = -zRot;
        }
    }

    public static float getNameTagDrop(LivingEntity entity) {
        Float drop = WORLD_DROP.get(entity.getId());
        if (drop == null) {
            return 0.0F;
        }
        if (!PetHelper.isTamed(entity) || PetHelper.getOrder(entity) != PetOrder.SIT || entity.isPassenger()) {
            WORLD_DROP.remove(entity.getId());
            return 0.0F;
        }
        return drop;
    }

    public static void clear() {
        WORLD_DROP.clear();
    }
}
