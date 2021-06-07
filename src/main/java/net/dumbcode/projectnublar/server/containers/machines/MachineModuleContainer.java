package net.dumbcode.projectnublar.server.containers.machines;

import com.mojang.datafixers.util.Either;
import lombok.NonNull;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.ProjectNublarContainers;
import net.dumbcode.projectnublar.server.containers.machines.slots.MachineModuleSlot;
import net.dumbcode.projectnublar.server.network.S44SyncOpenedUsers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.IntPredicate;

public class MachineModuleContainer extends Container {

    private IntPredicate predicate = i -> true;

    private final int tab;
    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineModuleContainer(int id, MachineModuleBlockEntity<?> blockEntity, PlayerInventory inventory, int tab, int playerOffset, int xSize, MachineModuleSlot... slots) {
        super(ProjectNublarContainers.MACHINE_MODULES.get(), id);
        this.blockEntity = blockEntity;
        this.tab = tab;
        for (Slot slot : slots) {
            this.addSlot(slot);
        }

        if(playerOffset >= 0) {
            this.addPlayerSlots(inventory, playerOffset, xSize);
        }
    }


    public MachineModuleContainer setPredicate(@NonNull IntPredicate predicate) {
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
            ProjectNublar.NETWORK.send(PacketDistributor.DIMENSION.with(playerIn.level::dimension), new S44SyncOpenedUsers(this.blockEntity.getBlockPos(), this.blockEntity.getOpenedUsers()));
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

    public int getTab() {
        return tab;
    }

    public MachineModuleBlockEntity<?> getBlockEntity() {
        return blockEntity;
    }

    public static <T extends MachineModuleBlockEntity<T>> Optional<T> getFromMenu(Class<T> expected, @Nullable ServerPlayerEntity player) {
        if(player != null && player.containerMenu instanceof MachineModuleContainer) {
            MachineModuleContainer container = (MachineModuleContainer) player.containerMenu;
            if(expected.isInstance(container.getBlockEntity())) {
                return Optional.of(expected.cast(container.getBlockEntity()));
            }
        }
        return Optional.empty();
    }
}
