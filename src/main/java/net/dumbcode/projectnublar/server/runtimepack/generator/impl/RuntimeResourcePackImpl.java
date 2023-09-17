package net.dumbcode.projectnublar.server.runtimepack.generator.impl;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import net.dumbcode.projectnublar.server.runtimepack.generator.api.RuntimeResourcePack;
import net.dumbcode.projectnublar.server.runtimepack.generator.util.CallableFunction;
import net.dumbcode.projectnublar.server.runtimepack.generator.util.CountingInputStream;
import net.dumbcode.projectnublar.server.runtimepack.generator.util.UnsafeByteArrayOutputStream;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.lang.String.valueOf;


/**
 * @see RuntimeResourcePack
 */
public class RuntimeResourcePackImpl implements RuntimeResourcePack, IResourcePack {
  public static final ExecutorService EXECUTOR_SERVICE;
  /**
   * Whether to dump all resources as local files. It depends on the config file. By default, it is {@code false}.
   */
  public static final boolean DUMP;
  /**
   * Whether to print milliseconds used for data generation. By default, it is {@code false}.
   */
  public static final boolean DEBUG_PERFORMANCE;

  public static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping()
      .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
      .registerTypeHierarchyAdapter(Advancement.Builder.class, (JsonSerializer<Advancement.Builder>) (builder, type, jsonSerializationContext) -> builder.serializeToJson())
      .create();
  // if it works, don't touch it
  /**
   * @since BRRP 0.7.0, ARRP 0.6.2
   * Author: Devan-Kerman
   */
  static final Set<String> KEY_WARNINGS = Collections.newSetFromMap(new ConcurrentHashMap<>());
  // @formatter:on
  private static final Logger LOGGER = LogManager.getLogger(RuntimeResourcePackImpl.class);

  static {
    Properties properties = new Properties();
    // Number of threads of the executor service. By default, depends on the config of the device. It is used for async resources, and async data generation defined in `rrp:pregen` entrypoint.
    int processors = Math.max(Runtime.getRuntime().availableProcessors() / 2 - 1, 1);
    // Whether to dump all the resources as local files. By default, false.
    boolean dump = false;
    // Whether to print a notice of milliseconds used for data generation.
    boolean performance = false;
    properties.setProperty("threads", valueOf(processors));
    properties.setProperty("dump assets", "false");
    properties.setProperty("debug performance", "false");

    File file = FMLPaths.CONFIGDIR.get().resolve("rrp.properties").toFile();
    try (FileReader reader = new FileReader(file)) {
      properties.load(reader);
      processors = Integer.parseInt(properties.getProperty("threads"));
      dump = Boolean.parseBoolean(properties.getProperty("dump assets"));
      performance = Boolean.parseBoolean(properties.getProperty("debug performance"));
    } catch (Throwable t) {
      LOGGER.warn("Invalid config, creating new one!");
      //noinspection ResultOfMethodCallIgnored
      file.getParentFile().mkdirs();
      try (FileWriter writer = new FileWriter(file)) {
        properties.store(writer, "number of threads RRP should use for generating resources");
      } catch (IOException ex) {
        LOGGER.error("Unable to write to RRP config!", ex);
      }
    }
    EXECUTOR_SERVICE = Executors.newFixedThreadPool(processors, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("BRRP-Workers-%s").build());
    DUMP = dump;
    DEBUG_PERFORMANCE = performance;
    KEY_WARNINGS.add("filter");
    KEY_WARNINGS.add("language");
  }

  public final int packVersion;
  private final ResourceLocation id;
  private final Lock waiting = new ReentrantLock();
  private final Map<ResourceLocation, Supplier<byte[]>> data = new ConcurrentHashMap<>();
  private final Map<ResourceLocation, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
  private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();
  private boolean forbidsDuplicateResource = false;

  public RuntimeResourcePackImpl(ResourceLocation id) {
    this(id, 5);
  }

  public RuntimeResourcePackImpl(ResourceLocation id, int version) {
    this.packVersion = version;
    this.id = id;
  }

  private static byte[] serialize(Object object) {
    UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(ubaos, StandardCharsets.UTF_8);
    GSON.toJson(object, writer);
    try {
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return ubaos.getBytes();
  }

  public static ResourceLocation fix(ResourceLocation identifier, String prefix, String append) {
    return new ResourceLocation(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + append);
  }

  @Override
  public void setForbidsDuplicateResource(boolean b) {
    forbidsDuplicateResource = true;
  }

  @Override
  public void addRecoloredImage(ResourceLocation identifier, InputStream target, IntUnaryOperator operator) {
    this.addLazyResource(ResourcePackType.CLIENT_RESOURCES, fix(identifier, "textures", "png"), (i, r) -> {
      try {

        // optimize buffer allocation, input and output image after recoloring should be roughly the same size
        CountingInputStream is = new CountingInputStream(target);
        // repaint image
        BufferedImage base = ImageIO.read(is);
        BufferedImage recolored = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < base.getHeight(); y++) {
          for (int x = 0; x < base.getWidth(); x++) {
            recolored.setRGB(x, y, operator.applyAsInt(base.getRGB(x, y)));
          }
        }
        // write image
        UnsafeByteArrayOutputStream baos = new UnsafeByteArrayOutputStream(is.bytes());
        ImageIO.write(recolored, "png", baos);
        return baos.getBytes();
      } catch (Throwable e) {
        LOGGER.error("Failed to add resources:", e);
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public Future<byte[]> addAsyncResource(ResourcePackType type, ResourceLocation path, CallableFunction<ResourceLocation, byte[]> data) {
    Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> data.get(path));
    final Map<ResourceLocation, Supplier<byte[]>> sys = this.getSys(type);
    if (forbidsDuplicateResource && sys.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate resource id %s in runtime resource pack %s.", path, getName()));
    }
    sys.put(path, () -> {
      try {
        return future.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    });
    return future;
  }

  @Override
  public void addLazyResource(ResourcePackType type, ResourceLocation path, BiFunction<RuntimeResourcePack, ResourceLocation, byte[]> func) {
    final Map<ResourceLocation, Supplier<byte[]>> sys = this.getSys(type);
    if (forbidsDuplicateResource && sys.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate resource id %s in runtime resource pack %s.", path, getName()));
    }
    sys.put(path, new Memoized<>(func, path));
  }

  @Override
  public byte[] addResource(ResourcePackType type, ResourceLocation path, byte[] data) {
    final Map<ResourceLocation, Supplier<byte[]>> sys = this.getSys(type);
    if (forbidsDuplicateResource && sys.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate resource id %s in runtime resource pack %s.", path, getName()));
    }
    sys.put(path, Suppliers.ofInstance(data));
    return data;
  }

  @Override
  public Future<byte[]> addAsyncRootResource(String path, CallableFunction<String, byte[]> data) {
    if (forbidsDuplicateResource && root.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate root resource id %s in runtime resource pack %s!", path, getName()));
    }
    Future<byte[]> future = EXECUTOR_SERVICE.submit(() -> data.get(path));
    this.root.put(path, () -> {
      try {
        return future.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    });
    return future;
  }

  @Override
  public void addLazyRootResource(String path, BiFunction<RuntimeResourcePack, String, byte[]> data) {
    if (forbidsDuplicateResource && root.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate root resource id %s in runtime resource pack %s!", path, getName()));
    }
    this.root.put(path, new Memoized<>(data, path));
  }

  @Override
  public byte[] addRootResource(String path, byte[] data) {
    if (forbidsDuplicateResource && root.containsKey(path)) {
      throw new IllegalArgumentException(String.format("Duplicate root resource id %s in runtime resource pack %s!", path, getName()));
    }
    this.root.put(path, () -> data);
    return data;
  }

  @Override
  public byte[] addAsset(ResourceLocation id, byte[] data) {
    if (forbidsDuplicateResource && assets.containsKey(id)) {
      throw new IllegalArgumentException(String.format("Duplicate asset id %s in runtime resource pack %s!", id, getName()));
    }
    assets.put(id, Suppliers.ofInstance(data));
    return data;
  }

  @Override
  public byte[] addData(ResourceLocation id, byte[] data) {
    if (forbidsDuplicateResource && this.data.containsKey(id)) {
      throw new IllegalArgumentException(String.format("Duplicate data id %s in runtime resource pack %s!", id, getName()));
    }
    this.data.put(id, Suppliers.ofInstance(data));
    return data;
  }

  @Override
  public byte[] addTexture(ResourceLocation id, BufferedImage image) {
    UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", ubaos);
    } catch (IOException e) {
      throw new RuntimeException("impossible.", e);
    }
    return this.addAsset(fix(id, "textures", "png"), ubaos.getBytes());
  }

  @Override
  public byte[] addAdvancement(ResourceLocation id, Advancement.Builder advancement) {
    return this.addData(fix(id, "advancements", "json"), serialize(advancement));
  }

  @Override
  public Future<?> async(Consumer<RuntimeResourcePack> action) {
    this.lock();
    return EXECUTOR_SERVICE.submit(() -> {
      action.accept(this);
      this.waiting.unlock();
    });
  }

  @Override
  public void dumpDirect(Path output) {
    LOGGER.info("Dumping {}.", getName());
    // data dump time
    try {
      for (Map.Entry<String, Supplier<byte[]>> e : this.root.entrySet()) {
        Path root = output.resolve(e.getKey());
        Files.createDirectories(root.getParent());
        Files.write(root, e.getValue().get());
      }

      Path assets = output.resolve("assets");
      Files.createDirectories(assets);
      for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : this.assets.entrySet()) {
        this.write(assets, entry.getKey(), entry.getValue().get());
      }

      Path data = output.resolve("data");
      Files.createDirectories(data);
      for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : this.data.entrySet()) {
        this.write(data, entry.getKey(), entry.getValue().get());
      }
      LOGGER.info("Dumping {} finished.", getName());
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  @Override
  public void load(Path dir) throws IOException {
    try (Stream<Path> stream = Files.walk(dir)) {
      for (Path file : (Iterable<Path>) () -> stream.filter(Files::isRegularFile).map(dir::relativize).iterator()) {
        String s = file.toString();
        if (s.startsWith("assets")) {
          String path = s.substring("assets".length() + 1);
          this.load(path, this.assets, Files.readAllBytes(file));
        } else if (s.startsWith("data")) {
          String path = s.substring("data".length() + 1);
          this.load(path, this.data, Files.readAllBytes(file));
        } else {
          byte[] data = Files.readAllBytes(file);
          this.root.put(s, () -> data);
        }
      }
    }
  }

  @Override
  @Deprecated
  public void dump(File output) {
    this.dump(Paths.get(output.toURI()));
  }

  @Override
  public void dump(ZipOutputStream zos) throws IOException {
    this.lock();
    for (Map.Entry<String, Supplier<byte[]>> entry : this.root.entrySet()) {
      zos.putNextEntry(new ZipEntry(entry.getKey()));
      zos.write(entry.getValue().get());
      zos.closeEntry();
    }

    for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : this.assets.entrySet()) {
      ResourceLocation id = entry.getKey();
      zos.putNextEntry(new ZipEntry("assets/" + id.getNamespace() + "/" + id.getPath()));
      zos.write(entry.getValue().get());
      zos.closeEntry();
    }

    for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : this.data.entrySet()) {
      ResourceLocation id = entry.getKey();
      zos.putNextEntry(new ZipEntry("data/" + id.getNamespace() + "/" + id.getPath()));
      zos.write(entry.getValue().get());
      zos.closeEntry();
    }
    this.waiting.unlock();
  }

  @Override
  public void load(ZipInputStream stream) throws IOException {
    ZipEntry entry;
    while ((entry = stream.getNextEntry()) != null) {
      String s = entry.toString();
      if (s.startsWith("assets")) {
        String path = s.substring("assets".length() + 1);
        this.load(path, this.assets, this.read(entry, stream));
      } else if (s.startsWith("data")) {
        String path = s.substring("data".length() + 1);
        this.load(path, this.data, this.read(entry, stream));
      } else {
        byte[] data = this.read(entry, stream);
        this.root.put(s, () -> data);
      }
    }
  }

  @Override
  public ResourceLocation getId() {
    return this.id;
  }

  /**
   * pack.png and that's about it, I think/hope
   *
   * @param fileName the name of the file, can't be a path tho
   * @return the pack.png image as a stream
   */
  @Override
  public InputStream getRootResource(String fileName) {
    if (!fileName.contains("/") && !fileName.contains("\\")) {
      this.lock();
      Supplier<byte[]> supplier = this.root.get(fileName);
      if (supplier == null) {
        this.waiting.unlock();
        return null;
      }
      this.waiting.unlock();
      return new ByteArrayInputStream(supplier.get());
    } else {
      throw new IllegalArgumentException("File name can't be a path");
    }
  }

  @Override
  public InputStream getResource(ResourcePackType type, ResourceLocation id) {
    this.lock();
    Supplier<byte[]> supplier = this.getSys(type).get(id);
    if (supplier == null) {
      LOGGER.warn("No resource found for " + id);
      this.waiting.unlock();
      return null;
    }
    this.waiting.unlock();
    return new ByteArrayInputStream(supplier.get());
  }

  @Override
  public Collection<ResourceLocation> getResources(ResourcePackType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
    this.lock();
    Set<ResourceLocation> identifiers = new HashSet<>();
    for (ResourceLocation identifier : this.getSys(type).keySet()) {
      if (identifier.getNamespace().equals(namespace) && identifier.getPath().startsWith(prefix) && pathFilter.test(identifier.getPath())) {
        identifiers.add(identifier);
      }
    }
    this.waiting.unlock();
    return identifiers;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public boolean hasResource(ResourcePackType type, ResourceLocation id) {
    this.lock();
    boolean contains = this.getSys(type).containsKey(id);
    this.waiting.unlock();
    return contains;
  }

  @Override
  public Set<String> getNamespaces(ResourcePackType type) {
    this.lock();
    Set<String> namespaces = new HashSet<>();
    for (ResourceLocation identifier : this.getSys(type).keySet()) {
      namespaces.add(identifier.getNamespace());
    }
    this.waiting.unlock();
    return namespaces;
  }

  @Override
  public <T> T getMetadataSection(IMetadataSectionSerializer<T> metaReader) {
    InputStream stream = this.getRootResource("pack.mcmeta");
    if (stream != null) {
      return ResourcePack.getMetadataFromStream(metaReader, stream);
    } else {
      if (metaReader.getMetadataSectionName().equals("pack")) {
        JsonObject object = new JsonObject();
        object.addProperty("pack_format", this.packVersion);
        object.addProperty("description", "runtime resource pack");
        return metaReader.fromJson(object);
      }
      if (KEY_WARNINGS.add(metaReader.getMetadataSectionName())) {
        LOGGER.info("'" + metaReader.getMetadataSectionName() + "' is an unsupported metadata key");
      }
      return metaReader.fromJson(new JsonObject());
    }
  }

  @Override
  public String getName() {
    return "Runtime Resource Pack " + this.id.toString();
  }

  @Override
  public void close() {
    LOGGER.info("Closing rrp " + this.id);

    // lock
    this.lock();
    if (DUMP) {
      this.dump();
    }

    // unlock
    this.waiting.unlock();
  }

  protected byte[] read(ZipEntry entry, InputStream stream) throws IOException {
    byte[] data = new byte[Math.toIntExact(entry.getSize())];
    if (stream.read(data) != data.length) {
      throw new IOException("Zip stream was cut off! (maybe incorrect zip entry length? maybe u didn't flush your stream?)");
    }
    return data;
  }

  protected void load(String fullPath, Map<ResourceLocation, Supplier<byte[]>> map, byte[] data) {
    int sep = fullPath.indexOf('/');
    String namespace = fullPath.substring(0, sep);
    String path = fullPath.substring(sep + 1);
    map.put(new ResourceLocation(namespace, path), () -> data);
  }

  private void lock() {
    if (!this.waiting.tryLock()) {
      if (DEBUG_PERFORMANCE) {
        long start = System.currentTimeMillis();
        this.waiting.lock();
        long end = System.currentTimeMillis();
        LOGGER.warn("Waited " + (end - start) + "ms for lock in RRP: " + this.id);
      } else {
        this.waiting.lock();
      }
    }
  }

  private void write(Path dir, ResourceLocation identifier, byte[] data) {
    try {
      Path file = dir.resolve(identifier.getNamespace()).resolve(identifier.getPath());
      Files.createDirectories(file.getParent());
      try (OutputStream output = Files.newOutputStream(file)) {
        output.write(data);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Map<ResourceLocation, Supplier<byte[]>> getSys(ResourcePackType side) {
    return side == ResourcePackType.CLIENT_RESOURCES ? this.assets : this.data;
  }

  private class Memoized<T> implements Supplier<byte[]> {
    private final BiFunction<RuntimeResourcePack, T, byte[]> func;
    private final T path;
    private byte[] data;

    public Memoized(BiFunction<RuntimeResourcePack, T, byte[]> func, T path) {
      this.func = func;
      this.path = path;
    }

    @Override
    public byte[] get() {
      if (this.data == null) {
        this.data = func.apply(RuntimeResourcePackImpl.this, path);
      }
      return this.data;
    }
  }
}