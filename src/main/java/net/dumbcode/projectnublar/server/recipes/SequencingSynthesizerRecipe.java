package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;

import java.util.function.Function;
import java.util.function.Predicate;

public class SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity> {

    private final ResourceLocation registryName;
    private final int time;
    private final Predicate<ItemStack> input;
    private final Function<ItemStack, ItemStack> outputFunc;

    public SequencingSynthesizerRecipe(ResourceLocation registryName, int time, Predicate<ItemStack> input, Function<ItemStack, ItemStack> outputFunc) {
        this.registryName = registryName;
        this.time = time;
        this.input = input;
        this.outputFunc = outputFunc;
    }

    @Override
    public boolean accpets(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        IItemHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        return this.input.test(inSlot) && handler.insertItem(process.getOutputSlots()[0], this.outputFunc.apply(inSlot), true).isEmpty();
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.time;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {

    }

    @Override
    public boolean acceptsInputSlot(int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        switch (slotIndex) {
            case 0: return this.input.test(testStack);
            case 1: return testStack.getItem() == ItemHandler.DISC;
            default: return false;
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}
