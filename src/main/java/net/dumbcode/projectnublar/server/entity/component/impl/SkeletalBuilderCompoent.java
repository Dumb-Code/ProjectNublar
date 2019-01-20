package net.dumbcode.projectnublar.server.entity.component.impl;

import net.dumbcode.projectnublar.server.entity.ModelStage;
import net.dumbcode.projectnublar.server.entity.component.EntityComponent;
import net.minecraft.nbt.NBTTagCompound;

public class SkeletalBuilderCompoent implements EntityComponent {
    public int modelIndex;
    public ModelStage stage = ModelStage.ADULT;

    @Override
    public NBTTagCompound serialize(NBTTagCompound compound) {
        compound.setInteger("model_index", this.modelIndex);
        compound.setInteger("stage", this.stage.ordinal());
        return compound;
    }

    @Override
    public void deserialize(NBTTagCompound compound) {
        this.modelIndex = compound.getInteger("model_index");
        this.stage = ModelStage.values()[compound.getInteger("stage")];
    }
}
