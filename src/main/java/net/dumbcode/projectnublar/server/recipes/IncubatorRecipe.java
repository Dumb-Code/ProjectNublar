package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.IncubatorBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.BasicDinosaurItem;
import net.dumbcode.projectnublar.server.item.DinosaurProvider;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public enum IncubatorRecipe implements MachineRecipe<IncubatorBlockEntity> {
    INSTANCE;

    private static int TIME_SECONDS = 120;
    private static int TIME_TICKS = TIME_SECONDS * 20;
    private static int TIME_PER_PERCENT = TIME_TICKS / 100;

    @Override
    public boolean accepts(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        Item item = blockEntity.getHandler().getStackInSlot(process.getInputSlots()[0]).getItem();
        return blockEntity.getPlantMatter() == IncubatorBlockEntity.TOTAL_PLANT_MATTER && item instanceof BasicDinosaurItem && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(item);
    }

    @Override
    public int getRecipeTime(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return TIME_PER_PERCENT;
    }

    @Override
    public void onRecipeFinished(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        ItemStack out = blockEntity.getHandler().getStackInSlot(process.getInputSlots()[0]);
        Item item = out.getItem();
        NBTTagCompound nbt = out.getOrCreateSubCompound(ProjectNublar.MODID);
        if(nbt.getInteger("AmountDone") < 99) {
            nbt.setInteger("AmountDone", nbt.getInteger("AmountDone") + 1);
        } else if(item instanceof DinosaurProvider) {
            blockEntity.getHandler().setStackInSlot(process.getOutputSlots()[0], new ItemStack(ItemHandler.DINOSAUR_INCUBATED_EGG.get(((DinosaurProvider) item).getDinosaur())));
        }
    }

    @Override
    public boolean acceptsInputSlot(IncubatorBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        Item item = testStack.getItem();
        switch (slotIndex) {
            case 0: return item instanceof BasicDinosaurItem && ItemHandler.DINOSAUR_UNINCUBATED_EGG.containsValue(item);
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation("incubation");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(IncubatorBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 0;
    }
}