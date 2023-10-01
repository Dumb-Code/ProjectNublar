package net.dumbcode.projectnublar.server.containers.pouch.slot;

import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchSlot extends Slot {
    private final int stackLimit;
    public PouchSlot(IInventory inventory, int index, int x, int y, int stackLimit) {
        super(inventory, index, x, y);
        this.stackLimit = stackLimit;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof FossilItem;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.getMaxStackSize() == 1 ? 1: stackLimit;
    }
}