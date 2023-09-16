package net.dumbcode.projectnublar.server.runtimepack.generator.json.models;

import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.minecraftforge.api.distmarker.Dist;

/**
 * <p>Specifies the rotation, translation and scale of a model in a specified {@link JDisplay.DisplayPosition}.</p>
 * <p>For example, the position of torch in {@link net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JDisplay.DisplayPosition#THIRDPERSON_RIGHTHAND "thirdperson_righthand"} is:</p>
 * <pre>{@code
 * {
 *   "rotation": [ -90, 0, 0 ],
 *   "translation": [ 0, 1, -3 ],
 *   "scale": [ 0.55, 0.55, 0.55 ]
 * }
 * }</pre>
 * <p>which can be defined as:</p>
 * <pre>{@code
 * JPosition.ofRotation(-90, 0, 0).translation(0, 1, -3).scale(0.55, 0.55, 0.55);
 * }</pre>
 */
@PreferredEnvironment(Dist.CLIENT)
public class JPosition implements Cloneable {
  /**
   * The rotation of the model, in the format of [x, y, z]. Rotation degrees will be applied around the corresponding axis. For example, a rotation of [0, 45, 0] will rotate the model around Y axis.
   */
  public final float[] rotation = new float[3];
  /**
   * The translation of the model, in the format of [x, y, z]. Model will be offset at the corresponding axis. Each value should be between -80 and 80. However, this class dos not inspect it.
   */
  public final float[] translation = new float[3];
  /**
   * The scale of the model, in the format of [x, y, z]. Each value should be no larger than 4. However, this class does not inspect it.
   */
  public final float[] scale = new float[3];

  public JPosition() {
  }


  public static JPosition ofRotation(float x, float y, float z) {
    return new JPosition().rotation(x, y, z);
  }


  public static JPosition ofTranslation(float x, float y, float z) {
    return new JPosition().translation(x, y, z);
  }


  public static JPosition ofScale(float x, float y, float z) {
    return new JPosition().scale(x, y, z);
  }


  public JPosition rotation(float x, float y, float z) {
    this.rotation[0] = x;
    this.rotation[1] = y;
    this.rotation[2] = z;
    return this;
  }


  public JPosition translation(float x, float y, float z) {
    this.translation[0] = x;
    this.translation[1] = y;
    this.translation[2] = z;
    return this;
  }


  public JPosition scale(float x, float y, float z) {
    this.scale[0] = x;
    this.scale[1] = y;
    this.scale[2] = z;
    return this;
  }

  @Override
  public JPosition clone() {
    try {
      return (JPosition) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
