package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public interface MachineRecipe<B extends MachineModuleBlockEntity<B>> {
    boolean accepts(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    int getRecipeTime(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    void onRecipeFinished(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    default void onRecipeStarted(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process) {}
    default void onRecipeTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process) {}

    boolean acceptsInputSlot(B blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<B> process);

    default MachineModuleBlockEntity.ProcessInterruptAction getInterruptAction(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process, MachineModuleBlockEntity.ProcessInterruptReason reason) {
        return reason == MachineModuleBlockEntity.ProcessInterruptReason.NO_POWER ? MachineModuleBlockEntity.ProcessInterruptAction.PAUSE : MachineModuleBlockEntity.ProcessInterruptAction.RESET;
    }

    default boolean shouldSlotChangeCauseReset(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process, int slot) {
        return false;
    }

    default boolean shouldGlobalSlotChangeCauseReset(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process, int slot) {
        return false;
    }

    default boolean startsAutomatically() {
        return true;
    }

    ResourceLocation getRegistryName();

    // energy stuff
    int getCurrentConsumptionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
    int getCurrentProductionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess<B> process);
}
