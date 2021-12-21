package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public enum IncubatorRecipe implements MachineRecipe<IncubatorBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        Item item = process.getInputStack(0).getItem();
        return blockEntity.getPlantMatter() >= IncubatorBlockEntity.DEFAULT_PLANT_MATTER && item instanceof BasicDinosaurItem && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(item);
    }

    @Override
    public int getRecipeTime(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        return 36000 - 6000*blockEntity.getTier(MachineModuleType.BULB);//30 minutes total, 18 seconds per %
    }

    @Override
    public void onRecipeTick(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        ItemStack out = process.getInputStack(0);
        CompoundNBT nbt = out.getOrCreateTagElement(ProjectNublar.MODID);
        nbt.putFloat("AmountDone", Math.round(1000F * process.getTime() / (float)process.getTotalTime()) / 10F);
    }

    @Override
    public void onRecipeStarted(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        ItemStack stack = process.getInputStack(0);
        process.setTime((int) (stack.getOrCreateTagElement(ProjectNublar.MODID).getFloat("AmountDone") * process.getTotalTime()));
    }

    @Override
    public void onRecipeFinished(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        ItemStack out = process.getInputStack(0);
        blockEntity.getHandler().setStackInSlot(process.getOutputSlot(0), new ItemStack(ItemHandler.DINOSAUR_INCUBATED_EGG.get(((DinosaurProvider) out.getItem()).getDinosaur())));
    }

    @Override
    public boolean acceptsInputSlot(IncubatorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        Item item = testStack.getItem();
        if (slotIndex == 0) {
            return item instanceof BasicDinosaurItem && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(item);
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation("incubation");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<IncubatorBlockEntity> process) {
        return 0;
    }
}
