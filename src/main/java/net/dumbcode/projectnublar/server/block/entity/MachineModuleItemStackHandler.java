package net.dumbcode.projectnublar.server.block.entity;

import net.dumbcode.projectnublar.server.recipes.MachineRecipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class MachineModuleItemStackHandler<B extends MachineModuleBlockEntity<B>> extends ItemStackHandler {

    private final MachineModuleBlockEntity<B> blockEntity;

    public MachineModuleItemStackHandler(MachineModuleBlockEntity<B> blockEntity, int size) {
        super(size);
        this.blockEntity = blockEntity;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(!blockEntity.isItemValidFor(slot, stack)) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    protected void onContentsChanged(int slot) {
        MachineModuleBlockEntity.Process process = this.blockEntity.getProcess(slot);
        if(process != null) {
            if(!process.isProcessing()) {
                this.blockEntity.searchForRecipes(process);
            }
        }
        super.onContentsChanged(slot);
    }
}
