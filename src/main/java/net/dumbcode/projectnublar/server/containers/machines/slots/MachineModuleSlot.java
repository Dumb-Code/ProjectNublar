package net.dumbcode.projectnublar.server.containers.machines.slots;

import com.mojang.datafixers.util.Either;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MachineModuleSlot extends SlotItemHandler {

    @Setter
    @Getter
    private boolean enabled = true;

    @Nullable
    private final MachineModuleBlockEntity<?> blockEntity;

    public MachineModuleSlot(Either<IItemHandler, ? extends MachineModuleBlockEntity<?>> inv, int index, int xPosition, int yPosition) {
        super(inv.map(i -> i, MachineModuleBlockEntity::getHandler), index, xPosition, yPosition);
        this.blockEntity = inv.right().orElse(null);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        if(this.blockEntity != null) {
            return this.blockEntity.isItemValidFor(this.getSlotIndex(), stack);
        }
        return super.mayPlace(stack);
    }
}
