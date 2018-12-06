package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInfomation;
import net.dumbcode.projectnublar.server.dinosaur.data.EntityProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.SkeletalInformation;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;
import java.util.Random;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {
    @GameRegistry.ObjectHolder(ProjectNublar.MODID + ":missing")
    public static Dinosaur MISSING = null;

    private final ModelProperties modelProperties = new ModelProperties();
    private final ItemProperties itemProperties = new ItemProperties();
    private final EntityProperties entityProperties = new EntityProperties();
    private final SkeletalInformation skeletalInformation = new SkeletalInformation();
    private final DinosaurInfomation dinosaurInfomation = new DinosaurInfomation();

    private ModelContainer modelContainer;
    private ModelContainer noAnimationModelContainer;

    public Dinosaur() {

    }

    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getResourcePath());
    }

    public String getFormattedName() {
        return (this.getRegName().getResourceDomain().equals(ProjectNublar.MODID) ? this.getRegName().getResourcePath() : this.getRegName().toString()).toLowerCase().replace(":", "_");
    }

    public DinosaurEntity createEntity(World world) {
        DinosaurEntity entity = this.getEntityProperties().getEntityCreateFunction().apply(world);
        entity.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur = this;
        return entity;
    }

    @Nonnull //A quick nonnull registry name. Usefull to prevent complier warnings
    public ResourceLocation getRegName() {
        if(this.getRegistryName() == null) {
            throw new RuntimeException("Null Registry Name Found");
        }
        return this.getRegistryName();
    }

    public ResourceLocation getTextureLocation(DinosaurEntity entity) {
        ResourceLocation regname = getRegName();
        boolean male = entity.getOrExcept(EntityComponentTypes.GENDER).male;
        return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (male ? "male" : "female") + "_" + entity.getGrowthStage().name().toLowerCase(Locale.ROOT) + ".png");
    }

    public TextComponentTranslation createNameComponent() {
        return new TextComponentTranslation(getRegName().getResourceDomain()+".dino."+getRegName().getResourcePath()+".name");
    }

    public static class JsonAdapter implements JsonSerializer<Dinosaur>, JsonDeserializer<Dinosaur> {

        @Override
        public Dinosaur deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = element.getAsJsonObject();
            if(object.has("_is_example") && object.get("_is_example").getAsBoolean()) {
                return null;
            }
            Dinosaur dinosaur = new Dinosaur();
            ItemProperties readItemProperties = context.deserialize(object.get("item_attributes"), TypeToken.of(ItemProperties.class).getType());
            ModelProperties readModelProperties = context.deserialize(object.get("model_properties"), TypeToken.of(ModelProperties.class).getType());
            SkeletalInformation readSkeletalInformation = context.deserialize(object.get("skeletal_information"), TypeToken.of(SkeletalInformation.class).getType());
            EntityProperties readEntityProperties = context.deserialize(object.get("entity_properties"), TypeToken.of(EntityProperties.class).getType());
            dinosaur.getSkeletalInformation().copyFrom(readSkeletalInformation);
            dinosaur.getModelProperties().copyFrom(readModelProperties);
            dinosaur.getItemProperties().copyFrom(readItemProperties);
            dinosaur.getEntityProperties().copyFrom(readEntityProperties);
            return dinosaur;
        }

        @Override
        public JsonElement serialize(Dinosaur dino, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("model_properties", context.serialize(dino.modelProperties));
            object.add("item_attributes", context.serialize(dino.itemProperties));
            object.add("skeletal_information", context.serialize(dino.skeletalInformation));
            object.add("entity_properties", context.serialize(dino.entityProperties));
            return object;
        }
    }

    public static Dinosaur getRandom() {
        Collection<Dinosaur> from = ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection();
        Random rnd = new Random();
        int i = rnd.nextInt(from.size());
        return from.toArray(new Dinosaur[0])[i];

    }
}
