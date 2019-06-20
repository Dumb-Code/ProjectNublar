package net.dumbcode.projectnublar.server.dinosaur;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.animation.ModelContainer;
import net.dumbcode.dumblibrary.server.entity.component.*;
import net.dumbcode.dumblibrary.server.info.AnimationSystemInfo;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.animation.DinosaurEntitySystemInfo;
import net.dumbcode.projectnublar.server.dinosaur.data.DinosaurInformation;
import net.dumbcode.projectnublar.server.dinosaur.data.ItemProperties;
import net.dumbcode.dumblibrary.server.entity.ComponentAccess;
import net.dumbcode.projectnublar.server.entity.DinosaurEntity;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.NublarEntityComponentTypes;
import net.dumbcode.projectnublar.server.entity.component.impl.AgeComponent;
import net.dumbcode.dumblibrary.server.entity.component.impl.AnimationComponent;
import net.dumbcode.dumblibrary.server.entity.component.impl.GenderComponent;
import net.dumbcode.projectnublar.server.utils.StringUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

@Getter
@Setter
public class Dinosaur extends IForgeRegistryEntry.Impl<Dinosaur> {

    private final ItemProperties itemProperties = new ItemProperties();
    private final DinosaurInformation dinosaurInfomation = new DinosaurInformation();

    private final EntityComponentAttacher attacher = new EntityComponentAttacher();

    private Map<ModelStage, ModelContainer<DinosaurEntity>> modelContainer = Maps.newEnumMap(ModelStage.class);

    private Map<ModelStage, DinosaurEntitySystemInfo> systemInfo = Maps.newEnumMap(ModelStage.class);

    private List<ModelStage> activeModels = Lists.newArrayList(); //todo : seriailzie


    public Dinosaur() {
        for (ModelStage value : ModelStage.values()) {
            this.systemInfo.put(value, new DinosaurEntitySystemInfo(this, value));
        }
    }

    public String getOreSuffix() {
        return StringUtils.toCamelCase(getRegName().getPath());
    }

    public String getFormattedName() {
        return (this.getRegName().getNamespace().equals(ProjectNublar.MODID) ? this.getRegName().getPath() : this.getRegName().toString()).toLowerCase().replace(":", "_");
    }

    public DinosaurEntity createEntity(World world) {
        return createEntity(world, null);
    }

    public DinosaurEntity createEntity(World world, @Nullable EntityComponentAttacher.ConstructConfiguration config) {
        if(config == null) {
            config = this.attacher.getDefaultConfig();
        }
        DinosaurEntity entity = new DinosaurEntity(world);
        entity.getOrExcept(NublarEntityComponentTypes.DINOSAUR).dinosaur = this;
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
        String name = entity.get(NublarEntityComponentTypes.AGE).map(AgeComponent::getStage).orElse(ModelStage.ADULT).getName();
        if(gcomp != null) {
            return new ResourceLocation(regname.getNamespace(), "textures/entities/" + regname.getPath() + "/" + (gcomp.male ? "male" : "female") + "_" + name + ".png");
        }
        return new ResourceLocation(regname.getNamespace(), "textures/entities/" + regname.getPath() + "/" + name + ".png");
    }

    public TextComponentTranslation createNameComponent() {
        return new TextComponentTranslation(getRegName().getNamespace()+".dino."+getRegName().getPath()+".name");
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
            dinosaur.getItemProperties().copyFrom(readItemProperties);
            return dinosaur;
        }

        @Override
        public JsonElement serialize(Dinosaur dino, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
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

    public static class DinosaurModelGetter extends AnimationComponent.ModelGetter<DinosaurEntity> {

        private Dinosaur dinosaur;

        public DinosaurModelGetter(Dinosaur dinosaur) {
            super(dinosaur.getRegistryName());
            this.dinosaur = dinosaur;
        }

        @Override
        public ResourceLocation getLocation(DinosaurEntity entity){
            ModelStage stage = ModelStage.ADULT;
            AgeComponent component = ((ComponentAccess) entity).getOrNull(NublarEntityComponentTypes.AGE);
            if(component != null) {
                stage = component.stage;
            }
            ResourceLocation regname = this.dinosaur.getRegName();
            return new ResourceLocation(regname.getNamespace(), "models/entities/" + regname.getPath() + "/" + stage.getName().toLowerCase(Locale.ROOT) + "/" + regname.getPath() + "_" + stage.getName());
        }

        @Override
        public AnimationSystemInfo<DinosaurEntity> getInfo(DinosaurEntity entity) {
            return this.dinosaur.getSystemInfo().get(entity.getState());
        }

        @Override
        public NBTTagCompound serialize(NBTTagCompound compound) {
            compound.setString("id", this.dinosaur.getRegName().toString());
            return compound;
        }

        @Override
        public void deserialize(NBTTagCompound compound) {
            this.dinosaur = ProjectNublar.DINOSAUR_REGISTRY.getValue(new ResourceLocation(compound.getString("id")));
        }

        @Override
        public void serialize(ByteBuf buf) {
            ByteBufUtils.writeRegistryEntry(buf, this.dinosaur);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            this.dinosaur = ByteBufUtils.readRegistryEntry(buf, ProjectNublar.DINOSAUR_REGISTRY);
        }
    }


}
