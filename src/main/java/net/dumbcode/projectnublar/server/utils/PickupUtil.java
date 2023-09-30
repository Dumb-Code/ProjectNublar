package net.dumbcode.projectnublar.server.utils;

import net.dumbcode.projectnublar.server.containers.pouch.FossilPouchInventory;
import net.dumbcode.projectnublar.server.fossil.blockitem.FossilItem;
import net.dumbcode.projectnublar.server.item.FossilPouchItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public class PickupUtil {

    /**
     * @param inv Player Inventory to add the item to
     * @param incoming the itemstack being picked up
     * @return if the item was completely picked up by the sack(s)
     */
    public static boolean interceptItem(PlayerInventory inv, ItemStack incoming) {
        PlayerEntity player = inv.player;
        if (player.level.isClientSide || incoming.isEmpty()) {//thanks Hookshot
            return false;
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof FossilPouchItem && onItemPickup(player, incoming, stack)) {
                return true;
            }
        }
        return false;
    }

    public static boolean onItemPickup(PlayerEntity player, ItemStack pickStack, ItemStack pouch) {
        if (pickStack.getItem() instanceof FossilItem) {
            boolean canPickup = pickStack.getItem() instanceof FossilItem;
            if (!canPickup) {
                return false;
            }
            FossilPouchItem sackItem = (FossilPouchItem) pouch.getItem();
            FossilPouchInventory inventory = FossilPouchItem.getInv(pouch);

            int count = pickStack.getCount();
            List<ItemStack> existing = new ArrayList<>();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.isEmpty()) {
                    boolean exists = false;
                    for (ItemStack stack1 : existing) {
                        if (areItemStacksCompatible(stack, stack1)) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        existing.add(stack.copy());
                    }
                }
            }

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                pickup(inventory, i, pickStack);
                if (pickStack.isEmpty()) break;
            }

            //leftovers
            if (pickStack.getCount() != count) {
                pouch.setPopTime(5);
                player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }
            return pickStack.isEmpty();
        }
        return false;
    }

    private static boolean areItemStacksCompatible(ItemStack stack, ItemStack stack1) {
        return ItemStack.tagMatches(stack, stack1) && ItemStack.isSame(stack, stack1);
    }

    public static void voidPickup(FossilPouchInventory inv, int slot, ItemStack toInsert, List<ItemStack> filter) {
        ItemStack existing = inv.getItem(slot);

        if (doesItemStackExist(toInsert, filter) && areItemStacksCompatible(existing, toInsert)) {
            int stackLimit = inv.getMaxStackSize();
            int total = Math.min(toInsert.getCount() + existing.getCount(), stackLimit);
            
            inv.setItem(slot, ItemHandlerHelper.copyStackWithSize(existing, total));
            toInsert.setCount(0);
        }
    }

    public static void pickup(FossilPouchInventory inv, int slot, ItemStack pickup) {
        ItemStack existing = inv.getItem(slot);

        if (existing.isEmpty()) {
            int stackLimit = inv.getMaxStackSize();
            int total = pickup.getCount();
            int remainder = total - stackLimit;
            //no overflow
            if (remainder <= 0) {
                inv.setItem(slot, pickup.copy());
                pickup.setCount(0);
            } else {
                inv.setItem(slot, ItemHandlerHelper.copyStackWithSize(pickup, stackLimit));
                pickup.setCount(remainder);
            }
            return;
        }

        if (ItemHandlerHelper.canItemStacksStack(pickup, existing)) {
            int stackLimit = inv.getMaxStackSize();
            int total = pickup.getCount() + existing.getCount();
            int remainder = total - stackLimit;
            //no overflow
            if (remainder <= 0) {
                inv.setItem(slot, ItemHandlerHelper.copyStackWithSize(existing, total));
                pickup.setCount(0);
            } else {
                inv.setItem(slot, ItemHandlerHelper.copyStackWithSize(pickup, stackLimit));
                pickup.setCount(remainder);
            }
        }
    }

    public static boolean doesItemStackExist(ItemStack stack, List<ItemStack> filter) {
        for (ItemStack filterStack : filter) {
            if (areItemStacksCompatible(stack, filterStack)) return true;
        }
        return false;
    }
}
