package net.dumbcode.projectnublar.server.containers.pouch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

public class FossilPouchInventory extends Inventory /*SimpleContainer*/ {
    protected final ItemStack itemStack;
    protected final int SIZE;

    protected final int stackLimit;
    public FossilPouchInventory(ItemStack stack, int SIZE, int stackLimit) {
        super(getStacks(stack, SIZE).toArray(new ItemStack[SIZE]));
        itemStack = stack;
        this.SIZE = SIZE;
        this.stackLimit = stackLimit;
    }

    public ItemStack getHolderStack() {
        return itemStack;
    }

    public static String getNBTTag() {
        return "Inventory";
    }

    public static NonNullList<ItemStack> getStacks(ItemStack usedStack, int SIZE) {
        CompoundNBT compoundTag = usedStack.getTagElement(getNBTTag());
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        if (compoundTag != null && compoundTag.contains("Items", 9)) {
            loadAllItems(compoundTag, itemStacks);
        }
        return itemStacks;
    }

    public static void loadAllItems(CompoundNBT pTag, NonNullList<ItemStack> pList) {
        ListNBT listtag = pTag.getList("Items", 10);

        for(int i = 0; i < listtag.size(); ++i) {
            CompoundNBT compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 0 && j < pList.size()) {
                pList.set(j, ItemStack.of(compoundtag));
            }
        }

    }

    @Override
    public void setChanged() {
        super.setChanged();
        CompoundNBT itemTag = itemStack.getTagElement(getNBTTag());
        if (itemTag == null)
            itemTag = itemStack.getOrCreateTagElement(getNBTTag());

        if (isEmpty()) {
            if (itemTag.contains("Items")) itemTag.remove("Items");
        } else {
            NonNullList<ItemStack> itemStacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
            for (int i = 0; i < getContainerSize(); i++) {
                itemStacks.set(i, getItem(i));
            }
            // containerHelper in new versions
            ItemStackHelper.saveAllItems(itemTag, itemStacks);
        }

        if (shouldDeleteNBT(itemTag)) {
            itemStack.removeTagKey(getNBTTag());
        }
    }

    public boolean shouldDeleteNBT(CompoundNBT blockEntityTag) {
        if (!blockEntityTag.contains("Items"))
            return blockEntityTag.getAllKeys().isEmpty();
        return isEmpty();
    }

    @Override
    public int getMaxStackSize() {
        return stackLimit;
    }

    @Override
    public void stopOpen(PlayerEntity playerEntity) {
        if (itemStack.getCount() > 1) {
            int count = itemStack.getCount();
            itemStack.setCount(1);
            playerEntity.addItem(new ItemStack(itemStack.getItem(), count - 1));
        }
        setChanged();
    }
}