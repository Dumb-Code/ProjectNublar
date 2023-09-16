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
import net.minecraft.block.FenceGateBlock;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BRRPFenceGateBlock extends FenceGateBlock implements BlockResourceGenerator {
  public final Block baseBlock;

  public BRRPFenceGateBlock(Block baseBlock, Properties settings) {
    super(settings);
    this.baseBlock = baseBlock;
  }

  public BRRPFenceGateBlock(Block baseBlock) {
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
    return JBlockStates.delegate(BlockModelProviderInvoker.createFenceGate(
        this,
            IdentifierExtension.append("_open", blockModelId),
        blockModelId,
            IdentifierExtension.append("_wall_open", blockModelId),
            IdentifierExtension.append("_wall", blockModelId)
    ));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel("block/template_fence_gate").addTexture("texture", getTextureId(StockTextureAliases.TEXTURE));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void writeBlockModel(RuntimeResourcePack pack) {
    final JModel blockModel = getBlockModel();
    final ResourceLocation blockModelId = getBlockModelId();
    pack.addModel(blockModel, blockModelId);
    pack.addModel(blockModel.parent("block/template_fence_gate_open"), IdentifierExtension.append("_open", blockModelId));
    pack.addModel(blockModel.parent("block/template_fence_gate_wall"), IdentifierExtension.append("_wall", blockModelId));
    pack.addModel(blockModel.parent("block/template_fence_gate_wall_open"), IdentifierExtension.append("_wall_open", blockModelId));
  }

  /**
   * This recipe uses the base block and stick as the ingredients.
   */
  @Override
  public JRecipe getCraftingRecipe() {
    final Item secondIngredient = getSecondIngredient();
    return baseBlock == null || secondIngredient == null ? null :
        new JShapedRecipe(this)
            .pattern("#W#", "#W#")
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
