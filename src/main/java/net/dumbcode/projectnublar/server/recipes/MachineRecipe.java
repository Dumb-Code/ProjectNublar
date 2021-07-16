package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface MachineRecipe<B extends MachineModuleBlockEntity<B>> {
    boolean accepts(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    int getRecipeTime(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    void onRecipeFinished(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    default void onRecipeStarted(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process) {}
    default void onRecipeTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process) {}

    boolean acceptsInputSlot(B blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<B> process);

    default MachineModuleBlockEntity.ProcessInterruptAction getInterruptAction(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process, MachineModuleBlockEntity.ProcessInterruptReason reason) {
        return reason == MachineModuleBlockEntity.ProcessInterruptReason.INVALID_INPUTS ? MachineModuleBlockEntity.ProcessInterruptAction.RESET : MachineModuleBlockEntity.ProcessInterruptAction.PAUSE;
    }



    ResourceLocation getRegistryName();

    // energy stuff
    int getCurrentConsumptionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    int getCurrentProductionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
}
