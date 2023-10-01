package net.dumbcode.projectnublar.server.recipes.crafting;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectNublarRecipesSerializers {
    public static final DeferredRegister<IRecipeSerializer<?>> REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ProjectNublar.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<ArtificialEggAddDnaCraftingRecipe>> ARTIFICIAL_EGG_ADD_DNA = REGISTER.register("artificial_egg_add_dna", () -> new SpecialRecipeSerializer<>(ArtificialEggAddDnaCraftingRecipe::new));
//    public static final RegistryObject<SpecialRecipeSerializer<ArtificialEggAddDnaCraftingRecipe>> EGG_PRINTER = REGISTER.register("artifical_egg_add_dna", () -> new SpecialRecipeSerializer<>(ArtificialEggAddDnaCraftingRecipe::new));
}
