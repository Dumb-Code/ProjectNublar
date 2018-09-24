package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
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
        ItemStackHandler handler = blockEntity.getHandler();
        return this.input.test(handler.getStackInSlot(process.getInputSlots()[0])) && handler.getStackInSlot(0).getItem() == ItemHandler.STORAGE_DRIVE;
    }

    @Override
    public int getRecipeTime(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return this.time;
    }

    @Override
    public void onRecipeFinished(SequencingSynthesizerBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStackHandler handler = blockEntity.getHandler();
        ItemStack inStack = handler.getStackInSlot(process.getInputSlots()[0]);
        ItemStack out = inStack.splitStack(1);

        if(!out.isEmpty() && !blockEntity.getWorld().isRemote) {
            DriveUtils.addItemToDrive(handler.getStackInSlot(0), out);
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
