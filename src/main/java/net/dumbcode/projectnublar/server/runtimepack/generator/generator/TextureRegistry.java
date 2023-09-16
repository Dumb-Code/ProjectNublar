package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.dumbcode.projectnublar.server.runtimepack.generator.IdentifierExtension;
import net.minecraft.block.Block;
import net.minecraft.data.ModelTextures;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <p>This is used for recording textures. Sometimes you created a block, and wants the {@link BlockResourceGenerator#getTextureId} use textures that differ from the block id. You do not need to override the method. You can directly use this registry to specify the textures used.</p>
 * <p>Notice that the texture registry is <i>not always</i> respected when getting the texture. For example, in {@link BRRPCubeBlock}, the texture definitions may be predetermined when instantiating. That may ignore this registry.</p>
 * <p>Block-based blocks, for example, slabs, may query the texture registry for their base blocks, as defined in {@link BRRPSlabBlock#getTextureId}. This affects all blocks that override {@link BlockResourceGenerator#getBaseBlock()}, but <i>does not affect</i> vanilla blocks because they do not implement that method.</p>
 * <p>The texture key respects their parent keys (for "fallback keys"). For example, {@link StockTextureAliases#EAST} fall backs to {@link StockTextureAliases#SIDE}, which fall backs to {@link StockTextureAliases#ALL}, so when querying the east texture of the block, and that texture key of the block is not registered, side texture will be used; if side texture does not exist as well, its all texture will be used, and if that is still absent, {@code null} value will be returned.</p>
 * <p>To register, you can simple call {@link #register}, and you can call {@link #getTexture} to get the texture.</p>
 * <p><i>Notice that textures of vanilla blocks are not registered</i> by default. It's OK to register vanilla blocks, no problem, but you may have to consider compatibility with other mods. If you're sure to register vanilla blocks to this registry, it's highly recommended to keep consistent with the vanilla texture (see {@link net.minecraft.data.BlockModelProvider}).</p>
 * <p>For example, if you register a sandstone block like this:</p>
 * <pre>{@code
 * TextureRegistry.registerAppended(Blocks.SANDSTONE, StockTextureAliases.TOP, "_top");
 * TextureRegistry.registerAppended(Blocks.SANDSTONE, StockTextureAliases.BOTTOM, "_bottom");
 *
 * TextureRegistry.registerWithName(Blocks.SMOOTH_SANDSTONE, "sandstone_top");
 * }</pre>
 * <p>then:</p>
 * <ul>
 * <li>{@code TextureRegistry.getTexture(Blocks.SANDSTONE, StockTextureAliases.TOP)} returns <code style="color:maroon">minecraft:block/sandstone_top</code>.</li><li>
 * {@code TextureRegistry.getTexture(Blocks.SANDSTONE)} returns {@code null}.</li><li>
 * And {@code ResourceGeneratorHelper.getTextureId(Blocks.SANDSTONE, null)} may return <code style="color:maroon">minecraft:block/sandstone</code>, as the default behaviour.</li><li>
 * {@code TextureRegistry.getTexture(Blocks.SMOOTH_SANDSTONE)} returns <code style="color:maroon">minecraft:block/sandstone_top</code>, as registered above, and</li><li>
 * {@code TextureRegistry.getTexture(Blocks.SMOOTH_SANDSTONE, StockTextureAliases.TOP)} also returns <code style="color:maroon">minecraft:block/sandstone_top</code>, because it fall backs to general registry.</li>
 * </ul>
 */
public final class TextureRegistry {
  /**
   * The map storing blocks and their texture maps. Keys are blocks, and values are their texture maps.
   */
  private static final Object2ObjectMap<Block, ModelTextures> TEXTURE_MAPS = new Object2ObjectOpenHashMap<>();

  /**
   * <p>Register a block texture with a default texture key ({@link StockTextureAliases#ALL}). The texture key {@code StockTextureAliases.ALL} is the fallback texture key of most texture keys, so it is regarded default.</p>
   * <p>If there is no texture map that corresponds to the block, the map will be created and put with the default texture key and texture.</p>
   *
   * @param block   The block you register.
   * @param texture The identifier of the texture. It is usually in the form of <code><i>namespace:</i>block/<i>path</i></code>
   */
  public static void register(Block block, ResourceLocation texture) {
    register(block, StockTextureAliases.ALL, texture);
  }

  /**
   * <p>Register a block texture with the specified texture key.</p>
   * <p>If there is no texture map that corresponds to the block, the map will be created and put with the texture key and texture.</p>
   *
   * @param block      The block you register.
   * @param textureKey The texture key. Vanilla texture keys can be found in {@link StockTextureAliases}.
   * @param texture    The identifier of the texture. It is usually in the form of <code><i>namespace:</i>block/<i>path</i></code>
   */
  public static void register(Block block, StockTextureAliases textureKey, ResourceLocation texture) {
    TEXTURE_MAPS.computeIfAbsent(block, b -> new ModelTextures()).put(textureKey, texture);
  }

  /**
   * <p>Register a block texture with the specified texture key. The identifier is created by the block identifier with the specified suffix.</p>
   * <p>In this case, the block id will be used, so please make sure that the block has been registered. This is a convenient way.</p>
   * <p>The block with id {@code minecraft:sandstone} with the suffix {@code "_top"} parameter will use the following texture identifier: {@code minecraft:blocks/sandstone_top}.</p>
   *
   * @param block      The block you register.
   * @param textureKey The texture key. Vanilla texture keys can be found in {@link StockTextureAliases}.
   * @param suffix     The suffix to append to the block id.
   */
  public static void registerAppended(Block block, StockTextureAliases textureKey, String suffix) {
    register(block, textureKey, IdentifierExtension.pend("blocks/", suffix, Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(block), "The block is not registered.")));
  }

  /**
   * <p>Register {@code null} a block texture with the specified texture key. The identifier is created with the same namespace of the block id and the specified path.</p>
   * <p>In this case, the namespace of block id will be used, so please make sure that the block has been registered.</p>
   * <p>For example, the block with id {@code minecraft:smooth_sandstone} with the path {@code "sandstone_top"} parameter will use the following texture identifier: {@code minecraft:block/sandstone_top}.</p>
   *
   * @param block      The block you register.
   * @param textureKey The texture key. Vanilla texture keys can be found in {@link StockTextureAliases}.
   * @param path       The path of the identifier (not including {@code "blocks/"}).
   */
  public static void registerWithName(Block block, StockTextureAliases textureKey, String path) {
    final ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
    Preconditions.checkNotNull(id, "The block is not registered");
    register(block, textureKey, new ResourceLocation(id.getNamespace(), "blocks/" + path));
  }

  /**
   * <p>Get a block texture with the specified texture key. If the texture map is not found with the block, {@code null} will be returned regardless of the texture key. If the map exists but the texture key does not exist, parent key (or called "fallback key") of that key will be used, and if it still does not exist, {@code null} will be returned.</p>
   *
   * @param block      The block you query.
   * @param textureKey The texture key you query.
   * @return The identifier of the corresponding texture, or {@code null} if the texture does not exist.
   * @see #getTexture(Block)
   * @see ModelTextures#get(StockTextureAliases)
   */
  public static ResourceLocation getTexture(Block block, StockTextureAliases textureKey) {
    final ModelTextures textureMap = TEXTURE_MAPS.getOrDefault(block, null);
    if (textureMap == null) return null;
    try {
      // In textureMap#getTexture, the exception will be thrown if the texture is not found. However, we do not throw any exceptions. We just return null.
      return textureMap.get(textureKey);
    } catch (IllegalStateException e) {
      return null;
    }
  }

  /**
   * Get a block texture with the default texture key. If the texture map is not found with the block, {@code null} with be returned. The default texture key {@link StockTextureAliases#ALL} has no parent key.
   *
   * @param block The block you query.
   * @return The identifier of the corresponding texture, or {@code null} if the texture does not exist.
   * @see #getTexture(Block)
   * @see ModelTextures#get(StockTextureAliases)
   */
  public static ResourceLocation getTexture(Block block) {
    return getTexture(block, StockTextureAliases.ALL);
  }

  /**
   * <p>Get the <i>unmodifiable view</i> of the map storing the blocks and their textures. However, their texture maps are not unmodifiable; they are passes <i>as is</i>.</p>
   * <p>To get the specific texture map, you can, for example:</p>
   * {@code
   * TextureRegistry.getTexture().get(Blocks.STONE)
   * }
   *
   * @return The unmodifiable view of map of texture map.
   */
  public static Map<Block, ModelTextures> getTextures() {
    return Collections.unmodifiableMap(TEXTURE_MAPS);
  }
}
