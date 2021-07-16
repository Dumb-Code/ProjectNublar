package net.dumbcode.projectnublar.server.recipes;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleItemStackHandler;
import net.dumbcode.projectnublar.server.item.ItemHandler;
import net.dumbcode.projectnublar.server.item.MachineModuleType;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

public enum  EggPrinterRecipe implements MachineRecipe<EggPrinterBlockEntity> {
    INSTANCE;

    @Override
    public boolean accepts(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        MachineModuleItemStackHandler<EggPrinterBlockEntity> handler = blockEntity.getHandler();
        ItemStack boneStack = handler.getStackInSlot(process.getInputSlot(1));
        if(handler.getStackInSlot(process.getInputSlot(0)).getItem() == ItemHandler.EMBRYO_FILLED_SYRINGE.get() && boneStack.getItem() == Items.BONE_MEAL && boneStack.getCount() >= 5) {
            return handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(ItemHandler.EMPTY_SYRINGE.get()), true).isEmpty() && handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(ItemHandler.ARTIFICIAL_EGG.get()), true).isEmpty();
        }
        return false;
    }


    @Override
    public int getRecipeTime(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        return 12000 - 2400*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public void onRecipeFinished(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        MachineModuleItemStackHandler<EggPrinterBlockEntity> handler = blockEntity.getHandler();
        boolean brokenEgg = blockEntity.getTier(MachineModuleType.LEVELING_SENSORS) == 0 && blockEntity.getLevel().random.nextFloat() < 0.1F;

        handler.insertOutputItem(process.getOutputSlot(1), new ItemStack(ItemHandler.EMPTY_SYRINGE.get()), false);
        ItemStack remaining = handler.insertOutputItem(process.getOutputSlot(0), new ItemStack(brokenEgg ? ItemHandler.BROKEN_ARTIFICIAL_EGG.get() : ItemHandler.ARTIFICIAL_EGG.get()), false);
        if(!remaining.isEmpty()) {
            InventoryHelper.dropItemStack(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), remaining);
        }

        handler.getStackInSlot(process.getInputSlot(0)).shrink(1);
        handler.getStackInSlot(process.getInputSlot(1)).shrink(5);


    }

    @Override
    public boolean acceptsInputSlot(EggPrinterBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        Item item = testStack.getItem();
        switch (slotIndex) {
            case 0: return item == ItemHandler.EMBRYO_FILLED_SYRINGE.get();
            case 1: return item == Items.BONE_MEAL;
        }
        return false;
    }

    @Override
    public MachineModuleBlockEntity.ProcessInterruptAction getInterruptAction(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process, MachineModuleBlockEntity.ProcessInterruptReason reason) {
        return MachineModuleBlockEntity.ProcessInterruptAction.PAUSE;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return new ResourceLocation(ProjectNublar.MODID, "egg_printing");
    }

    // TODO: test values, change for balance
    @Override
    public int getCurrentConsumptionPerTick(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        return 20;
    }

    @Override
    public int getCurrentProductionPerTick(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        return 0;
    }
}
