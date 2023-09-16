package net.dumbcode.projectnublar.server.runtimepack.generator.json.models;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;

/**
 * A face on the specified direction of the {@link JElement}.
 */
@PreferredEnvironment(Dist.CLIENT)
public class JFace implements Cloneable {
  /**
   * This field is deprecated because, it will always exist. The value is by default [0, 0, 0] which definitely cause bugs. Actually, in Minecraft, the uv can be missing, which allows Minecraft to automatically determine. Therefore, {@link #uvs} is used instead, which is a nullable {@link FloatArrayList}.
   */
  @Deprecated // forRemoval = true
  private transient final float[] uv = new float[4];
  @SerializedName("uv")
  public FloatArrayList uvs;
  public final String texture;
  public String cullface;
  /**
   * The allowed values are: 0, 90, 180, 270.
   */
  public Integer rotation;
  public Integer tintindex;

  /**
   * @param textureVarName The variable name of the texture. <b>Not prefixed with {@code "#"}</b>, as it will be auto prefixed.
   */
  public JFace(String textureVarName) {
    this.texture = '#' + textureVarName;
  }

  /**
   * In this case, the {@link #uvs} will be removed, so Minecraft automatically determines the uv according to the "from" and "to" in the {@link JElement}.<br>
   * Usually you needn't call this method, as it is undefined by default.
   */

  public JFace autoUv() {
    this.uvs = null;
    return this;
  }


  public JFace uv(float x1, float y1, float x2, float y2) {
    this.uvs = FloatArrayList.wrap(new float[]{x1, y1, x2, y2});
    return this;
  }

  /**
   * Set the cullface in the specified direction, or {@code null} if there is no cullface.
   */

  public JFace cullface(Direction direction) {
    this.cullface = direction == null ? null : direction.toString();
    return this;
  }

  /**
   * Usually the cullface is a direction, so you should use {@link #cullface(Direction)}.
   */
  @Deprecated

  public JFace cullface(String cullface) {
    this.cullface = cullface;
    return this;
  }

  /**
   * Set the rotation of the texture. It is usually 0, 90, 180 or 270.<br>
   * You can also call {@link #rot90()}, {@link #rot180()} or {@link #rot270()}.
   */


  public JFace rotation(int rotation) {
    this.rotation = rotation;
    return this;
  }


  public JFace rot90() {
    this.rotation = 90;
    return this;
  }


  public JFace rot180() {
    this.rotation = 180;
    return this;
  }


  public JFace rot270() {
    this.rotation = 270;
    return this;
  }


  public JFace tintIndex(int index) {
    this.tintindex = index;
    return this;
  }

  @Override
  public JFace clone() {
    try {
      return (JFace) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
