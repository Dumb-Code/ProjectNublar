package net.dumbcode.projectnublar.server.block.entity;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@Setter
@Accessors(chain = true)
public class MachineModuleFluidTank extends FluidTank {

    private boolean canDrain = true;
    private boolean canFill = true;

    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineModuleFluidTank(MachineModuleBlockEntity<?> blockEntity, int capacity) {
        super(capacity);
        this.blockEntity = blockEntity;
    }

    public MachineModuleFluidTank(MachineModuleBlockEntity<?> blockEntity, int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
        this.blockEntity = blockEntity;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if(!this.canDrain) {
            return FluidStack.EMPTY;
        }
        return super.drain(maxDrain, action);
    }

    public FluidStack drainInternal(int maxDrain, FluidAction action) {
        return super.drain(maxDrain, action);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if(!this.canFill) {
            return 0;
        }
        return super.fill(resource, action);
    }

    public int fillInternal(FluidStack resource, FluidAction action) {
        return super.fill(resource, action);
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        this.blockEntity.setChanged();
    }
}
