package net.yigitguven.petting.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PettingMod.MODID);

    public static final RegistryObject<MenuType<PetInventoryMenu>> PET_INVENTORY = REGISTRY.register("pet_inventory", 
        () -> IForgeMenuType.create(PetInventoryMenu::new));
}
