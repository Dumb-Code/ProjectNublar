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
        return process.getInputStack(0).getItem() == ItemHandler.EMBRYO_FILLED_SYRINGE.get() && blockEntity.getBoneMatter() >= EggPrinterBlockEntity.MINIMUM_BONE_MATTER;
    }

    @Override
    public int getRecipeTime(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        return 12000 - 2400*blockEntity.getTier(MachineModuleType.COMPUTER_CHIP);
    }

    @Override
    public void onRecipeFinished(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        boolean brokenEgg = blockEntity.getTier(MachineModuleType.LEVELING_SENSORS) == 0 && blockEntity.getLevel().random.nextFloat() < 0.1F;
        process.insertOutputItem(new ItemStack(brokenEgg ? ItemHandler.BROKEN_ARTIFICIAL_EGG.get() : ItemHandler.ARTIFICIAL_EGG.get()), 0);

        process.getInputStack(0).shrink(1);
        process.insertOutputItem(new ItemStack(ItemHandler.EMPTY_SYRINGE.get()), 1);

        blockEntity.setBoneMatter(blockEntity.getBoneMatter() - EggPrinterBlockEntity.MINIMUM_BONE_MATTER);
    }

    @Override
    public boolean acceptsInputSlot(EggPrinterBlockEntity blockEntity, int slotIndex, ItemStack testStack, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process) {
        Item item = testStack.getItem();
        if (slotIndex == 0) {
            return item == ItemHandler.EMBRYO_FILLED_SYRINGE.get();
        }
        return false;
    }

    @Override
    public MachineModuleBlockEntity.ProcessInterruptAction getInterruptAction(EggPrinterBlockEntity blockEntity, MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process, MachineModuleBlockEntity.ProcessInterruptReason reason) {
        if(reason == MachineModuleBlockEntity.ProcessInterruptReason.NO_POWER) {
            return MachineModuleBlockEntity.ProcessInterruptAction.PAUSE;
        }
        return MachineModuleBlockEntity.ProcessInterruptAction.RESET;
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
