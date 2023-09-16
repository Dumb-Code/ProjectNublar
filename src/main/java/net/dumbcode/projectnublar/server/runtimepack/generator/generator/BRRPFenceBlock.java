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
import net.minecraft.block.FenceBlock;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BRRPFenceBlock extends FenceBlock implements BlockResourceGenerator {
  /**
   * The base block of the fence block.
   */
  public final Block baseBlock;

  @Override
  public Block getBaseBlock() {
    return baseBlock;
  }

  public BRRPFenceBlock(Block baseBlock, Properties settings) {
    super(settings);
    this.baseBlock = baseBlock;
  }

  public BRRPFenceBlock(Block baseBlock) {
    this(baseBlock, AbstractBlock.Properties.copy(baseBlock));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JBlockStates getBlockStates() {
    final ResourceLocation blockModelId = getBlockModelId();
    return JBlockStates.delegate(BlockModelProviderInvoker.createFence(
        this,
            IdentifierExtension.append("_post", blockModelId),
            IdentifierExtension.append("_side", blockModelId)
    ));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel().addTexture("texture", getTextureId(StockTextureAliases.TEXTURE));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void writeBlockModel(RuntimeResourcePack pack) {
    final JModel blockModel = getBlockModel();
    final ResourceLocation blockModelId = getBlockModelId();
    pack.addModel(blockModel.parent("block/fence_post"), IdentifierExtension.append("_post", blockModelId));
    pack.addModel(blockModel.parent("block/fence_side"), IdentifierExtension.append("_side", blockModelId));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getItemModel() {
    return getBlockModel().parent("block/fence_inventory");
  }

  /**
   * This recipe uses the base block and stick as the ingredients.
   */
  @Override
  public JRecipe getCraftingRecipe() {
    final Item secondIngredient = getSecondIngredient();
    return baseBlock == null || secondIngredient == null ? null :
        new JShapedRecipe(this)
            .resultCount(3)
            .pattern("W#W", "W#W")
            .addKey("W", baseBlock)
            .addKey("#", secondIngredient)
            // The second ingredient does not matter for recipe.
            // Therefore, the recipe is unlocked when you obtain the base block, instead of the second ingredient.
            .addInventoryChangedCriterion("has_ingredient", baseBlock);
  }

  /**
   * The second ingredient used in the crafting recipe. It's by default a stick. In {@link #getCraftingRecipe()}, the crafting recipe is composed of 6 base blocks and 2 second ingredients.
   *
   * @return The second ingredient to craft.
   */
  public Item getSecondIngredient() {
    return Items.STICK;
  }
}
