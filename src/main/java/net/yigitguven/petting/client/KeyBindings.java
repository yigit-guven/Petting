package net.yigitguven.petting.client;

public class KeyBindings {
    public static net.minecraft.client.KeyMapping OPEN_PET_SETTINGS;

    public static void register() {
        try {
            OPEN_PET_SETTINGS = new net.minecraft.client.KeyMapping("controls.petting.open_settings", org.lwjgl.glfw.GLFW.GLFW_KEY_P, "key.categories.petting");
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                try {
                    net.minecraft.client.KeyMapping[] existing = mc.options.keyMappings;
                    if (existing == null) {
                        mc.options.keyMappings = new net.minecraft.client.KeyMapping[]{OPEN_PET_SETTINGS};
                    } else {
                        boolean found = false;
                        for (net.minecraft.client.KeyMapping km : existing) if (km == OPEN_PET_SETTINGS || km.getName().equals(OPEN_PET_SETTINGS.getName())) { found = true; break; }
                        if (!found) {
                            net.minecraft.client.KeyMapping[] arr = java.util.Arrays.copyOf(existing, existing.length + 1);
                            arr[existing.length] = OPEN_PET_SETTINGS;
                            mc.options.keyMappings = arr;
                        }
                    }
                } catch (Throwable ignored) {
                    // best-effort; if this fails, Controls UI may not show mapping until restart
                }
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }
}
