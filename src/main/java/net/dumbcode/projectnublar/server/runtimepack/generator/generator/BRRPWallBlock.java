package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import net.dumbcode.projectnublar.mixin.BlockModelProviderInvoker;
import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate.JBlockStates;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JRecipe;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.recipe.JShapedRecipe;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.WallBlock;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BRRPWallBlock extends WallBlock implements BlockResourceGenerator {
  public final Block baseBlock;

  public BRRPWallBlock(Block baseBlock, Properties settings) {
    super(settings);
    this.baseBlock = baseBlock;
  }

  public BRRPWallBlock(Block baseBlock) {
    this(baseBlock, AbstractBlock.Properties.copy(baseBlock));
  }

  @Override
  public Block getBaseBlock() {
    return baseBlock;
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JBlockStates getBlockStates() {
    final ResourceLocation blockModelId = getBlockModelId();
    return JBlockStates.delegate(BlockModelProviderInvoker.createWall(
        this,
        IdentifierExtension.append("_post", blockModelId),
        IdentifierExtension.append("_side", blockModelId),
        IdentifierExtension.append("_side_tall", blockModelId)
    ));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel("block/template_wall_post").addTexture("wall", getTextureId(StockTextureAliases.WALL));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void writeBlockModel(RuntimeResourcePack pack) {
    final ResourceLocation blockModelId = getBlockModelId();
    final JModel blockModel = getBlockModel();
    pack.addModel(blockModel, IdentifierExtension.append("_post", blockModelId));
    pack.addModel(blockModel.parent("block/template_wall_side"), IdentifierExtension.append("_side", blockModelId));
    pack.addModel(blockModel.parent("block/template_wall_side_tall"), IdentifierExtension.append("_side_tall", blockModelId));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getItemModel() {
    return new JModel("block/wall_inventory").addTexture("wall", getTextureId(StockTextureAliases.WALL));
  }

  /**
   *
   */
  @Override
  public JRecipe getCraftingRecipe() {
    return baseBlock == null ? null : new JShapedRecipe(this)
        .resultCount(6)
        .pattern("###", "###")
        .addKey("#", baseBlock)
        .addInventoryChangedCriterion("has_ingredient", baseBlock);
  }
}
