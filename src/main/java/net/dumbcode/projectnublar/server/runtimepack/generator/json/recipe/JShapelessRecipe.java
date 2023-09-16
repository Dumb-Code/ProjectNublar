package net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the recipe for <b>shapeless crafting</b>. That means, in the crafting table, as long as you placed the enough ingredient, no matter what shape, it can be crafted.
 *
 */
@SuppressWarnings("unused")
public class JShapelessRecipe extends JResultRecipe {
  private static final String TYPE = "minecraft:crafting_shapeless";

  /**
   * The ingredient list of this recipe.
   */
  @SerializedName("ingredients")
  public final List<JIngredient> ingredientList;

  /**
   * Create a new object with the specified result, and the list of ingredient.
   *
   * @param result      The identifier (as string) of the result item of this shapeless recipe.
   * @param ingredients The collection of the identifiers (as string) of ingredient items. In this case, it should not contain item tags.
   * @since 0.6.2 It's not advised to call this method; you may call {@link #JShapelessRecipe(String, String...)} instead, as it uses varargs.
   */

  public JShapelessRecipe(String result, Collection<String> ingredients) {
    super(TYPE, result);
    this.ingredientList = ingredients.stream().map(JIngredient::ofItem).collect(Collectors.toList());
  }

  /**
   * Create a new object with the specified result, and the list of ingredient.
   *
   * @param result      The identifier (as string) of the result item of this shapeless recipe.
   * @param ingredients The identifiers (as string) of ingredient items. In this case, it should not contain item tags.
   */
  public JShapelessRecipe(String result, String... ingredients) {
    super(TYPE, result);
    this.ingredientList = Arrays.stream(ingredients).map(JIngredient::ofItem).collect(Collectors.toList());
  }

  /**
   * Create a new object with the specified result, and the list of ingredient.
   *
   * @param result      The identifier (as string) of the result item of this shapeless recipe.
   * @param ingredients The collection of the identifiers of ingredient items. In this case, it should not contain item tags.
   */
  public JShapelessRecipe(ResourceLocation result, ResourceLocation...ingredients) {
    super(TYPE, result);
    this.ingredientList = Arrays.stream(ingredients).map(JIngredient::ofItem).collect(Collectors.toList());
  }

  /**
   * Create a new object with the specified result, and the list of ingredient.
   *
   * @param result      The identifier (as string) of the result item of this shapeless recipe.
   * @param ingredients The collection of the ingredient items. In this case, it should not contain item tags.
   */
  public JShapelessRecipe(IItemProvider result, IItemProvider... ingredients) {
    super(TYPE, result);
    this.ingredientList = Arrays.stream(ingredients).map(JIngredient::ofItem).collect(Collectors.toList());
  }

  /**
   * Create a new shapeless recipe object, with the specified result and ingredient list. The two parameters will be directly used.
   *
   * @param result         The result of the recipe.
   * @param ingredientList The list of ingredients. It will be directly used as the field.
   */

  public JShapelessRecipe(final JResult result, List<JIngredient> ingredientList) {
    super(TYPE, result);
    this.ingredientList = ingredientList;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public JShapelessRecipe resultCount(int count) {
    return (JShapelessRecipe) super.resultCount(count);
  }

  @Override
  public JShapelessRecipe group(final String group) {
    return (JShapelessRecipe) super.group(group);
  }

  @Override
  public JShapelessRecipe clone() {
    return (JShapelessRecipe) super.clone();
  }
}
