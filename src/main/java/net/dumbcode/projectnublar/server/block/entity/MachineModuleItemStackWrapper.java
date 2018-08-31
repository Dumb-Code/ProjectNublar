package net.dumbcode.projectnublar.server.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class MachineModuleItemStackWrapper implements IItemHandlerModifiable {
    private final IItemHandlerModifiable compose;
    private final int[] slots;

    public MachineModuleItemStackWrapper(IItemHandlerModifiable compose, int[] slots) {
        this.compose = compose;
        this.slots = slots;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if(this.checkSlot(slot)) {
            this.compose.setStackInSlot(this.slots[slot], stack);
        }
    }

    @Override
    public int getSlots() {
        return this.slots.length;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if(this.checkSlot(slot)) {
            return this.compose.getStackInSlot(this.slots[slot]);
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(this.checkSlot(slot)) {
            return this.compose.insertItem(this.slots[slot], stack, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(this.checkSlot(slot)) {
            return this.compose.extractItem(this.slots[slot], amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if(this.checkSlot(slot)) {
            return this.compose.getSlotLimit(this.slots[slot]);
        }
        return 0;
    }

    private boolean checkSlot(int slot) {
        return slot >= 0 && slot < this.slots.length;
    }
}
