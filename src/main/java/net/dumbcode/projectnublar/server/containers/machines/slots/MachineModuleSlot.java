package net.dumbcode.projectnublar.server.containers.machines.slots;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class MachineModuleSlot extends SlotItemHandler {

    @Setter
    @Getter
    private boolean enabled = true;

    private final MachineModuleBlockEntity blockEntity;

    public MachineModuleSlot(MachineModuleBlockEntity blockEntity, int index, int xPosition, int yPosition) {
        super(blockEntity.getHandler(), index, xPosition, yPosition);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return this.blockEntity.isItemValidFor(this.getSlotIndex(), stack);
    }
}
