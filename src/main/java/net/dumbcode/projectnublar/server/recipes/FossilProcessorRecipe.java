package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Function;
import java.util.function.Predicate;

public class FossilProcessorRecipe implements MachineRecipe<FossilProcessorBlockEntity> {

    private final ResourceLocation registryName;
    private final Predicate<ItemStack> inputTest;
    private final Function<ItemStack, ItemStack> outputCreator;
    private final int recipeTime;

    public FossilProcessorRecipe(ResourceLocation registryName, Predicate<ItemStack> inputTest, Function<ItemStack, ItemStack> outputCreator, int recipeTime) {
        this.registryName = registryName;
        this.inputTest = inputTest;
        this.outputCreator = outputCreator;
        this.recipeTime = recipeTime;
    }

    @Override
    public boolean accpets(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        return this.inputTest.test(inSlot) && handler.insertItem(process.getOutputSlots()[0], this.outputCreator.apply(inSlot), true).isEmpty();
    }

    @Override
    public int getRecipeTime(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.recipeTime;
    }

    @Override
    public void onRecipeFinished(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler itemStackHandler = blockEntity.getHandler();
        ItemStack inputStack = itemStackHandler.getStackInSlot(process.getInputSlots()[0]);
        inputStack.shrink(1);
        ItemStack outSlot = itemStackHandler.getStackInSlot(process.getOutputSlots()[0]);
        ItemStack output = this.outputCreator.apply(inputStack);
        if(outSlot.isEmpty()) {
            itemStackHandler.setStackInSlot(process.getOutputSlots()[0], output);
        } else {
            outSlot.grow(output.getCount());
        }
    }

    @Override
    public boolean acceptsInputSlot(int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        switch (slotIndex) {
            case 1: return this.inputTest.test(testStack);
            case 2: return testStack.getItem() == ItemHandler.EMPTY_TEST_TUBE;
            case 3: return testStack.getItem() == ItemHandler.FILTER;
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}
