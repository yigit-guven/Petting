package net.yigitguven.petting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.config.PettingClientConfig;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.util.PetHelper;

import java.util.Map;

public class PetCollarLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation COLLAR_TEXTURE =
            new ResourceLocation(Petting.MODID, "textures/entity/collar.png");

    public PetCollarLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    private static class CandidateHolder {
        org.joml.Matrix4f poseMatrix;
        org.joml.Matrix3f normalMatrix;
        ModelPart.Cube cube;
        int score = -1;
        float volume = -1.0F;
        float minY = Float.MAX_VALUE;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (livingEntity.isInvisible()) {
            return;
        }

        if (!PettingServerConfig.isCollarModelsEnabled() || !PettingClientConfig.isCollarModelsEnabled()) {
            return;
        }

        ItemStack collar = PetHelper.getCollar(livingEntity);
        if (collar.isEmpty() || !(collar.getItem() instanceof CollarItem collarItem)) {
            return;
        }

        DyeColor collarColor = collarItem.getColor();

        M parentModel = getParentModel();
        if (parentModel == null) {
            return;
        }

        CandidateHolder candidate = new CandidateHolder();
        ModelPart.Visitor visitor = (pose, path, cubeIndex, cube) -> {
            String p = path.toLowerCase();
            int score = 0;
            if (p.contains("real_head") || p.endsWith("/realhead")) {
                score = 100;
            } else if (p.contains("center_head") || p.contains("centerhead") || p.contains("head_main")) {
                score = 95;
            } else if (p.contains("head") && !p.contains("left") && !p.contains("right")) {
                score = 90;
            } else if (p.contains("head_parts") || p.contains("headparts")) {
                score = 85;
            } else if (p.contains("head")) {
                score = 80;
            } else if (p.contains("neck")) {
                score = 50;
            }

            float w = cube.maxX - cube.minX;
            float h = cube.maxY - cube.minY;
            float d = cube.maxZ - cube.minZ;
            float vol = w * h * d;

            if (score > 0) {
                if (score > candidate.score || (score == candidate.score && vol > candidate.volume)) {
                    candidate.score = score;
                    candidate.volume = vol;
                    candidate.poseMatrix = new org.joml.Matrix4f(pose.pose());
                    candidate.normalMatrix = new org.joml.Matrix3f(pose.normal());
                    candidate.cube = cube;
                }
            } else if (candidate.score < 0) {
                if (cube.minY < candidate.minY && vol > 8.0F) {
                    candidate.minY = cube.minY;
                    candidate.volume = vol;
                    candidate.poseMatrix = new org.joml.Matrix4f(pose.pose());
                    candidate.normalMatrix = new org.joml.Matrix3f(pose.normal());
                    candidate.cube = cube;
                }
            }
        };

        poseStack.pushPose();
        if (parentModel instanceof HierarchicalModel<?> hm) {
            hm.root().visit(poseStack, visitor);
        } else if (parentModel instanceof HeadedModel hm) {
            hm.getHead().visit(poseStack, visitor);
        } else {
            for (java.lang.reflect.Field f : parentModel.getClass().getDeclaredFields()) {
                if (ModelPart.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    try {
                        ModelPart part = (ModelPart) f.get(parentModel);
                        if (part != null) {
                            part.visit(poseStack, visitor);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
        poseStack.popPose();

        if (candidate.cube == null || candidate.poseMatrix == null) {
            return;
        }

        ModelPart.Cube headCube = candidate.cube;
        final float INV_SCALE = 1.0F / 16.0F;

        float x0 = headCube.minX * INV_SCALE;
        float x1 = headCube.maxX * INV_SCALE;
        float yMax = headCube.maxY * INV_SCALE;
        float z0 = headCube.minZ * INV_SCALE;
        float z1 = headCube.maxZ * INV_SCALE;

        float headW = x1 - x0;
        float headD = z1 - z0;
        float headH = (headCube.maxY - headCube.minY) * INV_SCALE;
        if (headW <= 0.005F || headD <= 0.005F) {
            return;
        }

        float margin = 0.35F * INV_SCALE;
        float collarHeight = Math.max(1.2F * INV_SCALE, Math.min(2.0F * INV_SCALE, headH * 0.22F));
        float strapThick = 0.4F * INV_SCALE;

        float bx0 = x0 - margin;
        float bx1 = x1 + margin;
        float bz0 = z0 - margin;
        float bz1 = z1 + margin;

        float centerX = (x0 + x1) * 0.5F;
        float centerZ = (z0 + z1) * 0.5F;

        float by0 = yMax - (0.5F * INV_SCALE);
        float by1 = by0 + collarHeight;

        float[] colors = collarColor.getTextureDiffuseColors();
        int cr = (int) (colors[0] * 255.0F);
        int cg = (int) (colors[1] * 255.0F);
        int cb = (int) (colors[2] * 255.0F);
        int strapColor = 0xFF000000 | (cr << 16) | (cg << 8) | cb;

        boolean isEnchanted = collarItem.isFoil(collar);
        RenderType renderType = RenderType.armorCutoutNoCull(COLLAR_TEXTURE);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, renderType, false, isEnchanted);

        renderCollar(candidate.poseMatrix, candidate.normalMatrix, consumer, bx0, by0, bz0, bx1, by1, bz1, centerX, centerZ, strapThick, collarHeight, strapColor, packedLight, INV_SCALE);
    }

    private static void renderCollar(
            org.joml.Matrix4f poseMatrix, org.joml.Matrix3f normalMatrix, VertexConsumer consumer,
            float bx0, float by0, float bz0,
            float bx1, float by1, float bz1,
            float centerX, float centerZ,
            float strapThick, float collarHeight,
            int strapColor, int packedLight,
            float invScale
    ) {
        int overlay = OverlayTexture.NO_OVERLAY;

        float strapU0 = 0.0F;
        float strapU1 = 1.0F;
        float strapV0 = 0.0F;
        float strapV1 = 4.0F / 32.0F;
        float topV0 = 0.0F;
        float topV1 = 1.0F / 32.0F;
        float botV0 = 3.0F / 32.0F;
        float botV1 = 4.0F / 32.0F;

        addStrapBox(poseMatrix, normalMatrix, consumer, bx0, by0, bz0, bx1, by1, bz0 + strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);
        addStrapBox(poseMatrix, normalMatrix, consumer, bx0, by0, bz1 - strapThick, bx1, by1, bz1, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);
        addStrapBox(poseMatrix, normalMatrix, consumer, bx0, by0, bz0 + strapThick, bx0 + strapThick, by1, bz1 - strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);
        addStrapBox(poseMatrix, normalMatrix, consumer, bx1 - strapThick, by0, bz0 + strapThick, bx1, by1, bz1 - strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);

        float tagSize = Math.max(1.6F * invScale, Math.min(2.2F * invScale, collarHeight * 1.15F));
        float tagMinX = centerX - tagSize * 0.5F;
        float tagMaxX = centerX + tagSize * 0.5F;
        float tagMinY = by0 + (collarHeight - tagSize) * 0.5F;
        float tagMaxY = tagMinY + tagSize;
        float tagZ = bz0 - (0.04F * invScale);

        float buckleU0 = 0.0F;
        float buckleU1 = 0.25F;
        float buckleV0 = 0.25F;
        float buckleV1 = 0.5F;

        addQuad(poseMatrix, normalMatrix, consumer,
                tagMinX, tagMinY, tagZ,
                tagMinX, tagMaxY, tagZ,
                tagMaxX, tagMaxY, tagZ,
                tagMaxX, tagMinY, tagZ,
                0, 0, -1,
                buckleU0, buckleV0, buckleU1, buckleV1,
                0xFFFFFFFF, packedLight, overlay);
    }

    private static void addStrapBox(
            org.joml.Matrix4f poseMatrix, org.joml.Matrix3f normalMatrix, VertexConsumer consumer,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            int color, int packedLight, int overlay,
            float u0, float v0, float u1, float v1,
            float topV0, float topV1,
            float botV0, float botV1
    ) {
        addQuad(poseMatrix, normalMatrix, consumer, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ, 0, 0, -1, u0, v0, u1, v1, color, packedLight, overlay);
        addQuad(poseMatrix, normalMatrix, consumer, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ, 0, 0, 1, u0, v0, u1, v1, color, packedLight, overlay);
        addQuad(poseMatrix, normalMatrix, consumer, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ, -1, 0, 0, u0, v0, u1, v1, color, packedLight, overlay);
        addQuad(poseMatrix, normalMatrix, consumer, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ, 1, 0, 0, u0, v0, u1, v1, color, packedLight, overlay);
        addQuad(poseMatrix, normalMatrix, consumer, minX, minY, maxZ, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, 0, -1, 0, u0, topV0, u1, topV1, color, packedLight, overlay);
        addQuad(poseMatrix, normalMatrix, consumer, minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, 0, 1, 0, u0, botV0, u1, botV1, color, packedLight, overlay);
    }

    private static void addQuad(
            org.joml.Matrix4f poseMatrix, org.joml.Matrix3f normalMatrix, VertexConsumer consumer,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float nx, float ny, float nz,
            float u0, float v0, float u1, float v1,
            int color, int packedLight, int overlay
    ) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        consumer.vertex(poseMatrix, x1, y1, z1).color(r, g, b, a).uv(u0, v0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        consumer.vertex(poseMatrix, x2, y2, z2).color(r, g, b, a).uv(u0, v1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        consumer.vertex(poseMatrix, x3, y3, z3).color(r, g, b, a).uv(u1, v1).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
        consumer.vertex(poseMatrix, x4, y4, z4).color(r, g, b, a).uv(u1, v0).overlayCoords(overlay).uv2(packedLight).normal(normalMatrix, nx, ny, nz).endVertex();
    }
}
