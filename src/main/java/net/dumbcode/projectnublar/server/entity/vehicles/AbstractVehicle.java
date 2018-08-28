package net.dumbcode.projectnublar.server.entity.vehicles;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C13VehicleInputStateUpdated;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.MovementInput;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

public abstract class AbstractVehicle<E extends Enum> extends Entity {

    private final E[] values;
    public int inputState;

    public AbstractVehicle(World worldIn, E[] values) {
        super(worldIn);
        this.values = values;
    }

    public int getControlState() {
        return this.inputState;
    }

    public void setControlState(int state) {
        this.inputState = state;
    }

    public boolean getInput(E e) {
        return (this.inputState & this.getMask(e)) != 0;
    }

    public int getIntInput(E e) {
        return this.getInput(e) ? 1 : 0;
    }

    public void setInput(E e, boolean newState) {
        int state = this.getControlState();
        int mask = this.getMask(e);
        this.setControlState(newState ? state | mask : state & ~mask);
    }

    private int getMask(E e) {
        return (int)Math.pow(2, e.ordinal());
    }

    @Override
    protected void entityInit() {

    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if(this.world.isRemote) {
            this.handleInputs();
        }
//        if(!world.isRemote)
        {
            this.applyMovement();
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        }
    }

    protected void handleInputs() {
        Entity driver = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0); //Move to controlling passanger
        if(!(driver instanceof  EntityPlayerSP)) {
            return;
        }
        EntityPlayerSP player = (EntityPlayerSP) driver;
        int previousState = this.getControlState();
        for (E e : this.values) {
            this.setInput(e, this.isActive(player, e));
        }
        if(this.getControlState() != previousState) {
            ProjectNublar.NETWORK.sendToServer(new C13VehicleInputStateUpdated(this, this.getControlState()));
        }
    }

    @SideOnly(Side.CLIENT)
    protected abstract boolean isActive(EntityPlayerSP player, E e);

    protected abstract void applyMovement();

    protected enum DefaultInput {
        LEFT, RIGHT, FORWARD, BACKWARDS
    }

}
