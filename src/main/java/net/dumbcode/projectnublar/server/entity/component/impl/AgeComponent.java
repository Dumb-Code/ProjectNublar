package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.server.ecs.ComponentAccess;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.CanBreedComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.ScaleAdjustmentComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.dumbcode.projectnublar.server.entity.ComponentHandler;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AgeComponent extends EntityComponent implements RenderLocationComponent, ScaleAdjustmentComponent, CanBreedComponent {

    @Getter private List<AgeStage> orderedAges = Lists.newLinkedList();

    @Getter @Setter
    public int ageInTicks = 0;

    public AgeStage stage = AgeStage.MISSING;

    @Setter private float percentageStage = 1F;

    public boolean setRawStage(String stage) {
        for (AgeStage orderedAge : this.orderedAges) {
            if(stage.equals(orderedAge.getName())) {
                this.stage = orderedAge;
                return true;
            }
        }
        return false;

    }

    public void resetStageTo(String stage) {
        if (this.setRawStage(stage)) {
            this.ageInTicks = 0;
            for (AgeStage age : this.orderedAges) {
                if(age == this.stage || age.getTime() < 0) {
                    break;
                }
                this.ageInTicks += age.getTime();
            }

        }
    }

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("Age", this.ageInTicks);
        compound.setString("CurrentAge", this.stage.getName());

        NBTTagList ages = new NBTTagList();
        for (AgeStage age : this.orderedAges) {
            NBTTagCompound ageTag = new NBTTagCompound();
            ageTag.setString("Name", age.getName());
            ageTag.setInteger("Time", age.getTime());
            ageTag.setString("ModelStage", age.getModelStage());

            ages.appendTag(ageTag);
        }

        compound.setTag("OrderedAges", ages);

        return super.serialize(compound);
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.orderedAges.clear();
        this.stage = AgeStage.MISSING;

        this.ageInTicks = compound.getInteger("Age");
        NBTTagList ages = compound.getTagList("OrderedAges", Constants.NBT.TAG_COMPOUND);
        for (NBTBase base : ages) {
            NBTTagCompound ageTag = (NBTTagCompound) base;
            this.orderedAges.add(new AgeStage(ageTag.getString("Name"), ageTag.getInteger("Time"), ageTag.getString("ModelStage")));
        }

        this.setRawStage(compound.getString("CurrentAge"));
        super.deserialize(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeShort(this.orderedAges.size());
        for (AgeStage s : this.orderedAges) {
            ByteBufUtils.writeUTF8String(buf, s.getName());
            buf.writeInt(s.getTime());
            ByteBufUtils.writeUTF8String(buf, s.getModelStage());
        }
        buf.writeInt(this.orderedAges.indexOf(this.stage));

        buf.writeInt(this.ageInTicks);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.orderedAges.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.orderedAges.add(new AgeStage(ByteBufUtils.readUTF8String(buf), buf.readInt(), ByteBufUtils.readUTF8String(buf)));
        }
        this.stage = this.orderedAges.get(buf.readInt());

        this.ageInTicks = buf.readInt();
    }

    @NonNull
    public AgeStage getStage() {
        return this.stage;
    }

    @NonNull
    public Optional<AgeStage> getModelState() {
        String stage = this.stage.getModelStage();
        return this.orderedAges.stream().filter(p -> p.getName().equals(stage)).findFirst();
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        fileLocation.addName(this.stage.getModelStage(), 20);
        texture.addFolderName(this.stage.getModelStage(), 20);
    }

    @Override
    public void applyScale(Consumer<Supplier<Float>> scale) {
        scale.accept(() -> {
            if(this.stage.getTime() >= 0) {
                return 0.5F + this.percentageStage * 0.5F;
            }
            return 1F;
        });
    }

    @Override
    public boolean canBreedWith(ComponentAccess otherEntity) {
        return this.stage.isCanBreed() && otherEntity.get(ComponentHandler.AGE).map(a -> a.stage.isCanBreed()).orElse(true);
    }

    @Setter
    @Getter
    @Accessors(chain = true)
    public static class Storage implements EntityComponentStorage<AgeComponent> {

        private final List<AgeStage> orderedAges = Lists.newLinkedList();
        private String defaultStageName = "";

        @Override
        public AgeComponent constructTo(AgeComponent component) {
            component.orderedAges = this.orderedAges;
            component.stage = this.orderedAges.stream().filter(s -> s.getName().equals(this.defaultStageName)).findFirst().orElse(this.orderedAges.get(0));

            for (AgeStage age : this.orderedAges) {
                if(age == component.stage || age.getTime() < 0) {
                    break;
                }
                component.ageInTicks += age.getTime();
            }

            return component;
        }

        public Storage addStage(AgeStage stage) {
            this.orderedAges.add(stage);
            return this;
        }

        @Override
        public void readJson(JsonObject json) {

            this.orderedAges.clear();
            JsonArray ages = JsonUtils.getJsonArray(json, "ordered_ages");
            for (JsonElement element : ages) {
                JsonObject jsonObject = JsonUtils.getJsonObject(element, "ordered_ages_element");
                this.orderedAges.add(new AgeStage(JsonUtils.getString(jsonObject, "name"), JsonUtils.getInt(jsonObject, "time"), JsonUtils.getString(json, "model_stage")));
            }
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonArray ages = new JsonArray();
            for (AgeStage age : this.orderedAges) {
                JsonObject ageTag = new JsonObject();
                ageTag.addProperty("name", age.getName());
                ageTag.addProperty("time", age.getTime());
                ageTag.addProperty("model_stage", age.getModelStage());

                ages.add(ageTag);
            }

            json.add("ordered_ages", ages);
        }
    }


}
