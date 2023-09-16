package net.dumbcode.projectnublar.server.runtimepack.generator.json.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import it.unimi.dsi.fastutil.objects.Object2FloatLinkedOpenHashMap;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.loot.JCondition;
import net.minecraftforge.api.distmarker.Dist;

import java.lang.reflect.Type;

/**
 * <p>A predicate for overriding. It is in essence a map, in which a key is a string representing a predicate name, and the value is the float value.</p>
 * <p>Currently, Minecraft does not support non-float values for model predicates. Even if {@code "custom_model_data"} takes an int, the value will be converted to float as well.</p>
 * <p>If you still need non-float values, you may just override {@link #serialize(Type, JsonSerializationContext)} method.</p>
 * <p>Usually the model predicate supports the following keys:</p><code>
 * <ul>
 *   <li>"angle"</li>
 *   <li>"blocking"</li>
 *   <li>"broken"</li>
 *   <li>"cast"</li>
 *   <li>"cooldown"</li>
 *   <li>"damage"</li>
 *   <li>"damaged"</li>
 *   <li>"lefthanded"</li>
 *   <li>"pull"</li>
 *   <li>"pulling"</li>
 *   <li>"throwing"</li>
 *   <li>"time"</li>
 *   <li>"custom_model_data"</li>
 * </ul></code>
 *
 * <p>The class is used for models. If you mean a condition in a loot table, please use {@link JCondition} instead.</p>
 *
 */
@PreferredEnvironment(Dist.CLIENT)
public class JPredicate extends Object2FloatLinkedOpenHashMap<String> implements JsonSerializable {
  /**
   * This method quite resembles {@link it.unimi.dsi.fastutil.objects.Object2FloatMap#put(Object, float)}, but returns the object itself, making it possible to chain-call.
   */

  public JPredicate addPredicate(String name, float value) {
    put(name, value);
    return this;
  }

  /**
   * This static method is a simplified version for a simple predicate. Most predicates have only one entry. So you can use this method, for example, <pre>{@code
   * JPredicate.of("time", 0.125);
   * }</pre>
   * in place of<pre>{@code
   * new JPredicate().addPredicate("time", 0.125);
   * }</pre>
   */

  public static JPredicate of(String name, float value) {
    return new JPredicate().addPredicate(name, value);
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject object = new JsonObject();
    forEach(object::addProperty);
    return object;
  }
}
