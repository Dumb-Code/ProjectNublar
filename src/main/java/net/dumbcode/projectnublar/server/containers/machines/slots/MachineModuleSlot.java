package net.dumbcode.projectnublar.server.containers.machines.slots;

import lombok.Setter;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class MachineModuleSlot extends SlotItemHandler implements SlotCanBeDisabled {

    @Setter
    private boolean active = true;

    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineModuleSlot(MachineModuleBlockEntity<?> blockEntity, int index, int xPosition, int yPosition) {
        super(blockEntity.getHandler(), index, xPosition, yPosition);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return this.blockEntity.isItemValidFor(this.getSlotIndex(), stack);
    }

    @Override
    public MachineModuleSlot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        super.setBackground(atlas, sprite);
        return this;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }
}
