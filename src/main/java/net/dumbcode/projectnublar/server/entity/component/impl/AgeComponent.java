package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.common.util.Constants;

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
    public CompoundTag serialize(CompoundTag compound) {
        compound.putInt("Age", this.ageInTicks);
        compound.putString("CurrentAge", this.stage.getName());

        ListNBT ages = new ListNBT();
        for (AgeStage age : this.orderedAges) {
            CompoundTag ageTag = new CompoundTag();
            ageTag.putString("Name", age.getName());
            ageTag.putInt("Time", age.getTime());
            ageTag.putString("ModelStage", age.getModelStage());

            ageTag.putBoolean("CanBreed", age.isCanBreed());

            ages.add(ageTag);
        }

        compound.put("OrderedAges", ages);

        return super.serialize(compound);
    }

    @Override
    public void deserialize(CompoundTag compound) {
        this.orderedAges.clear();
        this.stage = AgeStage.MISSING;

        this.ageInTicks = compound.getInt("Age");
        ListNBT ages = compound.getList("OrderedAges", Constants.NBT.TAG_COMPOUND);
        for (INBT base : ages) {
            CompoundTag ageTag = (CompoundTag) base;
            this.orderedAges.add(new AgeStage(ageTag.getString("Name"), ageTag.getInt("Time"), ageTag.getString("ModelStage")).setCanBreed(ageTag.getBoolean("CanBreed")));
        }

        this.setRawStage(compound.getString("CurrentAge"));
        super.deserialize(compound);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeShort(this.orderedAges.size());
        for (AgeStage s : this.orderedAges) {
            buf.writeUtf(s.getName());
            buf.writeInt(s.getTime());
            buf.writeUtf(s.getModelStage());
            buf.writeBoolean(s.isCanBreed());
        }
        buf.writeInt(this.orderedAges.indexOf(this.stage));

        buf.writeInt(this.ageInTicks);
    }

    @Override
    public void deserialize(FriendlyByteBuf buf) {
        this.orderedAges.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.orderedAges.add(new AgeStage(buf.readUtf(), buf.readInt(), buf.readUtf()).setCanBreed(buf.readBoolean()));
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
        public void constructTo(AgeComponent component) {
            component.orderedAges = this.orderedAges;
            component.stage = this.orderedAges.stream().filter(s -> s.getName().equals(this.defaultStageName)).findFirst().orElse(this.orderedAges.get(0));

            for (AgeStage age : this.orderedAges) {
                if(age == component.stage || age.getTime() < 0) {
                    break;
                }
                component.ageInTicks += age.getTime();
            }

        }

        public Storage addStage(AgeStage stage) {
            this.orderedAges.add(stage);
            return this;
        }

        @Override
        public void readJson(JsonObject json) {

            this.orderedAges.clear();
            JsonArray ages = JSONUtils.getAsJsonArray(json, "ordered_ages");
            for (JsonElement element : ages) {
                JsonObject jsonObject = element.getAsJsonObject();
                this.orderedAges.add(new AgeStage(JSONUtils.getAsString(jsonObject, "name"), JSONUtils.getAsInt(jsonObject, "time"), JSONUtils.getAsString(json, "model_stage")));
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
