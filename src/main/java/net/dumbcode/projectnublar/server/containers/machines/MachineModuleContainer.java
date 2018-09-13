package net.dumbcode.projectnublar.server.containers.machines;

import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class MachineModuleContainer extends Container {

    public MachineModuleContainer(EntityPlayer player, int playerOffset, MachineModuleSlot... slots) {

        for (MachineModuleSlot slot : slots) {
            this.addSlotToContainer(slot);
        }

        if(playerOffset >= 0) {
            this.addPlayerSlots(player, playerOffset);
        }
    }

    protected void addPlayerSlots(EntityPlayer player, int yOffet) {
        InventoryPlayer playerInventory = player.inventory;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, yOffet + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, yOffet + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true; //TODO ?
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack transferred = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotIndex);

        int otherSlots = this.inventorySlots.size() - 36;

        if (slot != null && slot.getHasStack()) {
            ItemStack current = slot.getStack();
            transferred = current.copy();

            if (slotIndex < otherSlots) {
                if (!this.mergeItemStack(current, otherSlots, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(current, 0, otherSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (current.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return transferred;
    }
}
