package net.dumbcode.projectnublar.server.containers.pouch;

import lombok.Getter;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.dumbcode.projectnublar.server.containers.pouch.slot.PouchSlot;
import net.dumbcode.projectnublar.server.item.FossilPouchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;

import java.util.UUID;

public class FossilPouchMenu extends Container {

    private final FossilPouchInventory inv;
    @Getter
    private final int width;
    public final NonNullList<Slot> playerInvSlots = NonNullList.create();
    @Getter
    private final int height;
    private final UUID uuid;

    //gets called clientside. The real inv does not matter
    public FossilPouchMenu(int syncId, PlayerInventory playerInventory, PacketBuffer /*FriendlyByteBuf*/ packetByteBuf) {
        this(syncId, playerInventory, packetByteBuf.readInt(), packetByteBuf.readInt(), packetByteBuf.readUUID(), Items.AIR.getDefaultInstance());
    }

    public FossilPouchMenu(int syncId, PlayerInventory playerInv, int width, int height, UUID uuid, ItemStack stack) {
        super(ProjectNublarContainers.SACK_MENU.get(), syncId);
        this.inv = new FossilPouchInventory(stack, width * height, 64);
        this.width = width;
        this.height = height;
        this.uuid = uuid;
        // Backpack inventory
        for (int n = 0; n < height; ++n) {
            for (int m = 0; m < width; ++m) {
                this.addSlot(new PouchSlot(inv, m + n * width, 8 + m * 18, 18 + n * 18, 64), true);
            }
        }

        // Player inventory
        for (int n = 0; n < 3; ++n) {
            for (int m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInv, m + n * 9 + 9, 8 + (width * 18 - 162) / 2 + m * 18, 31 + (height + n) * 18), false);
            }
        }

        // Player hotbar
        for (int n = 0; n < 9; ++n) {
            this.addSlot(new Slot(playerInv, n, 8 + (width * 18 - 162) / 2 + n * 18, 89 + height * 18), false);
        }

        this.inv.startOpen(playerInv.player);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        if (player.level.isClientSide) return true;

        ItemStack stack = inv.getHolderStack();
        boolean uuidMatch = FossilPouchItem.isUUIDMatch(stack, this.uuid);

        return !stack.isEmpty() && stack.getItem() instanceof FossilPouchItem && uuidMatch;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            final ItemStack stack2 = slot.getItem();
            stack = stack2.copy();

            if (index < this.inv.getContainerSize()) {
                if (!this.moveItemStackTo(stack2, this.inv.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack2, 0, this.inv.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return stack;
    }

    @Override
    public ItemStack clicked(int slotIndex, int button, ClickType type, PlayerEntity player) {
        if (slotIndex >= 0 && player.inventory.selected + 27 + this.inv.getContainerSize() == slotIndex) {
            if (type != ClickType.CLONE) {
                return ItemStack.EMPTY;
            }
        }
        super.clicked(slotIndex, button, type, player);
        return ItemStack.EMPTY;
    }

    public void addSlot(Slot slot, boolean isSack) {
        if (isSack) {
            this.addSlot(slot);
            return;
        }
        playerInvSlots.add(slot);
        this.addSlot(slot);
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        this.inv.stopOpen(player);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int i = startIndex;
        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.tagMatches(stack, itemstack) && ItemStack.matches(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = stack.getMaxStackSize() == 1 ? 1: slot.getMaxStackSize();
                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.setChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.setChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (reverseDirection) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (reverseDirection) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                Slot slot1 = this.slots.get(i);
                ItemStack itemstack1 = slot1.getItem();
                if (itemstack1.isEmpty() && slot1.mayPlace(stack)) {
                    if (stack.getCount() > slot1.getMaxStackSize()) {
                        slot1.set(stack.split(slot1.getMaxStackSize()));
                    } else {
                        slot1.set(stack.split(stack.getCount()));
                    }

                    slot1.setChanged();
                    flag = true;
                    break;
                }

                if (reverseDirection) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return flag;
    }
}