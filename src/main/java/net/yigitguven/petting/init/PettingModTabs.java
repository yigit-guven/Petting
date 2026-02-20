package net.yigitguven.petting.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.PettingMod;

public class PettingModTabs {
    public static final ResourceKey<CreativeModeTab> PETTING_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(PettingMod.MODID, "petting_tab"));
    
    public static final CreativeModeTab PETTING_TAB = FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.petting"))
            .icon(() -> new ItemStack(PettingModItems.GOLDEN_WHEAT))
            .build();

    public static void register() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, PETTING_TAB_KEY, PETTING_TAB);
    }
}
