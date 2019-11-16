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
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponent;
import net.dumbcode.dumblibrary.server.ecs.component.EntityComponentStorage;
import net.dumbcode.dumblibrary.server.ecs.component.additionals.RenderLocationComponent;
import net.dumbcode.dumblibrary.server.ecs.component.impl.AgeStage;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.List;

public class AgeComponent extends EntityComponent implements RenderLocationComponent {

    @Getter private List<AgeStage> orderedAges = Lists.newLinkedList();

    @Getter @Setter
    public int ageInTicks = 0;

    public AgeStage stage = AgeStage.MISSING;

    @Setter private float percentageStage = 1F;

    public void setRawStage(String stage) {
        for (AgeStage orderedAge : this.orderedAges) {
            if(stage.equals(orderedAge.getName())) {
                this.stage = orderedAge;
                break;
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
            this.orderedAges.add(new AgeStage(ageTag.getString("Name"), ageTag.getInteger("Time")));
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
        }
        buf.writeInt(this.orderedAges.indexOf(this.stage));
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.orderedAges.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.orderedAges.add(new AgeStage(ByteBufUtils.readUTF8String(buf), buf.readInt()));
        }
        this.stage = this.orderedAges.get(buf.readInt());
    }

    @NonNull
    public AgeStage getStage() {
        return this.stage;
    }

    @Override
    public void editLocations(ConfigurableLocation texture, ConfigurableLocation fileLocation) {
        fileLocation.addName(this.stage.getName(), 20);
        texture.addFolderName(this.stage.getName(), 20);
    }

    @Accessors(chain = true)
    @Setter
    @Getter
    public static class Storage implements EntityComponentStorage<AgeComponent> {

        private final List<AgeStage> orderedAges = Lists.newLinkedList();

        @Override
        public AgeComponent construct() {
            AgeComponent component = new AgeComponent();
            component.orderedAges = this.orderedAges;
            component.stage = this.orderedAges.get(0);
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
                this.orderedAges.add(new AgeStage(JsonUtils.getString(jsonObject, "name"), JsonUtils.getInt(jsonObject, "time")));
            }
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonArray ages = new JsonArray();
            for (AgeStage age : this.orderedAges) {
                JsonObject ageTag = new JsonObject();
                ageTag.addProperty("name", age.getName());
                ageTag.addProperty("time", age.getTime());

                ages.add(ageTag);
            }

            json.add("ordered_ages", ages);
        }
    }


}
