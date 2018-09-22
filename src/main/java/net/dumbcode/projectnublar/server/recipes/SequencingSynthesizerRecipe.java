package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity> {

    private final ResourceLocation registryName;
    private final int time;
    private final Predicate<ItemStack> input;
    private final BiFunction<ItemStack, ItemStack, ItemStack> outputFunc;

    public SequencingSynthesizerRecipe(ResourceLocation registryName, int time, Predicate<ItemStack> input, BiFunction<ItemStack, ItemStack, ItemStack> outputFunc) {
        this.registryName = registryName;
        this.time = time;
        this.input = input;
        this.outputFunc = outputFunc;
    }

    @Override
    public boolean accpets(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlots()[0]);
        ItemStack chipSlot = handler.getStackInSlot(process.getInputSlots()[1]);
        return
                this.input.test(inSlot) &&
                        this.acceptsInputSlot(blockEntity,1, chipSlot, process) &&
                        handler.insertItem(process.getOutputSlots()[0], this.outputFunc.apply(inSlot, handler.getStackInSlot(process.getInputSlots()[1])), true).isEmpty();
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.time;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inStack = handler.getStackInSlot(process.getInputSlots()[0]);
        ItemStack output = this.outputFunc.apply(inStack, handler.getStackInSlot(process.getInputSlots()[1]));
        inStack.shrink(1);
        handler.insertItem(process.getOutputSlots()[0], output, false);
        handler.setStackInSlot(process.getInputSlots()[1], ItemStack.EMPTY);
    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        if(slotIndex < 2) { //NEEDS TESTING. CANT AT THE MOMENT AS ONLY 1 DINOSAUR
            ItemStack other = blockEntity.getHandler().getStackInSlot(slotIndex == 0 ? 1 : 0);

            if(testStack.getItem() instanceof DinosaurProvider && other.getItem() instanceof DinosaurProvider && (slotIndex == 1 || this.input.test(testStack))
                    && ((DinosaurProvider) testStack.getItem()).getDinosaur() != ((DinosaurProvider) other.getItem()).getDinosaur()) {
                return false;
            }
        }
        if(slotIndex == 0) {
            System.out.println(this.input.test(testStack));
        }
        switch (slotIndex) {
            case 0: return this.input.test(testStack);
            case 1: return testStack.getItem() == ItemHandler.EMPTY_CHIP || (testStack.getItem() instanceof BasicDinosaurItem && ItemHandler.STORAGE_CHIP.values().contains(testStack.getItem()));
            case 2: return true;
            default: return false;
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}
