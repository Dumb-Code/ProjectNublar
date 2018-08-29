package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;

public interface MachineRecipe<B extends MachineModuleBlockEntity> {
    boolean accpets(B blockEntity, MachineModuleBlockEntity.Process process);
    int getRecipeTime(B blockEntity, MachineModuleBlockEntity.Process process);
    void onRecipeFinished(B blockEntity, MachineModuleBlockEntity.Process process);
}
