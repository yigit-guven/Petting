package net.yigitguven.petting.init;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;

public class PettingModTabs {
    public static final RegistrySupplier<CreativeModeTab> PETTING_TAB = register("petting",
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.petting"))
                    .icon(() -> new ItemStack(PettingModItems.GOLDEN_PAW.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(PettingModItems.GOLDEN_WHEAT.get());
                        output.accept(PettingModItems.GOLDEN_STAR.get());
                        output.accept(PettingModItems.GOLDEN_FLESH.get());
                        output.accept(PettingModItems.GOLDEN_BONE.get());
                        output.accept(PettingModItems.GOLDEN_FISH.get());
                        output.accept(PettingModItems.GOLDEN_KELP.get());
                        output.accept(PettingModItems.GOLDEN_FUNGUS.get());
                        output.accept(PettingModItems.GOLDEN_EYE.get());
                        output.accept(PettingModItems.GOLDEN_SLIME.get());
                        output.accept(PettingModItems.COLLAR.get());
                        output.accept(PettingModItems.WHITE_COLLAR.get());
                        output.accept(PettingModItems.ORANGE_COLLAR.get());
                        output.accept(PettingModItems.MAGENTA_COLLAR.get());
                        output.accept(PettingModItems.LIGHT_BLUE_COLLAR.get());
                        output.accept(PettingModItems.YELLOW_COLLAR.get());
                        output.accept(PettingModItems.LIME_COLLAR.get());
                        output.accept(PettingModItems.PINK_COLLAR.get());
                        output.accept(PettingModItems.GRAY_COLLAR.get());
                        output.accept(PettingModItems.LIGHT_GRAY_COLLAR.get());
                        output.accept(PettingModItems.CYAN_COLLAR.get());
                        output.accept(PettingModItems.PURPLE_COLLAR.get());
                        output.accept(PettingModItems.BLUE_COLLAR.get());
                        output.accept(PettingModItems.BROWN_COLLAR.get());
                        output.accept(PettingModItems.GREEN_COLLAR.get());
                        output.accept(PettingModItems.BLACK_COLLAR.get());
                    })
                    .build());

    private static RegistrySupplier<CreativeModeTab> register(String name, CreativeModeTab tab) {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(Petting.MODID, name), tab);
        return new RegistrySupplier<>(tab);
    }

    public static void register() {
    }
}
