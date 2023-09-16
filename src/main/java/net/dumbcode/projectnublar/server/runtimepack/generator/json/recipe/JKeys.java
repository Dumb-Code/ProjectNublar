package net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.*;

public class JKeys extends ForwardingMap<String, JIngredient> implements Cloneable, JsonSerializable {
  /**
   * The map storing its keys, used for the forwarding map. It's by default a {@link LinkedHashMap}.
   */
  protected final Map<String, JIngredient> keys;
  /**
   * @deprecated You do not need it anymore.
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  protected final Map<String, List<JIngredient>> acceptableKeys;

  /**
   * Create a new, mutable {@code JKeys} object, with a new {@link LinkedHashMap} initialized.
   */
  public JKeys() {
    this.keys = new LinkedHashMap<>(9, 1);
    this.acceptableKeys = new HashMap<>();
  }

  /**
   * Create a new {@code JKeys} object with keys explicitly specified. You can create an immutable one by this. If you want to create an empty, mutable one, you should use {@link #JKeys()}.
   *
   * @param keys The map of keys and ingredients, which will be directly used as a field. You can make it immutable; in this case, methods such as {@link #key} should not be used, or {@link UnsupportedOperationException} may be thrown.
   */
  public JKeys(Map<String, JIngredient> keys) {
    this.keys = keys;
    this.acceptableKeys = Collections.emptyMap();
  }

  @Override
  protected Map<String, JIngredient> delegate() {
    return keys;
  }

  /**
   * @deprecated Please directly call {@link #JKeys()}.
   */
  @Deprecated
  public static JKeys keys() {
    return new JKeys();
  }

  /**
   * Add a key with the ingredient specified.
   *
   * @param key   The recipe key.
   * @param value The ingredient.
   */

  public JKeys key(final String key, final JIngredient value) {
    this.keys.put(key, value);
    return this;
  }

  /**
   * Add a key with a simple item ingredient specified.
   *
   * @param key   The recipe key.
   * @param value The identifier (as string) of the ingredient item.
   */

  public JKeys key(final String key, final String value) {
    return key(key, JIngredient.ofItem(value));
  }

  /**
   * Add a key with a simple item ingredient specified.
   *
   * @param key   The recipe key.
   * @param value The identifier of the ingredient item.
   */

  public JKeys key(final String key, final ResourceLocation value) {
    return key(key, JIngredient.ofItem(value));
  }

  /**
   * Add a key with a simple item ingredient specified.
   *
   * @param key   The recipe key.
   * @param value The ingredient item. Must be registered when calling this.
   */

  public JKeys key(final String key, final IItemProvider value) {
    return key(key, JIngredient.ofItem(value));
  }

  /**
   * Add a key with a simple item ingredient specified.
   *
   * @param key   The recipe key.
   * @param value The ingredient item. Must be registered when calling this.
   */

  public JKeys key(final String key, final Item value) {
    return key(key, JIngredient.ofItem(Preconditions.checkNotNull(ForgeRegistries.ITEMS.getKey(value))));
  }

  @Override
  public JKeys clone() {
    try {
      return (JKeys) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject object = new JsonObject();

    keys.forEach((final String key, final JIngredient ingredient) -> object.add(key,
        context.serialize(ingredient)));
    acceptableKeys.forEach((final String key, final List<JIngredient> acceptableIngredients) -> object.add(
        key,
        context.serialize(acceptableIngredients)));

    return object;
  }

  /**
   * @deprecated This class is kept for just compatibility. For example, some mixins may apply to it.
   */
  @Deprecated
  public static class Serializer implements JsonSerializer<JKeys> {
    @Override
    public JsonElement serialize(final JKeys src, final Type typeOfSrc, final JsonSerializationContext context) {
      return src.serialize(typeOfSrc, context);
    }
  }
}
