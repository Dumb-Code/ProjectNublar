package net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe;

import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * <p>This is a recipe for <b>shaped crafting</b>.</p>
 *
 */
@SuppressWarnings("unused")
public class JShapedRecipe extends JResultRecipe {
  private static final String TYPE = "minecraft:crafting_shaped";
  protected JPattern pattern;
  protected JKeys key;

  public JShapedRecipe(JResult result) {
    super(TYPE, result);
  }

  public JShapedRecipe(String result) {
    super(TYPE, result);
  }

  public JShapedRecipe(ResourceLocation result) {
    super(TYPE, result);
  }

  public JShapedRecipe(Item result) {
    super(TYPE, result);
  }

  public JShapedRecipe(IItemProvider result) {
    super(TYPE, result);
  }

  public JShapedRecipe(JResult result, JPattern pattern, JKeys keys) {
    super(TYPE, result);
    this.pattern = pattern;
    this.key = keys;
  }


  public JShapedRecipe pattern(JPattern pattern) {
    this.pattern = pattern;
    return this;
  }


  public JShapedRecipe pattern(String... pattern) {
    this.pattern = new JPattern(pattern);
    return this;
  }


  public JShapedRecipe addKey(String key, JIngredient value) {
    if (this.key == null) {
      this.key = new JKeys();
    }
    this.key.key(key, value);
    return this;
  }


  public JShapedRecipe addKey(String key, String value) {
    return this.addKey(key, JIngredient.ofItem(value));
  }


  public JShapedRecipe addKey(String key, ResourceLocation value) {
    return this.addKey(key, JIngredient.ofItem(value));
  }


  public JShapedRecipe addKey(String key, Item value) {
    return this.addKey(key, JIngredient.ofItem(Preconditions.checkNotNull(ForgeRegistries.ITEMS.getKey(value), "Please register the object at first.")));
  }


  public JShapedRecipe addKey(String key, IItemProvider value) {
    return this.addKey(key, JIngredient.ofItem(value));
  }

  @Override
  public JShapedRecipe resultCount(int count) {
    return (JShapedRecipe) super.resultCount(count);
  }

  @Override
  public JShapedRecipe group(final String group) {
    return (JShapedRecipe) super.group(group);
  }


  @Override
  public JShapedRecipe clone() {
    return (JShapedRecipe) super.clone();
  }
}
