package net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate;

import com.google.common.collect.ForwardingMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.data.BlockModelDefinition;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>A <b>variant definition</b> defines which models and adjust will be used in each block state property.</p>
 * <p>It's a simple hash map for the key-value pairs. The key is a string specifying the variant conditions, which can be null, a single or multiple property key-value pairs(s). The value is an array of block model definitions, which can be sometimes a singleton.</p>
 * <p>A block states definition is either composed of "variant definition"s, or composed of "multiparts". A block states definition consisting of variant definitions can have codes like the following format:</p>
 * <pre>{@code
 * BlockStatesDefinition.variants(VariantDefinition.ofNoVariants(
 *   new JBlockModel("minecraft", "stone")
 * ))
 * }</pre>
 * <p>will generated a block states definition file like this:</p>
 * <pre>{@code
 * {"variants": {
 *   "": {"model": "minecraft:stone"}
 * }}
 * }</pre>
 *
 * @author SolidBlock
 * @see net.minecraft.data.BlockModelDefinition
 */
@SuppressWarnings("unused")
@PreferredEnvironment(Dist.CLIENT)
public class JVariants extends ForwardingMap<String, JBlockModel[]> implements JsonSerializable {
  /**
   * The real map storing its contents, used for the forwarding map. It is usually a {@link LinkedHashMap} by default, but you can specify other types of maps.
   */
  private final Map<String, JBlockModel[]> variants;

  /**
   * Create a new {@code jVariant} object, which contains a linked hash map. This is the most common use. In this case, you can add properties to it.
   */
  public JVariants() {
    this(new LinkedHashMap<>());
  }

  /**
   * Create a new {@code jVariant} object, with the map explicitly specified.
   *
   * @param variants The map of Property. It can be immutable, which means you should not use methods like {@link #addVariant}. If you're deciding to create a new one, you may just use {@link #JVariants()}, unless you're sure to make it immutable.
   */
  public JVariants(Map<String, JBlockModel[]> variants) {
    this.variants = variants;
  }

  /**
   * This is a convenient way to create a block model if that model contains no variants. The "variant" in this case is an empty string, and the model definition will be used regardless of block states.
   *
   * @param modelDefinition The block model definition.
   * @see #of(String, JBlockModel...)
   * @see JBlockStates#simple(ResourceLocation)
   */

  public static JVariants ofNoVariants(JBlockModel... modelDefinition) {
    return of("", modelDefinition);
  }

  /**
   * Convert a block model definition to an array containing four rotations. For example,
   * <pre>{@code
   * {"model": "block/dirt"}
   * }</pre>
   * will be converted to
   * <pre>{@code
   * [ {"model": "block/dirt", "y": 0  },
   *   {"model": "block/dirt", "y": 90 },
   *   {"model": "block/dirt", "y": 180},
   *   {"model": "block/dirt", "y": 270} ]
   * }</pre>
   * In this case, Minecraft will randomly choose one when rendering.
   *
   * @param model The block model definition that will be cloned.
   * @return The array of block model definitions in four rotations.
   * @see JBlockStates#simpleRandomRotation
   */

  public static JBlockModel[] ofRandomRotation(JBlockModel model) {
    final JBlockModel[] result = new JBlockModel[4];
    for (int i = 0; i < result.length; i++) {
      result[i] = model.clone().y(90 * i);
    }
    return result;
  }

  /**
   * Create a block model variant definition of a simple horizontal facing block. For example, the block model definition that represents the following json
   * <pre>{@code
   * {"model": "brrp:block/stone_vertical_slab", "uvlock": true}
   * }</pre>
   * will be converted to a variant definition like this:
   * <pre>{@code
   * {"facing=south":
   *      {"model": "brrp:block/stone_vertical_slab", "uvlock": true, "y": 0  },
   *  "facing=west":
   *      {"model": "brrp:block/stone_vertical_slab", "uvlock": true, "y": 90 },
   *  "facing=north":
   *      {"model": "brrp:block/stone_vertical_slab", "uvlock": true, "y": 180},
   *  "facing=east":
   *      {"model": "brrp:block/stone_vertical_slab", "uvlock": true, "y": 270} }
   * }</pre>
   *
   * @see JBlockStates#simpleHorizontalFacing
   */

  public static JVariants ofHorizontalFacing(JBlockModel model) {
    final JVariants JVariants = new JVariants();
    for (Direction direction : Direction.Plane.HORIZONTAL) {
      JVariants.addVariant(BlockStateProperties.HORIZONTAL_FACING, direction, model.clone().y(((int) direction.toYRot())));
    }
    return JVariants;
  }

  /**
   * Create a block model variant definition for a slab block. For example, the block model definition that represents the following json
   * <pre>{@code
   * {"model": "block/dirt"}
   * }</pre>
   * will result in the following variant definition (where the {@code bottomSlabIdentifier} is {@code brrp:block/dirt_slab} and the {@code topSlabIdentifier} is {@code brrp:block/dirt_slab_top}):
   * <pre>{@code
   * {"type=bottom": {"model": "brrp:block/dirt_slab"    },
   *  "type=top":    {"model": "brrp:block/dirt_slab_top"},
   *  "type=double": {"model": "block/dirt"              }}
   * }</pre>
   *
   * @param model                The basic block model definition for a double-slab block. It is usually equal to the "whole block" that the slab corresponds to. The parameter will be used for {@code "type=double"} without cloning, and will be used for {@code "type=bottom"} and {@code "type=top"} after cloning and switching the model id.
   * @param bottomSlabIdentifier The identifier of the model that will be used for the bottom slab.
   * @param topSlabIdentifier    The identifier of the model that will be used for the top slab.
   * @return The model variant definition for the slab block.
   * @apiNote This method does not support additional block state Property. If you'd like to deal with slabs with properties, and you can confirm that the slab has all properties that the base block has, you can use {@link #composeToSlab(Function, Function)}.
   */

  public static JVariants ofSlab(JBlockModel model, ResourceLocation bottomSlabIdentifier, ResourceLocation topSlabIdentifier) {
    return new JVariants()
        .addVariant(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM, model.clone().modelId(bottomSlabIdentifier))
        .addVariant(BlockStateProperties.SLAB_TYPE, SlabType.TOP, model.clone().modelId(topSlabIdentifier))
        .addVariant(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE, model);
  }

  /**
   * This is a convenient way to create a block model definition with one variant defined.<br>
   * If the {@code variant} is empty, you can call {@link #ofNoVariants(JBlockModel...)}.
   *
   * @param variant         The string describing the whole variant. It can be empty string {@code ""}, or one or more key-value pairs, for example {@code "snowy=false"}, or {@code "facing=south,half=top"}.
   * @param modelDefinition The block model definition.
   * @see #ofNoVariants(JBlockModel...)
   * @see #addVariant(String, JBlockModel...)
   */

  public static JVariants of(String variant, JBlockModel... modelDefinition) {
    return new JVariants().addVariant(variant, modelDefinition);
  }

  /**
   * This is a convenient way to create a block model definition with one variant defined.<br>
   * If the {@code variant} is empty, you can call {@link #ofNoVariants(JBlockModel...)}.
   *
   * @param property        The name of the property, represented as string.
   * @param value           The value of the property, represented as string.
   * @param modelDefinition The block model definition.
   * @see #addVariant(String, String, JBlockModel...)
   */

  public static JVariants of(String property, String value, JBlockModel... modelDefinition) {
    return new JVariants().addVariant(property, value, modelDefinition);
  }

  /**
   * This is a convenient way to create a block model definition with one variant defined.<br>
   * If the {@code variant} is empty, you can call {@link #ofNoVariants(JBlockModel...)}.
   *
   * @param property        The name of the property, represented as string.
   * @param value           The value of the property, usually a {@link IStringSerializable} object.
   * @param modelDefinition The block model definition.
   * @see #addVariant(String, String, JBlockModel...)
   */

  public static JVariants of(String property, IStringSerializable value, JBlockModel... modelDefinition) {
    return new JVariants().addVariant(property, value, modelDefinition);
  }

  /**
   * This is a convenient way to create a block model definition with one variant defined.
   *
   * @param property        A block state property. Vanilla properties can be found in {@link BlockStateProperties}.
   * @param value           The value that corresponds to the property.
   * @param modelDefinition The block model definition.
   * @see #addVariant(Property, Comparable, JBlockModel...)
   */

  public static <T extends Comparable<T>> JVariants of(Property<T> property, T value, JBlockModel... modelDefinition) {
    return new JVariants().addVariant(property, value, modelDefinition);
  }

  /**
   * Create a delegated object that has the same serialization of the delegation object. It can be seen as a bridge between {@link JVariants} and {@link BlockModelDefinition}.
   *
   * @param delegate The delegated object, whose serialization will be directly used.
   * @return The delegated object.
   */

  public static JVariants delegate(BlockModelDefinition delegate) {
    return new Delegate(delegate);
  }

  @Override
  protected Map<String, JBlockModel[]> delegate() {
    return variants;
  }

  /**
   * Convert a block model variant definition to those for a slab block. For example, the block model variant definition for a redstone ore is:
   * <pre>{@code
   * { "lit=false": {"model": "minecraft:block/redstone_lamp"   },
   *   "lit=true":  {"model": "minecraft:block/redstone_lamp_on"}}
   * }</pre>
   * can be converted to
   * <pre>{@code
   * { "lit=false,type=double": {"model": "minecraft:block/redstone_lamp"       },
   *   "lit=true,type=double":  {"model": "minecraft:block/redstone_lamp_on"    },
   *   "lit=false,type=bottom": {"model": "brrp:block/redstone_lamp_slab"       },
   *   "lit=true,type=bottom":  {"model": "brrp:block/redstone_lamp_on_slab"    },
   *   "lit=false,type=top":    {"model": "brrp:block/redstone_lamp_slab_top"   },
   *   "lit=true,type=top":     {"model": "brrp:block/redstone_lamp_on_slab_top"} }
   * }</pre>
   * in this case, the {@code bottomSlabIdFunction} and {@code topSlabIdFunction} can be:
   * <pre>{@code
   * id -> new ResourceLocation(id.getNamespace(), id.getPath() + "slab");
   * id -> new ResourceLocation(id.getNamespace(), id.getPath() + "slab_top");
   * }</pre>
   * <p>
   * The reason way this method takes function instead of direct IDs is that it may meet multiple different identifiers for the base block model.
   *
   * @param bottomSlabIdFunction The function that takes the base block model identifier and yields the bottom slab model identifier.
   * @param topSlabIdFunction    The function that takes the base block model identifier and yields the top slab model identifier.
   * @return The variant definition for the slab block.
   * @see #ofSlab
   */
  public JVariants composeToSlab(Function<ResourceLocation, ResourceLocation> bottomSlabIdFunction, Function<ResourceLocation, ResourceLocation> topSlabIdFunction) {
    final JVariants result = new JVariants();
    this.forEach((property, models) -> {
      if (!property.isEmpty()) property += ",";
      result.put(property + "type=double", models);
      result.put(property + "type=bottom", Arrays.stream(models).map(model -> model.clone().modelId(bottomSlabIdFunction.apply(model.model))).toArray(JBlockModel[]::new));
      result.put(property + "type=top", Arrays.stream(models).map(model -> model.clone().modelId(topSlabIdFunction.apply(model.model))).toArray(JBlockModel[]::new));
    });
    return result;
  }

  /**
   * Add a simple variant situation.
   *
   * @param variant         The string describing the whole variant. It can be empty string {@code ""}, or one or more key-value pairs, for example {@code "snowy=false"}, or {@code "facing=south,half=top"}.
   * @param modelDefinition The block model definition.
   * @see #of(String, JBlockModel...)
   */


  public JVariants addVariant(String variant, JBlockModel... modelDefinition) {
    put(variant, modelDefinition);
    return this;
  }

  @Override
  public JBlockModel[] put(String key, JBlockModel ... value) {
    return super.put(key, value);
  }

  /**
   * Add a simple variant situation with a property and value. In this method, a single key-value pair is used.
   *
   * @param property        A block state property. Vanilla properties can be found in {@link Property}.
   * @param value           The value that corresponds to the property.
   * @param modelDefinition The block model definition.
   * @see #of(Property, Comparable, JBlockModel...)
   */


  public <T extends Comparable<T>> JVariants addVariant(Property<T> property, T value, JBlockModel... modelDefinition) {
    return addVariant(property.getName() + "=" + property.getName(value), modelDefinition);
  }

  /**
   * Add a simple variant situation with a string property and value. In this method, a single key-value pair is used.
   *
   * @param property        The name of the property, represented as string.
   * @param value           The value of the property, represented as string.
   * @param modelDefinition The block model definition.
   * @see #of(String, String, JBlockModel...)
   */


  public JVariants addVariant(String property, String value, JBlockModel... modelDefinition) {
    return addVariant(property + "=" + value, modelDefinition);
  }

  /**
   * Add a simple variant situation with a string property and value. In this method, a single key-value pair is used.
   *
   * @param property        The name of the property, represented as string.
   * @param value           The value of the property, usually a {@link IStringSerializable} object.
   * @param modelDefinition The block model definition.
   * @see #of(String, String, JBlockModel...)
   */


  public JVariants addVariant(String property, IStringSerializable value, JBlockModel... modelDefinition) {
    return addVariant(property, value.getSerializedName(), modelDefinition);
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject object = new JsonObject();
    this.forEach((key, value) -> object.add(key, context.serialize(value.length == 1 ? value[0] : value)));
    return object;
  }


  private static final class Delegate extends JVariants implements JsonSerializable, Supplier<JsonElement> {
    private final BlockModelDefinition delegate;

    private Delegate(BlockModelDefinition delegate) {
      this.delegate = delegate;
    }

    @Override
    public JsonElement get() {
      return delegate.get();
    }

    @Override
    public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
      return delegate.get();
    }
  }
}
