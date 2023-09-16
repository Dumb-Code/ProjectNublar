package net.dumbcode.projectnublar.server.runtimepack.generator.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;

import java.util.function.Function;

/**
 * This callback detects the environment type of the resource loaded. If you want to generate or add resource packs according to the resource loading environment, you can use this.
 *
 * @author SolidBlock
 * @since forge
 */
public final class RRPCallbackForge {
  /**
   * Register your type-specific resource pack at a higher priority than minecraft and mod resources.
   */
  public static final ImmutableList.Builder<Function<ResourcePackType, IResourcePack>> BEFORE_VANILLA = new ImmutableList.Builder<>();
  /**
   * Register your type-specific resource pack at a lower priority than minecraft and mod resources.
   */
  public static final ImmutableList.Builder<Function<ResourcePackType, IResourcePack>> AFTER_VANILLA = new ImmutableList.Builder<>();
}
