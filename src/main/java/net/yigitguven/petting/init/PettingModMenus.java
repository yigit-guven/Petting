package net.yigitguven.petting.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, PettingMod.MODID);

    public static final RegistryObject<net.minecraft.world.inventory.MenuType<PetInventoryMenu>> PET_INVENTORY = REGISTRY.register("pet_inventory", 
        () -> net.minecraftforge.common.extensions.IForgeMenuType.create(PetInventoryMenu::new));
}
