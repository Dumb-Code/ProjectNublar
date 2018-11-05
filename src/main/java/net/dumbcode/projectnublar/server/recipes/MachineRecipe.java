package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface MachineRecipe<B extends MachineModuleBlockEntity> {
    boolean accepts(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    int getRecipeTime(B blockEntity, MachineModuleBlockEntity.MachineProcess process);
    void onRecipeFinished(B blockEntity, MachineModuleBlockEntity.MachineProcess process);

    boolean acceptsInputSlot(B blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process);

    ResourceLocation getRegistryName();
}
