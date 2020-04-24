package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public enum  EggPrinterRecipe implements MachineRecipe<EggPrinterBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        ItemStack boneStack = handler.getStackInSlot(process.getInputSlot(1));
        if(handler.getStackInSlot(process.getInputSlot(0)).getItem() == ItemHandler.EMBRYO_FILLED_SYRINGE && boneStack.getItem() == Items.DYE && boneStack.getMetadata() == 15 && boneStack.getCount() >= 5) {
            return handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(ItemHandler.EMPTY_SYRINGE), true).isEmpty() && handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(ItemHandler.ARTIFICIAL_EGG), true).isEmpty();
        }
        return false;
    }


    @Override
    public int getRecipeTime(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        return 12000 - 2400*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public void onRecipeFinished(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess process) {
        MachineModuleItemStackHandler handler = blockEntity.getHandler();
        boolean brokenEgg = blockEntity.getTier(MachineModuleType.LEVELING_SENSORS) == 0 && blockEntity.getWorld().rand.nextFloat() > 0.1F;

        handler.insertOutputItem(process.getOutputSlot(1), new ItemStack(ItemHandler.EMPTY_SYRINGE), false);
        handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(brokenEgg ? ItemHandler.BROKEN_ARTIFICIAL_EGG : ItemHandler.ARTIFICIAL_EGG), false);


        handler.getStackInSlot(process.getInputSlot(0)).shrink(1);
        handler.getStackInSlot(process.getInputSlot(1)).shrink(5);


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
