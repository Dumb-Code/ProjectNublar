package net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl;
import net.minecraft.data.IMultiPartPredicateBuilder;
import net.minecraft.state.StateContainer;
import net.minecraftforge.api.distmarker.Dist;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * <p>A <b>JWhenLogical</b> is a logical connection of multiple "when"s. It's composed of two parts:</p>
 * <ul><li>
 *   <b>{@linkplain  #operator}</b> - The logical representation of these several conditions. It is usually "OR", while "AND" is <i>defined but not used</i> in vanilla Minecraft, and there is no guarantee that "AND" is valid.
 * </li><li>
 *   <b>{@linkplain  #components}</b> - The multiple conditions, which is a simple list of "{@link IMultiPartPredicateBuilder}" objects.
 * </li></ul>
 * <p>This is a simple <i>forwarding list</i> for {@link #components}, so you can call list methods, such as {@link #add}, {@link #addAll}, to directly modify the {@code components}.</p>
 * <p>It quite resembles {@link IMultiPartPredicateBuilder.Operator}, as it's just written based on it, with some enhancements.</p>
 *
 * @see IMultiPartPredicateBuilder.Operator
 */
@PreferredEnvironment(Dist.CLIENT)
public class JWhenLogical extends ForwardingList<IMultiPartPredicateBuilder> implements IMultiPartPredicateBuilder, JsonSerializable {
  public final IMultiPartPredicateBuilder.Operator operator;
  public final List<IMultiPartPredicateBuilder> components;

  /**
   * Create a simple JWhenLogical object. However, directly calling the constructor is <i>not</i> recommended, unless you're sure to do that, for example, making multiple objects share one list of components, or making the list immutable. In most cases, you should directly call {@link #anyOf(IMultiPartPredicateBuilder...)} or {@link #allOf(IMultiPartPredicateBuilder...)}.
   *
   * @param operator   The logical operator.
   * @param components The list of components. It will be directly used as the field. It can be immutable, making methods like {@link #addCondition(IMultiPartPredicateBuilder)} fail.
   */

  public JWhenLogical(IMultiPartPredicateBuilder.Operator operator, List<IMultiPartPredicateBuilder> components) {
    this.operator = operator;
    this.components = components;
  }

  /**
   * Create a simple JWhenLogical object, which, when used for rendering, passes as long as one of the conditions is met. This is an example:
   * <pre>{@code
   * anyOf(
   *  JWhenBlockStateProperties.of("direction", "up", "down"),
   *  JWhenBlockStateProperties.of("waterlogged", "false"));
   * }</pre>
   * which can be serialized as followings:
   * <pre>{@code
   * {"OR": [
   *   {"direction": "up|down"},
   *   {"waterlogged": "false" } ]}
   * }</pre>
   */

  public static JWhenLogical anyOf(IMultiPartPredicateBuilder... conditions) {
    return new JWhenLogical(Operator.OR, Lists.newArrayList(conditions));
  }

  /**
   * Create a simple JWhenLogical object, which, when used for rendering, passes only if all conditions is met. Note that vanilla Minecraft does not use this type of logical condition, and there is no guarantee that it takes effect.
   */

  public static JWhenLogical allOf(IMultiPartPredicateBuilder... conditions) {
    return new JWhenLogical(Operator.AND, Lists.newArrayList(conditions));
  }

  /**
   * Add a condition to its components. Of course, you can also assemble the conditions well when constructing. It is quite similar to {@link List#add(Object)}, but returns the object itself.
   */

  public JWhenLogical addCondition(IMultiPartPredicateBuilder condition) {
    components.add(condition);
    return this;
  }

  /**
   * Add conditions to its components. Of course, you can also assemble the conditions well when constructing. It is similar to {@link List#add(Object)}, but returns the object itself.
   */

  public JWhenLogical addCondition(IMultiPartPredicateBuilder... condition) {
    components.addAll(Arrays.asList(condition));
    return this;
  }

  @Override
  protected List<IMultiPartPredicateBuilder> delegate() {
    return components;
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    JsonArray jsonArray = new JsonArray();
    this.components.stream().map(Supplier::get).forEach(jsonArray::add);
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(this.operator.name(), jsonArray);
    return jsonObject;
  }

  @Override
  public void validate(StateContainer<?, ?> stateManager) {
    this.components.forEach(component -> component.validate(stateManager));
  }

  @Override
  public JsonElement get() {
    return RuntimeResourcePackImpl.GSON.toJsonTree(this);
  }
}
