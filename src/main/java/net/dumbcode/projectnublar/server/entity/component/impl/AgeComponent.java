package net.dumbcode.projectnublar.server.entity.component.impl;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class AgeComponent implements EntityComponent {
    public Map<ModelStage, Integer> tickStageMap = Maps.newHashMap();
    public int ageInTicks = 0;

    public ModelStage stage = ModelStage.ADULT;
    public float percentageStage = 1F;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("age", this.ageInTicks);
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.ageInTicks = compound.getInteger("age");
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(this.ageInTicks);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.ageInTicks = buf.readInt();
    }

    public ModelStage getStage() {
        return this.stage;
    }
}
