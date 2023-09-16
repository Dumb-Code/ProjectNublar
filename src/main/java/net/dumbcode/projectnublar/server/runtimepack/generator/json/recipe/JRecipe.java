package net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>A <b>recipe</b> defines item conversion rules such as crafting, cooking, smithing or stonecutting. A recipe has a type, which defines which type of the recipe it belongs to, and an optional group, which states that different recipes with the equal group will be displayed together.</p>
 *
 */
public abstract class JRecipe implements Cloneable {
  /**
   * The type of the recipe. It is in essence in the format of {@link ResourceLocation}, but here it is as string. Possible values include <code style=color:navy>blasting, campfire_cooking, crafting_shaped, crafting_shapeless, smelting, smithing, smoking, stonecutting</code>, which are defined by those subtypes.
   */
  public final String type;

  /**
   * The optional recipe group. If defined, in the recipe book, different recipes with equal group will be shown together.
   */
  public String group;

  /**
   * <p>The advancement of this recipe. It usually triggers when you unlocked the recipe or obtained an ingredient, and rewards you with unlocking this recipe.</p>
   * <p>For example, for the smelting recipe of glass, when you obtain a sands block, the advancement will be achieved and the recipe of glass will be unlocked.</p>
   */
  public final transient Advancement.Builder advancementBuilder = Advancement.Builder.advancement().parent(new ResourceLocation("minecraft", "recipes/root")).requirements(IRequirementsStrategy.OR);

  /**
   * Create a new simple recipe object.
   *
   * @param type The type of the recipe. It is the identifier in the format of string. See {@link #type}.
   */
  public JRecipe(final String type) {
    this.type = type;
  }

  /**
   * Set the recipe group.
   */

  public JRecipe group(final String group) {
    this.group = group;
    return this;
  }

  @Override
  public JRecipe clone() {
    try {
      return (JRecipe) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  /**
   * Create a delegated JRecipe, whose serialization is identical to the delegate.
   *
   * @param delegate The RecipeJsonProvider whose serialization will be directly used.
   * @return The delegated JRecipe.
   */

  public static JRecipe delegate(final IFinishedRecipe delegate) {
    return new Delegate(delegate);
  }

  /**
   * Create a delegated JRecipe, whose serialization is identical to the delegate.
   *
   * @param delegate The CraftingRecipeJsonBuilder whose serialization will be directly used.
   * @return The delegated JRecipe.
   */

  public static JRecipe delegate(final ShapedRecipeBuilder delegate) {
    AtomicReference<IFinishedRecipe> jsonProvider = new AtomicReference<>();
    delegate.save(jsonProvider::set);
    return delegate(jsonProvider.get());
  }

  /**
   * The corresponding advancement builder of this recipe. By default, it is an advancement that triggers when the player obtains a specific ingredient or craft an item, and rewards the player with the recipe.
   *
   * @return The advancement builder of the recipe advancement.
   */
  public Advancement.Builder asAdvancement() {
    return advancementBuilder;
  }

  /**
   * Add a criterion of obtaining the advancement, that you get an item. The item is usually one of the ingredients. That means, when you gain that item, no matter in which way, you will achieve the advancement and unlock this recipe.
   *
   * @param criterionName The name of the advancement criterion. It is usually a short, descriptive name, such as {@code "has_stone"}.
   * @param item          The item that when obtained, the criterion will be triggered.
   */

  public JRecipe addInventoryChangedCriterion(String criterionName, IItemProvider item) {
    advancementBuilder.addCriterion(criterionName, InventoryChangeTrigger.Instance.hasItems(item));
    return this;
  }

  /**
   * Prepare the advancement of the recipe. It will add a criterion of unlocking the recipe itself, and a rewarding of that recipe. Therefore, you should give the recipe id.
   *
   * @param recipeId The id of the recipe.
   */

  public JRecipe prepareAdvancement(ResourceLocation recipeId) {
    advancementBuilder
        .rewards(new AdvancementRewards.Builder().addRecipe(recipeId))
        .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId));
    return this;
  }


  private static final class Delegate extends JRecipe implements JsonSerializable {
    public final IFinishedRecipe delegate;

    private Delegate(IFinishedRecipe delegate) {
      super(null);
      this.delegate = delegate;
    }

    @Override
    public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
      return delegate.serializeAdvancement();
    }
  }
}
