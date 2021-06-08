package net.dumbcode.projectnublar.server.entity.vehicles;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SVehicleInputStateUpdated;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractVehicle<E extends Enum<E>> extends Entity {

    private final E[] values;
    public int inputState;

    public AbstractVehicle(EntityType<?> type, World worldIn, E[] values) {
        super(type, worldIn);
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
    protected void defineSynchedData() {

    }

    @Override
    public void baseTick() {
        super.baseTick();

        if(this.level.isClientSide) {
            this.handleInputs();
        }
//        if(!world.isRemote)
        {
            this.applyMovement();
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    protected void handleInputs() {
        Entity driver = this.getPassengers().isEmpty() ? null : this.getPassengers().get(0); //Move to controlling passanger
        if(!(driver instanceof ClientPlayerEntity)) {
            return;
        }
        ClientPlayerEntity player = (ClientPlayerEntity) driver;
        int previousState = this.getControlState();
        for (E e : this.values) {
            this.setInput(e, this.isActive(player, e));
        }
        if(this.getControlState() != previousState) {
            ProjectNublar.NETWORK.sendToServer(new C2SVehicleInputStateUpdated(this.getId(), this.getControlState()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected abstract boolean isActive(ClientPlayerEntity player, E e);

    protected abstract void applyMovement();

    protected enum DefaultInput {
        LEFT, RIGHT, FORWARD, BACKWARDS
    }

}
