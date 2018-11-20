package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.FossilItem;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Function;
import java.util.function.Predicate;

public enum FossilProcessorRecipe implements MachineRecipe<FossilProcessorBlockEntity> {

    INSTANCE;

    @Override
    public boolean accepts(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        Item item = inSlot.getItem();
        return item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).containsValue(item)
                && handler.insertOutputItem(process.getOutputSlots()[0], new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(((FossilItem) item).getDinosaur())), true).isEmpty();
    }

    @Override
    public int getRecipeTime(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 30;
    }

    @Override
    public void onRecipeFinished(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler itemStackHandler = blockEntity.getHandler();
        ItemStack inputStack = itemStackHandler.getStackInSlot(process.getInputSlots()[0]);
        inputStack.shrink(1);
        Item item = inputStack.getItem();
        if(item instanceof DinosaurProvider) {
            itemStackHandler.insertOutputItem(process.getOutputSlots()[0], new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(((FossilItem) item).getDinosaur())), false);
        }
    }

    @Override
    public boolean acceptsInputSlot(FossilProcessorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        Item item = testStack.getItem();
        return slotIndex == 0 && item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).containsValue(item);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "");
    }
}
