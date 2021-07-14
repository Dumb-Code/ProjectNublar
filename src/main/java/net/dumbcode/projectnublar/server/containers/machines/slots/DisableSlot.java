package net.dumbcode.projectnublar.server.containers.machines.slots;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;

public class DisableSlot extends Slot implements SlotCanBeDisabled {

    @Setter
    private boolean active;

    public DisableSlot(IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_, int p_i1824_4_) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
