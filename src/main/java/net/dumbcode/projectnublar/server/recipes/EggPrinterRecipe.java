package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum  EggPrinterRecipe implements MachineRecipe<EggPrinterBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack boneStack = handler.getStackInSlot(process.getInputSlots()[1]);
        if(handler.getStackInSlot(process.getInputSlots()[0]).getItem() == ItemHandler.EMBRYO_FILLED_SYRINGE && boneStack.getItem() == Items.DYE && boneStack.getMetadata() == 15 && boneStack.getCount() >= 5) {
            return handler.insertOutputItem(process.getOutputSlots()[0], new ItemStack(ItemHandler.EMPTY_SYRINGE), true).isEmpty() && handler.insertOutputItem(process.getOutputSlots()[0], new ItemStack(ItemHandler.ARTIFICIAL_EGG), true).isEmpty();
        }
        return false;
    }


    @Override
    public int getRecipeTime(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 50;
    }

    @Override
    public void onRecipeFinished(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();

        handler.insertOutputItem(process.getOutputSlots()[1], new ItemStack(ItemHandler.EMPTY_SYRINGE), false);
        handler.insertOutputItem(process.getOutputSlots()[0], new ItemStack(ItemHandler.ARTIFICIAL_EGG), false);


        handler.getStackInSlot(process.getInputSlots()[0]).shrink(1);
        handler.getStackInSlot(process.getInputSlots()[1]).shrink(5);


    }

    @Override
    public boolean acceptsInputSlot(EggPrinterBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess process) {
        Item item = testStack.getItem();
        switch (slotIndex) {
            case 0: return item == ItemHandler.EMBRYO_FILLED_SYRINGE;
            case 1: return item == Items.DYE && testStack.getMetadata() == 15;
        }
        return false;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "egg_printing");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 0;
    }
}
