package net.yigitguven.petting.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.init.PettingModItems;

import java.util.ArrayList;
import java.util.List;

public class PetTamingCategory implements IRecipeCategory<PetTamingRecipe> {
    public static final IRecipeType<PetTamingRecipe> RECIPE_TYPE =
            IRecipeType.create(Identifier.fromNamespaceAndPath(Petting.MODID, "taming"), PetTamingRecipe.class);

    private final IDrawable icon;
    private final IDrawable arrow;
    private final Component title;

    public PetTamingCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(PettingModItems.GOLDEN_WHEAT.get());
        this.arrow = guiHelper.getRecipeArrow();
        this.title = Component.translatable("petting.jei.taming.title");
    }

    public static int getRowCount(PetTamingRecipe recipe) {
        return Math.min(3, Math.max(1, (recipe.tameableMobs().size() + 5) / 6));
    }

    @Override
    public IRecipeType<PetTamingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 170;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(PetTamingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 31, 23);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PetTamingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 23)
                .add(recipe.treat())
                .setStandardSlotBackground();

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

            builder.addOutputSlot(x, y)
                    .addItemStacks(slotItems)
                    .setStandardSlotBackground();
        }
    }
}
