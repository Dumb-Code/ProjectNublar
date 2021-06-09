package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.server.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentAttacher;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentType;
import net.dumbcode.dumblibrary.server.ecs.component.impl.ModelComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.EntityHandler;
import net.dumbcode.projectnublar.server.entity.component.impl.DinosaurDropsComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.MoodComponent;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;

@Getter
@Setter
public class Dinosaur extends ForgeRegistryEntry<Dinosaur> implements Comparable<Dinosaur> {

    public static final String CHILD_AGE = "child";
    public static final String ADULT_AGE = "adult";
    public static final String SKELETON_AGE = "skeleton";

    private static final Random RANDOM = new Random();

    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getPath());
    }
    private final ItemProperties itemProperties = new ItemProperties();
    private final DinosaurInformation dinosaurInfomation = new DinosaurInformation();

    private final EntityComponentAttacher attacher = new EntityComponentAttacher();

    private Map<String, AnimationContainer> modelContainer = Maps.newHashMap();

    public String getFormattedName() {
        return (this.getRegName().getNamespace().equals(ProjectNublar.MODID) ? this.getRegName().getPath() : this.getRegName().toString()).toLowerCase().replace(":", "_");
    }

    /**
     * Creates a dinosaur ecs with a default component config.
     *
     * @param world current world.
     * @return new dinosaur ecs.
     */
    public DinosaurEntity createEntity(World world) {
        return createEntity(world, null);
    }

    /**
     * Creates a dinosaur ecs with a custom component config.
     * @param world current world.
     * @param config custom config.
     * @return customized dinosaur ecs.
     */
    public DinosaurEntity createEntity(World world, @Nullable EntityComponentAttacher.ConstructConfiguration config) {
        if(config == null) {
            config = this.attacher.getDefaultConfig();
        }
        DinosaurEntity entity = new DinosaurEntity(EntityHandler.DINOSAUR.get(), world);
        entity.getOrExcept(ComponentHandler.DINOSAUR).setDinosaur(this);
        config.attachAll(entity);
        return entity;
    }
//
    public <T extends EntityComponent> void addEmptyComponent(RegistryObject<EntityComponentType<T, ?>> supplier) {
        this.addComponent(supplier.get());
    }


    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(RegistryObject<EntityComponentType<T, S>> supplier) {
        return this.addComponent(supplier.get());
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, S> type) {
        return this.attacher.addComponent(type); //delegate
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponentWithOverride(EntityComponentType<T, ?> type, EntityComponentType.StorageOverride<T, S> override) {
        return this.attacher.addComponent(type, override); //delegate
    }



    /**
     * Returns the registry name of the dinosaur if it exists,
     * otherwise throws a runtime exception.
     *
     * @return registry name.
     */
    @Nonnull
    public ResourceLocation getRegName() {
        if(this.getRegistryName() == null) {
            throw new NullPointerException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }

    public String createNameTranslationKey() {
        return getRegName().getNamespace()+".dino."+getRegName().getPath()+".name";
    }

    public TranslationTextComponent createNameComponent() {
        return new TranslationTextComponent(this.createNameTranslationKey());
    }

    public void attachDefaultComponents() {
        //Should be overpriced
    }

    /**
     * Gets a random dinosaur from the
     * available set of dinosaurs.
     * @return random dinosaur.
     */
    public static Dinosaur getRandom() {
        return getRandom(RANDOM);

    }

    public static Dinosaur getRandom(Random random) {
        Collection<Dinosaur> from = DinosaurHandler.getRegistry().getValues();
        int i = random.nextInt(from.size());
        return from.toArray(new Dinosaur[0])[i];

    }

    @Override
    public int compareTo(Dinosaur o) {
        return this.getRegistryName().compareTo(o.getRegistryName());
    }

    public static class JsonAdapter implements JsonSerializer<Dinosaur>, JsonDeserializer<Dinosaur> {

        @Override
        public Dinosaur deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            Dinosaur dinosaur = new Dinosaur();
            JsonObject object = element.getAsJsonObject();
            dinosaur.attacher.readFromJson(JSONUtils.getAsJsonArray(object, "entity_info"));
            return dinosaur;
        }

        @Override
        public JsonElement serialize(Dinosaur dino, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("entity_info", dino.attacher.writeToJson(new JsonArray()));
            return object;
        }
    }
}
