package fi.dy.masa.enderutilities.compat.jei;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.startup.StackHelper;

@mezz.jei.api.JEIPlugin
public class EnderUtilitiesJeiPlugin implements IModPlugin
{
    @Override
    public void register(IModRegistry registry)
    {
        registry.addRecipeClickArea(GuiEnderFurnace.class,     58, 35, 22, 15, VanillaRecipeCategoryUid.SMELTING);
        // FIXME Add the custom fuels to a custom handler(?)
        registry.addRecipeClickArea(GuiEnderFurnace.class,     34, 36, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class,  27, 11, 10, 10, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GuiCreationStation.class,   9, 29, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class, 203, 11, 10, 10, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GuiCreationStation.class, 217, 29, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class,  97, 36, 10, 10, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeClickArea(GuiCreationStation.class, 133, 72, 10, 10, VanillaRecipeCategoryUid.CRAFTING);

        RecipeHandlerCreationStation transferInfo = new RecipeHandlerCreationStation();
        StackHelper stackHelper = (StackHelper) registry.getJeiHelpers().getStackHelper();
        IRecipeTransferHandlerHelper handlerHelper = registry.getJeiHelpers().recipeTransferHandlerHelper();
        RecipeTransferHandlerCreationStation transferHandler = new RecipeTransferHandlerCreationStation(stackHelper, handlerHelper, transferInfo);

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(transferHandler, transferInfo.getRecipeCategoryUid());

        // Creation Station
        registry.addRecipeCatalyst(new ItemStack(EnderUtilitiesBlocks.MACHINE_1, 1, 2),
                VanillaRecipeCategoryUid.CRAFTING, VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);

        // Ender Furnace
        registry.addRecipeCatalyst(new ItemStack(EnderUtilitiesBlocks.ENDER_FURNACE, 1, 0),
                VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry)
    {
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry)
    {
    }
}
