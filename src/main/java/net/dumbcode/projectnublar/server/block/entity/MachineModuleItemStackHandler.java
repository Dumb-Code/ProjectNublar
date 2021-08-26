package net.dumbcode.projectnublar.server.block.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class MachineModuleItemStackHandler<B extends MachineModuleBlockEntity<B>> extends ItemStackHandler {

    private final MachineModuleBlockEntity<B> blockEntity;

    private final int decidedSize;

    public MachineModuleItemStackHandler(MachineModuleBlockEntity<B> blockEntity, int size) {
        super(size);
        this.decidedSize = size;
        this.blockEntity = blockEntity;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(!this.blockEntity.isItemValidFor(slot, stack)) {
            return stack;
        }
        ItemStack old = this.getStackInSlot(slot);
        ItemStack returnValue = super.insertItem(slot, stack, simulate);
        if(!simulate) {
            this.searchForNewRecipesIfNeeded(slot, old);
        }
        return returnValue;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack old = this.getStackInSlot(slot);
        ItemStack returnValue = super.extractItem(slot, amount, simulate);
        if(!simulate) {
            this.searchForNewRecipesIfNeeded(slot, old);
        }
        return returnValue;
    }

    public ItemStack insertOutputItem(int slot, @Nonnull ItemStack stack, boolean simulationOnly) {
        return super.insertItem(slot, stack, simulationOnly);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.blockEntity.slotSize(slot);
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.blockEntity.onSlotChanged(slot);
        this.blockEntity.setChanged();
        super.onContentsChanged(slot);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        ItemStack old = this.getStackInSlot(slot);
        super.setStackInSlot(slot, stack);
        this.searchForNewRecipesIfNeeded(slot, old);
    }

    private void searchForNewRecipesIfNeeded(int slot, ItemStack oldStack) {
        ItemStack newStack = this.getStackInSlot(slot);
        boolean shouldKeep = !newStack.isEmpty() && newStack.sameItem(oldStack) && ItemStack.tagMatches(newStack, oldStack);
        if(shouldKeep) {
            return;
        }
        MachineModuleBlockEntity.MachineProcess<B> process = this.blockEntity.getProcessFromSlot(slot);
        if(process != null) {
            if(process.isProcessing()) {
                process.causeSlotResetIfNecessary(slot);
            } else {
                this.blockEntity.searchForRecipes(process, false);
            }
        } else {
            for (int i = 0; i < this.blockEntity.getProcessCount(); i++) {
                this.blockEntity.getProcess(i).causeGlobalSlotResetIfNecessary(slot);
            }
        }
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        if(this.stacks.size() != this.decidedSize) {
            if(this.stacks.size() < this.decidedSize) {
                while(this.stacks.size() != this.decidedSize) {
                    this.stacks.add(ItemStack.EMPTY);
                }
            } else {
                while(this.stacks.size() != this.decidedSize) {
                    this.stacks.remove(this.stacks.size() - 1);
                }
            }
        }
    }

    public MachineModuleBlockEntity<B> getBlockEntity() {
        return blockEntity;
    }
}
