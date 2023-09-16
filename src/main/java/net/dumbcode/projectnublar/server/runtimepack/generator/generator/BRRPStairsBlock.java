package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import com.google.common.base.Suppliers;
import net.dumbcode.projectnublar.mixin.BlockModelProviderInvoker;
import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate.JBlockStates;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JTextures;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JRecipe;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JResult;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JShapedRecipe;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class BRRPStairsBlock extends StairsBlock implements BlockResourceGenerator {
  public final Supplier<Block> baseBlockSupplier;

  private BRRPStairsBlock(Properties settings, Supplier<BlockState> stateSupplier) {
    super(stateSupplier, settings);
    this.baseBlockSupplier = () -> stateSupplier.get().getBlock();
  }

  public BRRPStairsBlock(Supplier<Block> baseBlockSupplier, Properties settings) {
    super(() -> baseBlockSupplier.get().defaultBlockState(), settings);
    this.baseBlockSupplier = baseBlockSupplier;
  }

  public BRRPStairsBlock(Block baseBlock) {
    this(Suppliers.ofInstance(baseBlock), AbstractBlock.Properties.copy(baseBlock));
  }

  @Override
  public Block getBaseBlock() {
    return baseBlockSupplier.get();
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JBlockStates getBlockStates() {
    final ResourceLocation blockModelId = getBlockModelId();
    return JBlockStates.delegate(BlockModelProviderInvoker.createStairs(this, IdentifierExtension.append("_inner", blockModelId), blockModelId, IdentifierExtension.append("_outer", blockModelId)));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel("block/stairs").textures(JTextures.ofSides(getTextureId(StockTextureAliases.TOP),
        getTextureId(StockTextureAliases.SIDE),
        getTextureId(StockTextureAliases.BOTTOM)));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void writeBlockModel(RuntimeResourcePack pack) {
    final JModel blockModel = getBlockModel();
    final ResourceLocation id = getBlockModelId();
    pack.addModel(blockModel, id);
    pack.addModel(blockModel.parent("block/inner_stairs"), IdentifierExtension.append("_inner", id));
    pack.addModel(blockModel.parent("block/outer_stairs"), IdentifierExtension.append("_outer", id));
  }

  /**
   * It slightly resembles , but bypasses validation so as not to come error.
   */
  @Override
  public JRecipe getCraftingRecipe() {
    final Block baseBlock = getBaseBlock();
    return baseBlock == null ? null :
        new JShapedRecipe(new JResult(this)
            .count(4))
            .pattern("#  ", "## ", "###")
            .addKey("#", baseBlock)
            .addInventoryChangedCriterion("has_the_ingredient", baseBlock);
  }
}
