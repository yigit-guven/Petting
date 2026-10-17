package net.yigitguven.petting.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModTags;

import java.util.List;

@JeiPlugin
public class PettingJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_UID = ResourceLocation.fromNamespaceAndPath(Petting.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PetTamingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PetTamingRecipe> recipes = List.of(
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_WHEAT.get()), PettingModTags.EntityTypes.GOLDEN_WHEAT),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_STAR.get()), PettingModTags.EntityTypes.GOLDEN_STAR),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_FLESH.get()), PettingModTags.EntityTypes.GOLDEN_FLESH),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_BONE.get()), PettingModTags.EntityTypes.GOLDEN_BONE),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_FISH.get()), PettingModTags.EntityTypes.GOLDEN_FISH),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_KELP.get()), PettingModTags.EntityTypes.GOLDEN_KELP),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_FUNGUS.get()), PettingModTags.EntityTypes.GOLDEN_FUNGUS),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_EYE.get()), PettingModTags.EntityTypes.GOLDEN_EYE),
                new PetTamingRecipe(new ItemStack(PettingModItems.GOLDEN_SLIME.get()), PettingModTags.EntityTypes.GOLDEN_SLIME)
        );

        registration.addRecipes(PetTamingCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_WHEAT.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_STAR.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_FLESH.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_BONE.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_FISH.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_KELP.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_FUNGUS.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_EYE.get()), PetTamingCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(PettingModItems.GOLDEN_SLIME.get()), PetTamingCategory.RECIPE_TYPE);
    }
}
