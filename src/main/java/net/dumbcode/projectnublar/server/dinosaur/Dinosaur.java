package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;
import net.dumbcode.projectnublar.server.dinosaur.data.*;
import net.dumbcode.projectnublar.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentMap;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.projectnublar.server.entity.component.impl.GenderComponent;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    private final DinosaurInformation dinosaurInfomation = new DinosaurInformation();

    @SideOnly(Side.CLIENT)
    private ModelContainer<DinosaurEntity, ModelStage> modelContainer;
    @SideOnly(Side.CLIENT)
    private ModelContainer<DinosaurEntity, ModelStage> noAnimationModelContainer;
    private DinosaurEntitySystemInfo systemInfo;

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
        entity.getOrExcept(EntityComponentTypes.DINOSAUR).setDinosaur(this);
        return entity;
    }

    public void setProperties(EntityComponentMap map) {
        map.get(EntityComponentTypes.AGE).ifPresent(c -> c.tickStageMap = this.getEntityProperties().getTickStageMap());
        map.get(EntityComponentTypes.METABOLISM).ifPresent(c -> {
            c.food = this.getEntityProperties().getMaxFood();
            c.water = this.getEntityProperties().getMaxWater();
            c.foodRate = this.getEntityProperties().getFoodRate();
            c.waterRate = this.getEntityProperties().getWaterRate();

            c.diet = this.getEntityProperties().getDiet().copy();
            c.foodSmellDistance = this.getEntityProperties().getDistanceSmellFood();
        });
        map.get(EntityComponentTypes.ANIMATION).ifPresent(c -> {
            c.modelGetter = e -> {
                ModelStage stage = this.getSystemInfo().defaultStage();
                AgeComponent component = ((ComponentAccess) e).getOrNull(EntityComponentTypes.AGE);
                if(component != null) {
                    stage = component.stage;
                    if (!this.getSystemInfo().allAcceptedStages().contains(stage)) {
                        stage = this.getSystemInfo().defaultStage();
                    }
                }
                ResourceLocation regname = this.getRegName();
                return new ResourceLocation(regname.getResourceDomain(), "models/entities/" + regname.getResourcePath() + "/" + stage.getName().toLowerCase(Locale.ROOT) + "/" + this.getModelProperties().getMainModelMap().get(stage));
            };
            c.info = this.getSystemInfo();
        });
        map.get(EntityComponentTypes.HERD).ifPresent(c -> c.acceptedEntitiy = e -> e instanceof ComponentAccess && ((ComponentAccess) e).get(EntityComponentTypes.DINOSAUR).map(d -> d.getDinosaur() == this).orElse(false));
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
