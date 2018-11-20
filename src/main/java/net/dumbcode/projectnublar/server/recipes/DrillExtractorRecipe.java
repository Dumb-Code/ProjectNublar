package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.DrillExtractorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Function;
import java.util.function.Predicate;

public enum DrillExtractorRecipe implements MachineRecipe<DrillExtractorBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        if(inSlot.getItem() == ItemHandler.AMBER) {
            for (int i = 0; i < process.getOutputSlots().length; i++) {
                if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getRecipeTime(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 20;
    }

    @Override
    public void onRecipeFinished(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        for (int i : process.getOutputSlots()) {
            if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE) {
                ItemStack stack = new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(Dinosaur.getRandom()));
                if(!blockEntity.getWorld().isRemote) {
                    handler.setStackInSlot(i, stack);
                    inSlot.shrink(1);
                }
                return;
            }
        }
    }

    @Override
    public boolean acceptsInputSlot(DrillExtractorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        switch (slotIndex) {
            case 0: return testStack.getItem() == ItemHandler.AMBER;
            case 1:
            case 2:
            case 3:
            case 4:
                return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "amber_genetic_material");
    }
}
