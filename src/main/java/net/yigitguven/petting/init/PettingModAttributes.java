package net.yigitguven.petting.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.PettingMod;

public class PettingModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PettingMod.MODID);

    public static final RegistryObject<Attribute> MAX_PETS = REGISTRY.register("max_pets", 
        () -> new RangedAttribute("attribute.name.petting.max_pets", 1.0D, -1.0D, 1024.0D).setSyncable(true));
}
