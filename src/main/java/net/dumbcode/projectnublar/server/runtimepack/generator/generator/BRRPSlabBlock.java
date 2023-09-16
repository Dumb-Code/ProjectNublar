package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import com.google.gson.JsonObject;
import net.dumbcode.projectnublar.mixin.BlockLootTablesInvoker;
import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate.JBlockStates;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.loot.JLootTable;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JTextures;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JRecipe;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JShapedRecipe;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This is a simple extension of {@link SlabBlock} with the resource generation provided.
 */
public class BRRPSlabBlock extends SlabBlock implements BlockResourceGenerator {
  /**
   * The base block will be used to generate some files. It can be null.<br>
   * When the base block is null, the double-slab creates and uses the model or "slab_double" model, instead of directly using the model of base block.
   */
  public final Block baseBlock;

  @Override
  public Block getBaseBlock() {
    return baseBlock;
  }

  /**
   * Simply creates an instance with a given base block. The block settings of the base block will be used, so you do not need to provide it.
   */
  public BRRPSlabBlock(Block baseBlock) {
    this(baseBlock, AbstractBlock.Properties.copy(baseBlock));
  }

  public BRRPSlabBlock(Block baseBlock, Properties settings) {
    super(settings);
    this.baseBlock = baseBlock;
  }

  /**
   * Directly creates an instance without giving the base block.
   */
  public BRRPSlabBlock(Properties settings) {
    this(null, settings);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JBlockStates getBlockStates() {
    final ResourceLocation id = getBlockModelId();
    return JBlockStates.simpleSlab(baseBlock != null ? ResourceGeneratorHelper.getBlockModelId(baseBlock) : IdentifierExtension.append("_double", id), id, IdentifierExtension.append("_top", id));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel("block/slab").textures(JTextures.ofSides(
        getTextureId(StockTextureAliases.TOP),
        getTextureId(StockTextureAliases.SIDE),
        getTextureId(StockTextureAliases.BOTTOM)
    ));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void writeBlockModel(RuntimeResourcePack pack) {
    final JModel model = getBlockModel();
    if (model != null) {
      final ResourceLocation id = getBlockModelId();
      pack.addModel(model, id);
      pack.addModel(model.clone().parent("block/slab_top"), IdentifierExtension.append("_top", id));
      if (baseBlock == null) {
        pack.addModel(model.clone().parent("block/cube_bottom_top"), IdentifierExtension.append("_double", id));
      }
    }
  }

  private static final JsonObject BLOCK_STATE_PROPERTY = new JsonObject();

  static {
    BLOCK_STATE_PROPERTY.addProperty("type", "double");
  }

  @Override
  public JLootTable getALootTable() {
    return JLootTable.delegate(BlockLootTablesInvoker.createSlabItemTable(this).build());
  }

  /**
   * It slightly resembles , but bypasses validation.
   */
  @Override
  public JRecipe getCraftingRecipe() {
    return baseBlock == null ? null : new JShapedRecipe(this)
        .resultCount(6)
        .pattern("###")
        .addKey("#", baseBlock)
        .addInventoryChangedCriterion("has_ingredient", baseBlock);
  }
}
