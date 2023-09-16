package net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.JsonSerializable;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

public class JResult implements Cloneable, JsonSerializable {
  /**
   * The identifier (as string) of the resulting item.
   */
  public final String item;
  /**
   * The count of the result.
   */
  public Integer count;

  public JResult(final String id) {
    this.item = id;
  }

  public JResult(final ResourceLocation id) {
    this(id.toString());
  }

  /**
   * This method will query the id of the item. You should ensure that the item has been registered.
   */
  public JResult(final IItemProvider item) {
    this(Preconditions.checkNotNull(ForgeRegistries.ITEMS.getKey(item.asItem()), "Object not registered!"));
  }

  /**
   * @deprecated Please directly call {@link #JResult(IItemProvider)}.
   */
  @Deprecated
  public static JResult item(final Item item) {
    return result(Registry.ITEM.getKey(item).toString());
  }

  /**
   * @deprecated Please directly call {@link #JResult(String)} .
   */
  @Deprecated
  public static JResult result(final String id) {
    return new JResult(id);
  }

  /**
   * Set the count of this result.
   */

  public JResult count(int count) {
    this.count = count;
    return this;
  }

  @Override
  public JResult clone() {
    try {
      return (JResult) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }

  @Override
  public JsonElement serialize(Type typeOfSrc, JsonSerializationContext context) {
    final JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("item", item);
    if (count != null) jsonObject.addProperty("count", count);
    return jsonObject;
  }
}
