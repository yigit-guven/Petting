package net.yigitguven.petting.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.yigitguven.petting.Petting;

public class PetCollarLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {
    private static final Identifier COLLAR_TEXTURE =
            Identifier.fromNamespaceAndPath(Petting.MODID, "textures/entity/collar.png");

    public PetCollarLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    private static class CandidateHolder {
        PoseStack.Pose pose;
        ModelPart.Cube cube;
        int score = -1;
        float volume = -1.0F;
        float minY = Float.MAX_VALUE;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, S state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }

        DyeColor collarColor = state.getRenderData(PettingClientRender.COLLAR_COLOR_KEY);
        if (collarColor == null) {
            return;
        }

        M parentModel = getParentModel();
        if (parentModel == null) {
            return;
        }
        ModelPart root = parentModel.root();
        if (root == null) {
            return;
        }

        CandidateHolder candidate = new CandidateHolder();

        root.visit(poseStack, (pose, path, cubeIndex, cube) -> {
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
                    candidate.pose = pose.copy();
                    candidate.cube = cube;
                }
            } else if (candidate.score < 0) {
                // Fallback: look for the highest cube (lowest minY) with reasonable volume
                if (cube.minY < candidate.minY && vol > 8.0F) {
                    candidate.minY = cube.minY;
                    candidate.volume = vol;
                    candidate.pose = pose.copy();
                    candidate.cube = cube;
                }
            }
        });

        if (candidate.cube == null || candidate.pose == null) {
            return;
        }

        ModelPart.Cube headCube = candidate.cube;

        // In Minecraft entity models, coordinates in cubes are in pixel units.
        // VertexConsumer coordinates are in block units (1 block = 16 pixels).
        // Therefore, all pixel values must be scaled by 1/16F.
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

        float margin = 0.35F * INV_SCALE; // Snug ~1/3 pixel margin outside the neck to avoid z-fighting
        float collarHeight = Math.max(1.2F * INV_SCALE, Math.min(2.0F * INV_SCALE, headH * 0.22F));
        float strapThick = 0.4F * INV_SCALE;

        float bx0 = x0 - margin;
        float bx1 = x1 + margin;
        float bz0 = z0 - margin;
        float bz1 = z1 + margin;

        float centerX = (x0 + x1) * 0.5F;
        float centerZ = (z0 + z1) * 0.5F;

        // Position at the bottom seam of the head cube (neck)
        float by0 = yMax - (0.5F * INV_SCALE);
        float by1 = by0 + collarHeight;

        int diffuse = collarColor.getTextureDiffuseColor();
        int strapColor = 0xFF000000 | diffuse;

        PoseStack.Pose capturedPose = candidate.pose;

        submitNodeCollector.order(1).submitCustomGeometry(
                poseStack,
                RenderTypes.armorCutoutNoCull(COLLAR_TEXTURE),
                (ignoredPose, consumer) -> {
                    renderCollar(capturedPose, consumer, bx0, by0, bz0, bx1, by1, bz1, centerX, centerZ, strapThick, collarHeight, strapColor, packedLight, INV_SCALE);
                }
        );
    }

    private static void renderCollar(
            PoseStack.Pose pose, VertexConsumer consumer,
            float bx0, float by0, float bz0,
            float bx1, float by1, float bz1,
            float centerX, float centerZ,
            float strapThick, float collarHeight,
            int strapColor, int packedLight,
            float invScale
    ) {
        int overlay = OverlayTexture.NO_OVERLAY;

        // Strap UVs: rows 0..3 (V: 0.0 to 0.125)
        // With uniform horizontal gradient, u0=0.0 and u1=1.0 gives seamless leather shading without vertical banding
        float strapU0 = 0.0F;
        float strapU1 = 1.0F;
        float strapV0 = 0.0F;
        float strapV1 = 4.0F / 32.0F;
        float topV0 = 0.0F;
        float topV1 = 1.0F / 32.0F;
        float botV0 = 3.0F / 32.0F;
        float botV1 = 4.0F / 32.0F;

        // 1. Front strap (North)
        addStrapBox(pose, consumer, bx0, by0, bz0, bx1, by1, bz0 + strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);

        // 2. Back strap (South)
        addStrapBox(pose, consumer, bx0, by0, bz1 - strapThick, bx1, by1, bz1, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);

        // 3. Left strap (West)
        addStrapBox(pose, consumer, bx0, by0, bz0 + strapThick, bx0 + strapThick, by1, bz1 - strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);

        // 4. Right strap (East)
        addStrapBox(pose, consumer, bx1 - strapThick, by0, bz0 + strapThick, bx1, by1, bz1 - strapThick, strapColor, packedLight, overlay, strapU0, strapV0, strapU1, strapV1, topV0, topV1, botV0, botV1);

        // 5. Golden Circular Medallion Tag on the front center (~2 pixels square, 1:1 pixel aspect ratio)
        float tagSize = Math.max(1.6F * invScale, Math.min(2.2F * invScale, collarHeight * 1.15F));
        float tagMinX = centerX - tagSize * 0.5F;
        float tagMaxX = centerX + tagSize * 0.5F;
        float tagMinY = by0 + (collarHeight - tagSize) * 0.5F;
        float tagMaxY = tagMinY + tagSize;
        float tagZ = bz0 - (0.04F * invScale);

        // Medallion icon: rows 8..15 (U: 0.0..0.25, V: 0.25..0.5)
        float buckleU0 = 0.0F;
        float buckleU1 = 0.25F;
        float buckleV0 = 0.25F;
        float buckleV1 = 0.5F;

        // Render circular medallion front face directly on collar front (no square box edges)
        addQuad(pose, consumer,
                tagMinX, tagMinY, tagZ,
                tagMinX, tagMaxY, tagZ,
                tagMaxX, tagMaxY, tagZ,
                tagMaxX, tagMinY, tagZ,
                0, 0, -1,
                buckleU0, buckleV0, buckleU1, buckleV1,
                0xFFFFFFFF, packedLight, overlay);
    }

    private static void addStrapBox(
            PoseStack.Pose pose, VertexConsumer consumer,
            float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ,
            int color, int packedLight, int overlay,
            float u0, float v0, float u1, float v1,
            float topV0, float topV1,
            float botV0, float botV1
    ) {
        // North face (facing -Z)
        addQuad(pose, consumer,
                minX, minY, minZ,
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ,
                0, 0, -1, u0, v0, u1, v1, color, packedLight, overlay);

        // South face (facing +Z)
        addQuad(pose, consumer,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ,
                minX, minY, maxZ,
                0, 0, 1, u0, v0, u1, v1, color, packedLight, overlay);

        // West face (facing -X)
        addQuad(pose, consumer,
                minX, minY, maxZ,
                minX, maxY, maxZ,
                minX, maxY, minZ,
                minX, minY, minZ,
                -1, 0, 0, u0, v0, u1, v1, color, packedLight, overlay);

        // East face (facing +X)
        addQuad(pose, consumer,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                maxX, minY, maxZ,
                1, 0, 0, u0, v0, u1, v1, color, packedLight, overlay);

        // Top face (facing -Y)
        addQuad(pose, consumer,
                minX, minY, maxZ,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                0, -1, 0, u0, topV0, u1, topV1, color, packedLight, overlay);

        // Bottom face (facing +Y)
        addQuad(pose, consumer,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                0, 1, 0, u0, botV0, u1, botV1, color, packedLight, overlay);
    }

    private static void addQuad(
            PoseStack.Pose pose, VertexConsumer consumer,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float nx, float ny, float nz,
            float u0, float v0, float u1, float v1,
            int color, int packedLight, int overlay
    ) {
        consumer.addVertex(pose, x1, y1, z1).setColor(color).setUv(u0, v0).setOverlay(overlay).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(color).setUv(u0, v1).setOverlay(overlay).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x3, y3, z3).setColor(color).setUv(u1, v1).setOverlay(overlay).setLight(packedLight).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x4, y4, z4).setColor(color).setUv(u1, v0).setOverlay(overlay).setLight(packedLight).setNormal(pose, nx, ny, nz);
    }
}
