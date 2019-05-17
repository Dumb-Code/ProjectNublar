package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.dumbcode.projectnublar.server.entity.component.EntityComponentStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class AgeComponent implements EntityComponent {
    public Map<ModelStage, Integer> tickStageMap = Maps.newHashMap();
    public int ageInTicks = 0;

    public ModelStage stage = ModelStage.ADULT;
    public float percentageStage = 1F;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("age", this.ageInTicks);

        NBTTagCompound tickMap = new NBTTagCompound();
        for (Map.Entry<ModelStage, Integer> entry : this.tickStageMap.entrySet()) {
            tickMap.setInteger(entry.getKey().getName(), entry.getValue());
        }

        compound.setTag("tick_map", tickMap);

        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.ageInTicks = compound.getInteger("age");

        this.tickStageMap.clear();
        NBTTagCompound tickMap = compound.getCompoundTag("tick_map");
        for (ModelStage stage : ModelStage.values()) {
            if(tickMap.hasKey(stage.getName(), Constants.NBT.TAG_ANY_NUMERIC)) {
                this.tickStageMap.put(stage, tickMap.getInteger(stage.getName()));

            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(this.ageInTicks);
        buf.writeShort(this.tickStageMap.size());
        for (Map.Entry<ModelStage, Integer> entry : this.tickStageMap.entrySet()) {
            buf.writeByte(entry.getKey().ordinal());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.ageInTicks = buf.readInt();
        this.tickStageMap.clear();
        short size = buf.readShort();
        for (int i = 0; i < size; i++) {
            this.tickStageMap.put(ModelStage.values()[buf.readByte()], buf.readInt());
        }
    }

    public ModelStage getStage() {
        return this.stage;
    }

    @Accessors(chain = true)
    @Setter
    public static class Storage implements EntityComponentStorage<AgeComponent> {

        private final Map<ModelStage, Integer> tickStageMap = Maps.newEnumMap(ModelStage.class);

        @Override
        public AgeComponent construct() {
            AgeComponent component = new AgeComponent();
            component.tickStageMap = this.tickStageMap;
            return component;
        }

        @Override
        public void readJson(JsonObject json) {
            JsonElement alive = json.get("alive_map");
            if(alive instanceof JsonObject) {
                JsonObject aliveobj = alive.getAsJsonObject();
                for (ModelStage stage : ModelStage.values()) {
                    if(JsonUtils.isJsonPrimitive(aliveobj, stage.getName())) {
                        this.tickStageMap.put(stage, aliveobj.getAsJsonPrimitive(stage.getName()).getAsInt());
                    }
                }
            }
        }

        @Override
        public void writeJson(JsonObject json) {
            JsonObject alive = new JsonObject();
            for (Map.Entry<ModelStage, Integer> entry : this.tickStageMap.entrySet()) {
                if(entry.getKey().getDefaultTickAge() != entry.getValue()) {
                    alive.addProperty(entry.getKey().getName(), entry.getValue());
                }
            }
            json.add("alive_map", alive);
        }
    }
}
