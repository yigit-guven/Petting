package net.yigitguven.petting.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.init.PettingModItems;

import java.util.ArrayList;
import java.util.List;

public class PetTamingCategory implements IRecipeCategory<PetTamingRecipe> {
    public static final RecipeType<PetTamingRecipe> RECIPE_TYPE =
            RecipeType.create(Petting.MODID, "taming", PetTamingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable slotDrawable;
    private final Component title;

    public PetTamingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(170, 64);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(PettingModItems.GOLDEN_WHEAT.get()));
        this.arrow = guiHelper.createDrawable(new ResourceLocation("minecraft", "textures/gui/container/furnace.png"), 79, 35, 24, 17);
        this.slotDrawable = guiHelper.getSlotDrawable();
        this.title = Component.translatable("petting.jei.taming.title");
    }

    public static int getRowCount(PetTamingRecipe recipe) {
        return Math.min(3, Math.max(1, (recipe.tameableMobs().size() + 5) / 6));
    }

    @Override
    public RecipeType<PetTamingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(PetTamingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 31, 23);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PetTamingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 23)
                .addItemStack(recipe.treat())
                .setBackground(slotDrawable, -1, -1);

        List<ItemStack> mobs = recipe.tameableMobs();
        int rowCount = getRowCount(recipe);
        int startY = (64 - rowCount * 18) / 2;
        int maxSlots = rowCount * 6;
        int slotCount = Math.min(mobs.size(), maxSlots);

        for (int i = 0; i < slotCount; i++) {
            int col = i % 6;
            int row = i / 6;
            int x = 58 + col * 18;
            int y = startY + row * 18;

            List<ItemStack> slotItems;
            if (mobs.size() <= maxSlots) {
                slotItems = List.of(mobs.get(i));
            } else {
                slotItems = new ArrayList<>();
                for (int j = i; j < mobs.size(); j += maxSlots) {
                    slotItems.add(mobs.get(j));
                }
            }

            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y)
                    .addItemStacks(slotItems)
                    .setBackground(slotDrawable, -1, -1);
        }
    }
}
