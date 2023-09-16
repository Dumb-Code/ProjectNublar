package net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate;

import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

/**
 * The simple block model that usually represents as the value in {@link JVariants}. It defines the model id and some simple rotations of that model.
 * <p>
 * <B>Note: </B>This class is used as a model definition in the block states definition file. To represent a model's content, please use {@link net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel}.
 */
@SuppressWarnings("unused")
@PreferredEnvironment(Dist.CLIENT)
public class JBlockModel implements Cloneable {
  /**
   * The model identifier. Usually compulsory. For example, {@code "minecraft:block/oak_slab_top"}.
   */
  public ResourceLocation model;
  /**
   * The x rotation used. Must be times of 90.
   */
  public Integer x;
  /**
   * The y rotation used. Must be times of 90.
   */
  public Integer y;
  /**
   * Determines whether lock the uv of texture used.
   */
  public Boolean uvlock;
  /**
   * This field is used in the element of block definition arrays, which affect the probability that it will be selected in the random list.
   */
  public Integer weight;

  /**
   * This constructor method usually directly creates a full instance with parameters, so fields are usually nullable.
   */
  public JBlockModel(ResourceLocation model, Integer x, Integer y, Boolean uvlock, Integer weight) {
    this.model = model;
    this.x = x;
    this.y = y;
    this.uvlock = uvlock;
    this.weight = weight;
  }

  /**
   * Create a new, empty model definition. This is usually not recommended, as you can directly assign a model id.
   */
  public JBlockModel() {
  }

  /**
   * @param modelId The identifier of the block model. For example, {@code "minecraft:block/oak_slab_top"}.
   * @deprecated Please use {@link #JBlockModel(ResourceLocation)} or {@link #JBlockModel(String, String)}.
   */
  @Deprecated
  public JBlockModel(String modelId) {
    this(new ResourceLocation(modelId));
  }

  /**
   * @param modelId The identifier of the block model. For example, <code>{@code new ResourceLocation("minecraft", "block/oak_slab_top")}</code>.
   * @see #JBlockModel(String, String)
   */
  public JBlockModel(ResourceLocation modelId) {
    this.model = modelId;
  }

  /**
   * @param idNamespace The namespace of the block model id. For example, {@code "minecraft"}.
   * @param idPath      The path of the block model id. For example, {@code "block/oak_slab_top"}.
   */
  public JBlockModel(String idNamespace, String idPath) {
    this(new ResourceLocation(idNamespace, idPath));
  }

  /**
   * Simple create an array block model definition with random rotations. For example,
   * <pre>
   *   {@code simpleRandom(new SimpleBlockModelDefinition(new ResourceLocation("minecraft","block/dirt"))}
   * </pre>
   * which returns an array block model definition like this:
   * <pre>{@code
   *   [{ "model": "minecraft:block/dirt"  },
   *    { "model": "minecraft:block/dirt", "y": 90  },
   *    { "model": "minecraft:block/dirt", "y": 180  },
   *    { "model": "minecraft:block/dirt", "y": 270  }  ]
   * }</pre>
   */
  public static JBlockModel[] simpleRandom(JBlockModel basicModel) {
    final JBlockModel[] multiple = new JBlockModel[4];
    for (int i = 0; i < 4; i++) {
      final JBlockModel cloned = basicModel.clone();
      cloned.y = 90 * i;
      multiple[i] = cloned;
    }
    return multiple;
  }

  @Override
  public JBlockModel clone() {
    try {
      return (JBlockModel) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  /**
   * Set the model id.
   */

  public JBlockModel modelId(ResourceLocation model) {
    this.model = model;
    return this;
  }


  public JBlockModel x(int x) {
    this.x = x;
    return this;
  }


  public JBlockModel y(int y) {
    this.y = y;
    return this;
  }


  public JBlockModel uvlock() {
    this.uvlock = true;
    return this;
  }


  public JBlockModel uvlock(boolean uvlock) {
    this.uvlock = uvlock;
    return this;
  }


  public JBlockModel weight(int weight) {
    this.weight = weight;
    return this;
  }
}
