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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Arrays;
import java.util.Random;

public enum FossilProcessorRecipe implements MachineRecipe<FossilProcessorBlockEntity> {
    INSTANCE;

    private static final int FLUID_AMOUNT = FluidAttributes.BUCKET_VOLUME / 4;

    @Override
    public boolean accepts(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        MachineModuleItemStackHandler<FossilProcessorBlockEntity> handler = blockEntity.getHandler();
        ItemStack inSlot = handler.getStackInSlot(process.getInputSlot(0));
        Item item = inSlot.getItem();
        return item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).values().stream().anyMatch(i -> i.get() == item)
            && handler.getStackInSlot(3).getItem() instanceof FilterItem
            && blockEntity.getTank().getFluidAmount() >= FLUID_AMOUNT
            && handler.getStackInSlot(2).getItem() == ItemHandler.EMPTY_TEST_TUBE.get();
    }

    @Override
    public int getRecipeTime(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        return 4800 - 1200*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public boolean shouldSlotChangeCauseReset(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process, int slot) {
        return slot == 0;
    }

    @Override
    public void onRecipeFinished(FossilProcessorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        MachineModuleItemStackHandler<FossilProcessorBlockEntity> handler = blockEntity.getHandler();
        ItemStack inputStack = handler.getStackInSlot(process.getInputSlot(0));

        //Shrink the fossil
        inputStack.shrink(1);

        //TODO: verify the efficieny and that the graph is correct
        //Damage the filter item.
        ItemStack filter = handler.getStackInSlot(3);
        float efficiency = ((FilterItem)filter.getItem()).getEfficiency(filter);
        if (filter.hurt(1, blockEntity.getLevel().getRandom(), null)) {
            filter.shrink(1);
        }

        //Shrink the test tube
        handler.getStackInSlot(2).shrink(1);


        //Drain the tank
        blockEntity.getTank().drainInternal(FLUID_AMOUNT, IFluidHandler.FluidAction.EXECUTE);

        //Insert the output item
        Item item = inputStack.getItem();
        if(item instanceof DinosaurProvider) {
            ItemStack stack = new ItemStack(ItemHandler.TEST_TUBES_GENETIC_MATERIAL.get(((FossilItem) item).getDinosaur()).get());
            //Sets the size.
            //See here: https://www.desmos.com/calculator/c59djyd7c8 for a distribution for the 3 different filter types.
            //Where:
            //  d is the damage of the item (0 is no damage, 1 is fully damaged.)
            //  the X axis is the size of the genetic material produced.
            //  green -> iron filter
            //  red -> gold filter
            //  blue -> diamond filter
            //If less than 1, then don't insert the output item.
            int size = MathUtils.getWeightedResult(5 * efficiency + 1, 0.6F - efficiency / 2F);
            if(size > 0) {
                DinosaurGeneticMaterialItem.setSize(stack, size);
                process.insertOutputItem(stack);
            }
        }
    }

    @Override
    public boolean acceptsInputSlot(FossilProcessorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<FossilProcessorBlockEntity> process) {
        Item item = testStack.getItem();
        return slotIndex == 0 && item instanceof FossilItem && ItemHandler.FOSSIL_ITEMS.get(((FossilItem) item).getDinosaur()).values().stream().anyMatch(i -> i.get() == item);
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
