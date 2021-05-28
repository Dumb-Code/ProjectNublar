package net.dumbcode.projectnublar.server.containers.machines;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.network.S44SyncOpenedUsers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.function.IntPredicate;

public class MachineModuleContainer<B extends MachineModuleBlockEntity<B>> extends Container {

    private IntPredicate predicate = i -> true;

    @Nullable
    private final B blockEntity;

    public MachineModuleContainer(ContainerType<?> type, PlayerInventory inventory, int id, MachineModuleBlockEntity.ContainerInfo<B> info) {
        this(type, inventory, id, info, null);
    }

    public MachineModuleContainer(ContainerType<?> type, PlayerInventory inventory, int id, MachineModuleBlockEntity.ContainerInfo<B> info, @Nullable B blockEntity) {
        super(type, id);
        this.blockEntity = blockEntity;
        for (Slot slot : info.getSlotGenerator().apply(blockEntity == null ? Either.left(new EmptyHandler()) : Either.right(blockEntity))) {
            this.addSlot(slot);
        }

        if(info.getPlayerOffset() >= 0) {
            this.addPlayerSlots(inventory, info.getPlayerOffset(), info.getXSize());
        }
    }


    public MachineModuleContainer<B> setPredicate(@NonNull IntPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    protected void addPlayerSlots(PlayerInventory playerInventory, int yOffet, int xSize) {
        int xStart = (xSize - 162) / 2 + 1;

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, xStart + j * 18, yOffet + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, xStart + i * 18, yOffet + 58));
        }
    }



    @Override
    public void removed(PlayerEntity playerIn) {
        if(!playerIn.level.isClientSide) {
            this.blockEntity.getOpenedUsers().remove(playerIn.getUUID());
            ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(playerIn.level::dimension), new S44SyncOpenedUsers(this.blockEntity));
        }
        super.removed(playerIn);
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return true; //TODO ?
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int slotIndex) {
        ItemStack transferred = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        int otherSlots = this.slots.size() - 36;

        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem();
            transferred = current.copy();

            if (slotIndex < otherSlots) {
                if (!this.moveItemStackTo(current, otherSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean flag = false;
                for (int i = 0; i < otherSlots; i++) {
                    if(this.predicate.test(i)) {
                        if(current.isEmpty()) {
                            break;
                        }
                        flag |= this.moveItemStackTo(current, i, i + 1, false);

                    }
                }
                if(!flag) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.getCount() == 0) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return transferred;
    }
}
