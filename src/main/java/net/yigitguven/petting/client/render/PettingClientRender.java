package net.yigitguven.petting.client.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.yigitguven.petting.Petting;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class PettingClientRender {

    private static final Map<LivingEntityRenderState, PetRenderStateData> RENDER_DATA =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static @Nullable PetRenderStateData getRenderData(LivingEntityRenderState state) {
        return RENDER_DATA.get(state);
    }

    public static void putRenderData(LivingEntityRenderState state, PetRenderStateData data) {
        RENDER_DATA.put(state, data);
    }

    private static final MethodHandle ADD_LAYER_HANDLE;

    static {
        MethodHandle handle = null;
        try {
            Method m = LivingEntityRenderer.class.getDeclaredMethod("addLayer", RenderLayer.class);
            m.setAccessible(true);
            handle = MethodHandles.lookup().unreflect(m);
        } catch (Exception e) {
            for (Method m : LivingEntityRenderer.class.getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == RenderLayer.class) {
                    m.setAccessible(true);
                    try {
                        handle = MethodHandles.lookup().unreflect(m);
                        break;
                    } catch (Exception ignored) {}
                }
            }
        }
        ADD_LAYER_HANDLE = handle;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            try {
                EntityRenderer<?, ?> renderer = event.getEntityRenderer((EntityType) type);
                if (renderer instanceof LivingEntityRenderer livingRenderer) {
                    attachLayer(livingRenderer);
                }
            } catch (Exception ignored) {}
        }

        for (PlayerModelType skin : event.getModelTypes()) {
            EntityRenderer<?, ?> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer instanceof LivingEntityRenderer livingRenderer) {
                attachLayer(livingRenderer);
            }
            EntityRenderer<?, ?> mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer instanceof LivingEntityRenderer livingRenderer) {
                attachLayer(livingRenderer);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attachLayer(LivingEntityRenderer livingRenderer) {
        if (ADD_LAYER_HANDLE != null) {
            try {
                ADD_LAYER_HANDLE.invoke(livingRenderer, new PetCollarLayer(livingRenderer));
            } catch (Throwable t) {
                Petting.LOGGER.error("Failed to attach PetCollarLayer to {}", livingRenderer.getClass().getSimpleName(), t);
            }
        }
    }
}
