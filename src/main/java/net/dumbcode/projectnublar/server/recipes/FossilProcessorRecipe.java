package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum FossilProcessorRecipe implements MachineRecipe<FossilProcessorBlockEntity> {

    INSTANCE;

    @Override
    public boolean accepts(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        MachineModuleItemStackHandler<FossilProcessorBlockEntity> handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlot(0));
        Item item = inSlot.getItem();
        return item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).containsValue(item)
            && handler.getStackInSlot(3).getItem() instanceof FilterItem && handler.getStackInSlot(process.getOutputSlot(0)).isEmpty();
    }

    @Override
    public int getRecipeTime(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        return 4800 - 1200*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public void onRecipeFinished(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        MachineModuleItemStackHandler<FossilProcessorBlockEntity> handler = blockEntity.getHandler();
        ItemStack inputStack = handler.getStackInSlot(process.getInputSlot(0));
        ItemStack filter = handler.getStackInSlot(3);
        float efficiency = ((FilterItem)filter.getItem()).getEfficiency(filter);
        inputStack.shrink(1);
        if (filter.hurt(1, blockEntity.getLevel().getRandom(), null)) {
            filter.shrink(1);
        }
        Item item = inputStack.getItem();
        if(item instanceof DinosaurProvider) {
            ItemStack stack = new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(((FossilItem) item).getDinosaur()).get());
            DinosaurGeneticMaterialItem.setSize(stack, MathUtils.getWeightedResult(5*efficiency, 0.5F-efficiency/2F));
            handler.insertOutputItem(process.getOutputSlot(0), stack, false);
        }
    }

    @Override
    public boolean acceptsInputSlot(FossilProcessorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        Item item = testStack.getItem();
        return slotIndex == 0 && item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).containsValue(item);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        return 0;
    }
}
