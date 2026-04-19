package net.yigitguven.petting.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, PettingMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PetInventoryMenu>> PET_INVENTORY = REGISTRY.register("pet_inventory", 
        () -> IMenuTypeExtension.create(PetInventoryMenu::new));
}
