package net.dumbcode.projectnublar.server.runtimepack.generator.json.models;

import com.google.gson.annotations.SerializedName;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;

/**
 * Defines a rotation in the {@link JElement}. For example,
 * <pre>{@code
 * { "origin": [8, 8, 8],
 *   "axis": "x",
 *   "angle": 45,
 *   "rescale": false }
 *   }
 * </pre>
 * can be produced with
 * <pre>{@code
 * new JRotation(Direction.Axis.X, 8, 8, 8, 45).rescale(true);
 * }</pre>
 */
@SuppressWarnings("unused")
@PreferredEnvironment(Dist.CLIENT)
public class JRotation implements Cloneable {
  public final float[] origin = new float[3];
  /**
   * @deprecated Please use {@link #rotationAxis}.
   */
  @Deprecated
  private transient final char axis = ' ';
  @SerializedName("axis")
  public final String rotationAxis;
  /**
   * The rotation angle. It is usually in the increment of 22.5.
   */
  public Float angle;
  public Boolean rescale;

  public JRotation(Direction.Axis axis) {
    this.rotationAxis = axis.toString();
  }

  public JRotation(Direction.Axis axis, float x, float y, float z, float angle) {
    this(axis);
    origin(x, y, z);
    angle(angle);
  }


  public JRotation origin(float x, float y, float z) {
    this.origin[0] = x;
    this.origin[1] = y;
    this.origin[2] = z;
    return this;
  }


  public JRotation angle(Float angle) {
    this.angle = angle;
    return this;
  }


  public JRotation rescale(boolean rescale) {
    this.rescale = rescale;
    return this;
  }


  public JRotation rescale() {
    this.rescale = true;
    return this;
  }

  @Override
  public JRotation clone() {
    try {
      return (JRotation) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
