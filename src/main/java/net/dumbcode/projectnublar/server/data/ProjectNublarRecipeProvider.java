package net.dumbcode.projectnublar.server.data;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.recipes.crafting.ProjectNublarRecipesSerializers;
import net.minecraft.data.CustomRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;

import java.util.function.Consumer;

public class ProjectNublarRecipeProvider extends RecipeProvider {
    public ProjectNublarRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        buildRecipes(recipe -> consumer.accept(new NameChangingFinishedRecipe(recipe, ProjectNublar.MODID)));
    }

    private void buildRecipes(Consumer<IFinishedRecipe> consumer) {
        CustomRecipeBuilder.special(ProjectNublarRecipesSerializers.ARTIFICIAL_EGG_ADD_DNA.get()).save(consumer, "artificial_egg_add_dna");
    }
}
