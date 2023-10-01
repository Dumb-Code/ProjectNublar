package net.dumbcode.projectnublar.server.containers.machines.slots;

import lombok.Setter;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MachineModuleSlot extends SlotItemHandler implements SlotCanBeDisabled {

    @Setter
    private boolean active = true;

    private final MachineModuleBlockEntity<?> blockEntity;
    @Setter
    private IntPredicate isLocked = integer -> false;

    public MachineModuleSlot(MachineModuleBlockEntity<?> blockEntity, int index, int xPosition, int yPosition) {
        super(blockEntity.getHandler(), index, xPosition, yPosition);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return this.blockEntity.isItemValidFor(this.getSlotIndex(), stack);
    }


    @Override
    public boolean mayPickup(PlayerEntity playerIn) {
        if(this.isLocked.test(this.getSlotIndex())) {
            return false;
        }
        return super.mayPickup(playerIn);
    }

    @Override
    public MachineModuleSlot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
        super.setBackground(atlas, sprite);
        return this;
    }

    public MachineModuleSlot disable() {
        this.active = false;
        return this;
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

}
