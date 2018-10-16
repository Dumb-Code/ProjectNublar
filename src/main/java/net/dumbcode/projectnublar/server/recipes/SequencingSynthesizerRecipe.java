package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Predicate;

public class SequencingSynthesizerRecipe implements MachineRecipe<SequencingSynthesizerBlockEntity> {

    private final ResourceLocation registryName;
    private final int time;
    private final Predicate<ItemStack> input;

    public SequencingSynthesizerRecipe(ResourceLocation registryName, int time, Predicate<ItemStack> input) {
        this.registryName = registryName;
        this.time = time;
        this.input = input;
    }

    @Override
    public boolean accpets(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack in = handler.getStackInSlot(process.getInputSlots()[0]);
        if(in.getItem() instanceof DriveUtils.DriveInformation) {
            ItemStack out = ((DriveUtils.DriveInformation) in.getItem()).getOutItem(in);
            if((!out.isEmpty() && !handler.insertOutputItem(process.getOutputSlots()[0], out, true).isEmpty()) || !DriveUtils.canAdd(handler.getStackInSlot(0), in)) {
                return false;
            }

        }
        return this.input.test(in) && handler.getStackInSlot(0).getItem() == ItemHandler.STORAGE_DRIVE ;
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.time;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack inStack = handler.getStackInSlot(process.getInputSlots()[0]);
        ItemStack out = inStack.splitStack(1);

        if(!out.isEmpty() && !blockEntity.getWorld().isRemote) {
            DriveUtils.addItemToDrive(handler.getStackInSlot(0), out);
        }

        if(out.getItem() instanceof DriveUtils.DriveInformation) {
            ItemStack outItem = ((DriveUtils.DriveInformation) out.getItem()).getOutItem(out);
            handler.insertOutputItem(process.getOutputSlots()[0], outItem, false);
        }

    }

    @Override
    public boolean acceptsInputSlot(SequencingSynthesizerBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        switch (slotIndex) {
            case 0: return this.input.test(testStack);
            case 1: return false;
            default: return false;
        }
    }

    @Override
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }
}
