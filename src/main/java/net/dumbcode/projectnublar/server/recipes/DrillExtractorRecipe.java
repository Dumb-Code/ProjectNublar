package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.DrillExtractorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.item.DinosaurGeneticMaterialItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

public enum DrillExtractorRecipe implements MachineRecipe<DrillExtractorBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = process.getInputStack(0);
        if(inSlot.getItem() == ItemHandler.AMBER.get()) {
            for (int i = 0; i < process.getOutputSlots().length; i++) {
                if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE.get()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getRecipeTime(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        return 7200 - 600*blockEntity.getTier(MachineModuleType.DRILL_BIT);
    }

    @Override
    public void onRecipeFinished(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = process.getInputStack(0);
        for (int i : process.getOutputSlots()) {
            if(handler.getStackInSlot(i).getItem() == ItemHandler.EMPTY_TEST_TUBE.get()) {
                ItemStack stack = new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(Dinosaur.getRandom()));
                DinosaurGeneticMaterialItem.setSize(stack, MathUtils.getWeightedResult(blockEntity.getTier(MachineModuleType.DRILL_BIT) + 1, 0.5));
                if(!blockEntity.getLevel().isClientSide) {
                    handler.setStackInSlot(i, stack);
                    inSlot.shrink(1);
                }
                return;
            }
        }
    }

    @Override
    public boolean acceptsInputSlot(DrillExtractorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        switch (slotIndex) {
            case 0: return testStack.getItem() == ItemHandler.AMBER.get();
            case 1:
            case 2:
            case 3:
            case 4:
                return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE.get();
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "amber_genetic_material");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(DrillExtractorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<DrillExtractorBlockEntity> process) {
        return 0;
    }


}
