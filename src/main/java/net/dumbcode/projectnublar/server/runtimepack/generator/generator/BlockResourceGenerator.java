package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate.JBlockStates;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.loot.JLootTable;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JRecipe;
import net.minecraft.block.Block;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * <p>The interface is used for blocks.</p>
 * <p>Your custom block class can implement this interface, and override some methods you need. For example:</p>
 * <p>This interface simply <i>extends</i> {@link ItemResourceGenerator}, as the resources of the block item related to it will be also generated. It's also possible that the block does not have a block item; in this case the recipe and item model should be ignored.</p>
 * <pre>{@code
 * public class MyBlock extends Block implements BlockResourceGenerator {
 *   [...]
 *   @OnlyIn(Dist.CLIENT)
 *   public JModel getBlockModel() {
 *     return [...]
 *   }
 *   [...]
 * }
 * }</pre>
 * <p>Also, your custom class can implement this interface. In this case, you <i>must</i> override {@link #getBlockId()} method, as it cannot be casted to {@code Block}.</p>
 * <p>It's highly recommended but not required to annotate methods related to client (block states, block models, item models) with {@code @}{@link net.minecraftforge.api.distmarker.OnlyIn}<code>({@link Dist#CLIENT})</code>, as they are only used in the client version mod. The interface itself does not annotate it, in consideration of rare situations that the server really needs. But mostly these client-related methods are not needed in the server side.</p>
 */
public interface BlockResourceGenerator extends ItemResourceGenerator {
  /**
   * The base block of this block. You <i>should override</i> this method for block-based blocks, like stairs, slab, etc.
   *
   * @return The base block of this block.
   */
  default Block getBaseBlock() {
    return null;
  }

  /**
   * Query the id of the block in {@link ForgeRegistries#BLOCKS}.<br>
   * You <i>must</i> override this method if you're implementing this interface on a non-{@code Block} class, or will use it when it is not yet registered.
   *
   * @return The id of the block.
   */
  default ResourceLocation getBlockId() {
    return ForgeRegistries.BLOCKS.getKey((Block) this);
  }

  /**
   * Query the id of the corresponding block item. You can override when needed, but most time there is no need.
   * <p>
   * Usually the block id is the same as the item id, but we do not assume that here.
   *
   * @return The id of the block item, or {@code null} if the block has no item.
   */
  @Override
  default ResourceLocation getItemId() {
    if (this instanceof Block && BlockItem.BY_BLOCK.containsKey(this)) {
      return ForgeRegistries.ITEMS.getKey(BlockItem.BY_BLOCK.get(this));
    } else {
      return null;
    }
  }


  // CLIENT PART


  /**
   * The id of the block model. It is usually <code style="color: maroon"><i>namespace</i>:block/<i>path</i></code>. For example, the model id of stone is {@code minecraft:block/stone}.
   *
   * @return The id of the block model.
   */
  @PreferredEnvironment(Dist.CLIENT)
  default ResourceLocation getBlockModelId() {
    return IdentifierExtension.prepend("block/", getBlockId());
  }

  /**
   * <p>The texture used in models. It's usually in the format of {@code <i>namespace</i>:block/<i>path</i>}, which <i>mostly</i> equals to the block id. However, sometimes they differ. For example, the texture of <code style="color:maroon">minecraft:smooth_sandstone</code> is not <code style="color:maroon">minecraft:block/smooth_sandstone</code>; it's <code style="color:maroon">minecraft:block/sandstone_top</code>.</p>
   * <p>Some blocks have different textures in different parts. In this case, the parameter {@code type} is used. For example, a quartz pillar can have the following methods:</p>
   * <pre>{@code
   *   @OnlyIn(Dist.CLIENT) @Override
   *   public getTextureId(String type) {
   *     if ("end".equals(type)) {
   *       return new ResourceLocation("minecraft", "block/quartz_pillar_top");
   *     } else {
   *       return new ResourceLocation("minecraft", "block/quartz_pillar");
   *     }
   *   }
   * }</pre>
   *
   * @param textureKey The type used to distinguish texture.
   * @return The id of the texture.
   * @see TextureRegistry
   */
  @PreferredEnvironment(Dist.CLIENT)
  default String getTextureId(StockTextureAliases textureKey) {
    if (this instanceof Block) {
      Block thisBlock = (Block) this;
      final ResourceLocation texture = TextureRegistry.getTexture(thisBlock, textureKey);
      if (texture != null) return texture.toString();
      final Block baseBlock = getBaseBlock();
      if (baseBlock != null) {
        return ResourceGeneratorHelper.getTextureId(baseBlock, textureKey);
      }
    }
    return IdentifierExtension.prepend("block/", getBlockId()).toString();
  }

  /**
   * The block states definition of the block. In the object, the returned value of {@link #getBlockModelId()} is often used.
   *
   * @return The block states definition of the block.
   */
  @PreferredEnvironment(Dist.CLIENT)
  default JBlockStates getBlockStates() {
    return null;
  }

  /**
   * Write the block states definition (returned in {@link #getBlockStates}) to the runtime resource pack, if that is not {@code null}. Usually a block has one block states definition file, with the id identical to the block id.
   *
   * @param pack The runtime resource pack.
   */
  @PreferredEnvironment(Dist.CLIENT)
  default void writeBlockStates(RuntimeResourcePack pack) {
    final JBlockStates blockStates = getBlockStates();
    if (blockStates != null) pack.addBlockState(blockStates, getBlockId());
  }

  /**
   * The model of the block. If a block has multiple models, you may override this method for the most basic model, and override {@link #writeBlockModel}.
   *
   * @return The block model.
   */
  @PreferredEnvironment(Dist.CLIENT)
  default JModel getBlockModel() {
    return null;
  }

  /**
   * Write the block model (returned in {@link #getBlockModel}) to the runtime resource pack. It does nothing if the returned model is {@code null}. If the block has multiple models, you may override this method.
   *
   * @param pack The runtime resource pack.
   */
  @PreferredEnvironment(Dist.CLIENT)
  default void writeBlockModel(RuntimeResourcePack pack) {
    final JModel model = getBlockModel();
    if (model != null) pack.addModel(model, getBlockModelId());
  }

  /**
   * If the item id is null, which means the block item does not exist, the item model id will be null as well.
   *
   * @return The id of the block item model.
   */
  @Override
  @PreferredEnvironment(Dist.CLIENT)
  default ResourceLocation getItemModelId() {
    final ResourceLocation itemId = getItemId();
    if (itemId == null) return null;
    return IdentifierExtension.prepend("item/", itemId);
  }

  /**
   * If the item id is null, which means the block item does not exist, the item model will not be generated, let alone written.
   *
   * @param pack The runtime resource pack.
   */
  @Override
  @PreferredEnvironment(Dist.CLIENT)
  default void writeItemModel(RuntimeResourcePack pack) {
    final ResourceLocation itemModelId = getItemModelId();
    if (itemModelId != null) {
      final JModel model = getItemModel();
      if (model != null) {
        pack.addModel(model, itemModelId);
      }
    }
  }

  /**
   * The model of the block item. It probably directly inherits the block model. But sometimes it is a "layer0" with the texture (for example, grass panes). In that case you can override this model.<p>
   * If you do not need an item model, you can override it and make it return null.
   *
   * @return The item model.
   */
  @Override
  @PreferredEnvironment(Dist.CLIENT)
  default JModel getItemModel() {
    return new JModel(getBlockModelId());
  }

  /**
   * Write the block states definitions, block models and item models.
   *
   * @param pack The runtime resource pack.
   */
  @Override
  @PreferredEnvironment(Dist.CLIENT)
  default void writeAssets(RuntimeResourcePack pack) {
    writeBlockStates(pack);
    writeBlockModel(pack);
    writeItemModel(pack);
  }


  // SERVER PART

  /**
   * Get the id of the block loot table. It's by default in the format of <code><i>namespace:</i>blocks/<i>path</i></code>, note its "blocks" instead of "block". The loot table is used when the block is broken.
   *
   * @return The id of the block loot table.
   */
  default ResourceLocation getLootTableId() {
    return IdentifierExtension.prepend("blocks/", getBlockId());
  }

  /**
   * Get the block loot table. It's by default the simplest loot table, which means one block of itself will be dropped when broken.
   *
   * @return The block loot table.
   */
  default JLootTable getALootTable() {
    return JLootTable.simple(getItemId().toString());
  }

  /**
   * Write the block loot table to the runtime resource pack.
   *
   * @param pack The runtime resource pack.
   */
  default void writeLootTable(RuntimeResourcePack pack) {
    final JLootTable lootTable = getALootTable();
    if (lootTable != null) {
      pack.addLootTable(getLootTableId(), lootTable);
    }
  }

  /**
   * Get the stonecutting recipe of the block. This is quite useful for block-based blocks, like stairs, slabs and fences.
   * <p>
   * <b>Note:</b> Stonecutting recipes will not be generated unless {@link #shouldWriteStonecuttingRecipe()} returns {@code true}.
   *
   * @return The stonecutting recipe.
   * @see net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JStonecuttingRecipe
   */
  default JRecipe getStonecuttingRecipe() {
    return null;
  }

  /**
   * Whether to write stonecutting recipe. <b>It's by default <code>false</code></b> and you can override this method according to your actual need.
   *
   * @return The boolean value indicating whether to write stonecutting recipes of the block in {@link #writeRecipes(RuntimeResourcePack)}.
   */
  default boolean shouldWriteStonecuttingRecipe() {
    return false;
  }

  /**
   * For blocks, they may have stonecutting recipes. If you {@link #shouldWriteStonecuttingRecipe()} does not return {@code false} and {@link #getStonecuttingRecipe()} returns not null, the stonecutting recipe will be generated. The id of the stonecutting recipe is by default the crafting id appended with {@code "_from_stonecutting"}, but you can override it in {@link #getStonecuttingRecipeId()}.
   *
   * @param pack The runtime resource pack.
   */
  @Override
  default void writeRecipes(RuntimeResourcePack pack) {
    ItemResourceGenerator.super.writeRecipes(pack);
    if (shouldWriteStonecuttingRecipe()) {
      final JRecipe stonecuttingRecipe = getStonecuttingRecipe();
      if (stonecuttingRecipe != null) {
        final ResourceLocation stonecuttingRecipeId = getStonecuttingRecipeId();
        pack.addRecipe(stonecuttingRecipeId, stonecuttingRecipe);
        pack.addRecipeAdvancement(stonecuttingRecipeId, getAdvancementIdForRecipe(stonecuttingRecipeId), stonecuttingRecipe);
      }
    }
  }

  /**
   * @return The id of the stonecutting recipe. It is usually the recipe id appended {@code "_from_stonecutting"}.
   */
  default ResourceLocation getStonecuttingRecipeId() {
    return IdentifierExtension.append("_from_stonecutting", getRecipeId());
  }

  /**
   * {@inheritDoc}
   *
   * @param pack The runtime resource pack.
   */
  default void writeData(RuntimeResourcePack pack) {
    writeLootTable(pack);
    writeRecipes(pack);
  }
}
