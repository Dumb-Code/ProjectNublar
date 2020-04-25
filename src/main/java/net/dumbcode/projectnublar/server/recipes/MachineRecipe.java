package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface MachineRecipe<B extends MachineModuleBlockEntity> {
    boolean accepts(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    int getRecipeTime(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    void onRecipeFinished(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    default void onRecipeStarted(B blockEntity, MachineModuleBlockEntity.MachineProcess process) {}
    default void onRecipeTick(B blockEntity, MachineModuleBlockEntity.MachineProcess process) {}

    boolean acceptsInputSlot(B blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process);

    ResourceLocation getRegistryName();

    // energy stuff
    int getCurrentConsumptionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    int getCurrentProductionPerTick(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
}
