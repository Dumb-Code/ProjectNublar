package net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate;

import com.google.common.collect.ForwardingMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl;
import net.minecraft.data.IMultiPartPredicateBuilder;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.api.distmarker.Dist;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>A <b>JWhenProperties</b> represents a <b>when</b> object with one or multiple properties to match the block state. In the "multipart" object, if the block state matches the properties, the part will be used.</p>
 * <p>It's essentially a map, with keys being the name of properties, and values representing the matching rule of value of BlockStateProperties. Values can be directly a value, or multiple values joined with {@code "|"}, or one or more values prefixed by {@code "|"} representing negation.</p>
 * <p>This is a simple JSON example for a JWhenProperties object (note that comments are actually not allowed in JSONs):</p>
 * <pre>{@code
 * { "direction": "east",         // single value
 *        "type": "bottom|top",   // multiple alternative values
 *         "age": "!0",           // negated value
 *        "axis": "!x|y" }        // negated multiple values
 * }</pre>
 * <p>The object above can be generated with any of the following codes:</p>
 * <pre>{@code
 * // using pure strings
 * new JWhenProperties()
 *  .add("direction", "east")
 *  .add("type", "bottom", "top")     // or add("type", "bottom|top")
 *  .addNegated("age", "0")           // or add("age", "!0")
 *  .addNegated("axis", "x", "y");    // or add("axis", "!x|y")
 *
 * // using objects
 * new JWhenProperties()
 *  .add(BlockStateProperties.DIRECTION, Direction.EAST)
 *  .add(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, SlabType.TOP)
 *  .addNegated(BlockStateProperties.AGE, 0)
 *  .addNegated(BlockStateProperties.AXIS, Direction.Axis.X, Direction.Axis.Y);
 * }</pre>
 * <p>The negation is defined in {@link IMultiPartPredicateBuilder.Properties} for data generation but is actually not used. There is no guarantee that the usage of negation can take effect.</p>
 */
@PreferredEnvironment(Dist.CLIENT)
public class JWhenProperties extends ForwardingMap<String, String> implements IMultiPartPredicateBuilder, JsonSerializable {
  /**
   * The delegate map storing properties and values, both of which represent as strings.
   */
  public final Map<String, String> properties;

  /**
   * Create a new empty mutable JWhenProperties object. It takes the form of a linked hash map. If you want to initially specify one property, you may use {@link #of}.
   */
  public JWhenProperties() {
    this(new LinkedHashMap<>());
  }

  /**
   * Create a new empty JWhenProperties with a map for properties explicitly defined.
   *
   * @param properties The map storing BlockStateProperties. It will be directly used as the {@link #properties} field. It <i>can</i> be <i>immutable</i>, which means in this case methods like {@link #add} do not take effect.
   */
  public JWhenProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  /**
   * Create a new JWhenProperties with one property with one or multiple values initially defined.
   *
   * @param property The name of the property, representing as string.
   * @param values   The values of the property, representing as strings.
   */

  public static JWhenProperties of(String property, String... values) {
    return new JWhenProperties().add(property, values);
  }

  /**
   * Create a new JWhenProperties with one property with one or multiple values initially defined.
   *
   * @param property The name of the property, representing as string.
   * @param values   The values of the property, representing as {@code IStringSerializable}s.
   */

  public static JWhenProperties of(String property, IStringSerializable... values) {
    return new JWhenProperties().add(property, values);
  }

  /**
   * Create a new JWhenProperties with one property with one or multiple values initially defined.
   *
   * @param property The property. Vanilla properties can be found in {@link net.minecraft.state.Property}.
   * @param value    The values of the property.
   */
  @SafeVarargs

  public static <T extends Comparable<T>> JWhenProperties of(Property<T> property, T... value) {
    return new JWhenProperties().add(property, value);
  }

  /**
   * Serialize the object as JSON. In this case, the delegate field, {@link #properties}, will be used directly.
   *
   * @param typeOfSrc the actual type (fully genericized version) of the source object.
   */
  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(properties);
  }

  /**
   * Check the validation of this file. If it contains properties that the {@code stateManager} does not recognized, the validation fails.
   *
   * @throws IllegalStateException if it contains unrecognized BlockStateProperties.
   */
  @Override
  public void validate(StateContainer<?, ?> stateManager) {
    List<String> list = this.properties.keySet().stream().filter(property -> stateManager.getProperty(property) != null).collect(Collectors.toList());
    if (!list.isEmpty()) {
      throw new IllegalStateException("Properties " + list + " are missing from " + stateManager);
    }
  }

  @Override
  public JsonElement get() {
    return RuntimeResourcePackImpl.GSON.toJsonTree(this);
  }

  @Override
  protected Map<String, String> delegate() {
    return properties;
  }

  /**
   * Add a matching property to this object.
   *
   * @param property The property of block states. Vanilla properties can be found in {@link Properties}.
   * @param value    The value of this property.
   * @return The object itself.
   */

  public <T extends Comparable<T>> JWhenProperties add(Property<T> property, T value) {
    return add(property.getName(), property.getName(value));
  }

  /**
   * Add a matching property to this object, with multiple acceptable values given.
   *
   * @param property The property of block states. Vanilla properties can be found in {@link Properties}.
   * @param values   The values of this property, representing that each one of the values will be matched. In this case, they will be joined with {@code "|"}.
   * @return The object itself.
   */

  @SafeVarargs
  public final <T extends Comparable<T>> JWhenProperties add(Property<T> property, T... values) {
    return add(property.getName(), Arrays.stream(values).map(property::getName).collect(Collectors.joining("|")));
  }

  /**
   * Add a matching property to this object.
   *
   * @param property The property of block states, represented as string
   * @param value    The one or multiple values of this property, represented as string.
   * @return The object itself.
   */

  public JWhenProperties add(String property, String value) {
    properties.put(property, value);
    return this;
  }

  /**
   * Add a negated property to this object.
   *
   * @param property The property of block states, represented as string
   * @param value    The one or multiple values of this property, represented as string.
   * @return The object itself.
   */

  public JWhenProperties addNegated(String property, String value) {
    return add(property, "!" + value);
  }

  /**
   * Add a matching property to this object.
   *
   * @param property The property of block states, represented as string
   * @param value    The value of this property, represented as a {@link IStringSerializable} object.
   * @return The object itself.
   */

  public JWhenProperties add(String property, IStringSerializable value) {
    return add(property, value.getSerializedName());
  }

  /**
   * Add a negated property to this object.
   *
   * @param property The property of block states, represented as string
   * @param value    The value of this property, represented as a {@link IStringSerializable} object.
   * @return The object itself.
   */

  public JWhenProperties addNegated(String property, IStringSerializable value) {
    return add(property, "!" + value.getSerializedName());
  }

  /**
   * Add a matching property to this object, with multiple acceptable values given.
   *
   * @param property The property of block states, represented as string
   * @param values   The values of this property, representing that each one of the values will be matched. In this case, they will be joined with {@code "|"}.
   * @return The object itself.
   */

  public JWhenProperties add(String property, String... values) {
    return add(property, String.join("|", values));
  }

  /**
   * Add a negated property to this object, with multiple negated values given.
   *
   * @param property The property of block states, represented as string
   * @param values   The values of this property, representing that each one of the values will be negated. In this case, they will be joined with {@code "|"}.
   * @return The object itself.
   */

  public JWhenProperties addNegated(String property, String... values) {
    return add(property, "!" + String.join("|", values));
  }

  /**
   * Add a matching property to this object, with multiple acceptable values given.
   *
   * @param property The property of block states, represented as string
   * @param values   The values of this property, representing that each one of the values will be matched. In this case, they will be joined with {@code "|"}.
   * @return The object itself.
   */

  public JWhenProperties add(String property, IStringSerializable... values) {
    return add(property, Arrays.stream(values).map(IStringSerializable::getSerializedName).collect(Collectors.joining("|")));
  }

  /**
   * Add a negated property to this object, with multiple negated values given.
   *
   * @param property The property of block states, represented as string
   * @param values   The values of this property, representing that each one of the values will be negated. In this case, they will be joined with {@code "|"}.
   * @return The object itself.
   */

  public JWhenProperties addNegated(String property, IStringSerializable... values) {
    return add(property, "!" + Arrays.stream(values).map(IStringSerializable::getSerializedName).collect(Collectors.joining("|")));
  }
}
