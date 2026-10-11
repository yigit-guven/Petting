package net.yigitguven.petting.client.render;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.util.PetHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class PettingClientRender {
    public static final ContextKey<DyeColor> COLLAR_COLOR_KEY =
            new ContextKey<>(Identifier.fromNamespaceAndPath(Petting.MODID, "collar_color"));

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

    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>() {},
                (entity, renderState) -> {
                    if (PetHelper.isTamed(entity)) {
                        ItemStack collar = PetHelper.getCollar(entity);
                        if (!collar.isEmpty() && collar.getItem() instanceof CollarItem collarItem) {
                            renderState.setRenderData(COLLAR_COLOR_KEY, collarItem.getColor());
                        }
                    }
                }
        );
    }

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?, ?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer) {
                attachLayer(livingRenderer);
            }
        }

        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<?> playerRenderer = event.getPlayerRenderer(skin);
            if (playerRenderer != null) {
                attachLayer(playerRenderer);
            }
            AvatarRenderer<?> mannequinRenderer = event.getMannequinRenderer(skin);
            if (mannequinRenderer != null) {
                attachLayer(mannequinRenderer);
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
