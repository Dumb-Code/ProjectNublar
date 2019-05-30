package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.projectnublar.server.dinosaur.data.ModelProperties;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.*;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.GenderComponent;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Random;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {

    private final ModelProperties modelProperties = new ModelProperties();
    private final ItemProperties itemProperties = new ItemProperties();
    private final DinosaurInformation dinosaurInfomation = new DinosaurInformation();

    private final EntityComponentAttacher attacher = new EntityComponentAttacher();

    @SideOnly(Side.CLIENT)
    private ModelContainer<DinosaurEntity, ModelStage> modelContainer;
    @SideOnly(Side.CLIENT)
    private ModelContainer<DinosaurEntity, ModelStage> noAnimationModelContainer;
    private DinosaurEntitySystemInfo systemInfo = new DinosaurEntitySystemInfo(this);

    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getResourcePath());
    }

    public String getFormattedName() {
        return (this.getRegName().getResourceDomain().equals(ProjectNublar.MODID) ? this.getRegName().getResourcePath() : this.getRegName().toString()).toLowerCase().replace(":", "_");
    }

    public DinosaurEntity createEntity(World world) {
        return createEntity(world, null);
    }

    public DinosaurEntity createEntity(World world, @Nullable EntityComponentAttacher.ConstructConfiguration config) {
        if(config == null) {
            config = this.attacher.getDefaultConfig();
        }
        DinosaurEntity entity = new DinosaurEntity(world);
        entity.getOrExcept(EntityComponentTypes.DINOSAUR).dinosaur = this;
        config.attachAll(entity);
        return entity;
    }

    public <T extends EntityComponent, S extends EntityComponentStorage<T>> S addComponent(EntityComponentType<T, S> type) {
        return this.attacher.addComponent(type); //delegate
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
        GenderComponent gcomp = entity.getOrNull(EntityComponentTypes.GENDER);
        String name = entity.get(EntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(ModelStage.ADULT).getName();
        if(gcomp != null) {
            return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + (gcomp.male ? "male" : "female") + "_" + name + ".png");
        }
        return new ResourceLocation(regname.getResourceDomain(), "textures/entities/" + regname.getResourcePath() + "/" + name + ".png");
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
            dinosaur.getModelProperties().copyFrom(readModelProperties);
            dinosaur.getItemProperties().copyFrom(readItemProperties);
            return dinosaur;
        }

        @Override
        public JsonElement serialize(Dinosaur dino, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("model_properties", context.serialize(dino.modelProperties));
            object.add("item_attributes", context.serialize(dino.itemProperties));
            return object;
        }
    }

    public void attachDefaultComponents() {
    }

    public static Dinosaur getRandom() {
        Collection<Dinosaur> from = ProjectNublar.DINOSAUR_REGISTRY.getValuesCollection();
        Random rnd = new Random();
        int i = rnd.nextInt(from.size());
        return from.toArray(new Dinosaur[0])[i];

    }


}
