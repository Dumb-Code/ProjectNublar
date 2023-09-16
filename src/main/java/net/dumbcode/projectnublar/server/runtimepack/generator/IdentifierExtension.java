package net.dumbcode.projectnublar.server.runtimepack.generator;

import net.minecraft.util.ResourceLocation;

public class IdentifierExtension {
  /**
   * Append the string to the path of the identifier. The namespace will keep unchanged.
   *
   * @param prefix The string to be appended.
   * @return A new identifier.
   */
  public static ResourceLocation append(String prefix, ResourceLocation location) {
    return new ResourceLocation(location.getNamespace(), location.getPath() + prefix);
  }

  /**
   * Prepend the string to the path of the identifier. The namespace will keep unchanged.
   *
   * @param suffix The string to be prepended.
   * @return A new identifier.
   */
  public static ResourceLocation prepend(String suffix, ResourceLocation location) {
    return new ResourceLocation(location.getNamespace(), suffix + location.getPath());
  }

  /**
   * Prepend and append to the path of the identifier at the same time. The namespace will keep unchanged.
   *
   * @param prefix The string to be prepended.
   * @param suffix The string to be appended.
   * @return A new identifier.
   */
  public static ResourceLocation pend(String prefix, String suffix, ResourceLocation location) {
    return new ResourceLocation(location.getNamespace(), prefix + location.getPath() + suffix);
  }
}
