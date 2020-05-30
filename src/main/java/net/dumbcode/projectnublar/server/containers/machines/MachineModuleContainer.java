package net.dumbcode.projectnublar.server.containers.machines;

import lombok.NonNull;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.network.S44SyncOpenedUsers;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.function.IntPredicate;

public class MachineModuleContainer extends Container {

    private IntPredicate predicate = i -> true;
    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineModuleContainer(MachineModuleBlockEntity<?> blockEntity, EntityPlayer player, int playerOffset, int xSize, MachineModuleSlot... slots) {
        this.blockEntity = blockEntity;
        for (MachineModuleSlot slot : slots) {
            this.addSlotToContainer(slot);
        }

        if(playerOffset >= 0) {
            this.addPlayerSlots(player, playerOffset, xSize);
        }

        if(!player.world.isRemote) {
            this.blockEntity.getOpenedUsers().add(player.getUniqueID());
            ProjectNublar.NETWORK.sendToDimension(new S44SyncOpenedUsers(this.blockEntity), player.world.provider.getDimension());
        }
    }

    public MachineModuleContainer setPredicate(@NonNull IntPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    protected void addPlayerSlots(EntityPlayer player, int yOffet, int xSize) {
        InventoryPlayer playerInventory = player.inventory;

        int xStart = (xSize - 162) / 2 + 1;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, xStart + j * 18, yOffet + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInventory, i, xStart + i * 18, yOffet + 58));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        if(!playerIn.world.isRemote) {
            this.blockEntity.getOpenedUsers().remove(playerIn.getUniqueID());
            ProjectNublar.NETWORK.sendToDimension(new S44SyncOpenedUsers(this.blockEntity), playerIn.world.provider.getDimension());
        }
        super.onContainerClosed(playerIn);
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
            } else {
                boolean flag = false;
                for (int i = 0; i < otherSlots; i++) {
                    if(this.predicate.test(i)) {
                        if(current.isEmpty()) {
                            break;
                        }
                        flag |= this.mergeItemStack(current, i, i + 1, false);

                    }
                }
                if(!flag) {
                    return ItemStack.EMPTY;
                }
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
