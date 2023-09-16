package net.dumbcode.projectnublar.server.runtimepack.generator.generator;

import net.dumbcode.projectnublar.server.runtimepack.generator.json.blockstate.JBlockStates;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JModel;
import net.dumbcode.projectnublar.server.runtimepack.generator.json.models.JTextures;
import net.minecraft.block.Block;
import net.minecraft.data.StockTextureAliases;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * This is a simple extension of a cube block. You can specify textures for it.
 */
public class BRRPCubeBlock extends Block implements BlockResourceGenerator {
  public final String parent;
  public final JTextures textures;

  /**
   * This instance can provide models through these fields.
   *
   * @param settings The block settings.
   * @param parent   The id of the parent model. For example, {@code "block/cube_all"}.
   * @param textures Texture definitions of the model.
   */
  public BRRPCubeBlock(Properties settings, String parent, JTextures textures) {
    super(settings);
    this.parent = parent;
    this.textures = textures;
  }

  /**
   * The block will use the "allTexture" for all sides. It's a classic "cube_all".
   *
   * @param settings   The block settings.
   * @param allTexture Texture for all sides.
   * @return A new instance.
   */
  public static BRRPCubeBlock cubeAll(Properties settings, String allTexture) {
    return new BRRPCubeBlock(settings, "block/cube_all", JTextures.ofAll(allTexture));
  }

  /**
   * The block is a simple "cube_bottom_top".
   *
   * @param settings      The block settings.
   * @param topTexture    Texture for top side.
   * @param sideTexture   Texture for horizontal sides.
   * @param bottomTexture Texture for bottom side.
   * @return A new instance.
   */
  public static BRRPCubeBlock cubeBottomTop(Properties settings, String topTexture, String sideTexture, String bottomTexture) {
    return new BRRPCubeBlock(settings, "block/cube_bottom_top", JTextures.ofSides(topTexture, sideTexture, bottomTexture));
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JBlockStates getBlockStates() {
    return JBlockStates.simple(getBlockModelId());
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public JModel getBlockModel() {
    return new JModel(parent).textures(textures);
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public String getTextureId(StockTextureAliases textureKey) {
    final ResourceLocation texture = TextureRegistry.getTexture(this, textureKey);
    if (texture != null) return texture.toString();
    for (StockTextureAliases textureKey0 = textureKey; textureKey0 != null; textureKey0 = textureKey0.getParent()) {
      String texture0 = textures.get(textureKey0.getId());
      if (texture0 == null) continue;
      return texture0;
    }
    return BlockResourceGenerator.super.getTextureId(textureKey);
  }
}
