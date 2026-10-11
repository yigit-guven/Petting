package net.yigitguven.petting.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;

public class PettingModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Petting.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PETTING_TAB =
            CREATIVE_MODE_TABS.register("petting", () -> CreativeModeTab.builder()
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
}
