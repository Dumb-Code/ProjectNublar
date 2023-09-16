package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import com.google.common.base.Preconditions;
import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.dumbcode.projectnublar.server.runtimepack.generator.annotations.PreferredEnvironment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * <p>It's similar to {@link ItemResourceGenerator} and {@link BlockResourceGenerator}, but it provides more <i>static</i> methods so that they can be used for blocks and items that do not implement {@link ItemResourceGenerator}, which are usually vanilla blocks.</p>
 * <p>It's not recommended to inject the interfaces above to vanilla classes using mixins.</p>
 * <p>If your sure that the instance <i>is</i> a {@link ItemResourceGenerator}, you should use their methods instead, instead of these static methods.</p>
 *
 * @author SolidBlock
 */
public final class ResourceGeneratorHelper {
  public static ResourceLocation getItemId(IItemProvider item) {
    return (item instanceof ItemResourceGenerator) ? ((ItemResourceGenerator) item).getItemId() : ForgeRegistries.ITEMS.getKey(item.asItem());
  }

  /**
   * @since 0.6.2 Fixed the issue that the item model id is not correct.
   */
  public static ResourceLocation getItemModelId(IItemProvider item) {
    return (item instanceof ItemResourceGenerator) ? ((ItemResourceGenerator) item).getItemModelId() : IdentifierExtension.prepend("item/", Preconditions.checkNotNull(ForgeRegistries.ITEMS.getKey(item.asItem()), "The item is not registered."));
  }

  public static ResourceLocation getBlockId(Block block) {
    return (block instanceof BlockResourceGenerator) ? ((BlockResourceGenerator) block).getBlockId() : ForgeRegistries.BLOCKS.getKey(block);
  }

  public static ResourceLocation getBlockModelId(Block block) {
    return (block instanceof BlockResourceGenerator) ? ((BlockResourceGenerator) block).getBlockModelId() : IdentifierExtension.prepend("block/", Preconditions.checkNotNull(ForgeRegistries.BLOCKS.getKey(block), "The block is not registered."));
  }

  @PreferredEnvironment(Dist.CLIENT)
  public static String getTextureId(Block block, StockTextureAliases textureKey) {
    if (block instanceof BlockResourceGenerator) {
      return ((BlockResourceGenerator) block).getTextureId(textureKey);
    }
    final ResourceLocation texture = TextureRegistry.getTexture(block, textureKey);
    if (texture != null) return texture.toString();
    return IdentifierExtension.prepend("block/", getBlockId(block)).toString();
  }

  /**
   * Get the id of the ordinary recipe. This method does not check whether the recipe really exists.
   */
  public static ResourceLocation getRecipeId(IItemProvider item) {
    if (item instanceof ItemResourceGenerator) {
      return ((ItemResourceGenerator) item).getRecipeId();
    } else {
      return getItemId(item);
    }
  }

  public static ResourceLocation getAdvancementIdForRecipe(IItemProvider item, ResourceLocation recipeId) {
    if (item instanceof ItemResourceGenerator) {
      return ((ItemResourceGenerator) item).getAdvancementIdForRecipe(recipeId);
    } else {
      final ItemGroup group = item.asItem().getItemCategory();
      if (group != null) {
        return IdentifierExtension.prepend("recipes/" + group.getRecipeFolderName() + "/", recipeId);
      }
      return IdentifierExtension.prepend("recipes/", getItemId(item));
    }
  }

  public static ResourceLocation getLootTableId(AbstractBlock block) {
    if (block instanceof BlockResourceGenerator) {
      return block.getLootTable();
    } else {
      return block.getLootTable();
    }
  }
}
