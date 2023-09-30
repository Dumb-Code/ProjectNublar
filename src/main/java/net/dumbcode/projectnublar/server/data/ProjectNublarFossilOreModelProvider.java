package net.dumbcode.projectnublar.server.data;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.recipes.crafting.ProjectNublarRecipesSerializers;
import net.minecraft.data.CustomRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class ProjectNublarFossilOreModelProvider extends BlockStateProvider {
    public ProjectNublarFossilOreModelProvider(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, ProjectNublar.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
