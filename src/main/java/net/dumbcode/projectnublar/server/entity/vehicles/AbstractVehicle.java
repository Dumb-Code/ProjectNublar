package net.dumbcode.projectnublar.server.entity.vehicles;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public abstract class AbstractVehicle extends Entity {

    public static final DataParameter<Byte> INPUT_STATE = EntityDataManager.createKey(AbstractVehicle.class, DataSerializers.BYTE);


    public AbstractVehicle(World worldIn) {
        super(worldIn);
    }

    public byte getControlState() {
        return this.dataManager.get(INPUT_STATE);
    }

    public void setControlState(int state) {
        this.dataManager.set(INPUT_STATE, (byte) state);
    }

    public boolean getInput(Enum e) {
        return (this.dataManager.get(INPUT_STATE) & this.getMask(e)) != 0;
    }

    public void setInput(Enum e, boolean newState) {
        byte state = this.getControlState();
        int mask = this.getMask(e);
        this.setControlState(newState ? state | mask : state & ~mask);
    }

    private int getMask(Enum e) {
        return (int)Math.pow(2, e.ordinal());
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(INPUT_STATE, (byte) 0);

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    protected enum DefaultInput {
        LEFT, RIGHT, FORWARD, BACKWARDS;
    }
}
