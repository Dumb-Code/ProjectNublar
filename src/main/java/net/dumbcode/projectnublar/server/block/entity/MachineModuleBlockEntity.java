package net.dumbcode.projectnublar.server.block.entity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;

public class MachineModuleBlockEntity extends SimpleBlockEntity {
    @Getter
    @Setter
    private int stateID;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("StateID", this.stateID);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stateID = compound.getInteger("StateID");
    }

}
