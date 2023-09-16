package net.dumbcode.projectnublar.server.runtimepack.generator.json.loot;

import com.google.gson.*;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.loot.ILootFunctionConsumer;
import net.minecraft.loot.LootSerializers;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>The loot table function is in essence an "item modifier". The field "function" is the identifier (as string) of the loot table function.</p>
 * <p>The loot table function is quite complicated, so this class directly uses the field {@link #properties} to add any parameter.</p>
 */
public class JFunction implements Cloneable, JsonSerializable {
  public List<JCondition> conditions;
  /**
   * The name of the loot table function. Possible values: {@code "apply_bonus"}, {@code "copy_name"}, {@code "copy_nbt"}, so on.
   */
  public String function;
  public JsonObject properties = new JsonObject();

  public JFunction(String function) {
    function(function);
  }

  public static JFunction delegate(ILootFunctionConsumer delegate) {
    final Gson GSON = LootSerializers.createFunctionSerializer().create();
    return new JFunction(null) {
      @Override
      public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
        return GSON.toJsonTree(delegate);
      }
    };
  }

  /**
   * Set the name of the loot table function.
   *
   * @param function The function name, which is an identifier (as string).
   */

  public JFunction function(String function) {
    this.function = function;
    return this;
  }

  /**
   * Set all properties of the function, overriding existing ones, except {@link #function} and {@link #conditions}.
   */

  public JFunction set(JsonObject properties) {
    this.properties = properties;
    return this;
  }


  public JFunction parameter(String key, JsonElement value) {
    this.properties.add(key, value);
    return this;
  }


  public JFunction parameter(String key, String value) {
    return parameter(key, new JsonPrimitive(value));
  }


  public JFunction parameter(String key, Number value) {
    return parameter(key, new JsonPrimitive(value));
  }


  public JFunction parameter(String key, Boolean value) {
    return parameter(key, new JsonPrimitive(value));
  }


  public JFunction parameter(String key, ResourceLocation value) {
    return parameter(key, value.toString());
  }


  public JFunction parameter(String key, Character value) {
    return parameter(key, new JsonPrimitive(value));
  }

  /**
   * Add a condition to the function.
   *
   * @param condition The loot table condition.
   */

  public JFunction condition(JCondition condition) {
    if (conditions == null) this.conditions = new ArrayList<>();
    this.conditions.add(condition);
    return this;
  }

  /**
   * Add a condition to the function.
   *
   * @see JFunction#condition(JCondition)
   * @deprecated unintuitive name
   */
  @Deprecated
  public JFunction add(JCondition condition) {
    return condition(condition);
  }

  @Override
  public JFunction clone() {
    try {
      return (JFunction) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    if (conditions != null) {
      properties.add("conditions", context.serialize(conditions));
    }
    if (function != null) {
      properties.addProperty("function", function);
    }
    return properties;
  }

  /**
   * This class is kept for compatibility.
   *
   * @deprecated
   */
  @Deprecated
  public static class Serializer implements JsonSerializer<JFunction> {
    @Override
    public JsonElement serialize(JFunction src, Type typeOfSrc, JsonSerializationContext context) {
      return src.serialize(typeOfSrc, context);
    }
  }
}
