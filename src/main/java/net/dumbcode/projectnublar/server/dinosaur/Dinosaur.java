package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.AnimationContainer;
import net.dumbcode.dumblibrary.server.entity.component.*;
import net.dumbcode.dumblibrary.server.entity.component.impl.AgeStage;
import net.dumbcode.dumblibrary.server.entity.component.impl.GenderComponent;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {

    public static final String ADULT_AGE = "adult";
    public static final String SKELETON_AGE = "skeleton";

    private final ItemProperties itemProperties = new ItemProperties();
    private final DinosaurInformation dinosaurInfomation = new DinosaurInformation();
    private final EntityComponentAttacher attacher = new EntityComponentAttacher();

    private Map<String, AnimationContainer> modelContainer = Maps.newHashMap();
    private List<ModelStage> activeModels = Lists.newArrayList(); //todo : serialize


    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getPath());
    }

    public String getFormattedName() {
        return (this.getRegName().getNamespace().equals(ProjectNublar.MODID) ? this.getRegName().getPath() : this.getRegName().toString()).toLowerCase().replace(":", "_");
    }

    /**
     * Creates a dinosaur entity with a default component config.
     *
     * @param world current world.
     * @return new dinosaur entity.
     */
    public DinosaurEntity createEntity(World world) {
        return createEntity(world, null);
    }

    /**
     * Creates a dinosaur entity with a custom component config.
     * @param world current world.
     * @param config custom config.
     * @return customized dinosaur entity.
     */
    public DinosaurEntity createEntity(World world, @Nullable EntityComponentAttacher.ConstructConfiguration config) {
        if(config == null) {
            config = this.attacher.getDefaultConfig();
        }
        DinosaurEntity entity = new DinosaurEntity(world);
        entity.getOrExcept(ComponentHandler.DINOSAUR).dinosaur = this;
        config.attachAll(entity);
        return entity;
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, S> type) {
        return this.attacher.addComponent(type); //delegate
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, ?> type, EntityComponentType.StorageOverride<T, S> override) {
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
            throw new RuntimeException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }

    /**
     * Gets the texture location from the resources folder
     * for this dinosaur.
     * @param entity dinosaur entity.
     * @return Resource location of the entity texture.
     */
    public ResourceLocation getTextureLocation(DinosaurEntity entity) {
        ResourceLocation regname = getRegName();
        GenderComponent gcomp = entity.getOrNull(EntityComponentTypes.GENDER);
        String name = entity.get(ComponentHandler.AGE).map(AgeComponent::getStage).orElse(AgeStage.MISSING).getName();
        if(gcomp != null) {
            return new ResourceLocation(regname.getNamespace(), "textures/entities/" + regname.getPath() + "/" + (gcomp.male ? "male" : "female") + "_" + name + ".png");
        }
        return new ResourceLocation(regname.getNamespace(), "textures/entities/" + regname.getPath() + "/" + name + ".png");
    }

    public TextComponentTranslation createNameComponent() {
        return new TextComponentTranslation(getRegName().getNamespace()+".dino."+getRegName().getPath()+".name");
    }

    public void attachDefaultComponents() {
    }

    /**
     * Gets a random dinosaur from the
     * available set of dinosaurs.
     * @return random dinosaur.
     */
    public static Dinosaur getRandom() {
        Collection<Dinosaur> from = ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection();
        Random rnd = new Random();
        int i = rnd.nextInt(from.size());
        return from.toArray(new Dinosaur[0])[i];

    }

    public static class JsonAdapter implements JsonSerializer<Dinosaur>, JsonDeserializer<Dinosaur> {

        @Override
        public Dinosaur deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) {
            Dinosaur dinosaur = new Dinosaur();
            JsonObject object = element.getAsJsonObject();
            ItemProperties readItemProperties = context.deserialize(object.get("item_attributes"), TypeToken.of(ItemProperties.class).getType());
            dinosaur.attacher.readFromJson(JsonUtils.getJsonArray(object, "entity_info"));
            dinosaur.getItemProperties().copyFrom(readItemProperties);
            return dinosaur;
        }

        @Override
        public JsonElement serialize(Dinosaur dino, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("item_attributes", context.serialize(dino.itemProperties));
            object.add("entity_info", dino.attacher.writeToJson(new JsonArray()));
            return object;
        }
    }
}
