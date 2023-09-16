package net.dumbcode.projectnublar.server.runtimepack.generator.json.tags;

import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p><b>Tag</b>s are used to list block, entity types, functions, etc. The identifier of tag is <code style=color:maroon><i>namespace</i>:<i>type</i>/<i>pathContent</i></code>, where the <code><i>type</i></code> determines which object the tag contents refer to, and can be one of the following values:</p>
 * <pre style=color:navy>blocks entity_types fluids functions game_events items worldgen</pre>
 * <p>A tag itself has an identifier. It is determined when writing the tag into the resource pack. If you needs to predetermine its identifier, please use {@link IdentifiedTag}.</p>
 */
@SuppressWarnings("unused")
public class JTag {
  /**
   * <p>Determines whether the tag content replaces existing contents (if so), instead of appending it.</p>
   * <p>For example, assume if the values of {@code brrp:functions/example_tag} is {@code [id1, id2, id3]}, and your custom data pack also has the tag named {@code brrp:functions/example_tag}, and the values of the tag is {@code [id4, id5]}.</p>
   * <p>If {@code replace=false}, the {@code [id4, id5]} will be appended to the existing tag, which will be {@code [id1, id2, id3, id4, id5]}.</p>
   * <p>If {@code replace=true}, it directly replaces {@code [id1, id2, id3]}, which becomes {@code [id4, id5]}.</p>
   */
  public Boolean replace;
  /**
   * Values of this tag. They are stored in the form of string. They include identifiers and tag identifiers (prefixed by {@code "#"}).
   */
  public List<String> values = new ArrayList<>();

  /**
   * Create an empty tag object.
   */
  public JTag() {
  }

  /**
   * Create a new {@link IdentifiedTag} object with the specified type and identifier, and the same replace and values.
   *
   * @param type       The type of the tag.
   * @param identifier The identifier without type specification.
   * @return A new {@link IdentifiedTag} object.
   */

  public IdentifiedTag identified(String type, ResourceLocation identifier) {
    final IdentifiedTag identifiedTag = new IdentifiedTag(type, identifier);
    identifiedTag.replace = replace;
    identifiedTag.values = values;
    return identifiedTag;
  }

  /**
   * Create a new {@link IdentifiedTag} object with the specified type, and the namespace and path of the identifier, and the same replace and values.
   *
   * @param namespace The namespace of identifier.
   * @param type      The type of the tag.
   * @param path      The path without type specification.
   * @return A new {@link IdentifiedTag} object.
   */

  public IdentifiedTag identified(String namespace, String type, String path) {
    final IdentifiedTag identifiedTag = new IdentifiedTag(namespace, type, path);
    identifiedTag.replace = replace;
    identifiedTag.values = values;
    return identifiedTag;
  }

  /**
   * @deprecated Please directly use {@code new JTag().replace()}.
   */
  @Deprecated
  public static JTag replacingTag() {
    return tag().replace();
  }

  /**
   * Set the {@link #replace} to {@code true}.
   *
   * @see #replace(boolean)
   */

  public JTag replace() {
    this.replace = true;
    return this;
  }

  /**
   * Set the {@link #replace} of the tag. You can also directly specify it when constructing.
   *
   * @param replace Whether the tag is replacing.
   */

  public JTag replace(boolean replace) {
    this.replace = replace;
    return this;
  }

  /**
   * @deprecated Please directly use the constructor method {@link #JTag()}.
   */
  @Deprecated
  public static JTag tag() {
    return new JTag();
  }

  /**
   * @implNote Usually you should add the identifier by calling {@link #add(ResourceLocation)} or {@link #tag(ResourceLocation)}.
   */

  public JTag add(String identifier) {
    this.values.add(identifier);
    return this;
  }

  /**
   * Add the identifier of the entry to this tag.
   */

  public JTag add(ResourceLocation identifier) {
    this.values.add(identifier.toString());
    return this;
  }

  /**
   * Add identifiers of entries to this tag.
   */

  public JTag add(ResourceLocation...identifiers) {
    for (ResourceLocation identifier : identifiers) {
      add(identifier);
    }
    return this;
  }

  /**
   * Assume this tag is a block tag, query the block id and add to the tag. Please confirm that when calling this method, the block is correctly registered. If you haven't registered the block, you can register with {@link Registry#register} at first.
   */

  public JTag addBlock(Block block) {
    add(Preconditions.checkNotNull(ForgeRegistries.BLOCKS.getKey(block)));
    return this;
  }

  /**
   * Assume this tag is a block tag, query the identifiers of the blocks and add them to the tag. Please confirm that when calling this method, the blocks are correctly registered. If you haven't registered the blocks, you can register with {@link Registry#register} at first.
   */

  public JTag addBlocks(Iterable<Block> blocks) {
    blocks.forEach(this::addBlock);
    return this;
  }

  /**
   * Assume this tag is a block tag, query the identifiers of the blocks and add them to the tag. Please confirm that when calling this method, the blocks are correctly registered. If you haven't registered the blocks, you can register with {@link Registry#register} at first.
   */

  public JTag addBlocks(Block... blocks) {
    return addBlocks(Arrays.asList(blocks));
  }

  /**
   * Assume this tag is an item tag, query the item id and add to the tag. Please confirm that when calling this method, the item is correctly registered. If you haven't registered the item, you can register with {@link Registry#register} at first.
   */

  public JTag addItem(IItemProvider item) {
    add(Preconditions.checkNotNull(ForgeRegistries.ITEMS.getKey(item.asItem())));
    return this;
  }

  /**
   * Assume this tag is an item tag, query the identifiers of items and add them the tag. Please confirm that when calling this method, the items are correctly registered. If you haven't registered the items, you can register with {@link Registry#register} at first.
   */

  public JTag addItems(Iterable<IItemProvider> items) {
    items.forEach(this::addItem);
    return this;
  }

  /**
   * Assume this tag is an item tag, query the identifiers of items and add them the tag. Please confirm that when calling this method, the items are correctly registered. If you haven't registered the items, you can register with {@link Registry#register} at first.
   */

  public JTag addItems(IItemProvider... items) {
    return addItems(Arrays.asList(items));
  }

  /**
   * Assume this tag is a fluid tag, query the fluid id and add to the tag. Please confirm that when calling this method, the fluid is correctly registered. If you haven't registered the fluid, you can register with {@link Registry#register} at first.
   */

  public JTag addFluid(Fluid fluid) {
    add(Preconditions.checkNotNull(ForgeRegistries.FLUIDS.getKey(fluid)));
    return this;
  }

  /**
   * Assume this tag is a fluid tag, query the identifiers of the fluids and add them to the tag. Please confirm that when calling this method, the fluids are correctly registered. If you haven't registered the fluids, you can register with {@link Registry#register} at first.
   */

  public JTag addFluids(Iterable<Fluid> fluids) {
    fluids.forEach(this::addFluid);
    return this;
  }

  /**
   * Assume this tag is a fluid tag, query the identifiers of the fluids and add them to the tag. Please confirm that when calling this method, the fluids are correctly registered. If you haven't registered the fluids, you can register with {@link Registry#register} at first.
   */

  public JTag addFluids(Fluid... fluids) {
    return this.addFluids(Arrays.asList(fluids));
  }

  /**
   * Assume this tag is an entity-type tag, query the entity type id and add to the tag. Please confirm that when calling this method, the entity type is correctly registered. If you haven't registered the entity type, you can register with {@link Registry#register} at first.
   */

  public JTag addEntityType(EntityType<?> entityType) {
    add(Preconditions.checkNotNull(ForgeRegistries.ENTITIES.getKey(entityType)));
    return this;
  }

  /**
   * Assume this tag is an entity-type tag, query the identifiers of the entity types and add to the tag. Please confirm that when calling this method, the entity types are correctly registered. If you haven't registered the entity types, you can register with {@link Registry#register} at first.
   */

  public JTag addEntityTypes(Iterable<EntityType<?>> entityTypes) {
    entityTypes.forEach(this::addEntityType);
    return this;
  }

  /**
   * Assume this tag is an entity-type tag, query the identifiers of the entity types and add to the tag. Please confirm that when calling this method, the entity types are correctly registered. If you haven't registered the entity types, you can register with {@link Registry#register} at first.
   */
  public JTag addEntityTypes(EntityType<?>... entityTypes) {
    return this.addEntityTypes(Arrays.asList(entityTypes));
  }

  /**
   * add a tag to the tag
   *
   * @deprecated Ambiguous name. Please use {@link #addTag(ResourceLocation)}.
   */
  @Deprecated

  public JTag tag(ResourceLocation tag) {
    this.values.add('#' + tag.getNamespace() + ':' + tag.getPath());
    return this;
  }

  /**
   * Add another tag to this tag. Please be warned that the "another tag" identifier does not specify type. For example, the block tag can {@code addTag(new ResourceLocation("minecraft", "logs"))} instead of {@code addTag{new ResourceLocation("minecraft", "blocks/logs")}}.
   *
   * @param tagIdentifier The identifier of the tag you added. It does not contain the type specification.
   * @return The JTag instance itself, making it possible to chain-call.
   */

  public JTag addTag(ResourceLocation tagIdentifier) {
    this.values.add("#" + tagIdentifier.toString());
    return this;
  }

  /**
   * Add other tags to this tag. Please be warned that the "other tag" identifiers do not specify type.
   *
   * @param tagIdentifiers The identifiers of the tag you added. They do not contain the type specification.
   * @return The JTag instance itself, making it possible to chain-call.
   */

  public JTag addTagIds(Iterable<ResourceLocation> tagIdentifiers) {
    tagIdentifiers.forEach(this::addTag);
    return this;
  }

  /**
   * Add other tags to this tag. Please be warned that the "other tag" identifiers do not specify type.
   *
   * @param tagIdentifiers The identifiers of the tag you added. They do not contain the type specification.
   * @return The JTag instance itself, making it possible to chain-call.
   */

  public JTag addTagIds(ResourceLocation...tagIdentifiers) {
    return addTagIds(Arrays.asList(tagIdentifiers));
  }

  public JTag addTag(IdentifiedTag tag) {
    this.values.add("#" + tag.identifier.toString());
    return this;
  }

  /**
   * Add other tags to this tag. In this method, the "tags" parameter is the tags used for BRRP, and you assume that the type of these tags matches the type of this. Each of the objects has stored an identifier, so those identifiers can be directly used.
   */

  public JTag addTags(Iterable<IdentifiedTag> tags) {
    tags.forEach(this::addTag);
    return this;
  }

  /**
   * Add other tags to this tag. In this method, the "tags" parameter is the tags used for BRRP, and you assume that the type of these tags matches the type of this. Each of the objects has stored an identifier, so those identifiers can be directly used.
   */

  public JTag addTags(IdentifiedTag... tags) {
    return addTags(Arrays.asList(tags));
  }

  @Override
  public JTag clone() {
    try {
      return (JTag) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e);
    }
  }
}
