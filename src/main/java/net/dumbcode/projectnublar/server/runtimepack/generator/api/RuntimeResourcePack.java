package net.dumbcode.projectnublar.server.runtimepack.generator.api;

import com.google.gson.Gson;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.dumbcode.projectnublar.server.runtimepack.generator.impl.RuntimeResourcePackImpl;
import net.dumbcode.projectnublar.server.runtimepack.generator.util.CallableFunction;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntUnaryOperator;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * A resource pack whose assets and data are evaluated at runtime.
 * <p>
 * After creating a runtime resource pack, you should register it in {@link RRPCallbackForge} so that it can take effect when loading resources.
 *
 * @see RRPCallbackForge
 */
@SuppressWarnings("unused")
public interface RuntimeResourcePack extends IResourcePack {
  /**
   * The default output path to dump resources.
   */
  Path DEFAULT_OUTPUT = Paths.get("rrp");
  Gson GSON = RuntimeResourcePackImpl.GSON;

  /**
   * Create a new runtime resource pack with the default supported resource pack version
   */
  static RuntimeResourcePack create(String id) {
    return new RuntimeResourcePackImpl(new ResourceLocation(id));
  }

  /**
   * Create a new runtime resource pack with the specified resource pack version
   */
  static RuntimeResourcePack create(String id, int version) {
    return new RuntimeResourcePackImpl(new ResourceLocation(id), version);
  }

  /**
   * Create a new runtime resource pack with the default supported resource pack version
   */
  static RuntimeResourcePack create(ResourceLocation id) {
    return new RuntimeResourcePackImpl(id);
  }

  /**
   * Create a new runtime resource pack with the specified resource pack version
   */
  static RuntimeResourcePack create(ResourceLocation id, int version) {
    return new RuntimeResourcePackImpl(id, version);
  }

  static ResourceLocation id(String string) {
    return new ResourceLocation(string);
  }

  static ResourceLocation id(String namespace, String path) {
    return new ResourceLocation(namespace, path);
  }

  /**
   * Set this to {@code true} will make it throw an exception when a duplicate resource is added.
   */
  default void setForbidsDuplicateResource(boolean b) {
    // subclasses should override this.
  }

  /**
   * Reads, clones, and recolors the texture at the given path, and puts the newly created image in the given id.
   * <p>
   * <b>If your resource pack is registered at a higher priority than where you expect the texture to be in, Minecraft will
   * be unable to find the asset you are looking for.</b>
   *
   * @param identifier the place to put the new texture
   * @param target     the input stream of the original texture
   * @param pixel      the pixel recolorer
   */
  void addRecoloredImage(ResourceLocation identifier, InputStream target, IntUnaryOperator pixel);

  /**
   * Add an async resource, which is evaluated off-thread, and does not hold all resource retrieval unlike.
   *
   * @see #async(Consumer)
   */
  Future<byte[]> addAsyncResource(ResourcePackType type,
                                  ResourceLocation identifier,
                                  CallableFunction<ResourceLocation, byte[]> data);

  /**
   * Add resource that is lazily evaluated, which is, evaluated only when required to get, and will not be evaluated again if required to get again.
   */
  void addLazyResource(ResourcePackType type, ResourceLocation path, BiFunction<RuntimeResourcePack, ResourceLocation, byte[]> data);

  /**
   * Add a raw resource to the runtime resource pack.
   */

  byte[] addResource(ResourcePackType type, ResourceLocation path, byte[] data);

  /**
   * Add an async root resource, which is evaluated off-thread and does not hold all resource retrieval unlike.
   * <p>
   * A root resource is something like pack.png, pack.mcmeta, etc. By default, ARRP generates a default mcmeta.
   *
   * @see #async(Consumer)
   */
  Future<byte[]> addAsyncRootResource(String path,
                                      CallableFunction<String, byte[]> data);

  /**
   * Add a root resource that is lazily evaluated.
   * <p>
   * A root resource is something like pack.png, pack.mcmeta, etc. By default, ARRP generates a default mcmeta.
   */
  void addLazyRootResource(String path, BiFunction<RuntimeResourcePack, String, byte[]> data);

  /**
   * Add a raw resource to the root path
   * <p>
   * A root resource is something like pack.png, pack.mcmeta, etc. By default, ARRP generates a default mcmeta.
   */

  byte[] addRootResource(String path, byte[] data);

  /**
   * Add a custom client-side resource.
   */
  byte[] addAsset(ResourceLocation id, byte[] data);

  /**
   * Add a custom server data.
   */
  byte[] addData(ResourceLocation id, byte[] data);


  /**
   * Adds a texture png.
   * <p>
   * {@code ".png"} is automatically appended to the path.
   */

  byte[] addTexture(ResourceLocation id, BufferedImage image);

  /**
   * Add an advancement to the runtime resource pack.
   * <p>
   * The extension {@code ".json"} is automatically appended to the path.
   *
   * @param id          The {@linkplain ResourceLocation identifier} of the advancement.
   * @param advancement The advancement to be added.
   */
  byte[] addAdvancement(ResourceLocation id, Advancement.Builder advancement);

  /**
   * Invokes the action on the RRP executor. RRPs are thread-safe, so you can create expensive assets here. All resources
   * are blocked until all async tasks are completed.
   * <p>
   * Calling in this function from itself will result in an infinite loop
   *
   * @see #addAsyncResource(ResourcePackType, ResourceLocation, CallableFunction)
   */
  Future<?> async(Consumer<RuntimeResourcePack> action);

  /**
   * Write the runtime resource pack as local files, as if it is a regular resource pack or data pack, making you available to directly visit its content.
   */
  default void dump() {
    this.dump(DEFAULT_OUTPUT);
  }

  /**
   * Write the runtime resource pack as local files, as if it is a regular resource pack or data pack, making you available to directly visit its content.
   *
   * @param path The path to write the resource pack directly.
   */
  void dumpDirect(Path path);

  /**
   * Load a regular resource pack or data pack from a local path, and convert into a runtime resource pack.
   *
   * @param path The path of the regular resource pack or data pack.
   * @throws IOException if thrown when reading files.
   */
  void load(Path path) throws IOException;

  /**
   * Write the runtime resource pack as local files, making you available to directly visit its content.
   *
   * @deprecated use {@link #dump(Path)}
   */
  @Deprecated
  void dump(File file);

  /**
   * Write the runtime resource pack as local files, as if it is a regular resource pack or data pack, making you available to directly visit its content.
   *
   * @param path The path to write the resource pack. In the path, the folder named with identifier will be created.
   */
  default void dump(Path path) {
    Path folder = path.resolve(getId().getPath());
    this.dumpDirect(folder);
  }

  /**
   * Write the runtime resource pack as a local zip file, making you available to directly visit its content.
   *
   * @see ByteBufOutputStream
   */
  void dump(ZipOutputStream stream) throws IOException;

  /**
   * Load a regular resource pack or data pack from a zip file, and convert it to this runtime resource pack.
   *
   * @see ByteBufInputStream
   */
  void load(ZipInputStream stream) throws IOException;

  ResourceLocation getId();
}