package net.dumbcode.projectnublar.server.runtimepack.generator.json.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;

import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.function.Function;

/**
 * <p>Specifies the faces in the {@link JElement}.</p>
 * <p>It's essentially a map, in which the key is {@link Direction direction} and value is the {@link JFace face}.</p>
 */
@PreferredEnvironment(Dist.CLIENT)
public class JFaces extends EnumMap<Direction, JFace> implements Cloneable, JsonSerializable {
  // These fields exist for only compatibility.
  @Deprecated
  private JFace up;
  @Deprecated
  private JFace down;
  @Deprecated
  private JFace north;
  @Deprecated
  private JFace south;
  @Deprecated
  private JFace east;
  @Deprecated
  private JFace west;

  public JFaces() {
    super(Direction.class);
  }

  /**
   * This method quite resembles {@link java.util.Map#put(Object, Object)}, but will return the instance itself, making it possible to chain call.
   */

  public JFaces set(Direction direction, JFace face) {
    put(direction, face);
    return this;
  }

  /**
   * Set all faces the same JFace object.<p>
   * Note: Sometimes you need to set the cullface for specific direction. In that case, you may use {@link #setAllFaces(JFace, boolean)} or {@link #setAllFaces(Function)}.
   */

  public JFaces setAllFaces(JFace face) {
    for (Direction direction : Direction.values()) {
      put(direction, face);
    }
    return this;
  }

  /**
   * Set all faces the same JFace object, and add or remove a cullface for each side. In this case, the param {@code face} will be cloned.<p>
   * This will add or remove the cullface value for the cloned face. If you call {@link #setAllFaces(JFace)} without param {@code cullface}, the param {@code face} will be directly used and not modified.
   *
   * @see #setAllFaces(JFace)
   * @see #setAllFaces(Function)
   */

  public JFaces setAllFaces(JFace face, boolean cullface) {
    for (Direction direction : Direction.values()) {
      put(direction, face.clone().cullface(cullface ? direction : null));
    }
    return this;
  }

  /**
   * Set all faces of the value given by the function.
   *
   * @param faces The function to generate a face for each side.
   * @see #setAllFaces(JFace)
   * @see #setAllFaces(JFace, boolean)
   */

  public JFaces setAllFaces(Function<Direction, JFace> faces) {
    for (Direction direction : Direction.values()) {
      put(direction, faces.apply(direction));
    }
    return this;
  }


  public JFaces up(JFace face) {
    put(Direction.UP, face);
    return this;
  }


  public JFaces down(JFace face) {
    put(Direction.DOWN, face);
    return this;
  }


  public JFaces north(JFace face) {
    put(Direction.NORTH, face);
    return this;
  }


  public JFaces south(JFace face) {
    put(Direction.SOUTH, face);
    return this;
  }


  public JFaces east(JFace face) {
    put(Direction.EAST, face);
    return this;
  }


  public JFaces west(JFace face) {
    put(Direction.WEST, face);
    return this;
  }

  @Override
  public JFaces clone() {
    return (JFaces) super.clone();
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject object = new JsonObject();
    forEach((direction, jFace) -> object.add(direction.toString(), context.serialize(jFace)));
    return object;
  }
}
